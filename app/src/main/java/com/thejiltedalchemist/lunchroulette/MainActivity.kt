package com.thejiltedalchemist.lunchroulette

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.thejiltedalchemist.lunchroulette.databinding.ActivityMainBinding
import java.util.Random

class MainActivity : AppCompatActivity() {

    private val foodList = arrayListOf<String>()
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var restaurantsDBHelper: RestaurantsDBHelper
    private var spinAnimator: ObjectAnimator? = null
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantsDBHelper = RestaurantsDBHelper(this)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        activityMainBinding.decideButton.setOnClickListener { spin() }

        // Switch to the list view on click
        activityMainBinding.listFoodsButton.setOnClickListener {
            startActivity(Intent(this, RestaurantsListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // The list screen may have changed the data while this activity was in the background.
        loadRestaurants()
    }

    override fun onDestroy() {
        spinAnimator?.cancel()
        super.onDestroy()
    }

    private fun loadRestaurants() {
        // Never rebuild the wheel under a running spin; the animation's end re-enables the buttons.
        if (spinAnimator?.isRunning == true) return

        val restaurantsList = restaurantsDBHelper.readAllRestaurants()
        val names = restaurantsList.map { it.name }
        if (names != foodList) {
            foodList.clear()
            foodList.addAll(names)
            activityMainBinding.rouletteWheel.rotation = 0f
            activityMainBinding.selectedFoodText.text = resources.getText(R.string.default_selection)
            activityMainBinding.rouletteWheel.addRouletteItems(restaurantsList)
        }
        setSpinning(false)
    }

    private fun setSpinning(spinning: Boolean) {
        activityMainBinding.decideButton.isEnabled = !spinning && foodList.isNotEmpty()
        activityMainBinding.listFoodsButton.isEnabled = !spinning
    }

    private fun spin() {
        if (foodList.isEmpty() || spinAnimator?.isRunning == true) return

        // The winner is decided up front; the wheel then spins to land the pointer on it.
        val winnerIndex = random.nextInt(foodList.size)
        val winner = foodList[winnerIndex]
        val target = WheelGeometry.rotationForWinner(winnerIndex, foodList.size)

        val wheel: View = activityMainBinding.rouletteWheel
        // The wheel always rests at a normalised angle in [0, 360), so this sweeps the wheel
        // counter-clockwise through at least two full turns before settling on the target.
        val end = target - 360f * SPIN_ROTATIONS

        setSpinning(true)
        activityMainBinding.selectedFoodText.text = resources.getText(R.string.default_selection)

        spinAnimator = ObjectAnimator.ofFloat(wheel, View.ROTATION, wheel.rotation, end).apply {
            duration = SPIN_DURATION_MS
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                private var cancelled = false

                override fun onAnimationCancel(animation: Animator) {
                    cancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (cancelled) return
                    wheel.rotation = target
                    activityMainBinding.selectedFoodText.text = winner
                    setSpinning(false)
                }
            })
            start()
        }
    }

    companion object {
        private const val SPIN_ROTATIONS = 3
        private const val SPIN_DURATION_MS = 3000L
    }
}
