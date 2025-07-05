package com.orhan.mobilebankingapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class TransactionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionsAdapter
    private lateinit var transactionsList: MutableList<Transaction>
    private lateinit var database: DatabaseReference
    private lateinit var tc: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        recyclerView = findViewById(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionsList = mutableListOf()
        adapter = TransactionsAdapter(transactionsList)
        recyclerView.adapter = adapter

        tc = intent.getStringExtra("tc") ?: ""
        database = FirebaseDatabase.getInstance().getReference("users").child(tc).child("transactions")

        fetchTransactions()
    }

    private fun fetchTransactions() {
        database.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionsList.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val transaction = child.getValue(Transaction::class.java)
                        transaction?.let { transactionsList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@TransactionsActivity, "Herhangi bir işlem bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransactionsActivity, "Veri alınamadı: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
