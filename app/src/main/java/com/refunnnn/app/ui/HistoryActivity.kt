package com.refunnnn.app.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.refunnnn.app.adapter.HistoryAdapter
import com.refunnnn.app.databinding.ActivityHistoryBinding
import com.refunnnn.app.model.RedemptionHistory

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val adapter = HistoryAdapter()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setupRecyclerView()
        loadHistoryData()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadHistoryData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showEmptyState()
            return
        }

        db.collection("redemptions")
            .whereEqualTo("user_id", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val historyItems = documents.map { doc ->
                    RedemptionHistory(
                        id = doc.id,
                        transactionId = doc.getString("transaction_id") ?: "",
                        bottleId = doc.getString("bottle_id") ?: "",
                        point = doc.getLong("point") ?: 0,
                        userId = doc.getString("user_id") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                }

                if (historyItems.isEmpty()) {
                    showEmptyState()
                } else {
                    showHistoryList()
                    adapter.submitList(historyItems)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                showEmptyState()
            }
    }

    private fun showEmptyState() {
        binding.recyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
    }

    private fun showHistoryList() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
    }
} 