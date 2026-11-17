package com.thejiltedalchemist.lunchroulette

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar

import com.thejiltedalchemist.lunchroulette.databinding.ListPageBinding

class RestaurantsListActivity : AppCompatActivity() {

    private lateinit var restaurantsDBHelper : RestaurantsDBHelper
    private lateinit var listPageBinding: ListPageBinding
    private lateinit var adapter : RestaurantAdapter
    private lateinit var restaurantListViewHolder: RecyclerView
    private lateinit var recentlyDeletedItem: RestaurantsModel
    private var recentlyDeletedItemPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantsDBHelper = RestaurantsDBHelper(this)
        listPageBinding = ListPageBinding.inflate(layoutInflater)

        setContentView(listPageBinding.root)
        getLocationList()
        attachOnDeleteCallback()

        listPageBinding.addFoodButton.setOnClickListener {
            val newFood = listPageBinding.addFoodText.text.toString().trim()
            when {
                newFood.isBlank() ->
                    Toast.makeText(this, R.string.error_name_blank, Toast.LENGTH_SHORT).show()
                adapter.itemCount >= MAX_RESTAURANTS ->
                    Toast.makeText(this, getString(R.string.error_max_restaurants, MAX_RESTAURANTS), Toast.LENGTH_SHORT).show()
                !adapter.createNew(newFood) ->
                    Toast.makeText(this, getString(R.string.error_duplicate, newFood), Toast.LENGTH_SHORT).show()
                else -> {
                    listPageBinding.addFoodText.text?.clear()
                    Toast.makeText(this, getString(R.string.food_added, newFood), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // MainActivity reloads its data in onResume, so just return to it.
        listPageBinding.backToMain.setOnClickListener { finish() }
    }

    private fun getLocationList() {
        val restaurantList = restaurantsDBHelper.readAllRestaurants()
        adapter = RestaurantAdapter(restaurantList, restaurantsDBHelper)
        restaurantListViewHolder = listPageBinding.restaurantList
        restaurantListViewHolder.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        restaurantListViewHolder.layoutManager = LinearLayoutManager(this)
        restaurantListViewHolder.adapter = adapter
    }

    private fun attachOnDeleteCallback() {
        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) deleteItem(position)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(listPageBinding.restaurantList)
    }

    private fun deleteItem(position: Int) {
        recentlyDeletedItem = adapter.get(position)
        recentlyDeletedItemPosition = position
        adapter.removeAt(position)
        showUndoSnackbar(getString(R.string.food_deleted, recentlyDeletedItem.name))
    }

    private fun showUndoSnackbar(message: String) {
        Snackbar.make(restaurantListViewHolder, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) { undoDelete() }
            .show()
    }

    private fun undoDelete() {
        adapter.addAt(recentlyDeletedItemPosition, recentlyDeletedItem)
    }

    companion object {
        const val MAX_RESTAURANTS = 12
    }
}
