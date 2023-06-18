package com.example.scanit

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProductListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    private val productList: MutableList<Product> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)
        FirebaseApp.initializeApp(this)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        adapter = ProductAdapter(productList) { selectedProduct ->
            openProductViewActivity(selectedProduct)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        // Initialize Firebase Database and Storage references
        database = FirebaseDatabase.getInstance().reference.child("Products")
        storage = FirebaseStorage.getInstance().reference.child("Products")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()

                for (childSnapshot in snapshot.children) {
                    val uniqueId = childSnapshot.key.toString()
                    val itemName = childSnapshot.child("itemName").value.toString()
                    val itemPrice = childSnapshot.child("itemPrice").value.toString().toInt()
                    val itemQuantity = childSnapshot.child("itemQuantity").value.toString().toInt()
                    val itemCost = childSnapshot.child("itemCost").value.toString().toInt()
                    val itemBarcode = childSnapshot.child("itemBarcode").value.toString()
                    val itemCategory = childSnapshot.child("itemCategory").value.toString()
                    val itemExpiry = childSnapshot.child("itemExpiry").value.toString()

                    // Construct the image URL using the unique ID and the Firebase Storage reference
                    val imageUrlTask = storage.child("$uniqueId.jpg").downloadUrl
                    imageUrlTask.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val productView = Product(itemBarcode, itemCategory, itemName, itemExpiry, itemPrice, itemCost, itemQuantity, imageUrl)
                        productList.add(productView)
                        adapter.notifyDataSetChanged()
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to get image download URL: ${exception.message}")
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read database: ${error.message}")
            }
        })

        // Setting up floating action button
        val fabAddProduct: FloatingActionButton = findViewById(R.id.fabAddProduct)
        fabAddProduct.setOnClickListener {
            val intent = Intent(this@ProductListActivity, AddProductActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openProductViewActivity(selectedProduct: Product) {
        val intent = Intent(this, ProductViewActivity::class.java)
        intent.putExtra("selectedProduct", selectedProduct)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "ProductListActivity"
    }
}
