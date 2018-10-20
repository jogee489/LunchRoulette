package com.thejiltedalchemist.lunchroulette

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_item_restaurant.*

class RestaurantAdapter(private val context: Context,
                        private val items: ArrayList<RestaurantsModel>)
    : RecyclerView.Adapter<ViewHolder>()
{

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = items[position].name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_restaurant, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class ViewHolder(override val containerView: View)
    : RecyclerView.ViewHolder(containerView), LayoutContainer
{
     val name: TextView = label
 }