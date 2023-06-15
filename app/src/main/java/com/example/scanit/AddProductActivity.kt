package com.example.scanit

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Base64
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scanit.databinding.ActivityAddProductBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Calendar

class AddProductActivity : AppCompatActivity() {
    var sImage:String? = ""
    private lateinit var db: DatabaseReference
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var spinner: Spinner
    private lateinit var textView: TextView
    private lateinit var storeCategory: TextView
    private lateinit var button: Button
    private lateinit var imageView: ImageView
    private lateinit var editTextItemName: EditText
    private lateinit var editTextItemPrice: EditText
    private lateinit var editTextItemQuantity: EditText
    private lateinit var editTextItemCost: EditText
    private lateinit var editTextItemBarcode: EditText
    private lateinit var textViewItemCategory: TextView
    private lateinit var textViewItemExpiry: TextView



    companion object{
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        // Get the spinner from the layout
        spinner = findViewById(R.id.spinner)
        textView = findViewById(R.id.date_text) // textViewItemExpiry
        storeCategory = findViewById(R.id.store_category_text) //textViewItemCategory
        button = findViewById(R.id.date_button)
        imageView = findViewById(R.id.upload_image_view)
        editTextItemName = findViewById(R.id.name_text)
        editTextItemPrice = findViewById(R.id.price_text)
        editTextItemQuantity = findViewById(R.id.quantity_text)
        editTextItemCost = findViewById(R.id.cost_text)
        editTextItemBarcode = findViewById(R.id.barcode_text)

        val itemBarcode = intent.getStringExtra("itemBarcode") ?: ""

        editTextItemBarcode.text = Editable.Factory.getInstance().newEditable(itemBarcode)



        // Create an array adapter to hold the spinner items
        val items = arrayOf("Food", "Beverages", "Supplies", "Clothing")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)

        // Set the adapter on the spinner
        spinner.adapter = adapter

        // Set an onItemSelected listener on the spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val text = spinner.selectedItem.toString()
                storeCategory.text = text
                // Do something when an item is selected
                println("Selected item: ${items[position]}")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing when nothing is selected
            }
        }

        button.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    textView.text = "${month + 1}/${dayOfMonth}/${year}"
                    textView.setTextColor(Color.parseColor("#FFD691"))
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }


        //for selecting image in gallery
        val buttonSelectImage = findViewById<Button>(R.id.uploadButton)
        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val buttonAddProduct = findViewById<Button>(R.id.saveButton)
        buttonAddProduct.setOnClickListener {
            val itemName = editTextItemName.text.toString()
            val itemPrice = editTextItemPrice.text.toString().toInt()
            val itemQuantity = editTextItemQuantity.text.toString().toInt()
            val itemCategory = storeCategory.text.toString()
            val itemCost = editTextItemCost.text.toString().toInt()
            val itemExpiry = textView.text.toString()
            val itemBarcode = editTextItemBarcode.text.toString()

            val itemData: HashMap<String, Any> = HashMap()
            itemData["itemName"] = itemName
            itemData["itemPrice"] = itemPrice
            itemData["itemQuantity"] = itemQuantity
            itemData["itemCategory"] = itemCategory
            itemData["itemCost"] = itemCost
            itemData["itemExpiry"] = itemExpiry
            itemData["itemBarcode"] = itemBarcode

            val databaseRef = FirebaseDatabase.getInstance().getReference("Products")
            val newItemRef = databaseRef.push()
            newItemRef.setValue(itemData)

            val imageUri = getImageUriFromImageView()
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("Products/${newItemRef.key}.jpg")

            Picasso.get()
                .load(imageUri)
                .resize(512, 512)
                .centerCrop()
                .into(imageView, object : Callback{
                    override fun onSuccess() {
                        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()

                        val uploadTask = imageRef.putBytes(data)
                        uploadTask.addOnSuccessListener {
                            Toast.makeText(this@AddProductActivity, "Successfully Upload", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this@AddProductActivity, "Failed to Upload", Toast.LENGTH_SHORT).show()
                        }



                    }

                    override fun onError(e: Exception?) {
                        TODO("Not yet implemented")
                    }

                })

            editTextItemName.text.clear()
            editTextItemPrice.text.clear()
            editTextItemQuantity.text.clear()
            editTextItemBarcode.text.clear()
            editTextItemCost.text.clear()
            storeCategory.text = ""
            textView.text = ""

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun getImageUriFromImageView(): Uri {
        //compressing the image file
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val file = File(this.externalCacheDir, "temp_image.jpg")
        val outputStream = file.outputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()


        return Uri.fromFile(file)
    }
}