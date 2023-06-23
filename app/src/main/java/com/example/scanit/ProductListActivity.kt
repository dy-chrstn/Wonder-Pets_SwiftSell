package com.example.scanit

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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
    private lateinit var chipGroup: ChipGroup
    private lateinit var categoryReference: DatabaseReference
    private lateinit var selectedChip: Chip

    private val productList: MutableList<Product> = mutableListOf()
    private val filteredProductList: MutableList<Product> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)
        FirebaseApp.initializeApp(this)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        adapter = ProductAdapter(filteredProductList) { selectedProduct ->
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
                        updateFilteredProductList()
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to get image download URL: ${exception.message}")
                    }
                }

                updateFilteredProductList()
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

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnClickListener {
            searchView.isIconified = false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // No need to perform anything on submit
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterProductList(newText)
                return true
            }
        })

        chipGroup = findViewById(R.id.chip_group)
        categoryReference = FirebaseDatabase.getInstance().reference.child("Category")
        selectedChip = findViewById(R.id.chip_all)

        retrieveCategories()
        setupChipGroupListener()

    }

    private fun setupChipGroupListener() {
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            if (chip != null) {
                if (chip != selectedChip) {
                    selectedChip.isChecked = false
                    selectedChip = chip
                }
                selectedChip.setChipBackgroundColorResource(android.R.color.black)
                // Perform the desired action for the selected chip
            }
        }

    }

    private fun retrieveCategories() {
        categoryReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chipGroup.removeAllViews()

                // Add 'All' chip
                val allChip = createChip("All", "chip_all")
                chipGroup.addView(allChip)

                for (childSnapshot in dataSnapshot.children) {
                    val category = childSnapshot.getValue(String::class.java)
                    category?.let {
                        val chip = createChip(it, childSnapshot.key.toString())
                        chipGroup.addView(chip)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductListActivity", "Error retrieving categories: ${error.message}")
            }
        })
    }

    private fun createChip(text: String, chipId: String): Chip {
        val chip = LayoutInflater.from(this).inflate(R.layout.item_chip, chipGroup, false) as Chip
        chip.text = text
        chip.id = View.generateViewId()
        chip.isClickable = true
        chip.isCheckable = true
        chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#FFD691")) // Set initial background color
        chipGroup.isSingleSelection = true

        chip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedChip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#FFD691")) // Reset background color of previously selected chip
                selectedChip = chip
                chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#FAC367")) // Set background color of selected chip (black)
            }
        }

        return chip
    }

    private fun updateFilteredProductList() {
        filteredProductList.clear()
        filteredProductList.addAll(productList)
        adapter.notifyDataSetChanged()
    }

    private fun filterProductList(query: String) {
        filteredProductList.clear()
        filteredProductList.addAll(productList.filter { product ->
            product.itemName.contains(query, ignoreCase = true)
        })
        adapter.notifyDataSetChanged()
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