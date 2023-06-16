package com.example.scanit

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scanit.databinding.ActivityAddProductBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.util.Calendar
import java.util.Random

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
    private lateinit var editText: EditText
    private lateinit var addButton: Button
    private lateinit var removeButton: Button
    private lateinit var categoryReference: DatabaseReference
    private lateinit var barcodeGeneratedImage: ImageView
    private lateinit var generateButton: Button
    private lateinit var downloadButton: Button

    private val itemList: MutableList<String> = mutableListOf()



    companion object{
        private const val PICK_IMAGE_REQUEST = 1
        private const val BLACK = -0x1000000
        private const val WHITE = -0x1
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
        spinner = findViewById(R.id.spinner)
        addButton = findViewById(R.id.addButton)
        removeButton = findViewById(R.id.removeButton)
        editText = findViewById(R.id.suggestEditText)

        categoryReference = FirebaseDatabase.getInstance().reference.child("Category")


        val backButton: ImageButton = findViewById(R.id.imageButtonBack)
        backButton.setOnClickListener {
            finish()
        }

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.parseColor("#FFD691")) // Yellow color
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(Color.parseColor("#FFD691")) // Yellow color
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        categoryReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                itemList.clear()
                for (childSnapshot in dataSnapshot.children) {
                    val category = childSnapshot.getValue(String::class.java)
                    category?.let { itemList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        addButton.setOnClickListener {
            val newItem = editText.text.toString()
            if (newItem.isNotEmpty()) {
                categoryReference.push().setValue(newItem)
                editText.text.clear()
            }
        }

        removeButton.setOnClickListener {
            val selectedItem = spinner.selectedItem as String
            categoryReference.orderByValue().equalTo(selectedItem)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            snapshot.ref.removeValue()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })
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

        //barcode generator and download
        barcodeGeneratedImage = findViewById(R.id.barcodeGeneratedImage)
        generateButton = findViewById(R.id.generateButton)
        downloadButton = findViewById(R.id.downloadButton)

        generateButton.setOnClickListener {
            val barcodeValue = generateRandomBarcode()
            editTextItemBarcode.setText(barcodeValue)
            val barcodeBitmap = generateBarcodeBitmap(barcodeValue)
            barcodeGeneratedImage.setImageBitmap(barcodeBitmap)
        }

        downloadButton.setOnClickListener {
            val bitmap = (barcodeGeneratedImage.drawable as? BitmapDrawable)?.bitmap
            if(bitmap != null){
                saveImageToGallery(this, bitmap)
            }
        }


    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveImageToGallery(context: Context, bitmap: Bitmap) {
        val filename = "Barcode_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            var outputStream: OutputStream? = null
            try{
                outputStream = resolver.openOutputStream(imageUri)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream?.close()
                showToast(context, "Image saved to Gallery")
            }catch (e: Exception){
                e.printStackTrace()
                showToast(context, "Failed to save image")
            }finally {
                outputStream?.close()
            }

            MediaScannerConnection.scanFile(context, arrayOf(imageUri.path?.let { it1 -> File(it1).absolutePath }), null){
                _, _ ->
            }
        }

    }

    private fun generateBarcodeBitmap(barcodeValue: String): Bitmap {
        val widthPixels = dpToPx(200)
        val heightPixels = dpToPx(200)
        var bitMatrix: BitMatrix? = null // Initialize with null

        try {
            val multiFormatWriter = MultiFormatWriter()
            bitMatrix = multiFormatWriter.encode(barcodeValue, BarcodeFormat.EAN_13, widthPixels, heightPixels)
        } catch (e: WriterException) {
            e.printStackTrace()
            // Handle the exception if barcode generation fails
            // Return a default error image or throw an exception
        }

        val barcodeBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888)
        bitMatrix?.let { matrix ->
            for (x in 0 until widthPixels) {
                for (y in 0 until heightPixels) {
                    barcodeBitmap.setPixel(x, y, if (matrix[x, y]) BLACK else WHITE)
                }
            }
        }

        return barcodeBitmap
    }

    private fun dpToPx(dp: Int): Int{
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun generateRandomBarcode(): String {
        val random = Random()

        val barcodeDigits = mutableListOf<Int>()

        for (i in 0 until 12) {
            barcodeDigits.add(random.nextInt(10))
        }

        val checkDigit = calculateCheckDigit(barcodeDigits)

        barcodeDigits.add(checkDigit)

        return barcodeDigits.joinToString("")
    }

    private fun calculateCheckDigit(barcodeDigits: List<Int>): Int {
        val sum = barcodeDigits.mapIndexed { index, digits ->
            if(index % 2 == 0) digits else digits * 3
        }.sum()

        val checkDigit = (10 - (sum % 10)) % 10

        return checkDigit
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