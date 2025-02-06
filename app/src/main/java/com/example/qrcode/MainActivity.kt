package com.example.qrcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var resultTextView: TextView
    private lateinit var historyButton: ImageButton
    private lateinit var flashButton: ImageButton
    private var isFlashOn = false
    private var camera: Camera? = null


    private val scanHistory = mutableListOf<String>()
    @SuppressLint("MissingInflatedId")
    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        resultTextView = findViewById(R.id.Affiche)
        historyButton = findViewById(R.id.historyButton)















        // تحميل العمليات السابقة من SharedPreferences
        loadScanHistory()

        // طلب إذن الكاميرا
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                1001
            )
        } else {
            startCamera()
        }

        // عند الضغط على زر التاريخ، يفتح شاشة `HistoriqueActivity`
        historyButton.setOnClickListener {
            val intent = Intent(this, HistoriqueActivity::class.java)
            intent.putStringArrayListExtra("SCAN_HISTORY", ArrayList(scanHistory))
            startActivity(intent)
        }
        flashButton = findViewById(R.id.flashButton)
        flashButton.setOnClickListener {
            if (camera != null) {
                isFlashOn = !isFlashOn
                camera?.cameraControl?.enableTorch(isFlashOn)
                flashButton.setImageResource(if (isFlashOn) R.drawable.flashon else R.drawable.flashof)
            }
        }


        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @ExperimentalGetImage
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                processImageProxy(imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

            } catch (e: Exception) {
                Log.e("QRcode", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @ExperimentalGetImage
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val scannedData = when (barcode.valueType) {
                            Barcode.TYPE_URL -> "🔗 Lien : ${barcode.url?.url}"
                            Barcode.TYPE_TEXT -> "📄 Texte : ${barcode.displayValue}"
                            Barcode.TYPE_WIFI -> {
                                val wifi = barcode.wifi
                                if (wifi != null) {
                                    val ssid = wifi.ssid ?: "Inconnu"
                                    val password = wifi.password ?: "Aucun"
                                    val encryptionType = when (wifi.encryptionType) {
                                        Barcode.WiFi.TYPE_OPEN -> "Ouvert"
                                        Barcode.WiFi.TYPE_WEP -> "WEP"
                                        Barcode.WiFi.TYPE_WPA -> "WPA"
                                        else -> "Inconnu"
                                    }
                                    "📶 Réseau WiFi :\nSSID : $ssid\n🔑 Mot de passe : $password\n🔒 Cryptage : $encryptionType"
                                } else {
                                    "⚠️ Aucune donnée WiFi trouvée !"
                                }
                            }
                            else -> "❓ Autres données inconnues"
                        }

                        resultTextView.text = scannedData
                        scanHistory.add(scannedData)
                        saveScanHistory()  // حفظ البيانات الجديدة في SharedPreferences
                    }
                }
                .addOnFailureListener {
                    Log.e("QRcode", "Échec de la lecture du code-barres : ${it.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun saveScanHistory() {
        val sharedPref = getSharedPreferences("ScanHistory", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putStringSet("history", scanHistory.toSet())
        editor.apply()
    }

    private fun loadScanHistory() {
        val sharedPref = getSharedPreferences("ScanHistory", Context.MODE_PRIVATE)
        val savedHistory = sharedPref.getStringSet("history", emptySet()) ?: emptySet()
        scanHistory.addAll(savedHistory)
    }



    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
