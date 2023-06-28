package com.example.scanit

import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RecoverySystem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class TransactionActivity : AppCompatActivity() {

    private lateinit var listTransView: RecyclerView
    private lateinit var transDB: DatabaseReference
    private var arrayTrans: MutableList<buyModel> = mutableListOf()
    private lateinit var adapterTransView: viewTransAdapt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        listTransView = findViewById(R.id.recyclerViewTransaction)
        listTransView.layoutManager = LinearLayoutManager(this)
        adapterTransView = viewTransAdapt(ArrayList(arrayTrans))
        listTransView.adapter =  adapterTransView
        getTransItem()
    }

    private fun getTransItem(){
        val payTot = findViewById<TextView>(R.id.totPrice)
        val payAmount = findViewById<TextView>(R.id.AmountPay)
        val payChange = findViewById<TextView>(R.id.changeText)
        val id = intent.getStringExtra("getListBought")
        transDB = FirebaseDatabase.getInstance().getReference("Order/completeTransactions/$id")
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

                            arrayTrans.add(buyModel(key,barcodeGet,prodName,itemQuant,prodPrice,itemTot))
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