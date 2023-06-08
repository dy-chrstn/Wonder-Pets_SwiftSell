package com.example.scanit

import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.example.scanit.databinding.ActivityAddProductBinding
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddProductActivity : AppCompatActivity() {
    var sImage:String? = ""
    private lateinit var db: DatabaseReference
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var buttonDate : Button
    private lateinit var txtDatePicker : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        // Get the spinner from the layout
        val spinner = findViewById<Spinner>(R.id.spinner)
        val textView = findViewById<TextView>(R.id.date_text)

        val button = findViewById<Button>(R.id.date_button)

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




        button.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    textView.text = "${month + 1}/${dayOfMonth}/${year}"
                    textView.setTextColor(Color.parseColor("#FFD691"))
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }



    }



    fun insertData(view: View){

    }


    fun uploadImage(view: View){

    }


}