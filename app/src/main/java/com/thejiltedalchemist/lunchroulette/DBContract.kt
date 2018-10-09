package com.thejiltedalchemist.lunchroulette

import android.provider.BaseColumns

object DBContract {

    /* Inner class that defines the table contents */
    class RestaurantsEntry : BaseColumns {
        companion object {
            val TABLE_NAME = "restaurants"
            val COLUMN_NAME = "name"
            val COLUMN_ADDRESS = "address"
        }
    }
}