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
            val spinIndex = Random().nextInt(foodCount)
            val winner = foodList[spinIndex]

            button.isEnabled = false
            activityMainBinding.selectedFoodText.text = resources.getText(R.string.default_selection)

            // Pointer sits at 12 o'clock (270° in canvas arc coordinates).
            // Rotating the view by finalRotation places the centre of slice spinIndex at 270°.
            val arcAngle = 360f / foodCount
            val finalRotation = 270f - (spinIndex + 0.5f) * arcAngle
            val spinSpeed = 36
            val interval = 50L
            val rotations = 3L
            val spinDuration = (360 * rotations * interval) / spinSpeed
            ivWheel.rotation = finalRotation + 360f * rotations

            object : CountDownTimer(spinDuration, interval) {
                override fun onTick(millisUntilFinished: Long) {
                    ivWheel.rotation -= spinSpeed
                }

                override fun onFinish() {
                    ivWheel.rotation = finalRotation
                    button.isEnabled = true
                    activityMainBinding.selectedFoodText.text = winner
                }
            }.start()
        }
    }
}
