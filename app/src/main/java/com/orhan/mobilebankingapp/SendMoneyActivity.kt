package com.orhan.mobilebankingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SendMoneyActivity : AppCompatActivity() {

    private lateinit var etReceiverTc: EditText
    private lateinit var etAmount: EditText
    private lateinit var btnSend: Button

    private lateinit var database: DatabaseReference
    private lateinit var senderTc: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_money)

        etReceiverTc = findViewById(R.id.etReceiverTc)
        etAmount = findViewById(R.id.etAmount)
        btnSend = findViewById(R.id.btnSend)

        senderTc = intent.getStringExtra("tc") ?: ""

        database = FirebaseDatabase.getInstance().getReference("users")

        btnSend.setOnClickListener {
            val targetTc = etReceiverTc.text.toString().trim()
            val amountText = etAmount.text.toString().trim()

            if (targetTc.length != 11) {
                Toast.makeText(this, "Lütfen geçerli bir TC girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Lütfen geçerli bir tutar girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (targetTc == senderTc) {
                Toast.makeText(this, "Kendinize para gönderemezsiniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendMoney(targetTc, amount)
        }
    }

    private fun sendMoney(targetTc: String, amount: Double) {
        database.child(senderTc).get().addOnSuccessListener { senderSnapshot ->
            if (!senderSnapshot.exists()) {
                Toast.makeText(this, "Gönderici kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }
            val senderBalance = senderSnapshot.child("balance").getValue(Double::class.java) ?: 0.0

            if (senderBalance < amount) {
                Toast.makeText(this, "Yetersiz bakiye", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            database.child(targetTc).get().addOnSuccessListener { receiverSnapshot ->
                if (!receiverSnapshot.exists()) {
                    Toast.makeText(this, "Hedef kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val receiverBalance = receiverSnapshot.child("balance").getValue(Double::class.java) ?: 0.0

                val newSenderBalance = senderBalance - amount
                val newReceiverBalance = receiverBalance + amount

                val updates = hashMapOf<String, Any>(
                    "$senderTc/balance" to newSenderBalance,
                    "$targetTc/balance" to newReceiverBalance
                )

                database.updateChildren(updates).addOnSuccessListener {

                    Toast.makeText(this, "Para transferi başarılı", Toast.LENGTH_SHORT).show()

                    // İşlem kaydı - Gönderici için
                    val senderTransaction = mapOf(
                        "type" to "Para Gönderme",
                        "amount" to amount,
                        "date" to getCurrentDateTime(),
                        "info" to "Alıcı TC: $targetTc"
                    )
                    database.child(senderTc).child("transactions").push().setValue(senderTransaction)

                    // İşlem kaydı - Alıcı için
                    val receiverTransaction = mapOf(
                        "type" to "Para Alma",
                        "amount" to amount,
                        "date" to getCurrentDateTime(),
                        "info" to "Gönderici TC: $senderTc"
                    )
                    database.child(targetTc).child("transactions").push().setValue(receiverTransaction)

                    // Ana sayfaya dönüş
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("tc", senderTc)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                }.addOnFailureListener {
                    Toast.makeText(this, "Transfer başarısız: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Hedef kullanıcı verisi alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Gönderici verisi alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
