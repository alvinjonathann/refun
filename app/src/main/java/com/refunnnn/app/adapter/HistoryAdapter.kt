package com.refunnnn.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.refunnnn.app.databinding.ItemHistoryBinding
import com.refunnnn.app.model.RedemptionHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<RedemptionHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RedemptionHistory) {
            try {
                val date = Date(item.timestamp)
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                
                binding.dateText.text = dateFormat.format(date)
                binding.timeText.text = timeFormat.format(date)
                binding.pointsText.text = "+${item.point} points"
                binding.statusText.text = "Botol: ${item.bottleId}\nTransaksi: ${item.transactionId}"
            } catch (e: Exception) {
                e.printStackTrace()
                // Set default values in case of error
                binding.dateText.text = "N/A"
                binding.timeText.text = "N/A"
                binding.pointsText.text = "0 points"
                binding.statusText.text = "Unknown"
            }
        }
    }

    private class HistoryDiffCallback : DiffUtil.ItemCallback<RedemptionHistory>() {
        override fun areItemsTheSame(oldItem: RedemptionHistory, newItem: RedemptionHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RedemptionHistory, newItem: RedemptionHistory): Boolean {
            return oldItem == newItem
        }
    }
} 