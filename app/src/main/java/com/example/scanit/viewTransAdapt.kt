package com.example.scanit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class viewTransAdapt(private var transList : MutableList<buyModel>) : RecyclerView.Adapter<viewTransAdapt.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewTransAdapt.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewtransaction,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: viewTransAdapt.ViewHolder, position: Int) {
        val transData = transList[position]

        holder.transViewBC.text = transData.itemBarcode
        holder.transViewPName.text = transData.itemName
        holder.transViewQuant.text = transData.itemQuantity.toString()
        holder.transViewPrice.text = transData.itemPrice.toString()
        holder.transViewPTot.text = transData.itemTotal.toString()
    }

    override fun getItemCount(): Int {
        return transList.size
    }

    inner class ViewHolder(transView: View) : RecyclerView.ViewHolder(transView){
        val transViewBC: TextView = transView.findViewById(R.id.prodBC)
        val transViewPName: TextView = transView.findViewById(R.id.pName)
        val transViewQuant: TextView = transView.findViewById(R.id.pQnty)
        val transViewPrice: TextView = transView.findViewById(R.id.pPrice)
        val transViewPTot: TextView = transView.findViewById(R.id.pTot)
    }

    fun updateItems(items: List<buyModel>) {
        transList = ArrayList(items)
        notifyDataSetChanged()
    }
}