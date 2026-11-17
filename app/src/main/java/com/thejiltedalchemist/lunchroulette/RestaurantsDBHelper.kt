package com.thejiltedalchemist.lunchroulette

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.thejiltedalchemist.lunchroulette.DBContract.RestaurantsEntry as RestaurantsEntry

/** Outcome of storing a restaurant. */
enum class InsertResult {
    INSERTED,

    /** A restaurant with the same name (ignoring case) is already stored. */
    DUPLICATE,

    /** Any other database failure (disk full, locked, corrupt, ...). */
    FAILED
}

class RestaurantsDBHelper(context: Context, name: String? = DATABASE_NAME) :
    SQLiteOpenHelper(context, name, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // v1 -> v2: names became case-insensitively unique. Rebuild the table, keeping the
            // oldest of any names that now collide (OR IGNORE) and the original row order.
            // The address column was never populated, so nothing is lost by not copying it.
            val old = "${RestaurantsEntry.TABLE_NAME}_v1"
            db.execSQL("ALTER TABLE ${RestaurantsEntry.TABLE_NAME} RENAME TO $old")
            db.execSQL(SQL_CREATE_ENTRIES)
            db.execSQL(
                "INSERT OR IGNORE INTO ${RestaurantsEntry.TABLE_NAME} (${RestaurantsEntry.COLUMN_NAME}) " +
                    "SELECT ${RestaurantsEntry.COLUMN_NAME} FROM $old ORDER BY rowid"
            )
            db.execSQL("DROP TABLE $old")
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Nothing to undo: the newer schema is a superset of what this version reads.
    }

    /** Appends [restaurant] to the end of the list. */
    fun insertRestaurant(restaurant: RestaurantsModel): InsertResult = try {
        writableDatabase.insertOrThrow(RestaurantsEntry.TABLE_NAME, null, valuesFor(restaurant.name))
        InsertResult.INSERTED
    } catch (e: SQLiteConstraintException) {
        InsertResult.DUPLICATE
    } catch (e: SQLiteException) {
        Log.e(TAG, "Failed to insert restaurant", e)
        InsertResult.FAILED
    }

    /**
     * Inserts [restaurant] at [position] (clamped), so the stored order matches the order shown on screen.
     * Rows are ordered by rowid, so the table is rewritten in a transaction; with a handful of rows that is cheap.
     * On any failure the transaction is rolled back and the table is left unchanged.
     */
    fun insertRestaurantAt(position: Int, restaurant: RestaurantsModel): InsertResult {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val names = readAllRestaurants().map { it.name }.toMutableList()
            names.add(position.coerceIn(0, names.size), restaurant.name)
            db.delete(RestaurantsEntry.TABLE_NAME, null, null)
            names.forEach { db.insertOrThrow(RestaurantsEntry.TABLE_NAME, null, valuesFor(it)) }
            db.setTransactionSuccessful()
            InsertResult.INSERTED
        } catch (e: SQLiteConstraintException) {
            InsertResult.DUPLICATE
        } catch (e: SQLiteException) {
            Log.e(TAG, "Failed to insert restaurant at $position", e)
            InsertResult.FAILED
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Returns true if a row was deleted. The name column is case-insensitive, so this matches the
     * same name the uniqueness check does; `=` is used rather than LIKE so `%` and `_` are literal.
     */
    fun deleteRestaurant(name: String): Boolean = try {
        writableDatabase.delete(RestaurantsEntry.TABLE_NAME, "${RestaurantsEntry.COLUMN_NAME} = ?", arrayOf(name)) > 0
    } catch (e: SQLiteException) {
        Log.e(TAG, "Failed to delete restaurant", e)
        false
    }

    /** All restaurants in the order they appear on screen (insertion order). */
    fun readAllRestaurants(): ArrayList<RestaurantsModel> {
        val restaurants = ArrayList<RestaurantsModel>()
        readableDatabase.rawQuery(
            "SELECT ${RestaurantsEntry.COLUMN_NAME} FROM ${RestaurantsEntry.TABLE_NAME} ORDER BY rowid",
            null
        ).use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(RestaurantsEntry.COLUMN_NAME)
            while (cursor.moveToNext()) restaurants.add(RestaurantsModel(cursor.getString(nameColumn)))
        }
        return restaurants
    }

    private fun valuesFor(name: String) = ContentValues().apply { put(RestaurantsEntry.COLUMN_NAME, name) }

    companion object {
        private const val TAG = "RestaurantsDBHelper"

        // If you change the database schema, you must increment the database version
        // and add the matching step to onUpgrade.
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "FeedReader.db"

        // COLLATE NOCASE makes both the PRIMARY KEY uniqueness and `name = ?` comparisons ignore
        // ASCII case ("Pizza" == "pizza"). Non-ASCII letters are still compared exactly.
        private val SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RestaurantsEntry.TABLE_NAME + " (" +
                        RestaurantsEntry.COLUMN_NAME + " TEXT COLLATE NOCASE PRIMARY KEY," +
                        RestaurantsEntry.COLUMN_ADDRESS + " TEXT)"
    }

}
