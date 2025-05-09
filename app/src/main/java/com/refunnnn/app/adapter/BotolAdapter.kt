package com.refunnnn.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.refunnnn.app.R

// Data class botol
data class Botol(val nama: String, val volume: String, val point: Int)

class BotolAdapter(private val list: List<Botol>, private val isRedeem: Boolean = false) : RecyclerView.Adapter<BotolAdapter.BotolViewHolder>() {

    inner class BotolViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNama: TextView = view.findViewById(R.id.txtNamaBotol)
        val txtVolume: TextView = view.findViewById(R.id.txtVolume)
        val txtPoint: TextView = view.findViewById(R.id.txtPoint)
        val imgIcon: ImageView = view.findViewById(R.id.imgIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_botol, parent, false)
        return BotolViewHolder(view)
    }

    override fun onBindViewHolder(holder: BotolViewHolder, position: Int) {
        val botol = list[position]
        holder.txtNama.text = botol.nama
        holder.txtVolume.text = botol.volume
        if (isRedeem) {
            holder.txtPoint.text = "-${botol.point} poin"
            holder.txtPoint.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
            if (botol.nama.contains("gopay", ignoreCase = true)) {
                holder.imgIcon.setImageResource(R.drawable.voucher_gopay)
                holder.txtVolume.visibility = View.GONE
            } else {
                holder.imgIcon.setImageResource(R.drawable.ic_gopay)
            }
        } else {
            holder.txtPoint.text = "+${botol.point} poin"
            holder.txtPoint.setTextColor(android.graphics.Color.parseColor("#00A859"))
            holder.imgIcon.setImageResource(R.drawable.ic_bottle)
        }
    }

    override fun getItemCount(): Int = list.size
} 