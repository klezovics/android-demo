package com.grushevskyi.tr_challenge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RVAdapter(private var stockPrices: ArrayList<String>) :
    RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    override fun getItemCount(): Int = stockPrices.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.myPriceView.text = stockPrices[position]
        holder.myNameView.text = STOCK_NAMES_ORDERED[position]
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var myPriceView: TextView = itemView.findViewById(R.id.idPrice)
        var myNameView: TextView = itemView.findViewById(R.id.idName)
    }

    companion object {
        private val STOCK_NAMES_ORDERED: ArrayList<String> =
            ArrayList(StockDataProvider.getAllStockNames())
    }
}