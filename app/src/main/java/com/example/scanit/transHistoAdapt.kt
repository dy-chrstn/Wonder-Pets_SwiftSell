package com.example.scanit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class transHistoAdapt(private var buyList: ArrayList<transData>) : RecyclerView.Adapter<transHistoAdapt.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): transHistoAdapt.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_transac, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: transHistoAdapt.ViewHolder, position: Int) {
        val buyView = buyList[position]

        holder.transNumb.text = "Transaction No. ${buyView.transcationID.toString().toInt()}"
        holder.itemDescription.text = "Description \n ${buyView.description}"
        holder.itemTot.text = "Total: ${buyView.itemTotal.toString()}"


    }

    override fun getItemCount(): Int {
        return buyList.size
    }

    inner class ViewHolder(transView : View) : RecyclerView.ViewHolder(transView){
        val transNumb: TextView = itemView.findViewById(R.id.transIdText)
        val itemDescription: TextView = itemView.findViewById(R.id.descItem)
        val itemTot: TextView = itemView.findViewById(R.id.totalPay)

    }

    fun updateItems(items: List<transData>) {
        buyList = ArrayList(items)
        notifyDataSetChanged()
    }

}