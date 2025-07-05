package com.orhan.mobilebankingapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceCaptureActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var btnSave: Button
    private var tc: String = "user"
    private var name: String = "user"

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Kamera izni reddedildi", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_capture)

        previewView = findViewById(R.id.previewView)
        btnSave = findViewById(R.id.btnSaveFace)

        tc = intent.getStringExtra("tc") ?: "user"
        name = intent.getStringExtra("name") ?: "user"
        cameraExecutor = Executors.newSingleThreadExecutor()

        btnSave.setOnClickListener {
            captureImageAndGoNext()
        }

        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                Log.i("FaceCapture", "Kamera başlatıldı")
            } catch (e: Exception) {
                Log.e("FaceCapture", "Kamera başlatılamadı: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImageAndGoNext() {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val file = File(picturesDir, "$tc.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    runOnUiThread {
                        Toast.makeText(this@FaceCaptureActivity, "Yüz kaydedildi: ${file.absolutePath}", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@FaceCaptureActivity, LoginActivity::class.java)
                        intent.putExtra("tc", tc)
                        intent.putExtra("name", name)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(this@FaceCaptureActivity, "Kaydetme hatası: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
