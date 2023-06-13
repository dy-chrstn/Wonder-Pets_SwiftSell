package com.example.scanit

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat


class HomeFragment : Fragment() {
    private lateinit var requestCamera: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val scanBtn = view.findViewById<ImageButton>(R.id.camBtn)

        // Request camera permission using registerForActivityResult
        requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission(),){
            if(it){
                val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
                requireContext().startActivity(intent)
            }else{
                Toast.makeText(activity,"Permission not Granted",Toast.LENGTH_SHORT).show()
            }
        }
        scanBtn.setOnClickListener(){

            requestCamera?.launch(android.Manifest.permission.CAMERA)
            Toast.makeText(activity,"hi",Toast.LENGTH_SHORT).show()
        }
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button: CardView = view.findViewById(R.id.food_cv)

        button.setOnClickListener {
            val intent = Intent(requireContext(), ProductListActivity::class.java)
            startActivity(intent)
        }
    }


}