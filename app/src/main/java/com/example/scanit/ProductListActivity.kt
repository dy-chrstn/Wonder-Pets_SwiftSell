package com.example.scanit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList: MutableList<Product> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter

        val database = FirebaseDatabase.getInstance()
        val productsRef = database.getReference("Products")

        productsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()

                for (productSnapshot in snapshot.children){
                    val itemName = productSnapshot.child("itemName").getValue(String::class.java)
                    val itemPrice = productSnapshot.child("itemPrice").getValue(Int::class.java)
                    val itemQuantity = productSnapshot.child("itemQuantity").getValue(Int::class.java)
                    val itemImage = productSnapshot.child("itemImage").getValue(String::class.java)
                    val itemCost = productSnapshot.child("itemCost").getValue(Int::class.java)
                    val itemExpiry = productSnapshot.child("itemExpiry").getValue(String::class.java)
                    val itemBarcode = productSnapshot.child("itemBarcode").getValue(String::class.java)
                    val itemCategory = productSnapshot.child("itemCategory").getValue(String::class.java)

                    if (itemName != null && itemPrice != null && itemQuantity != null && itemImage != null &&
                        itemCost != null && itemExpiry != null && itemBarcode != null && itemCategory != null) {
                        val product = Product(itemName, itemPrice, itemQuantity, itemImage, itemCost, itemExpiry, itemBarcode, itemCategory) // Store the image string directly
                        productList.add(product)
                    }

                }

                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val fabAddProduct: FloatingActionButton = findViewById(R.id.fabAddProduct)

        fabAddProduct.setOnClickListener {
            val intent = Intent(this@ProductListActivity, AddProductActivity::class.java)
            startActivity(intent)
        }



    }



}