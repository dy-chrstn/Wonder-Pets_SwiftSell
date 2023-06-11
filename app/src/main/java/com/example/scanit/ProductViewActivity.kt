package com.example.scanit

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView

class ProductViewActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_view)

        val imageViewSelected: ImageView = findViewById(R.id.prodImg)
        val textNameSelected: TextView = findViewById(R.id.prodName)
        val textPriceSelected: TextView = findViewById(R.id.prodPrice)
        val textCostSelected: TextView = findViewById(R.id.prodCost)
//        val textExpirySelected: TextView = findViewById(R.id.textExpirySelected)
        val textBarcodeSelected: TextView = findViewById(R.id.prodBarcode)
//        val textCategorySelected: TextView = findViewById(R.id.textCategorySelected)
        val textQuantitySelected: TextView = findViewById(R.id.prodQty)

        val itemName = intent.getStringExtra("itemName") ?: ""
        val itemPrice = intent.getIntExtra("itemPrice", 0) ?: ""
        val itemQuantity = intent.getIntExtra("itemQuantity", 0) ?: ""
        val itemImage = intent.getStringExtra("itemImage") ?: ""
        val itemCost = intent.getIntExtra("itemCost", 0) ?: ""
//        val itemExpiry = intent.getStringExtra("itemExpiry") ?: ""
        val itemBarcode = intent.getStringExtra("itemBarcode") ?: ""
//        val itemCategory = intent.getStringExtra("itemCategory") ?: ""


        val imageBytes = Base64.decode(itemImage, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        imageViewSelected.setImageBitmap(bitmap)

        textNameSelected.text = itemName
        textPriceSelected.text = "\u20B1 $itemPrice"
        textCostSelected.text = "\u20B1 $itemCost"
//        textExpirySelected.text = itemExpiry
        textBarcodeSelected.text = itemBarcode
//        textCategorySelected.text = itemCategory
        textQuantitySelected.text = itemQuantity.toString()

    }
}