package com.example.scanit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        // Initialize database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Products")

        barcodeView = findViewById(R.id.barcode_scanner)

        // Initialize CaptureManager
        captureManager = CaptureManager(this, barcodeView)
        captureManager.initializeFromIntent(intent, savedInstanceState)
        captureManager.decode()

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

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
}