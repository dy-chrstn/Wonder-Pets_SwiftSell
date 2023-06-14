package com.example.scanit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val database = FirebaseDatabase.getInstance()
        val productin = view.findViewById<TextView>(R.id.textViewQuantity)
        val avgstocks = view.findViewById<TextView>(R.id.allstocks)
//        val reference = database.getReference("Products")
        val databaseReference = FirebaseDatabase.getInstance().getReference()
        val reference = databaseReference.child("Products") // Replace "your/path" with the actual path in your database





        reference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rowCount = dataSnapshot.childrenCount
                productin.text = "$rowCount"

// Specify the child key you want to fetch
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                    var sum = 0
                for (productSnapshot in snapshot.children){
                    val itemQuantity = productSnapshot.child("itemQuantity").getValue(Int::class.java)

                    if (itemQuantity != null) {
                        sum += itemQuantity
                    }

                }

                avgstocks.text = "$sum"

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
    companion object {

    }

    class ProductAdapter(private val productList: List<com.example.scanit.Product>): RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
        inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
            val textViewQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return productList.size
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = productList[position]
            holder.textViewQuantity.text = product.itemQuantity.toString()
        }




    }

}