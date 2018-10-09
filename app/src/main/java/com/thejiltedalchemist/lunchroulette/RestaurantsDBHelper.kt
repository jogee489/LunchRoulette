package com.thejiltedalchemist.lunchroulette

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
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
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
//        db.execSQL(SQL_DELETE_ENTRIES)
        // TODO: Ensure there is logic to assist in a graceful migration of data
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Throws(SQLiteConstraintException::class)
    fun insertRestaurant(restaurant: RestaurantsModel): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(RestaurantsEntry.COLUMN_NAME, restaurant.name)
        values.put(RestaurantsEntry.COLUMN_ADDRESS, restaurant.address)

        // Insert the new row, returning the primary key value of the new row
        val newRowId = db.insert(RestaurantsEntry.TABLE_NAME, null, values)
        println("Adding restaurant #$newRowId")

        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun deleteRestaurant(name: String): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase
        // Define 'where' part of query.
//        val selection = RestaurantsEntry.COLUMN_USER_ID + " LIKE ?"
        val selection = RestaurantsEntry.COLUMN_NAME + "LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(name)
        // Issue SQL statement.
        db.delete(RestaurantsEntry.TABLE_NAME, selection, selectionArgs)

        return true
    }

    fun readResturant(name: String): ArrayList<RestaurantsModel> {
        val users = ArrayList<RestaurantsModel>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + RestaurantsEntry.TABLE_NAME + " WHERE " +
                    RestaurantsEntry.COLUMN_NAME + "='" + name + "'", null)
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }

        var name: String
        var age: String
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                name = cursor.getString(cursor.getColumnIndex(RestaurantsEntry.COLUMN_NAME))
                age = cursor.getString(cursor.getColumnIndex(RestaurantsEntry.COLUMN_ADDRESS))

                users.add(RestaurantsModel(name, age))
                cursor.moveToNext()
            }
        }
        return users
    }

    fun readAllRestaurants(): ArrayList<RestaurantsModel> {
        val users = ArrayList<RestaurantsModel>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + RestaurantsEntry.TABLE_NAME, null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }
        var name: String
        var age: String
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                name = cursor.getString(cursor.getColumnIndex(RestaurantsEntry.COLUMN_NAME))
                age = cursor.getString(cursor.getColumnIndex(RestaurantsEntry.COLUMN_ADDRESS))

                users.add(RestaurantsModel(name, age))
                cursor.moveToNext()
            }
        }
        return users
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "FeedReader.db"

        private val SQL_CREATE_ENTRIES =
                "CREATE TABLE " + RestaurantsEntry.TABLE_NAME + " (" +
                        RestaurantsEntry.COLUMN_NAME + " TEXT PRIMARY KEY," +
                        RestaurantsEntry.COLUMN_ADDRESS + " TEXT)"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + RestaurantsEntry.TABLE_NAME
    }

}