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
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        val cancelBtn = findViewById<Button>(R.id.cancelBtn)

        val transID: Int = 0

        adapter.updateItems(viewModel.getItems())
        prodNameSelect.text = nameProd
        val priceText = priceProd
        prodPriceSelect.text = "\u20B1 $priceText"
        prodBCSelect.text = BCprod
        val qtyGet = itemQuantity.toString()

        cancelBtn.setOnClickListener(){
            val query = FirebaseDatabase.getInstance().getReference("Order/ongoingTransactions")
            query.removeValue().addOnSuccessListener{
                Toast.makeText(this@PosActivity,"The transaction is now saved on history",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {

            }
        }

        saveBtn.setOnClickListener{
            val query = FirebaseDatabase.getInstance().getReference("Order/ongoingTransactions")
            val putCompTrans = FirebaseDatabase.getInstance().getReference("Order/completeTransactions")

            val getId: Int
            Toast.makeText(this@PosActivity,"Hello",Toast.LENGTH_SHORT).show()
            query.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val getId = putCompTrans.push().key
                        val transaction = getId.toString()
                        val putCompTransChild = FirebaseDatabase.getInstance().getReference("Order/completeTransaction/$transaction")
                        for(transBuy in snapshot.children){
                            val getBarcode = transBuy.child("itemBarcode").getValue(String::class.java)
                            val prodName = transBuy.child("itemName").getValue(String::class.java)
                            val prodPrice = transBuy.child("itemPrice").getValue(Int::class.java)
                            val prodQnty = transBuy.child("itemQuantity").getValue(Int::class.java)
                            val itemTot = transBuy.child("itemTotal").getValue(Int::class.java)


                            val itemData: HashMap<String, Any> = HashMap()
                            itemData["itemBarcode"] = getBarcode.toString()
                            itemData["itemName"] = prodName.toString()
                            itemData["itemQuantity"] = prodQnty.toString()
                            itemData["itemPrice"] = prodPrice.toString()
                            itemData["itemTotal"] = itemTot.toString()
                            // Add the new item to the list
                            val newPosList = putCompTransChild.push()
                            newPosList.setValue(itemData)
                            /*
                            putCompTransChild.child("itemBarcode").push().setValue(getBarcode)
                            putCompTransChild.child("itemName").push().setValue(prodName)
                            putCompTransChild.child("itemPrice").push().setValue(prodPrice)
                            putCompTransChild.child("itemQuantity").push().setValue(prodQnty)
                            putCompTransChild.child("itemTotal").push().setValue(itemTot)*/
                        }
                        query.removeValue().addOnSuccessListener{
                            Toast.makeText(this@PosActivity,"The transaction is now saved on history",Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        }

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

        getItemData()

        btnAdd.setOnClickListener {

            val qtyVal = qnty.text.toString().toInt()
            val barcode = prodBCSelect.text.toString()
            val name = prodNameSelect.text.toString()
            val quantity = qtyVal.toString().toInt()
            val price = priceProd.toString().toDouble()
            val total = quantity * price
            val db = FirebaseDatabase.getInstance().getReference("Order/ongoingTransactions")

            val uniqueKey = db.push().key
            // Create a new Item object
            val transaction = uniqueKey.toString()
            val item = buyModel(transaction,barcode, name, quantity, price, total)
            val itemData: HashMap<String, Any> = HashMap()
            itemData["TransactionID"] = transaction
            itemData["itemBarcode"] = barcode
            itemData["itemName"] = name
            itemData["itemQuantity"] = quantity
            itemData["itemPrice"] = price
            itemData["itemTotal"] = total
            // Add the new item to the list
            val newPosList = db.push()
            newPosList.setValue(itemData)
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
        val query: Query = FirebaseDatabase.getInstance().getReference("Products").orderByChild("itemBarcode").equalTo(barcode)
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
    private fun getItemData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("ongoingTransactions")
        val query: Query = databaseReference.orderByKey()

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear() // Clear the existing itemList
                if (snapshot.exists()) {
                    for (buySel in snapshot.children) {
                        val transactionId = buySel.key.toString()
                        val itemData = buySel.getValue(buyModel::class.java)
                        if (itemData != null) {
                            itemList.add(itemData) // Add the item to the itemList
                        }
                    }

                    // Update the adapter with the updated itemList
                    adapter.updateItems(itemList)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the cancellation
            }
        })
    }
}