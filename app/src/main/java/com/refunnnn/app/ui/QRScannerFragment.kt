package com.refunnnn.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.refunnnn.app.R
import com.refunnnn.app.databinding.FragmentQrScannerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRScannerFragment : Fragment() {
    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize barcode scanner
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Check camera permission
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.viewHistoryButton.setOnClickListener {
            if (isAdded && findNavController().currentDestination?.id == R.id.qrScannerFragment) {
                findNavController().navigate(R.id.action_qrScannerFragment_to_historyFragment)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { qrValue ->
                                            handleScannedQRCode(qrValue)
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        }
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun handleScannedQRCode(qrValue: String) {
        if (!isAdded || _binding == null) return
        db.collection("transactions").document(qrValue)
            .get()
            .addOnSuccessListener { doc ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                if (!doc.exists()) {
                    Toast.makeText(requireContext(), "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                
                // Check if QR code has already been used
                val isUsed = doc.getBoolean("is_used") ?: false
                if (isUsed) {
                    Toast.makeText(requireContext(), "QR code sudah digunakan sebelumnya", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                
                val bottleIds = doc.get("bottle_ids") as? List<String> ?: emptyList()
                if (bottleIds.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada botol di transaksi ini", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val bottleId = bottleIds[0]
                db.collection("bottle_barcodes").document(bottleId)
                    .get()
                    .addOnSuccessListener { bottleDoc ->
                        if (!isAdded || _binding == null) return@addOnSuccessListener
                        if (!bottleDoc.exists()) {
                            Toast.makeText(requireContext(), "Data botol tidak ditemukan", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        val point = bottleDoc.getLong("point") ?: 0
                        val bottleName = bottleDoc.getString("name") ?: "Unknown Bottle"
                        binding.resultCard.visibility = View.VISIBLE
                        binding.bottleName.text = bottleName
                        binding.pointsEarned.text = "Poin yang didapat: $point"
                        
                        // Update transaction status to used
                        db.collection("transactions").document(qrValue)
                            .update("is_used", true)
                            .addOnSuccessListener {
                                addPointsToUser(point) {
                                    saveRedemptionHistory(qrValue, bottleId, point)
                                }
                            }
                            .addOnFailureListener {
                                if (!isAdded || _binding == null) return@addOnFailureListener
                                Toast.makeText(requireContext(), "Gagal memperbarui status transaksi", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        if (!isAdded || _binding == null) return@addOnFailureListener
                        Toast.makeText(requireContext(), "Gagal mengambil data botol", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                Toast.makeText(requireContext(), "Gagal memeriksa transaksi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addPointsToUser(point: Long, onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoint = snapshot.getLong("point") ?: 0
            transaction.update(userRef, "point", currentPoint + point)
        }.addOnSuccessListener {
            if (!isAdded || _binding == null) return@addOnSuccessListener
            Toast.makeText(requireContext(), "Poin berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }.addOnFailureListener {
            if (!isAdded || _binding == null) return@addOnFailureListener
            Toast.makeText(requireContext(), "Gagal menambahkan poin", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveRedemptionHistory(transactionId: String, bottleId: String, point: Long) {
        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "transaction_id" to transactionId,
            "bottle_id" to bottleId,
            "user_id" to uid,
            "point" to point,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("redemptions").add(data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
} 