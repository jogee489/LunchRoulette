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
        restaurantsDBHelper.readAllRestaurants().forEach { res -> foodList.add(res.name) }
        if (foodList.isEmpty()) activityMainBinding.decideButton.isEnabled = false
    }

    private fun pressToSpin(button: Button) {
        button.setOnClickListener {
            val ivWheel = activityMainBinding.wheel
            val foodCount = foodList.count()
            var spin = Random().nextInt(foodCount)
            val winner = foodList[spin]

            button.isEnabled = false
            activityMainBinding.selectedFoodText.text = "???"
            ivWheel.rotation = 17f //reset the rotation so we will still know the winner
            // Rotate at least once and land on an item
            spin = (spin + foodCount) * (360 / foodCount) // in degrees

            //TODO: ensure that the pointer lands on the proper item
            object : CountDownTimer(spin.toLong()*10, 18) {
                override fun onTick(millisUntilFinished: Long) {
                    ivWheel.rotation += 36
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
