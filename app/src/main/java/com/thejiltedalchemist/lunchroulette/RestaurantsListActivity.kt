package com.thejiltedalchemist.lunchroulette

import android.content.Intent
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
            val newFood = listPageBinding.addFoodText.text.toString()
            if (newFood.isNotBlank()) {
                adapter.createNew(newFood)
                listPageBinding.addFoodText.text.clear()
                Toast.makeText(this@RestaurantsListActivity, "Added $newFood to food list", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@RestaurantsListActivity, "Food is blank", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Switch back to the main activity
        listPageBinding.backToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Pull the complete list of locations
    private fun getLocationList() {
        val restaurantList = restaurantsDBHelper.readAllRestaurants()
        adapter = RestaurantAdapter(restaurantList, restaurantsDBHelper)
        restaurantListViewHolder = listPageBinding.restaurantList
        restaurantListViewHolder.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        restaurantListViewHolder.layoutManager = LinearLayoutManager(this)
        restaurantListViewHolder.adapter = adapter
    }

    // Attach the delete callback
    private fun attachOnDeleteCallback() {
        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteItem(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(listPageBinding.restaurantList)
    }

    // Delete the swiped item and store in case of undo delete
    private fun deleteItem(position: Int) {
        recentlyDeletedItem = adapter.get(position)
        recentlyDeletedItemPosition = position
        adapter.removeAt(position)
        showUndoSnackbar("Item \"${recentlyDeletedItem.name}\" was deleted.")
    }

    // Display message showing what was deleted and offering an undo button
    private fun showUndoSnackbar(message: String) {
        val snackbar = Snackbar
            .make(
                restaurantListViewHolder,
                message,
                Snackbar.LENGTH_LONG
            )
        snackbar.setAction("Undo") { undoDelete() }
        snackbar.show()
    }

    // Restore the recently deleted item to the item list
    private fun undoDelete() {
        adapter.addAt(recentlyDeletedItemPosition, recentlyDeletedItem)
    }
}