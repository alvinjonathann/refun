package com.refun.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.refun.app.R
import com.refun.app.databinding.ItemHistoryBinding
import com.refun.app.model.RedemptionHistory
import java.text.SimpleDateFormat
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
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(history: RedemptionHistory) {
            binding.apply {
                dateText.text = dateFormat.format(history.date)
                timeText.text = timeFormat.format(history.date)
                
                // Set points text and color based on whether it's positive or negative
                val pointsDisplay = if (history.points >= 0) "+${history.points}" else "${history.points}"
                pointsText.text = "$pointsDisplay points"
                
                // Set color based on points value
                val colorRes = if (history.points >= 0) R.color.success_green else R.color.error_red
                pointsText.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))
                
                statusText.text = history.status
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