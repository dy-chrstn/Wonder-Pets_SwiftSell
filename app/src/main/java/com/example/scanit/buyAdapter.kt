package com.example.scanit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class buyAdapter(private var itemList: ArrayList<buyModel>) : RecyclerView.Adapter<buyAdapter.ViewHolder>() {
    // Adapter methods...

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listaddbuy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        // Bind item data to views in the ViewHolder
        holder.itemBarcodeTextView.text = item.itemBarcode
        holder.itemNameTextView.text = item.itemName
        holder.itemQuantityTextView.text = item.itemQuantity.toString()
        holder.itemPriceTextView.text = item.itemPrice.toString()
        holder.itemTotalTextView.text = item.itemTotal.toString()
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemBarcodeTextView: TextView = itemView.findViewById(R.id.prodBC)
        val itemNameTextView: TextView = itemView.findViewById(R.id.pName)
        val itemQuantityTextView: TextView = itemView.findViewById(R.id.pQnty)
        val itemPriceTextView: TextView = itemView.findViewById(R.id.pPrice)
        val itemTotalTextView: TextView = itemView.findViewById(R.id.pTot)
    }

    // Method to update the items in the adapter
    fun updateItems(items: List<buyModel>) {
        itemList = ArrayList(items)
        notifyDataSetChanged()
    }

    fun clearItems() {
        itemList.clear()
        notifyDataSetChanged()
    }
    fun editQuant(barcode: String, quant: Int, total: Double) {
        for (index in itemList.indices) {
            val item = itemList[index]
            if (item.itemBarcode == barcode) {
                // Create a new instance of the buyModel with the updated variable
                val updatedItem = item.copy(itemQuantity = quant,itemTotal = total)
                // Update the item in the itemList
                itemList[index] = updatedItem
                notifyItemChanged(index)
                break // Exit the loop after finding the matching item
            }
        }
    }
}

