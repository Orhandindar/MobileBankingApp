package com.orhan.mobilebankingapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etTc: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etTc = findViewById(R.id.etTc)
        etPhone = findViewById(R.id.etPhone)
        etBirthDate = findViewById(R.id.etBirthDate)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)

        etBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = android.app.DatePickerDialog(this, { _, y, m, d ->
                etBirthDate.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
            }, year, month, day)
            datePicker.show()
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val tc = etTc.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val birthDate = etBirthDate.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val phoneValid = phone.startsWith("+90") && phone.length == 13

            if (name.isEmpty() || tc.length != 11 || !phoneValid || birthDate.isEmpty() || password.length != 6) {
                Toast.makeText(this, "Lütfen tüm bilgileri doğru ve eksiksiz giriniz", Toast.LENGTH_SHORT).show()
            } else {
                val database = FirebaseDatabase.getInstance().getReference("users")
                database.child(tc).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Bu TC ile kayıt zaten mevcut! Giriş sayfasına yönlendiriliyorsunuz.",
                                Toast.LENGTH_LONG
                            ).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }, 1500)
                        } else {
                            val userData = hashMapOf(
                                "name" to name.uppercase(),
                                "tc" to tc,
                                "phone" to phone,
                                "birthDate" to birthDate,
                                "password" to password,
                                "balance" to 10000.0
                            )

                            database.child(tc).setValue(userData).addOnSuccessListener {
                                Toast.makeText(this@RegisterActivity, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@RegisterActivity, FaceCaptureActivity::class.java)
                                intent.putExtra("tc", tc)
                                intent.putExtra("password", password)
                                startActivity(intent)
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this@RegisterActivity, "Kayıt sırasında bir hata oluştu", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@RegisterActivity, "Veritabanı hatası: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
