package com.thejiltedalchemist.lunchroulette

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

import com.thejiltedalchemist.lunchroulette.DBContract.RestaurantsEntry as RestaurantsEntry
import java.util.ArrayList

class RestaurantsDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Only schema version 1 exists so far. SQL_CREATE_ENTRIES uses IF NOT EXISTS, so this is
        // a safe no-op today. TODO: add real per-version migrations when the schema changes.
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    /** Returns true if the restaurant was stored, false if it was rejected (e.g. the name already exists). */
    fun insertRestaurant(restaurant: RestaurantsModel): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(RestaurantsEntry.COLUMN_NAME, restaurant.name)

        // insert() returns -1 when the row was not inserted (e.g. a duplicate primary key)
        return db.insert(RestaurantsEntry.TABLE_NAME, null, values) != -1L
    }

    /** Returns true if a row was deleted. Matches the name exactly (LIKE would treat % and _ as wildcards). */
    fun deleteRestaurant(name: String): Boolean {
        val db = writableDatabase
        val selection = RestaurantsEntry.COLUMN_NAME + " = ?"
        return db.delete(RestaurantsEntry.TABLE_NAME, selection, arrayOf(name)) > 0
    }

    fun readRestaurant(name: String): ArrayList<RestaurantsModel> {
        val users = ArrayList<RestaurantsModel>()
        val db = writableDatabase
        try {
            val query = """
SELECT *
FROM ${RestaurantsEntry.TABLE_NAME}
WHERE ${RestaurantsEntry.COLUMN_NAME}=?
"""
            db.rawQuery(query, arrayOf(name)).use { cursor ->
                val nameColumn = cursor.getColumnIndex(RestaurantsEntry.COLUMN_NAME)
                while (cursor.moveToNext()) {
                    users.add(
                            RestaurantsModel(
                                    cursor.getString(nameColumn)
                            )
                    )
                }
                return users
            }
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }
    }

    fun readAllRestaurants(): ArrayList<RestaurantsModel> {
        val users = ArrayList<RestaurantsModel>()
        val db = writableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery("select * from " + RestaurantsEntry.TABLE_NAME, null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }
        var name: String
        if (cursor!!.moveToFirst()) {
            while (!cursor.isAfterLast) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantsEntry.COLUMN_NAME))

                users.add(RestaurantsModel(name))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return users
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "FeedReader.db"

        private val SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RestaurantsEntry.TABLE_NAME + " (" +
                        RestaurantsEntry.COLUMN_NAME + " TEXT PRIMARY KEY," +
                        RestaurantsEntry.COLUMN_ADDRESS + " TEXT)"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + RestaurantsEntry.TABLE_NAME
    }

}