package com.orhan.mobilebankingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var etTc: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etTc = findViewById(R.id.etTc)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val tc = etTc.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (tc.length != 11) {
                Toast.makeText(this, "Lütfen 11 haneli TC girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length != 6) {
                Toast.makeText(this, "Lütfen 6 haneli şifre girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance().getReference("users")
            database.child(tc).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val savedPassword = snapshot.child("password").getValue(String::class.java)
                    if (password == savedPassword) {
                        Toast.makeText(this, "Şifre doğru, yüz doğrulama başlıyor...", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, FaceVerifyActivity::class.java)
                        intent.putExtra("tc", tc)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Şifre yanlış!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Kullanıcı bulunamadı! Kayıt sayfasına yönlendiriliyorsunuz...", Toast.LENGTH_SHORT).show()


                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Hata oluştu: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
