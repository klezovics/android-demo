package com.grushevskyi.tr_grushevskyi1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RVAdapter (private var mData: ArrayList<String>) : RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    // total number of rows
    override fun getItemCount(): Int = mData.count()

    // inflates the row layout from xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.row_layout, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.myPriceView.text = mData[position]
        holder.myNameView.text = listOfMyStocks[position]
    }

    // stores and recycles views as they are scrolled off the screen
    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        //Goes away
        var myPriceView: TextView = itemView.findViewById(R.id.idPrice)
        var myNameView: TextView = itemView.findViewById(R.id.idName)

    }

    companion object {
        private val listOfMyStocks: ArrayList<String> = arrayListOf(
            "Apple",
            "Adidas",
            "Airbus",
            "Allianz",
            "Alphabet",
            "Amazon",
            "Axa",
            "Bank of America",
            "Bayer",
            "Berkshire"
        )
    }
}