package com.example.scanit

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class ProductViewActivity : AppCompatActivity() {
    private lateinit var imageViewProduct: ImageView
    private lateinit var textViewItemName: TextView
    private lateinit var textViewItemPrice: TextView
    private lateinit var textViewItemQuantity: TextView
    private lateinit var textViewItemBarcode: TextView
    private lateinit var textViewItemCategory: TextView
    private lateinit var textViewItemExpiry: TextView
    private lateinit var textViewItemCost: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_view)

        imageViewProduct = findViewById(R.id.prodImg)
        textViewItemName = findViewById(R.id.prodName)
        textViewItemPrice = findViewById(R.id.prodPrice)
        textViewItemQuantity = findViewById(R.id.prodQty)
        textViewItemBarcode = findViewById(R.id.prodBarcode)
        textViewItemCategory = findViewById(R.id.prodCategory)
        textViewItemExpiry = findViewById(R.id.prodExpiry)
        textViewItemCost = findViewById(R.id.prodCost)


        val searchResults = intent.getSerializableExtra("searchResults")
        //for opening product view via search
        if(searchResults is List<*>){

        }else{
            //for opening product view via selection in product list
            val selectedProduct = intent.getParcelableExtra<Product>("selectedProduct")
            selectedProduct?.let { product ->
                Picasso.get().load(product.imageUrl)
                    .placeholder(R.drawable.image_loader)
                    .error(R.drawable.error)
                    .into(imageViewProduct)

                textViewItemName.text = product.itemName
                textViewItemPrice.text = "₱${product.itemPrice}.00"
                textViewItemQuantity.text = product.itemQuantity.toString()
                textViewItemBarcode.text = product.itemBarcode
                textViewItemCategory.text = product.itemCategory
                textViewItemExpiry.text = product.itemExpiry
                textViewItemCost.text = "₱${product.itemCost}.00"
            }
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }



    }
}