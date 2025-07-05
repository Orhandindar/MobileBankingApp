package com.orhan.mobilebankingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreditActivity : AppCompatActivity() {

    private lateinit var tvCreditStatus: TextView
    private lateinit var etCreditAmount: EditText
    private lateinit var spinnerTerm: Spinner
    private lateinit var tvResult: TextView
    private lateinit var btnApply: Button
    private lateinit var btnPay: Button

    private lateinit var database: DatabaseReference
    private lateinit var tc: String

    private var currentBalance = 0.0
    private var creditRemaining = 0.0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit)

        tvCreditStatus = findViewById(R.id.tvCreditStatus)
        etCreditAmount = findViewById(R.id.etCreditAmount)
        spinnerTerm = findViewById(R.id.spinnerTerm)
        tvResult = findViewById(R.id.tvResult)
        btnApply = findViewById(R.id.btnApply)
        btnPay = findViewById(R.id.btnPay)

        tc = intent.getStringExtra("tc") ?: ""

        database = FirebaseDatabase.getInstance().getReference("users").child(tc)

        val terms = listOf("3 ay", "6 ay", "12 ay")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, terms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTerm.adapter = adapter

        getUserData()

        btnApply.setOnClickListener {
            val creditAmountStr = etCreditAmount.text.toString()
            if (creditAmountStr.isEmpty()) {
                Toast.makeText(this, "Lütfen kredi tutarını girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val creditAmount = creditAmountStr.toDouble()
            if (creditAmount <= 0) {
                Toast.makeText(this, "Geçerli bir kredi tutarı girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedTerm = spinnerTerm.selectedItem.toString()

            val creditData = mapOf(
                "amount" to creditAmount,
                "term" to selectedTerm,
                "remaining" to creditAmount
            )

            val newBalance = currentBalance + creditAmount

            database.child("credit").setValue(creditData).addOnSuccessListener {
                database.child("balance").setValue(newBalance).addOnSuccessListener {
                    Toast.makeText(this, "Kredi başvurusu başarılı! Bakiye güncellendi.", Toast.LENGTH_SHORT).show()

                    // İşlem kaydı - Kredi Başvurusu
                    val transaction = mapOf(
                        "type" to "Kredi Başvurusu",
                        "amount" to creditAmount,
                        "date" to getCurrentDateTime(),
                        "info" to "Vade: $selectedTerm"
                    )
                    database.child("transactions").push().setValue(transaction)

                    setResult(RESULT_OK)
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Bakiye güncellenemedi: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Kredi kaydedilemedi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnPay.setOnClickListener {
            if (creditRemaining <= 0) {
                Toast.makeText(this, "Ödenecek kredi yok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val paymentAmount = 1000.0

            if (paymentAmount > creditRemaining) {
                Toast.makeText(this, "Ödeme tutarı kalan krediden fazla olamaz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (paymentAmount > currentBalance) {
                Toast.makeText(this, "Bakiyeniz yetersiz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val remainingAfterPayment = creditRemaining - paymentAmount
            val newBalance = currentBalance - paymentAmount

            database.child("credit").child("remaining").setValue(remainingAfterPayment).addOnSuccessListener {
                database.child("balance").setValue(newBalance).addOnSuccessListener {
                    Toast.makeText(this, "Kredi başarıyla ödendi!", Toast.LENGTH_SHORT).show()

                    // İşlem kaydı - Kredi Ödemesi
                    val transaction = mapOf(
                        "type" to "Kredi Ödemesi",
                        "amount" to paymentAmount,
                        "date" to getCurrentDateTime(),
                        "info" to "Kalan Borç: $remainingAfterPayment"
                    )
                    database.child("transactions").push().setValue(transaction)

                    setResult(RESULT_OK)
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Bakiye güncellenemedi: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Kredi bilgisi güncellenemedi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserData() {
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                currentBalance = snapshot.child("balance").getValue(Double::class.java) ?: 0.0
                val creditSnapshot = snapshot.child("credit")
                creditRemaining = if (creditSnapshot.exists()) {
                    creditSnapshot.child("remaining").getValue(Double::class.java) ?: 0.0
                } else {
                    0.0
                }
                updateUI()
            }
        }
    }

    private fun updateUI() {
        tvCreditStatus.text = if (creditRemaining > 0) {
            "Kalan kredi borcu: $creditRemaining ₺"
        } else {
            "Aktif kredi yok"
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
