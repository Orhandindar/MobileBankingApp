package com.orhan.mobilebankingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvBalance: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnTransactions: Button
    private lateinit var btnSendMoney: Button
    private lateinit var btnPayBills: Button
    private lateinit var btnApplyCredit: Button

    private lateinit var database: DatabaseReference
    private lateinit var tc: String

    private val REQUEST_CODE_UPDATE_BALANCE = 100

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvName = findViewById(R.id.tvName)
        tvBalance = findViewById(R.id.tvBalance)
        btnLogout = findViewById(R.id.btnLogout)
        btnTransactions = findViewById(R.id.btnTransactions)
        btnSendMoney = findViewById(R.id.btnSendMoney)
        btnPayBills = findViewById(R.id.btnPayBills)
        btnApplyCredit = findViewById(R.id.btnApplyCredit)

        tc = intent.getStringExtra("tc") ?: ""

        database = FirebaseDatabase.getInstance().getReference("users")

        getUserInfo()

        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnTransactions.setOnClickListener {
            val intent = Intent(this, TransactionsActivity::class.java)
            intent.putExtra("tc", tc)
            startActivityForResult(intent, REQUEST_CODE_UPDATE_BALANCE)
        }

        btnSendMoney.setOnClickListener {
            val intent = Intent(this, SendMoneyActivity::class.java)
            intent.putExtra("tc", tc)
            startActivityForResult(intent, REQUEST_CODE_UPDATE_BALANCE)
        }

        btnPayBills.setOnClickListener {
            val intent = Intent(this, PayBillsActivity::class.java)
            intent.putExtra("tc", tc)
            startActivityForResult(intent, REQUEST_CODE_UPDATE_BALANCE)
        }

        btnApplyCredit.setOnClickListener {
            val intent = Intent(this, CreditActivity::class.java)
            intent.putExtra("tc", tc)
            startActivityForResult(intent, REQUEST_CODE_UPDATE_BALANCE)
        }
    }

    private fun getUserInfo() {
        database.child(tc).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").value.toString().uppercase()
                val balance = snapshot.child("balance").value.toString()

                tvName.text = name
                tvBalance.text = "Bakiye: $balance ₺"
            } else {
                Toast.makeText(this, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Veri alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE_BALANCE && resultCode == RESULT_OK) {
            getUserInfo()
            Toast.makeText(this, "Bakiye güncellendi", Toast.LENGTH_SHORT).show()
        }
    }
}
