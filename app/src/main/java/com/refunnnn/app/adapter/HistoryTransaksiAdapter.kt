package com.refunnnn.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.refunnnn.app.R
import java.text.SimpleDateFormat
import java.util.*

data class HistoryTransaksi(
    val location: String,
    val timestamp: Long,
    val totalBotol: Int,
    val totalPoin: Int,
    val bottleList: List<Botol>
)

class HistoryTransaksiAdapter(private val list: List<HistoryTransaksi>) : RecyclerView.Adapter<HistoryTransaksiAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTanggal: TextView = view.findViewById(R.id.txtTanggal)
        val txtLocation: TextView = view.findViewById(R.id.txtLocation)
        val txtTotalBotol: TextView = view.findViewById(R.id.txtTotalBotol)
        val txtTotalPoin: TextView = view.findViewById(R.id.txtTotalPoin)
        val recyclerBotol: RecyclerView = view.findViewById(R.id.recyclerBotol)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_transaksi, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", Locale("id"))
        holder.txtTanggal.text = sdf.format(Date(item.timestamp))
        holder.txtLocation.text = item.location
        if (item.location == "Penukaran Poin") {
            holder.txtTotalBotol.text = "1 item"
            holder.txtTotalPoin.text = "- ${item.totalPoin} poin"
            holder.txtTotalPoin.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
            holder.recyclerBotol.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.recyclerBotol.adapter = BotolAdapter(item.bottleList, isRedeem = true)
        } else {
            holder.txtTotalBotol.text = "${item.totalBotol} botol"
            holder.txtTotalPoin.text = "• ${item.totalPoin} poin"
            holder.txtTotalPoin.setTextColor(android.graphics.Color.parseColor("#00A859"))
            holder.recyclerBotol.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.recyclerBotol.adapter = BotolAdapter(item.bottleList)
        }
    }

    override fun getItemCount(): Int = list.size
} 