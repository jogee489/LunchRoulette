package com.thejiltedalchemist.lunchroulette

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.thejiltedalchemist.lunchroulette.databinding.ActivityMainBinding
import java.util.Random


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

        // Spin the wheel
        pressToSpin(activityMainBinding.decideButton)

        // Switch to the list view on click
        activityMainBinding.listFoodsButton.setOnClickListener {
            val intent = Intent(this, RestaurantsListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadRestaurants() {
        val restaurantsList = restaurantsDBHelper.readAllRestaurants()
        restaurantsList.forEach { res -> foodList.add(res.name) }
        if (foodList.isEmpty()) activityMainBinding.decideButton.isEnabled = false
        activityMainBinding.rouletteWheel.addRouletteItems(restaurantsList)
    }

    private fun pressToSpin(button: Button) {
        button.setOnClickListener {
            val ivWheel = activityMainBinding.rouletteWheel
            val foodCount = foodList.count()
            var spin = Random().nextInt(foodCount)
            val winner = foodList[spin]

            button.isEnabled = false
            activityMainBinding.selectedFoodText.text = resources.getText(R.string.default_selection)
            spin *= (360 / foodCount) // winner in degrees
            ivWheel.rotation = spin.toFloat()

            val spinSpeed = 36 // Picked number to make it look fast!
            val interval = 50L // Higher to avoid skips
            val rotations = 3L
            val spinDuration = (360*rotations*interval)/spinSpeed // Land where we started
            object : CountDownTimer(spinDuration, interval) {
                override fun onTick(millisUntilFinished: Long) {
                    ivWheel.rotation -= spinSpeed
                }

                override fun onFinish() {
                    // enabling the button again
                    button.isEnabled = true
                    activityMainBinding.selectedFoodText.text = winner
                }
            }.start()
        }
    }
}
