package com.refunnnn.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import com.refunnnn.app.R
import com.google.firebase.firestore.FirebaseFirestore
import com.refunnnn.app.model.Gift

class GiftCatalogueFragment : Fragment() {
    private val gifts = mutableListOf<Gift>()
    private lateinit var adapter: GiftAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gift_catalogue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerGifts)
        adapter = GiftAdapter(gifts) { gift ->
            val bundle = Bundle().apply { putString("giftId", gift.id) }
            findNavController().navigate(R.id.action_giftCatalogueFragment_to_giftDetailFragment, bundle)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val backBtn = view.findViewById<View>(R.id.btnBackGiftCatalogue)
        backBtn?.setOnClickListener { requireActivity().onBackPressed() }

        // Listen Firestore
        FirebaseFirestore.getInstance()
            .collection("gifts")
            .addSnapshotListener { snapshot, _ ->
                gifts.clear()
                if (snapshot != null && !snapshot.isEmpty) {
                    snapshot.forEach { doc ->
                        val gift = Gift(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            points = (doc.getLong("points") ?: 0L).toInt(),
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: ""
                        )
                        gifts.add(gift)
                    }
                } else {
                    // Dummy jika Firestore kosong
                    gifts.add(
                        Gift(
                            id = "dummy1",
                            title = "Voucher gopay Rp 10.000",
                            points = 50,
                            imageUrl = "",
                            description = "Voucher gopay senilai Rp 10.000"
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
    }
}

// Adapter pakai custom layout
class GiftAdapter(private val items: List<Gift>, val onClick: (Gift) -> Unit) : RecyclerView.Adapter<GiftViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gift, parent, false)
        return GiftViewHolder(view)
    }
    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }
    override fun getItemCount() = items.size
}

class GiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(gift: Gift, onClick: (Gift) -> Unit) {
        val img = itemView.findViewById<ImageView>(R.id.giftImage)
        val title = itemView.findViewById<TextView>(R.id.giftTitle)
        val points = itemView.findViewById<TextView>(R.id.giftPoints)
        img.setImageResource(R.drawable.voucher_gopay)
        title.text = gift.title
        points.text = "${gift.points} points"
        points.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        itemView.setOnClickListener { onClick(gift) }
    }
} 