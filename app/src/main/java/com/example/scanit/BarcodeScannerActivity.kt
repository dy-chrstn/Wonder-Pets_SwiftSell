package com.example.scanit

import ScanItSharedPreferences
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.example.scanit.ProductViewActivity
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.database.*
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class BarcodeScannerActivity : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var captureManager: CaptureManager
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uploadButton: ImageButton
    private var cameraId: String? = null
    private var cameraManager: CameraManager? = null
    private lateinit var sharedPreferences: ScanItSharedPreferences

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                imageUri?.let {
                    val imagePath = getImagePathFromUri(imageUri)
                    // ... continue with image processing logic

                    // Pass the image path to the barcode detection method
                    detectBarcodeFromImage(imagePath)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = ScanItSharedPreferences.getInstance(this@BarcodeScannerActivity)
        var userName = sharedPreferences.getUsername()
        setContentView(R.layout.activity_scan_add_pos)

        // Initialize database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("$userName/Products")

        barcodeView = findViewById(R.id.barcode_scanner)

        // Initialize CaptureManager
        captureManager = CaptureManager(this, barcodeView)
        captureManager.initializeFromIntent(intent, savedInstanceState)
        captureManager.decode()

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager?.cameraIdList?.get(0)

        // Set the barcode callback
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.text?.let { itemBarcode ->
                    fetchItemData(itemBarcode)
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                // Handle possible result points
            }
        })

        // Set up the upload button
        uploadButton = findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            openGalleryForImage()
        }

        // Set up the flashlight button
        val button = findViewById<ImageButton>(R.id.flashlightButton)
        button.setOnClickListener {
            toggleFlashlight()
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun getImagePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun detectBarcodeFromImage(imagePath: String?) {
        val barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.EAN_13)
            .build()

        if (!barcodeDetector.isOperational) {
            showToast("Could not set up barcode detector")
            return
        }

        val bitmap = BitmapFactory.decodeFile(imagePath)
        val frame = Frame.Builder().setBitmap(bitmap).build()
        val barcodes = barcodeDetector.detect(frame)

        if (barcodes.size() > 0) {
            val barcode = barcodes.valueAt(0)
            val barcodeValue = barcode.rawValue

            // Query the database to check if the barcode exists
            val query = databaseReference.orderByChild("itemBarcode").equalTo(barcodeValue)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val itemDataSnapshot = dataSnapshot.children.first()

                        val itemName = itemDataSnapshot.child("itemName").getValue(String::class.java)
                        val itemPrice = itemDataSnapshot.child("itemPrice").getValue(Int::class.java)
                        val itemQty = itemDataSnapshot.child("itemQuantity").getValue(Int::class.java)
                        val itemImg = itemDataSnapshot.child("itemImage").getValue(String::class.java)
                        val itemCost = itemDataSnapshot.child("itemCost").getValue(Int::class.java)
                        val itemExpiry = itemDataSnapshot.child("itemExpiry").getValue(String::class.java)
                        val itemCode = itemDataSnapshot.child("itemBarcode").getValue(String::class.java)

                        val intent = Intent(applicationContext, ProductViewActivity::class.java)
                        intent.putExtra("itemName", itemName)
                        intent.putExtra("itemPrice", itemPrice)
                        intent.putExtra("itemQuantity", itemQty)
                        intent.putExtra("itemImage", itemImg)
                        intent.putExtra("itemCost", itemCost)
                        intent.putExtra("itemExpiry", itemExpiry)
                        intent.putExtra("itemBarcode", itemCode)

                        startActivity(intent)
                    } else {
                        // Barcode does not exist in the database
                        // Handle the case when barcode does not exist
                        // ...
                        showToast("Barcode does not exist in the database")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    // ...
                    showToast("Error querying the database")
                }
            })
        } else {
            showToast("No barcode detected in the image")
        }
    }

    private fun fetchItemData(itemBarcode: String) {
        val query = databaseReference.orderByChild("itemBarcode").equalTo(itemBarcode)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val itemDataSnapshot = dataSnapshot.children.first()

                    val itemName = itemDataSnapshot.child("itemName").getValue(String::class.java)
                    val itemPrice = itemDataSnapshot.child("itemPrice").getValue(Int::class.java)
                    val itemQty = itemDataSnapshot.child("itemQuantity").getValue(Int::class.java)
                    val itemImg = itemDataSnapshot.child("itemImage").getValue(String::class.java)
                    val itemCost = itemDataSnapshot.child("itemCost").getValue(Int::class.java)
                    val itemExpiry = itemDataSnapshot.child("itemExpiry").getValue(String::class.java)
                    val itemCode = itemDataSnapshot.child("itemBarcode").getValue(String::class.java)

                    val intent = Intent(applicationContext, ProductViewActivity::class.java)
                    intent.putExtra("itemName", itemName)
                    intent.putExtra("itemPrice", itemPrice)
                    intent.putExtra("itemQuantity", itemQty)
                    intent.putExtra("itemImage", itemImg)
                    intent.putExtra("itemCost", itemCost)
                    intent.putExtra("itemExpiry", itemExpiry)
                    intent.putExtra("itemBarcode", itemCode)

                    startActivity(intent)
                } else {

                    val intent = Intent(applicationContext, AddProductActivity::class.java)
                    intent.putExtra("itemBarcode", itemBarcode)
                    startActivity(intent)

//                    Toast.makeText(this@BarcodeScannerActivity, "Barcode not found in the database", Toast.LENGTH_SHORT).show()
                    // Barcode not found in the database
                    // Handle the case when barcode is not found
                    // ...
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                // ...
            }
        })
    }

    private fun toggleFlashlight() {
        val cameraPermission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), 1)
        } else {
            try {
                val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                if (cameraId != null) {
                    cameraManager?.setTorchMode(cameraId, true)
                    showToast("Flashlight turned on")
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                showToast("Failed to turn on flashlight")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Check and request camera permission if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            captureManager.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        captureManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureManager.onResume()
            } else {
                // Handle camera permission denial
                // ...
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
}
