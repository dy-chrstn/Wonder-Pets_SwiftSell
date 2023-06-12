package com.example.scanit

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView

class ProductViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_view)

        val imageViewSelected: ImageView = findViewById(R.id.imageViewSelected)
        val textNameSelected: TextView = findViewById(R.id.textNameSelected)
        val textPriceSelected: TextView = findViewById(R.id.textPriceSelected)
        val textCostSelected: TextView = findViewById(R.id.textCostSelected)
        val textExpirySelected: TextView = findViewById(R.id.textExpirySelected)
        val textBarcodeSelected: TextView = findViewById(R.id.textBarcodeSelected)
        val textCategorySelected: TextView = findViewById(R.id.textCategorySelected)
        val textQuantitySelected: TextView = findViewById(R.id.textQuantitySelected)


        val searchResults = intent.getSerializableExtra("searchResults")
        //for opening product view via search
        if(searchResults is List<*>){
            val productList = searchResults.filterIsInstance<Product>()

            if (productList.isNotEmpty()){
                val product = productList[0]

                val decodedImageBytes = Base64.decode(product.itemImage, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(decodedImageBytes, 0, decodedImageBytes.size)
                imageViewSelected.setImageBitmap(decodedImage)
                textNameSelected.text = product.itemName
                textPriceSelected.text = product.itemPrice.toString()
                textCostSelected.text = product.itemCost.toString()
                textExpirySelected.text = product.itemExpiry
                textBarcodeSelected.text = product.itemBarcode
                textCategorySelected.text = product.itemCategory
                textQuantitySelected.text = product.itemQuantity.toString()
            }
        }else{
            //for opening product view via selection in product list
            val itemName = intent.getStringExtra("itemName") ?: ""
            val itemPrice = intent.getIntExtra("itemPrice", 0) ?: ""
            val itemQuantity = intent.getIntExtra("itemQuantity", 0) ?: ""
            val itemImage = intent.getStringExtra("itemImage") ?: ""
            val itemCost = intent.getIntExtra("itemCost", 0) ?: ""
            val itemExpiry = intent.getStringExtra("itemExpiry") ?: ""
            val itemBarcode = intent.getStringExtra("itemBarcode") ?: ""
            val itemCategory = intent.getStringExtra("itemCategory") ?: ""


            val imageBytes = Base64.decode(itemImage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageViewSelected.setImageBitmap(bitmap)

            textNameSelected.text = itemName
            textPriceSelected.text = "\u20B1${itemPrice.toString()}"
            textCostSelected.text = "\u20B1${itemCost.toString()}"
            textExpirySelected.text = itemExpiry
            textBarcodeSelected.text = itemBarcode
            textCategorySelected.text = itemCategory
            textQuantitySelected.text = itemQuantity.toString()
        }
    }
}