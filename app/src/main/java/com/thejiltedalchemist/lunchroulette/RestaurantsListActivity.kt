package com.thejiltedalchemist.lunchroulette

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_page.*

class ListActivity : AppCompatActivity() {

    private lateinit var restaurantsDBHelper : RestaurantsDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantsDBHelper = RestaurantsDBHelper(this)

        setContentView(R.layout.list_page)
        getLocationList()

        backToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getLocationList() {
        val restaurants = restaurantsDBHelper.readAllRestaurants()
        val adapter = RestaurantAdapter(this, restaurants)
        restaurantList.layoutManager = LinearLayoutManager(this)
        restaurantList.adapter = adapter
    }
}