package com.example.scanit

import ScanItSharedPreferences
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.RecoverySystem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ShareCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class TransactionActivity : AppCompatActivity() {

    private lateinit var listTransView: RecyclerView
    private lateinit var transDB: DatabaseReference
    private var arrayTrans: MutableList<buyModel> = mutableListOf()
    private lateinit var adapterTransView: viewTransAdapt
    private var sharedPreferences: ScanItSharedPreferences = ScanItSharedPreferences.getInstance(this@TransactionActivity)
    private var userName = sharedPreferences.getUsername()
    private lateinit var backButton: ImageButton
    private lateinit var shareBtn: ImageButton
    private lateinit var layoutTrans : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        listTransView = findViewById(R.id.recyclerViewTransaction)
        listTransView.layoutManager = LinearLayoutManager(this)
        adapterTransView = viewTransAdapt(ArrayList(arrayTrans))
        listTransView.adapter =  adapterTransView
        layoutTrans = findViewById(R.id.transLayout)
        getTransItem()

        backButton = findViewById(R.id.backImageButton)
        shareBtn = findViewById(R.id.shareImageButton)

        backButton.setOnClickListener {
            finish()
        }

        shareBtn.setOnClickListener {
            val result = "check out my amazing result!!"
            val uri = takeScreenshot(layoutTrans)

            if (uri != null) {
                val shareIntent = ShareCompat.IntentBuilder.from(this@TransactionActivity)
                    .setType("image/*")
                    .setStream(uri)
                    .setText(result)
                    .setChooserTitle("Share Result")
                    .createChooserIntent()

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Add this line to grant read permission to the receiving app
                startActivity(shareIntent)
            } else {
                Toast.makeText(this@TransactionActivity, "Failed to take screenshot", Toast.LENGTH_SHORT).show()
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

        return FileProvider.getUriForFile(this@TransactionActivity, "com.example.scanit.fileprovider", imageFile)
    }

    private fun getTransItem(){
        val payTot = findViewById<TextView>(R.id.totPrice)
        val payAmount = findViewById<TextView>(R.id.AmountPay)
        val payChange = findViewById<TextView>(R.id.changeText)
        val id = intent.getStringExtra("getListBought")
        transDB = FirebaseDatabase.getInstance().getReference("$userName/Order/completeTransactions/$id")
        transDB.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(transGet in snapshot.children){
                        val key = transGet.key.toString()
                        if(key != "changeGiven" && key != "custPay" && key != "totalBuy" && key != "dateTrans" && key != "timeTrans"){
                            val barcodeGet = transGet.child("itemBarcode").getValue(String::class.java).toString()
                            val prodName = transGet.child("itemName").getValue(String::class.java).toString()
                            val prodPrice = transGet.child("itemPrice").getValue(String::class.java).toString().toDouble()
                            val itemQuant = transGet.child("itemQuantity").getValue(String::class.java).toString().toInt()
                            val itemTot = transGet.child("itemTotal").getValue(String::class.java).toString().toDouble()
                            val itemCat = transGet.child("itemCategory").getValue(String::class.java).toString()

                            arrayTrans.add(buyModel(key,barcodeGet,prodName,itemCat,itemQuant,prodPrice,itemTot))
                            }
                    }
                    payTot.text = snapshot.child("totalBuy").getValue(String::class.java).toString()
                    payAmount.text = snapshot.child("custPay").getValue(String::class.java).toString()
                    payChange.text = snapshot.child("changeGiven").getValue(String::class.java).toString()
                    adapterTransView.updateItems(arrayTrans)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }


}