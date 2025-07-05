package com.orhan.mobilebankingapp

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

class FaceVerifyActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_face_verify)

        previewView = findViewById(R.id.previewView)
        btnSave = findViewById(R.id.btnSaveFace)

        tc = intent.getStringExtra("tc") ?: "user"
        name = intent.getStringExtra("name") ?: "user"

        cameraExecutor = Executors.newSingleThreadExecutor()

        btnSave.setOnClickListener { captureImage() }

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
                Log.i("FaceVerify", "Kamera başlatıldı")
            } catch (e: Exception) {
                Log.e("FaceVerify", "Kamera başlatılamadı: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val newFileName = "${tc}_${name}.jpg"
        val newFile = File(picturesDir, newFileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(newFile).build()

        Log.i("FaceVerify", "Fotoğraf kaydedilecek yol: ${newFile.absolutePath}")

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    runOnUiThread {
                        Toast.makeText(this@FaceVerifyActivity, "Yüz kaydedildi, doğrulama yapılıyor...", Toast.LENGTH_SHORT).show()

                        val savedFile = File(picturesDir, "$tc.jpg") // Kayıtlı ilk yüz resmi (kayıt olurken alınan)
                        if (!savedFile.exists()) {
                            Toast.makeText(this@FaceVerifyActivity, "Kayıtlı yüz resmi bulunamadı!", Toast.LENGTH_LONG).show()
                            return@runOnUiThread
                        }

                        val bitmapSaved = BitmapFactory.decodeFile(savedFile.absolutePath)
                        val bitmapNew = BitmapFactory.decodeFile(newFile.absolutePath)

                        val similarity = compareBitmaps(bitmapSaved, bitmapNew)
                        val threshold = 0.9  // 2 foto arasındaki uyum eşiği %0-100

                        if (similarity >= threshold) {
                            Toast.makeText(this@FaceVerifyActivity, "Doğrulama başarılı! Benzerlik: %.2f".format(similarity), Toast.LENGTH_LONG).show()

                            val intent = Intent(this@FaceVerifyActivity, HomeActivity::class.java)
                            intent.putExtra("tc", tc)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@FaceVerifyActivity, "Doğrulama başarısız! Yüzler uyuşmuyor. Lütfen tekrar deneyin. Benzerlik: %.2f".format(similarity), Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(this@FaceVerifyActivity, "Kaydetme hatası: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    //  Resimler 100x100 piksele çevriliyor ve hesaplanıyo (0.0 - 1.0)
    private fun compareBitmaps(bitmap1: Bitmap, bitmap2: Bitmap): Double {
        val width = 100
        val height = 100

        val scaled1 = Bitmap.createScaledBitmap(bitmap1, width, height, false)
        val scaled2 = Bitmap.createScaledBitmap(bitmap2, width, height, false)

        var diff = 0L

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel1 = scaled1.getPixel(x, y)
                val pixel2 = scaled2.getPixel(x, y)

                val r1 = (pixel1 shr 16) and 0xff
                val g1 = (pixel1 shr 8) and 0xff
                val b1 = pixel1 and 0xff

                val r2 = (pixel2 shr 16) and 0xff
                val g2 = (pixel2 shr 8) and 0xff
                val b2 = pixel2 and 0xff

                diff += kotlin.math.abs(r1 - r2) + kotlin.math.abs(g1 - g2) + kotlin.math.abs(b1 - b2)
            }
        }

        val maxDiff = width * height * 3 * 255
        return 1.0 - diff.toDouble() / maxDiff
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
