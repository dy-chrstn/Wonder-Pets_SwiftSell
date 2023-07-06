package com.example.scanit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ProductViewActivity : AppCompatActivity() {
    private lateinit var imageViewProduct: ImageView
    private lateinit var textViewItemName: TextView
    private lateinit var textViewItemPrice: TextView
    private lateinit var textViewItemQuantity: TextView
    private lateinit var textViewItemBarcode: TextView
    private lateinit var textViewItemCategory: TextView
    private lateinit var textViewItemExpiry: TextView
    private lateinit var textViewItemCost: TextView
    private lateinit var shareBtn: ImageButton
    private lateinit var prodLayout : LinearLayoutCompat
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
        shareBtn = findViewById(R.id.share)
        prodLayout = findViewById(R.id.prodViewLayout)

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

        shareBtn.setOnClickListener {
            val result = "check out my amazing result!!"
            val uri = takeScreenshot(prodLayout)

            if (uri != null) {
                val shareIntent = ShareCompat.IntentBuilder.from(this@ProductViewActivity)
                    .setType("image/*")
                    .setStream(uri)
                    .setText(result)
                    .setChooserTitle("Share Result")
                    .createChooserIntent()

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Add this line to grant read permission to the receiving app
                startActivity(shareIntent)
            } else {
                Toast.makeText(this@ProductViewActivity, "Failed to take screenshot", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun takeScreenshot(view: View): Uri? {
        val screenshot = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)
        view.draw(canvas)

        val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "screenshot.jpg")
        val outputStream: OutputStream? = FileOutputStream(imageFile)
        screenshot.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream?.close()

        return FileProvider.getUriForFile(this@ProductViewActivity, "com.example.scanit.fileprovider", imageFile)
    }
}