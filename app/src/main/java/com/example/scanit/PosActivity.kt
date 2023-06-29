package com.example.scanit

import ScanItSharedPreferences
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PosActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var requestCamera: ActivityResultLauncher<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: buyAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var prodBCSelect: TextView
    private var itemData: HashMap<String, Any> = HashMap()
    private var priceProd: Int = 0
    private lateinit var viewModel: buyViewModel
    private lateinit var qnty: TextView
    private lateinit var totalProdSelect: TextView
    private lateinit var prodPriceSelect: TextView
    private var sharedPreferences: ScanItSharedPreferences = ScanItSharedPreferences.getInstance(this@PosActivity)
    private var userName = sharedPreferences.getUsername()


    private val itemList: MutableList<buyModel> = mutableListOf() // Declaration and initialization of itemList
    private var itemQuantity = 0
    private var qtyGet = ""
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos)
        qnty = findViewById(R.id.Quant)
        totalProdSelect = findViewById(R.id.totalProd)
        prodPriceSelect = findViewById(R.id.price)
        prodBCSelect = findViewById(R.id.barText)
        val prodNameSelect: TextView = findViewById(R.id.prodname)


        // Initialize the BuyViewModel
        viewModel = ViewModelProvider(this).get(buyViewModel::class.java)

        // Initialize the adapter with the BuyViewModel
        adapter = buyAdapter(ArrayList(itemList))

        recyclerView = findViewById(R.id.listBuy)
        layoutManager = LinearLayoutManager(this)


        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        databaseReference = FirebaseDatabase.getInstance().getReference("$userName/Products")

        val BCGet  = intent.getStringExtra("itemBarcode") ?: ""
        val BCprod: String = BCGet.toString()
        val nameProd = intent.getStringExtra("itemName") ?: ""
        priceProd = intent.getIntExtra("itemPrice", 0) ?: 0
        itemQuantity = intent?.getIntExtra("itemQuantity", 0) ?: 0

        //val currentDate = LocalDate.now().toString()

        val amountTot = findViewById<TextView>(R.id.totPrice)
        val payAmount = findViewById<EditText>(R.id.AmountPay)
        val payChange = findViewById<TextView>(R.id.changeText)

        val plusBtn = findViewById<ImageButton>(R.id.plus)
        val minusBtn = findViewById<ImageButton>(R.id.minus)
        val scanBtn = findViewById<Button>(R.id.ScanBar)
        val btnAdd = findViewById<Button>(R.id.addBuy)
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        val cancelBtn = findViewById<Button>(R.id.cancelBtn)

        //back to previous activity after pressing this.
        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            finish()
        }



        adapter.updateItems(viewModel.getItems())
        prodNameSelect.text = nameProd
        val priceText = priceProd
        prodPriceSelect.text = "\u20B1 $priceText"
        prodBCSelect.text = BCprod
        qtyGet = itemQuantity.toString()

        val OGtrans:Query = FirebaseDatabase.getInstance().getReference("$userName/Order/ongoingTransactions")
        OGtrans.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var updTot: Double = 0.0
                    for (snapShotProdTot in snapshot.children) {
                        val getProdTot = snapShotProdTot.child("itemTotal").getValue(Double::class.java)
                        updTot += getProdTot.toString().toDouble()
                    }
                    amountTot.text = updTot.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        setTot()
        getItemData()

        cancelBtn.setOnClickListener(){
            val query = FirebaseDatabase.getInstance().getReference("$userName/Order/ongoingTransactions")

            query.removeValue().addOnSuccessListener{
                Toast.makeText(this@PosActivity,"The transaction has been reset",Toast.LENGTH_SHORT).show()
                resetTrans(amountTot,payChange,payAmount)

                adapter.clearItems()
                adapter.notifyDataSetChanged()
            }.addOnFailureListener {

            }
        }
        var getLargeId: Int
        saveBtn.setOnClickListener{
            val query = FirebaseDatabase.getInstance().getReference("$userName/Order/ongoingTransactions")
            val putCompTrans = FirebaseDatabase.getInstance().getReference("$userName/Order/completeTransactions")
            val getTotText = amountTot.text.toString()
            val getChangeText = payChange.text.toString()
            val getPayAmount = payAmount.text.toString()
            adapter.clearItems()
            adapter.notifyDataSetChanged()
            query.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){

                        putCompTrans.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (childSnapshot in dataSnapshot.children) {
                                        val id = childSnapshot.key.toString().toIntOrNull() ?: 0
                                        val getLargeId = id + 1
                                        putCompTrans(getLargeId, query, snapshot, getTotText, getChangeText, getPayAmount)
                                        putCompTrans.key.toString()
                                    }
                                } else {
                                    // Handle the case where no data is found
                                    getLargeId = 0
                                    putCompTrans(getLargeId, query, snapshot, getTotText, getChangeText, getPayAmount)
                                }

                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle the error case
                            }
                        })

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
            resetTrans(amountTot,payChange,payAmount)

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



        btnAdd.setOnClickListener {

            val qtyVal = qnty.text.toString().toInt()
            val barcode = prodBCSelect.text.toString()
            val name = prodNameSelect.text.toString()
            val quantity = qtyVal.toString().toInt()
            val price = priceProd.toString().toDouble()
            val total: Double = quantity * price
            val db = FirebaseDatabase.getInstance().getReference("$userName/Order/ongoingTransactions")
            val uniqueKey = db.push().key
            // Create a new Item object
            val transaction = uniqueKey.toString()
            val item = buyModel(transaction,barcode, name, quantity, price, total)
            itemData["TransactionID"] = transaction
            itemData["itemBarcode"] = barcode
            itemData["itemName"] = name
            itemData["itemQuantity"] = quantity
            itemData["itemPrice"] = price
            itemData["itemTotal"] = total
            /*
            db.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(setDb in snapshot.children){
                            val getProdId = setDb.key
                            if(snapshot.child("itemBarcode").getValue(String::class.java) == barcode ){
                                if(getProdId == "itemQuantity"){

                                }
                            }

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            */

            // Add the new item to the list
            val newPosList = db.push()
            newPosList.setValue(itemData)
            viewModel.addItem(item)

            // Update the adapter with the updated list from the BuyViewModel
            adapter.updateItems(viewModel.getItems())

            adapter.notifyDataSetChanged()
            var totalPrice = amountTot.text.toString().toDouble() + total
            amountTot.text = totalPrice.toString()
            prodBCSelect.text = ""
            qnty.text = "0"
            prodNameSelect.text = ""
            totalProdSelect.text = ""
            prodPriceSelect.text = ""


            //This is just a run if it can decrease the quantiy from Products

            /*
            for (i in itemList) {
                totalPrice += i.itemTotal
                amountTot.text = totalPrice.toString()
            }*/

            // Update the adapter with the updated list
            //adapter.updateItems(itemList)
        }

        plusBtn.setOnClickListener {
            val qntyVal = qnty.text.toString().toInt()
            if (qntyVal != qtyGet.toInt() && qtyGet.toInt() >= 0) {
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
            if (qntyVal > 0) {
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
        val query: Query = FirebaseDatabase.getInstance().getReference("$userName/Products").orderByChild("itemBarcode").equalTo(barcode)
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
                        itemQuantity = productSnapshot.child("itemQuantity").value.toString().toInt()
                        qtyGet = itemQuantity.toString()
                        setTot()
                        prodNameSelect.text = nameProd
                        prodPriceSelect.text = "\u20B1 ${priceProd.toDouble()}"
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
        databaseReference = FirebaseDatabase.getInstance().getReference("$userName/Order/ongoingTransactions")
        val query: Query = databaseReference.orderByKey()

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear() // Clear the existing itemList
                if (snapshot.exists()) {
                    for (buySel in snapshot.children) {
                        val itemData = buySel.getValue(buyModel::class.java)
                        if (itemData != null) {
                            itemList.add(itemData) // Add the item to the itemList
                        }
                    }

                    // Update the adapter with the updated itemList
                    adapter.updateItems(itemList)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the cancellation
            }
        })
    }
     @RequiresApi(Build.VERSION_CODES.O)
     fun putCompTrans(TranId:Int, query: DatabaseReference, snapshot: DataSnapshot, amountTot: String, changePay: String, amountPay: String){
        val putCompTransChild = FirebaseDatabase.getInstance().getReference("$userName/Order/completeTransactions/${TranId.toString()}")
        for(transBuy in snapshot.children){
            val getBarcode = transBuy.child("itemBarcode").getValue(String::class.java)
            val prodName = transBuy.child("itemName").getValue(String::class.java)
            val prodPrice = transBuy.child("itemPrice").getValue(Int::class.java)
            val prodQnty = transBuy.child("itemQuantity").getValue(Int::class.java)
            val itemTot = transBuy.child("itemTotal").getValue(Double::class.java)


            itemData["itemBarcode"] = getBarcode.toString()
            itemData["itemName"] = prodName.toString()
            itemData["itemQuantity"] = prodQnty.toString()
            itemData["itemPrice"] = prodPrice.toString()
            itemData["itemTotal"] = itemTot.toString()
            // Add the new item to the list
            val newPosList = putCompTransChild.push()
            newPosList.setValue(itemData)
            val UpdProd = FirebaseDatabase.getInstance().getReference("$userName/Products").orderByChild("itemBarcode").equalTo(getBarcode.toString())
            UpdProd.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val prodSnapShot = snapshot.children.first()
                    val itemQuantRef = prodSnapShot.child("itemQuantity").ref
                    if(prodSnapShot.child("itemQuantity").getValue(Int::class.java).toString().toInt() != 0 && prodSnapShot.child("itemQuantity").getValue(Int::class.java).toString().toInt() >= 1){
                        val updateQuant =  prodSnapShot.child("itemQuantity").getValue(Int::class.java).toString().toInt() - prodQnty.toString().toInt()
                        itemQuantRef.setValue(updateQuant)
                    }else{
                        Toast.makeText(this@PosActivity,"the \"$prodName\" is empty please schedule to restock",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
         putCompTransChild.child("totalBuy").setValue(amountTot)
         putCompTransChild.child("custPay").setValue(amountPay)
         putCompTransChild.child("changeGiven").setValue(changePay)

         val currentDate = LocalDate.now().toString()
         putCompTransChild.child("dateTrans").setValue(currentDate)

         val currentTime = LocalTime.now()
         val format = DateTimeFormatter.ofPattern("hh:mm a")
         val currentTimeString = currentTime.format(format)
         putCompTransChild.child("timeTrans").setValue(currentTimeString)

    }
    private fun setTot(){
        val CalQntPrice = qnty.text.toString().toInt() * priceProd
        totalProdSelect.text = CalQntPrice.toString()
    }

    private fun resetTrans(amountTot:TextView, payChange:TextView,payAmount:EditText){
        amountTot.text = "0"
        payChange.text = "0"
        payAmount.text.clear()
        qnty.text = "1"
    }
}