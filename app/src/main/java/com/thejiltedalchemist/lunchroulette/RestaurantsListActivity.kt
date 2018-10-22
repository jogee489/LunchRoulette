package com.thejiltedalchemist.lunchroulette

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import kotlinx.android.synthetic.main.list_page.*

class RestaurantsListActivity : AppCompatActivity() {

    private lateinit var restaurantsDBHelper : RestaurantsDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantsDBHelper = RestaurantsDBHelper(this)

        setContentView(R.layout.list_page)
        getLocationList()

        // Create the custom swipe callback
        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = restaurantList.adapter as RestaurantAdapter
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(restaurantList)

        // Switch back to the main activity
        backToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getLocationList() {
        val restaurants = restaurantsDBHelper.readAllRestaurants()
        val adapter = RestaurantAdapter(this, restaurants, restaurantsDBHelper)
        restaurantList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        restaurantList.layoutManager = LinearLayoutManager(this)
        restaurantList.adapter = adapter
    }
}