package com.thejiltedalchemist.lunchroulette

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

import com.thejiltedalchemist.lunchroulette.databinding.ListPageBinding

class RestaurantsListActivity : AppCompatActivity() {

    private lateinit var restaurantsDBHelper : RestaurantsDBHelper
    private lateinit var listPageBinding: ListPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantsDBHelper = RestaurantsDBHelper(this)
        listPageBinding = ListPageBinding.inflate(layoutInflater)

        setContentView(listPageBinding.root)
        getLocationList()

        // Create the custom swipe callback
        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = listPageBinding.restaurantList.adapter as RestaurantAdapter
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(listPageBinding.restaurantList)

        // Switch back to the main activity
        listPageBinding.backToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getLocationList() {
        val restaurants = restaurantsDBHelper.readAllRestaurants()
        val adapter = RestaurantAdapter(restaurants, restaurantsDBHelper)
        val boundRestaurantList = listPageBinding.restaurantList
        boundRestaurantList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        boundRestaurantList.layoutManager = LinearLayoutManager(this)
        boundRestaurantList.adapter = adapter
    }
}