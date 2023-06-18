package com.example.scanit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productView = productList[position]
        holder.bind(productView)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.textViewPrice)
        private val itemQuantityTextView: TextView = itemView.findViewById(R.id.textViewQuantity)
        private val itemImageView: ImageView = itemView.findViewById(R.id.imageViewProduct)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(productView: Product) {
            itemNameTextView.text = productView.itemName
            itemPriceTextView.text = productView.itemPrice.toString()
            itemQuantityTextView.text = productView.itemQuantity.toString()

            // Load image from Firebase Storage using Picasso
            Picasso.get().load(productView.imageUrl)
                .placeholder(R.drawable.image_loader)
                .error(R.drawable.error)
                .into(itemImageView)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val selectedProduct = productList[position]
                onItemClick.invoke(selectedProduct)
            }
        }
    }

    companion object {
        private const val TAG = "ProductAdapter"
    }
}
