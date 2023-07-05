package com.example.scanit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class transHistoAdapt(private var buyList: MutableList<transData>,  private val onItemClick: (transData) -> Unit) : RecyclerView.Adapter<transHistoAdapt.ViewHolder>(){
    private lateinit var mListener: onItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): transHistoAdapt.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_transac, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: transHistoAdapt.ViewHolder, position: Int) {
        val buyView = buyList[position]

        holder.transNumb.text = "Transaction No. ${buyView.transcationID.toString().toInt()}"
        holder.itemDescription.text = "Description \n ${buyView.description}"
        holder.itemTot.text = "Total: ${buyView.itemTotal.toString()}"
        holder.itemDate.text = "Date: ${buyView.itemDate.toString()}"
        holder.itemTime.text = "Time: ${buyView.itemTime.toString()}"

    }

    override fun getItemCount(): Int {
        return buyList.size
    }

    inner class ViewHolder(transView : View) : RecyclerView.ViewHolder(transView), View.OnClickListener{
        val transNumb: TextView = itemView.findViewById(R.id.transIdText)
        val itemDescription: TextView = itemView.findViewById(R.id.descItem)
        val itemTot: TextView = itemView.findViewById(R.id.totalPay)
        val itemDate: TextView = itemView.findViewById(R.id.dateText)
        val itemTime: TextView = itemView.findViewById(R.id.timeText)

        init{
            transView.setOnClickListener(this)
        }

        fun bind(productView: transData) {
            transNumb.text = productView.transcationID.toString()
            itemDescription.text = productView.description.toString()
            itemTot.text = productView.itemTotal.toString()
            itemDate.text = productView.itemDate.toString()
            itemTime.text = productView.itemTime.toString()
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val getListBought = buyList[position]
                onItemClick.invoke(getListBought)
            }
        }


    }

    fun updateItems(items: List<transData>) {
        buyList = ArrayList(items)
        notifyDataSetChanged()
    }

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

}