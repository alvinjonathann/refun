package com.refunnnn.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.refunnnn.app.R
import com.refunnnn.app.databinding.FragmentScanBinding

class ScanFragment : Fragment() {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil argumen dariCekHarga dan teruskan ke QRScannerFragment
        val fromCekHarga = arguments?.getBoolean("fromCekHarga") == true
        val bundle = Bundle().apply { putBoolean("fromCekHarga", fromCekHarga) }
        findNavController().navigate(
            R.id.action_scanFragment_to_qrScannerFragment,
            bundle,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.scanFragment, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 