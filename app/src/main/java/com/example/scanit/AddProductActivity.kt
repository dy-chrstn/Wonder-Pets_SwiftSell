package com.example.scanit

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.scanit.databinding.ActivityAddProductBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Calendar
import android.util.Base64
import java.util.Locale

class AddProductActivity : AppCompatActivity() {
    var sImage:String? = ""
    private lateinit var db: DatabaseReference
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var buttonDate : Button
    private lateinit var txtDatePicker : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the spinner from the layout
        val spinner = findViewById<Spinner>(R.id.spinner)
        val textView = findViewById<TextView>(R.id.date_text)
        val storeCategory = findViewById<TextView>(R.id.store_category_text)
        val button = findViewById<Button>(R.id.date_button)

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



    }



    fun insertData(view: View){
        val itemCategory = binding.storeCategoryText.text.toString()
        val itemExpiry = binding.dateText.text.toString()
        val itemName = binding.nameText.text.toString()
        val itemPrice = binding.priceText.text.toString()
        val itemCost = binding.costText.text.toString()
        val itemQuantity = binding.quantityText.text.toString()
        val itemBarcode = binding.barcodeText.text.toString()
        db = FirebaseDatabase.getInstance().getReference("Products")
        val item = itemDs(itemCategory, itemExpiry, sImage, itemName, itemPrice, itemCost, itemQuantity, itemBarcode)
        val databaseReference = FirebaseDatabase.getInstance().reference
        val id = databaseReference.push().key
        db.child(id.toString()).setValue(item).addOnSuccessListener {
            binding.nameText.text.clear()
            binding.priceText.text.clear()
            binding.costText.text.clear()
            binding.quantityText.text.clear()
            binding.barcodeText.text.clear()
            sImage = ""

            Toast.makeText(this, "Data Saved Successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Data Not Inserted", Toast.LENGTH_SHORT).show()
        }




    }


    fun uploadImage(view: View){
        var myFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        myFileIntent.setType("image/*")
        ActivityResultLauncher.launch(myFileIntent)

    }
    private val ActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ){result:ActivityResult ->
        if(result.resultCode == RESULT_OK){
            val uri = result.data!!.data
            try {
                val inputStream = contentResolver.openInputStream(uri!!)
                val myBitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val bytes = stream.toByteArray()

                sImage =Base64.encodeToString(bytes,android.util.Base64.DEFAULT)
                binding.uploadImageView.setImageBitmap(myBitmap)
                inputStream!!.close()
                Toast.makeText(this, "Image Selected", Toast.LENGTH_SHORT).show()


            }catch (ex: Exception){
                Toast.makeText(this, ex.message.toString(), Toast.LENGTH_LONG).show()
            }
        }

    }


}