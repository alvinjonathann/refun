package com.refunnnn.app.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
    val bottleList: List<Botol>,
    val voucherCode: String?
)

class HistoryTransaksiAdapter(private val list: List<HistoryTransaksi>) : RecyclerView.Adapter<HistoryTransaksiAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTanggal: TextView = view.findViewById(R.id.txtTanggal)
        val txtLocation: TextView = view.findViewById(R.id.txtLocation)
        val txtTotalBotol: TextView = view.findViewById(R.id.txtTotalBotol)
        val txtTotalPoin: TextView = view.findViewById(R.id.txtTotalPoin)
        val recyclerBotol: RecyclerView = view.findViewById(R.id.recyclerBotol)
        val txtVoucherCode: TextView = view.findViewById(R.id.txtVoucherCode)
        val btnCopyCode: TextView = view.findViewById(R.id.btnCopyCode)
        val txtBotolLabel: TextView = view.findViewById(R.id.txtBotolLabel)
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
            
            // Check if it's a GoPay voucher and show the code
            val botol = item.bottleList.firstOrNull()
            if (botol?.nama?.contains("gopay", ignoreCase = true) == true) {
                holder.txtBotolLabel.visibility = View.GONE
                val voucherCode = item.voucherCode
                if (voucherCode != null) {
                    holder.txtVoucherCode.visibility = View.VISIBLE
                    holder.txtVoucherCode.text = "Voucher Code: $voucherCode"
                    holder.btnCopyCode.visibility = View.VISIBLE
                    holder.btnCopyCode.setOnClickListener {
                        val clipboard = holder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Voucher Code", voucherCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(holder.itemView.context, "Voucher code copied!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    holder.txtVoucherCode.visibility = View.GONE
                    holder.btnCopyCode.visibility = View.GONE
                }
            } else {
                holder.txtBotolLabel.visibility = View.VISIBLE
                holder.txtVoucherCode.visibility = View.GONE
                holder.btnCopyCode.visibility = View.GONE
            }
            
            holder.recyclerBotol.adapter = BotolAdapter(item.bottleList, isRedeem = true)
            holder.recyclerBotol.layoutManager = LinearLayoutManager(holder.itemView.context)
        } else {
            holder.txtTotalBotol.text = "${item.totalBotol} botol"
            holder.txtTotalPoin.text = "• ${item.totalPoin} poin"
            holder.txtTotalPoin.setTextColor(android.graphics.Color.parseColor("#00A859"))
            holder.recyclerBotol.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.recyclerBotol.adapter = BotolAdapter(item.bottleList)
            holder.txtVoucherCode.visibility = View.GONE
            holder.btnCopyCode.visibility = View.GONE
            holder.txtBotolLabel.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = list.size
} 