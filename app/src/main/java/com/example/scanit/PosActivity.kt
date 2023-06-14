package com.example.scanit

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scanit.buyViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.Scanner

class PosActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var requestCamera: ActivityResultLauncher<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: buyAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var prodBCSelect: TextView
    private var priceProd: Int = 0
    private lateinit var viewModel: buyViewModel

    private val itemList: MutableList<buyModel> = mutableListOf() // Declaration and initialization of itemList

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos)
        val qnty: TextView = findViewById(R.id.Quant)
        val totalProdSelect: TextView = findViewById(R.id.totalProd)
        prodBCSelect = findViewById(R.id.barText)
        val prodNameSelect: TextView = findViewById(R.id.prodname)
        val prodPriceSelect: TextView = findViewById(R.id.price)

        // Initialize the BuyViewModel
        viewModel = ViewModelProvider(this).get(buyViewModel::class.java)

        // Initialize the adapter with the BuyViewModel
        adapter = buyAdapter(ArrayList(itemList))

        recyclerView = findViewById(R.id.listBuy)
        layoutManager = LinearLayoutManager(this)


        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        databaseReference = FirebaseDatabase.getInstance().getReference("Products")

        val BCGet  = intent.getStringExtra("itemBarcode") ?: ""
        val BCprod: String = BCGet.toString()
        val nameProd = intent.getStringExtra("itemName") ?: ""
        priceProd = intent.getIntExtra("itemPrice", 0) ?: 0
        val itemQuantity = intent.getIntExtra("itemQuantity", 0) ?: ""

        val amountTot = findViewById<TextView>(R.id.totPrice)
        val payAmount = findViewById<EditText>(R.id.AmountPay)
        val payChange = findViewById<TextView>(R.id.changeText)

        val plusBtn = findViewById<ImageButton>(R.id.plus)
        val minusBtn = findViewById<ImageButton>(R.id.minus)
        val scanBtn = findViewById<Button>(R.id.ScanBar)
        val btnAdd = findViewById<Button>(R.id.addBuy)

        adapter.updateItems(viewModel.getItems())
        prodNameSelect.text = nameProd
        val priceText = priceProd
        prodPriceSelect.text = "\u20B1 $priceText"
        prodBCSelect.text = BCprod
        val qtyGet = itemQuantity.toString()

        val scanner = Scanner(System.`in`)

        // Start a new thread to continuously listen for input
        payAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Handle text change here
                val input = s.toString()

                if (input.isNotEmpty()) { // Check if input is not empty
                    try {
                        // Value is a valid integer, perform your desired actions here
                        val resultChange =
                            payAmount.text.toString().toDouble() - amountTot.text.toString()
                                .toDouble()
                        payChange.text = resultChange.toString()
                    } catch (e: NumberFormatException) {
                        // Input is not a valid integer
                        println("Invalid input: $input")
                    }
                }

                // Perform your desired actions with the input here
                // For example, you can check for specific conditions or trigger events based on the input
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })

        prodBCSelect.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Handle text change here
                val input = s.toString()

                if (input.isNotEmpty()) { // Check if input is not empty
                    try {
                        // Value is a valid integer, perform your desired actions here

                    } catch (e: NumberFormatException) {
                        // Input is not a valid integer
                    }
                }

                // Perform your desired actions with the input here
                // For example, you can check for specific conditions or trigger events based on the input
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
                val barcode = s.toString().trim()
                if (barcode.isNotEmpty()) {
                    fetchInformationFromFirebase(barcode)
                }

            }
        })

        btnAdd.setOnClickListener {
            val qntyVal = qnty.text.toString().toInt()

            val barcode = prodBCSelect.text.toString()
            val name = prodNameSelect.text.toString()
            val quantity = qntyVal.toString().toInt()
            val price = priceProd.toString().toDouble()
            val total = quantity * price

            // Create a new Item object
            val item = buyModel(barcode, name, quantity, price, total)

            // Add the new item to the list
            viewModel.addItem(item)

            // Update the adapter with the updated list from the BuyViewModel
            adapter.updateItems(viewModel.getItems())

            adapter.notifyDataSetChanged()
            var totalPrice = 0.0
            for (i in itemList) {
                totalPrice += i.itemTotal
                amountTot.text = totalPrice.toString()
            }

            // Update the adapter with the updated list
            //adapter.updateItems(itemList)
        }

        plusBtn.setOnClickListener {
            val qntyVal = qnty.text.toString().toInt()
            if (qntyVal != qtyGet.toInt()) {
                val updatedValue = qntyVal + 1
                qnty.text = updatedValue.toString()
                val updTotal = priceProd * updatedValue
                totalProdSelect.text = updTotal.toString()
            } else {
                Toast.makeText(this, "You reached the maximum quantity", Toast.LENGTH_SHORT).show()
            }

        }

        minusBtn.setOnClickListener {
            val qntyVal = qnty.text.toString().toInt()
            if (qntyVal != 0) {
                val updatedValue = qntyVal - 1
                qnty.text = updatedValue.toString()
                val updTotal = priceProd * updatedValue
                totalProdSelect.text = updTotal.toString()
            }
        }

        requestCamera =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    val intent = Intent(this@PosActivity, ScanAddPos::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@PosActivity, "Permission not Granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        scanBtn.setOnClickListener {
            requestCamera.launch(android.Manifest.permission.CAMERA)
        }

    }

    private fun fetchInformationFromFirebase(barcode: String) {
        val query: Query = databaseReference.orderByChild("itemBarcode").equalTo(barcode)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val prodNameSelect: TextView = findViewById(R.id.prodname)
                val prodPriceSelect: TextView = findViewById(R.id.price)
                if (dataSnapshot.exists()) {
                    for (productSnapshot in dataSnapshot.children) {
                        val nameProd: String =
                            productSnapshot.child("itemName").value.toString()
                        priceProd =
                            productSnapshot.child("itemPrice").value.toString().toInt()

                        prodNameSelect.text = nameProd
                        prodPriceSelect.text = "\u20B1 $priceProd"
                    }
                } else {
                    prodNameSelect.text = "Product Not Found"
                    prodPriceSelect.text = ""
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    applicationContext,
                    "Error fetching data from Firebase.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}