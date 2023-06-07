package com.example.scanit

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers.Main


class ProductListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_product_list, container, false)

        val floatingActionButton = view.findViewById<FloatingActionButton>(R.id.floating_add_btn)

        floatingActionButton.setOnClickListener {
            activity?.let {
                val intent = Intent (it, AddProductActivity::class.java)
                it.startActivity(intent)
            }
        }


        return view
    }


}