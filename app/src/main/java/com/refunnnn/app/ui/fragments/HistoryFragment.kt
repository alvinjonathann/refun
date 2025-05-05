package com.refunnnn.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.refunnnn.app.R
import com.refunnnn.app.adapter.HistoryAdapter
import com.refunnnn.app.model.RedemptionHistory
import android.util.Log

class HistoryFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        adapter = HistoryAdapter()

        val recyclerView = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        val emptyStateLayout = view.findViewById<View>(R.id.emptyStateLayout)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadHistoryData()
    }

    override fun onResume() {
        super.onResume()
        loadHistoryData()
    }

    private fun loadHistoryData() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("HISTORY", "Current UID: $userId")
        firestore.collection("redemptions")
            .whereEqualTo("user_id", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val items = result.map { doc ->
                    RedemptionHistory(
                        id = doc.id,
                        transactionId = doc.getString("transaction_id") ?: "",
                        bottleId = doc.getString("bottle_id") ?: "",
                        point = doc.getLong("point") ?: 0,
                        userId = doc.getString("user_id") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                }
                Log.d("HISTORY", "Found ${items.size} items")
                if (items.isEmpty()) {
                    val recyclerView = view?.findViewById<RecyclerView>(R.id.historyRecyclerView)
                    val emptyStateLayout = view?.findViewById<View>(R.id.emptyStateLayout)
                    emptyStateLayout?.visibility = View.VISIBLE
                    recyclerView?.visibility = View.GONE
                } else {
                    val recyclerView = view?.findViewById<RecyclerView>(R.id.historyRecyclerView)
                    val emptyStateLayout = view?.findViewById<View>(R.id.emptyStateLayout)
                    emptyStateLayout?.visibility = View.GONE
                    recyclerView?.visibility = View.VISIBLE
                    adapter.submitList(items)
                }
            }
            .addOnFailureListener { e ->
                Log.e("HISTORY", "Query failed: ${e.message}")
                e.printStackTrace()
                val recyclerView = view?.findViewById<RecyclerView>(R.id.historyRecyclerView)
                val emptyStateLayout = view?.findViewById<View>(R.id.emptyStateLayout)
                recyclerView?.visibility = View.GONE
                emptyStateLayout?.visibility = View.VISIBLE
            }
    }
} 