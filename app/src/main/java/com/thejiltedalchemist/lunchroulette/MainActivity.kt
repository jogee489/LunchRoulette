package com.thejiltedalchemist.lunchroulette

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import java.util.Random

import com.thejiltedalchemist.lunchroulette.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val foodList = arrayListOf<String>()
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var restaurantsDBHelper : RestaurantsDBHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantsDBHelper = RestaurantsDBHelper(this)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        loadRestaurants()

        activityMainBinding.decideButton.setOnClickListener {
            val random = Random()
            val randomFood = random.nextInt(foodList.count())
            activityMainBinding.selectedFoodText.text = foodList[randomFood]
        }

        activityMainBinding.addFoodButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "Clicked addFood", Toast.LENGTH_SHORT).show()
            val newFood = activityMainBinding.addFoodText.text.toString()
            if (newFood.isNotBlank()) {
                foodList.add(newFood)
                restaurantsDBHelper.insertRestaurant(RestaurantsModel(newFood, "address"))
                activityMainBinding.addFoodText.text.clear()
                Toast.makeText(this@MainActivity, "Added $newFood to food list", Toast.LENGTH_SHORT).show()
                println(foodList)
                //TODO: reload food list on MainActivity
            } else {
                Toast.makeText(this@MainActivity, "Food is blank", Toast.LENGTH_SHORT).show()
            }
        }

        // Switch to the list view on click
        activityMainBinding.listFoodsButton.setOnClickListener {
            val intent = Intent(this, RestaurantsListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadRestaurants() {
        restaurantsDBHelper.readAllRestaurants().forEach { res -> foodList.add(res.name) }
    }
}
