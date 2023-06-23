package com.example.scanit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardFragment : Fragment() {
    private lateinit var dbItem: DatabaseReference
    private lateinit var transHistoView: RecyclerView
    private lateinit var histoAdapt: transHistoAdapt

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        transHistoView = view.findViewById(R.id.histoTransactView)

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        transHistoView.layoutManager = layoutManager

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buyList = ArrayList<transData>()
        histoAdapt = transHistoAdapt(buyList)
        transHistoView.adapter = histoAdapt

        val databaseReference = FirebaseDatabase.getInstance().getReference()
        val reference = databaseReference.child("Products")

        // Fetch data from Firebase
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rowCount = dataSnapshot.childrenCount
                val productin = view.findViewById<TextView>(R.id.textViewQuantity)
                productin.text = "$rowCount"

                var sum = 0
                for (productSnapshot in dataSnapshot.children) {
                    val itemQuantity = productSnapshot.child("itemQuantity").getValue(Int::class.java)
                    if (itemQuantity != null) {
                        sum += itemQuantity
                    }
                }

                val avgstocks = view.findViewById<TextView>(R.id.allstocks)
                avgstocks.text = "$sum"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Fetch transaction history from Firebase
        dbItem = FirebaseDatabase.getInstance().getReference("Order/completeTransactions")
        dbItem.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(transSnap: DataSnapshot) {
                if (transSnap.exists()) {
                    for (childTransId in transSnap.children) {
                        val valTransId = childTransId.key
                        val transHisto = FirebaseDatabase.getInstance()
                            .getReference("Order/completeTransactions/$valTransId")
                        var makeSent = ""
                        var valTot = 0.0
                        var updTot = 0.0
                        var capListPut = 0
                        var setSent = ""
                        transHisto.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {

                                    for (getTransVal in snapshot.children) {

                                        val prodName =
                                            getTransVal.child("itemName").getValue(String::class.java)
                                        val prodQuant =
                                            getTransVal.child("itemQuantity").getValue(String::class.java)
                                        val prodTot =
                                            getTransVal.child("itemTotal").getValue(String::class.java)

                                        if(capListPut < 7){
                                            setSent =
                                                "${prodName.toString()}...${prodQuant.toString()}...${prodTot.toString()} \n"
                                            capListPut++
                                        }else if(capListPut == 7){
                                            setSent = "..."
                                        }

                                        makeSent = makeSent.plus(setSent)
                                        updTot += prodTot.toString().toDouble()
                                        valTot = updTot
                                    }
                                    val item = transData(
                                        valTransId.toString().toInt(),
                                        makeSent,
                                        valTot
                                    )
                                    Toast.makeText(requireContext(),"$valTot",Toast.LENGTH_SHORT).show()
                                    buyList.add(item)

                                    histoAdapt.updateItems(buyList)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })


                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    companion object {
        // Companion object code
    }
}
