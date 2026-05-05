package com.thejiltedalchemist.lunchroulette

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.thejiltedalchemist.lunchroulette.databinding.ActivityMainBinding
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        private const val SPIN_SPEED = 36f
        private const val SPIN_INTERVAL_MS = 50L
        private const val SPIN_ROTATIONS = 3L
    }

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
            val foodCount = foodList.size
            val spinIndex = Random.nextInt(foodCount)
            val winner = foodList[spinIndex]

            button.isEnabled = false
            activityMainBinding.selectedFoodText.text = getString(R.string.default_selection)

            // Pointer sits at 12 o'clock (270° in canvas arc coordinates).
            // Rotating the view by finalRotation places the centre of slice spinIndex at 270°.
            val arcAngle = 360f / foodCount
            val finalRotation = 270f - (spinIndex + 0.5f) * arcAngle
            val spinDuration = (360 * SPIN_ROTATIONS * SPIN_INTERVAL_MS) / SPIN_SPEED.toLong()
            ivWheel.rotation = finalRotation + 360f * SPIN_ROTATIONS

            object : CountDownTimer(spinDuration, SPIN_INTERVAL_MS) {
                override fun onTick(millisUntilFinished: Long) {
                    ivWheel.rotation -= SPIN_SPEED
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
