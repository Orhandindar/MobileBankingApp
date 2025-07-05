package com.orhan.mobilebankingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PayBillsActivity : AppCompatActivity() {

    private lateinit var spinnerBills: Spinner
    private lateinit var btnPayBill: Button

    private lateinit var database: DatabaseReference
    private lateinit var tc: String

    private var currentBalance = 0.0

    // Faturalar
    private val bills = listOf(
        Pair("05551234567", 100.0),
        Pair("05557654321", 200.0),
        Pair("05331239876", 400.0),
        Pair("05445556677", 600.0),
        Pair("05009998877", 800.0),
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_bills)

        spinnerBills = findViewById(R.id.spinnerBills)
        btnPayBill = findViewById(R.id.btnPayBill)

        tc = intent.getStringExtra("tc") ?: ""
        database = FirebaseDatabase.getInstance().getReference("users").child(tc)


        val billStrings = bills.map { (num, amount) -> "$num - ${amount}₺" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, billStrings)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBills.adapter = adapter

        getUserBalance()

        btnPayBill.setOnClickListener {
            val selectedIndex = spinnerBills.selectedItemPosition
            val (phone, amount) = bills[selectedIndex]

            if (amount > currentBalance) {
                Toast.makeText(this, "Yetersiz bakiye!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newBalance = currentBalance - amount

            database.child("balance").setValue(newBalance).addOnSuccessListener {
                Toast.makeText(this, "$phone numarasına $amount ₺ fatura ödendi.", Toast.LENGTH_SHORT).show()

                // İşlem kaydı - Fatura ödeme
                val transaction = mapOf(
                    "type" to "Fatura Ödeme",
                    "amount" to amount,
                    "date" to getCurrentDateTime(),
                    "info" to "Telefon No: $phone"
                )
                database.child("transactions").push().setValue(transaction)

                currentBalance = newBalance

                setResult(RESULT_OK)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Bakiye güncellenemedi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserBalance() {
        database.child("balance").get().addOnSuccessListener { snapshot ->
            currentBalance = snapshot.getValue(Double::class.java) ?: 0.0
        }.addOnFailureListener {
            Toast.makeText(this, "Bakiye alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
