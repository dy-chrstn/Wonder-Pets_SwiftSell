package com.example.scanit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(private val productList: List<Product>): RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    private lateinit var context: Context
    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val textViewQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        val decodedImage = decodeImage(product.itemImage)
        holder.imageViewProduct.setImageBitmap(decodedImage)

        // Set the string values to the TextViews
        holder.textViewName.text = product.itemName
        holder.textViewPrice.text = product.itemPrice.toString()
        holder.textViewQuantity.text = product.itemQuantity.toString()

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductViewActivity::class.java)
            intent.putExtra("itemName", product.itemName)
            intent.putExtra("itemPrice", product.itemPrice)
            intent.putExtra("itemQuantity", product.itemQuantity)
            intent.putExtra("itemImage", product.itemImage)
            intent.putExtra("itemCost", product.itemCost)
            intent.putExtra("itemCategory", product.itemCategory)
            intent.putExtra("itemExpiry", product.itemExpiry)
            intent.putExtra("itemBarcode", product.itemBarcode)

            context.startActivity(intent)

        }



    }

    private fun decodeImage(imageString: String): Bitmap?{
        val decodedBytes = Base64.decode(imageString, Base64.DEFAULT)
        if (decodedBytes != null && decodedBytes.isNotEmpty()) {
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }
        return null
    }

    companion object {
        fun notifyDataSetChanged() {
            TODO("Not yet implemented")
        }
    }


}