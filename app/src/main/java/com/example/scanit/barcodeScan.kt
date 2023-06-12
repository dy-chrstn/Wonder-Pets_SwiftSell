package com.example.scanit

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.scanit.databinding.ActivityBarcodeScanBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException


class barcodeScan : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var barcodeDetect: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    var intentData = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    private fun iniBc(){
        barcodeDetect = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.EAN_13)
            .build()
        cameraSource = CameraSource.Builder(this,barcodeDetect)
            .setRequestedPreviewSize(1920, 1080 )
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setAutoFocusEnabled(true)
            .build()
        binding.viewScan!!.holder.addCallback(object : SurfaceHolder.Callback{
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try{
                    cameraSource.start(binding.viewScan!!.holder)

                }catch (e: IOException){
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })
        barcodeDetect.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {
                Toast.makeText(applicationContext,"Barcode Scanner has been stopped", Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if(barcodes.size() != 0){
                    binding.btnAct!!.text = "SEARCH ITEM"
                    intentData = barcodes.valueAt(0).displayValue
                    binding.barText.setText(intentData)
                    //finish()
                    val intent = Intent(this@barcodeScan,ProductViewActivity::class.java)
                    startActivity(intent)
                }
            }

        })
    }
    override fun onPause(){
        super.onPause()
        cameraSource!!.release()
    }

    override fun onResume(){
        super.onResume()
        iniBc()
    }
}