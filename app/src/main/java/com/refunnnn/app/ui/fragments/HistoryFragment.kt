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
import android.widget.TextView
import com.refunnnn.app.adapter.Botol
import com.refunnnn.app.adapter.BotolAdapter
import com.google.firebase.Timestamp
import com.refunnnn.app.adapter.HistoryTransaksi
import com.refunnnn.app.adapter.HistoryTransaksiAdapter

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

        val recyclerHistory = view.findViewById<RecyclerView>(R.id.recyclerHistory)
        recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        val userId = auth.currentUser?.uid ?: return

        // Query hasil scan QR (user_id, total_point, bottle_list, timestamp, location)
        firestore.collection("redemptions")
            .whereEqualTo("user_id", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result1 ->
                val list1 = result1.map { doc ->
                    val bottleListRaw = doc["bottle_list"]
                    val location = doc.getString("location") ?: "Binus @Bandung - Paskal Campus"
                    val totalPoin = (doc.getLong("total_point") ?: 0L).toInt()
                    val bottleList = (bottleListRaw as? List<Map<String, Any>>)?.map {
                        com.refunnnn.app.adapter.Botol(
                            nama = it["nama"] as? String ?: "",
                            volume = it["volume"] as? String ?: "",
                            point = (it["point"] as? Long ?: 0L).toInt()
                        )
                    } ?: emptyList()
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    com.refunnnn.app.adapter.HistoryTransaksi(
                        location = location,
                        timestamp = timestamp,
                        totalBotol = bottleList.size,
                        totalPoin = totalPoin,
                            bottleList = bottleList,
                        voucherCode = null
                    )
                }

                // Query hasil redeem (userId, totalPoints, giftTitle, quantity, timestamp)
                firestore.collection("redemptions")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { result2 ->
                        val list2 = result2.map { doc ->
                            val giftId = doc.getString("giftId") ?: ""
                            val giftTitle = doc.getString("giftTitle") ?: ""
                            val quantity = (doc.getLong("quantity") ?: 0L).toInt()
                            var totalPoints = (doc.getLong("totalPoints") ?: 0L).toInt()
                            val timestamp = (doc.getTimestamp("timestamp")?.toDate()?.time ?: doc.getLong("timestamp") ?: 0L)
                            val displayTitle = if (giftId == "dummy1" || giftTitle.isBlank()) "Voucher gopay Rp 10.000" else giftTitle
                            if (giftId == "dummy1" && totalPoints == 0) totalPoints = 50
                            val bottleList = listOf(
                                com.refunnnn.app.adapter.Botol(
                                    nama = displayTitle,
                                    volume = "",
                                    point = totalPoints
                                )
                            )
                            com.refunnnn.app.adapter.HistoryTransaksi(
                                location = "Penukaran Poin",
                                timestamp = timestamp,
                                totalBotol = quantity,
                                totalPoin = totalPoints,
                                bottleList = bottleList,
                                voucherCode = doc.getString("voucherCode")
                            )
                        }
                        // Gabungkan dan urutkan berdasarkan timestamp
                        val allList = (list1 + list2).sortedByDescending { it.timestamp }
                        recyclerHistory.adapter = com.refunnnn.app.adapter.HistoryTransaksiAdapter(allList)
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("HISTORY", "Query failed: ${e.message}", e)
            }
    }
}
// test github commit error