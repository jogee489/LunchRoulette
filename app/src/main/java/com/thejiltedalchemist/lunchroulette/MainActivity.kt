package com.thejiltedalchemist.lunchroulette

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Random

class MainActivity : AppCompatActivity() {

    val foodList = arrayListOf("Chinese", "Burgers", "Pizza", "Thai", "Italian")

    lateinit var restaurantsDBHelper : RestaurantsDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        restaurantsDBHelper = RestaurantsDBHelper(this)
        loadRestaurants()

        decideButton.setOnClickListener {
            println("You clicked me!")
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

        listFoodsButton.setOnClickListener {
            println("Clicked list food button")
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadRestaurants() {
        restaurantsDBHelper.readAllRestaurants().forEach { res -> foodList.add(res.name) }
    }
}
