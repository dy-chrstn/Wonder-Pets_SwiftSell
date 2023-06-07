package com.example.scanit

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.example.scanit.databinding.ActivityAddProductBinding
import com.google.firebase.database.DatabaseReference
import java.util.Calendar

class AddProductActivity : AppCompatActivity() {
    var sImage:String? = ""
    private lateinit var db: DatabaseReference
    private lateinit var binding: ActivityAddProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        // Get the spinner from the layout
        val spinner = findViewById<Spinner>(R.id.spinner)
        val buttonDate = findViewById<Button>(R.id.pickDate_btn)
        // Create an array adapter to hold the spinner items
        val items = arrayOf("Food", "Beverages", "Supplies", "Clothing")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)

        // Set the adapter on the spinner
        spinner.adapter = adapter

        // Set an onItemSelected listener on the spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Do something when an item is selected
                println("Selected item: ${items[position]}")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing when nothing is selected
            }
        }

        buttonDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    cal.set(year, month, dayOfMonth)
                    val date = cal.time
                    // Do something with the date
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
        }



    }

    fun insertData(view: View){

    }


    fun uploadImage(view: View){

    }


}