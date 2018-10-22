package com.thejiltedalchemist.lunchroulette

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Random

class MainActivity : AppCompatActivity() {

    private val foodList = arrayListOf<String>()

    lateinit var restaurantsDBHelper : RestaurantsDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantsDBHelper = RestaurantsDBHelper(this)

        setContentView(R.layout.activity_main)

        loadRestaurants()

        decideButton.setOnClickListener {
            val random = Random()
            val randomFood = random.nextInt(foodList.count())
            selectedFoodText.text = foodList[randomFood]
        }

        addFoodButton.setOnClickListener {
            val newFood = addFoodText.text.toString()
            if (newFood.isNotBlank()) {
                foodList.add(newFood)
                restaurantsDBHelper.insertRestaurant(RestaurantsModel(newFood, "address"))
                addFoodText.text.clear()
                println(foodList)
            }
        }

        // Switch to the list view on click
        listFoodsButton.setOnClickListener {
            val intent = Intent(this, RestaurantsListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadRestaurants() {
        restaurantsDBHelper.readAllRestaurants().forEach { res -> foodList.add(res.name) }
    }
}
