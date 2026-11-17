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

    /**
     * Deletes the row at [position] from the database and the list. Returns false (leaving the list
     * untouched and putting the swiped row back on screen) if the database delete failed.
     */
    fun removeAt(position: Int): Boolean {
        if (!restaurantsDBHelper.deleteRestaurant(getNameAt(position))) {
            notifyItemChanged(position)
            return false
        }
        items.removeAt(position)
        notifyItemRemoved(position)
        return true
    }

    /**
     * Stores [item] at [position] (clamped to the list bounds) in both the database and the list,
     * so the on-screen order and the stored order stay the same. The list is only changed on success.
     */
    fun addAt(position: Int, item: RestaurantsModel): InsertResult {
        val index = position.coerceIn(0, items.size)
        val result = if (index == items.size) {
            restaurantsDBHelper.insertRestaurant(item)
        } else {
            restaurantsDBHelper.insertRestaurantAt(index, item)
        }
        if (result == InsertResult.INSERTED) {
            items.add(index, item)
            notifyItemInserted(index)
        }
        return result
    }

}

class ViewHolder(binding: ListItemRestaurantBinding)
    : RecyclerView.ViewHolder(binding.root)
{
    val name: TextView = binding.label
}