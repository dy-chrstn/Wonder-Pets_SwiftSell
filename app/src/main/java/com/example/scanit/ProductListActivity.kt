package com.example.scanit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.Serializable

class ProductListActivity : AppCompatActivity() {
    private val searchResults: MutableList<Product> = mutableListOf()
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

                // Inside your onDataChange() method
                for (productSnapshot in snapshot.children) {
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
                        val product = Product(itemName, itemPrice, itemQuantity, itemImage, itemCategory, itemCost, itemBarcode, itemExpiry)
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


        val searchView = findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Optional: Perform search as the user types
                return true
            }


        })

    }

    private fun performSearch(query: String?) {
        val database = FirebaseDatabase.getInstance()
        val productsRef = database.getReference("Products")

        val queryRef = productsRef.orderByChild("itemName").equalTo(query)
        queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                searchResults.clear()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        searchResults.add(product)
                        break  // Stop after finding the first matching item
                    }
                }

                ProductAdapter.notifyDataSetChanged()

                if (searchResults.isNotEmpty()) {
                    val selectedProduct = searchResults[0]
                    openSearchResultActivity(selectedProduct)
                } else {
                    Toast.makeText(this@ProductListActivity, "No matching item found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProductListActivity, "Error performing search", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun openSearchResultActivity(selectedProduct: Product) {
        val intent = Intent(this, ProductViewActivity::class.java)
        intent.putExtra("selectedProduct", selectedProduct)
        startActivity(intent)
    }
}