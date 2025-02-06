package com.example.qrcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.BarcodeEncoder

class MainActivity2 : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var qrCodeImageView: ImageView

    private lateinit var generateQRButton: MaterialButton
    private lateinit var scannerQRButton: MaterialButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // Initialize UI elements
        editText = findViewById(R.id.idEdt)
        qrCodeImageView = findViewById(R.id.idIVQRCode)

        generateQRButton = findViewById(R.id.idBtnGenerateQR)
        scannerQRButton = findViewById(R.id.idBtnScannerQR)

        // Generate QR Code on button click
        generateQRButton.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) {
                generateQRCode(text)
            } else {
                Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show()
            }
        }

        // Request permission and start scanning
        scannerQRButton.setOnClickListener {
            val intent =Intent(this , MainActivity::class.java)
            startActivity(intent)

        }
    }

    // Function to generate a QR Code
    private fun generateQRCode(data: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(data, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400)
            qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show()
        }
    }








}

