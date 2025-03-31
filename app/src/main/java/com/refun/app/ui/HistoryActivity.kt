package com.refun.app.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.refun.app.adapter.HistoryAdapter
import com.refun.app.databinding.ActivityHistoryBinding
import com.refun.app.model.RedemptionHistory
import java.util.Date

class HistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadHistory()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun loadHistory() {
        // TODO: Replace with actual database call
        // This is just sample data for now
        val sampleHistory = listOf(
            RedemptionHistory(
                id = "1",
                points = 500,
                date = Date(),
                status = "Completed"
            ),
            RedemptionHistory(
                id = "2",
                points = 1000,
                date = Date(System.currentTimeMillis() - 86400000), // Yesterday
                status = "Completed"
            )
        )

        if (sampleHistory.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            historyAdapter.submitList(sampleHistory)
        }
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.historyRecyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.VISIBLE
    }
} 