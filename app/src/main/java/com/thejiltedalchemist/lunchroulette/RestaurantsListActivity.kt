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

    private class DeletedItem(val position: Int, val restaurant: RestaurantsModel)

    private lateinit var restaurantsDBHelper : RestaurantsDBHelper
    private lateinit var listPageBinding: ListPageBinding
    private lateinit var adapter : RestaurantAdapter
    private lateinit var restaurantListViewHolder: RecyclerView

    // Deletions that can still be undone, oldest first. Each Undo restores the most recent one.
    private val deletedItems = ArrayDeque<DeletedItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantsDBHelper = RestaurantsDBHelper(this)
        listPageBinding = ListPageBinding.inflate(layoutInflater)

        setContentView(listPageBinding.root)
        getLocationList()
        attachOnDeleteCallback()

        listPageBinding.addFoodButton.setOnClickListener {
            val newFood = normalizeRestaurantName(listPageBinding.addFoodText.text.toString())
            if (newFood.isEmpty()) {
                toast(getString(R.string.error_name_blank))
            } else if (addRestaurant(adapter.itemCount, newFood)) {
                listPageBinding.addFoodText.text?.clear()
                toast(getString(R.string.food_added, newFood))
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

    /**
     * Adds [name] at [position] subject to the size limit and the uniqueness rule, telling the user why
     * if it cannot be added. Used by both the Add button and Undo, so they obey the same rules.
     */
    private fun addRestaurant(position: Int, name: String): Boolean {
        val error = if (adapter.itemCount >= MAX_RESTAURANTS) {
            getString(R.string.error_max_restaurants, MAX_RESTAURANTS)
        } else {
            when (adapter.addAt(position, RestaurantsModel(name))) {
                InsertResult.INSERTED -> null
                InsertResult.DUPLICATE -> getString(R.string.error_duplicate, name)
                InsertResult.FAILED -> getString(R.string.error_save_failed, name)
            }
        }
        if (error != null) toast(error)
        return error == null
    }

    private fun deleteItem(position: Int) {
        val restaurant = adapter.get(position)
        if (!adapter.removeAt(position)) {
            toast(getString(R.string.error_delete_failed, restaurant.name))
            return
        }
        deletedItems.addLast(DeletedItem(position, restaurant))
        showUndoSnackbar(restaurant.name)
    }

    private fun showUndoSnackbar(name: String) {
        Snackbar.make(restaurantListViewHolder, getString(R.string.food_deleted, name), Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) { undoDelete() }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    // Undo (ACTION) and a newer deletion replacing this bar (CONSECUTIVE) keep the undo
                    // window open; a timeout, swipe or manual dismissal ends it.
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION &&
                        event != Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE
                    ) deletedItems.clear()
                }
            })
            .show()
    }

    private fun undoDelete() {
        val deleted = deletedItems.removeLastOrNull() ?: return
        addRestaurant(deleted.position, deleted.restaurant.name)
        // Offer to undo the previous deletion too, if there was one.
        deletedItems.lastOrNull()?.let { showUndoSnackbar(it.restaurant.name) }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        const val MAX_RESTAURANTS = 12
    }
}
