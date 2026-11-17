package com.thejiltedalchemist.lunchroulette

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.thejiltedalchemist.lunchroulette.databinding.ListItemRestaurantBinding

class RestaurantAdapter(private val items: ArrayList<RestaurantsModel>,
                        private val restaurantsDBHelper: RestaurantsDBHelper)
    : RecyclerView.Adapter<ViewHolder>()
{

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = items[position].name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemRestaurantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun getNameAt(position: Int) = items[position].name

    fun get(position: Int) = items[position]

    fun removeAt(position: Int) {
        restaurantsDBHelper.deleteRestaurant(getNameAt(position))
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    /** Inserts [item] at [position] (clamped to the list bounds). Returns false if it could not be stored. */
    fun addAt(position: Int, item: RestaurantsModel): Boolean {
        if (!restaurantsDBHelper.insertRestaurant(item)) return false
        val index = position.coerceIn(0, items.size)
        items.add(index, item)
        notifyItemInserted(index)
        return true
    }

    /** Appends a new restaurant. Returns false if the name is already on the list. */
    fun createNew(locationName: String): Boolean = addAt(items.size, RestaurantsModel(locationName))

}

class ViewHolder(binding: ListItemRestaurantBinding)
    : RecyclerView.ViewHolder(binding.root)
{
    val name: TextView = binding.label
}