package com.refunnnn.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private var fromCekHarga: Boolean = false
    private var isProcessingQRCode = false
    private val scannedPoints = mutableListOf<PointItem>()
    private lateinit var pointsAdapter: PointsAdapter

    data class PointItem(val bottleName: String, val point: Long)

    class PointsAdapter(private val items: List<PointItem>) : RecyclerView.Adapter<PointsAdapter.PointViewHolder>() {
        class PointViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(android.R.id.text1)
            val point: TextView = view.findViewById(android.R.id.text2)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return PointViewHolder(v)
        }
        override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
            val item = items[position]
            holder.name.text = item.bottleName
            holder.point.text = "Poin: ${item.point}"
        }
        override fun getItemCount() = items.size
    }

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

        fromCekHarga = arguments?.getBoolean("fromCekHarga") == true

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
        binding.btnBack.setOnClickListener {
            if (isAdded && findNavController().currentDestination?.id == R.id.qrScannerFragment) {
                findNavController().popBackStack()
            }
        }

        pointsAdapter = PointsAdapter(scannedPoints)
        binding.pointsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.pointsRecyclerView.adapter = pointsAdapter
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
        if (isProcessingQRCode) return
        isProcessingQRCode = true
        if (!isAdded || _binding == null) return
        db.collection("transactions").document(qrValue)
            .get()
            .addOnSuccessListener { doc ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                if (!doc.exists()) {
                    Toast.makeText(requireContext(), "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                //edited (delete later)
//                val bottleIds = doc.get("bottle_ids") as? List<String> ?: emptyList()
//                Log.d("BottleIDS", bottleIds.toString())

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

                var totalPoints : Long = 0
                //edited
//                val bottleId = bottleIds[0]
                //edited
                for (bottleId in bottleIds) {
                    db.collection("bottle_barcodes").document(bottleId)
                        .get()
                        .addOnSuccessListener { bottleDoc ->
                            if (!isAdded || _binding == null) return@addOnSuccessListener
                            if (!bottleDoc.exists()) {
                                Toast.makeText(requireContext(), "Data botol tidak ditemukan", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }
                            val point = bottleDoc.getLong("point") ?: 0
                            totalPoints += point
                            val bottleName = bottleDoc.getString("name") ?: "Unknown Bottle"
                            scannedPoints.add(PointItem(bottleName, point))
                            pointsAdapter.notifyItemInserted(scannedPoints.size - 1)
                            binding.resultCard.visibility = View.VISIBLE
                            binding.bottleName.text = bottleName
                            binding.pointsEarned.text = "Poin yang didapat: $point"

//                            // Update transaction status to used
//                            db.collection("transactions").document(qrValue)
//                                .update("is_used", true)
//                                .addOnSuccessListener {
//                                    addPointsToUser(point) {
//                                        saveRedemptionHistory(qrValue, bottleId, point)
//                                        if (fromCekHarga) {
//                                            findNavController().navigate(
//                                                R.id.homeFragment,
//                                                null,
//                                                androidx.navigation.NavOptions.Builder()
//                                                    .setPopUpTo(R.id.homeFragment, false)
//                                                    .build()
//                                            )
//                                        }
//                                    }
//                                }
//                                .addOnFailureListener {
//                                    if (!isAdded || _binding == null) return@addOnFailureListener
//                                    Toast.makeText(requireContext(), "Gagal memperbarui status transaksi", Toast.LENGTH_SHORT).show()
//                                }
                        }
                        .addOnFailureListener {
                            if (!isAdded || _binding == null) return@addOnFailureListener
                            Toast.makeText(requireContext(), "Gagal mengambil data botol", Toast.LENGTH_SHORT).show()
                        }
                }

                //edited
                // Update transaction status to used
                db.collection("transactions").document(qrValue)
                    .update("is_used", true)
                    .addOnSuccessListener {
                        // edited addPointsToUser(point) {
                        addPointsToUser(totalPoints) {
                            // edited saveRedemptionHistory(qrValue, bottleId, point)
                            saveRedemptionHistory(qrValue, bottleIds, totalPoints)
                            if (fromCekHarga) {
                                findNavController().navigate(
                                    R.id.homeFragment,
                                    null,
                                    androidx.navigation.NavOptions.Builder()
                                        .setPopUpTo(R.id.homeFragment, false)
                                        .build()
                                )
                            }
                        }
                    }
                    .addOnFailureListener {
                        if (!isAdded || _binding == null) return@addOnFailureListener
                        Toast.makeText(requireContext(), "Gagal memperbarui status transaksi", Toast.LENGTH_SHORT).show()
                    }

//                db.collection("bottle_barcodes").document(bottleId)
//                    .get()
//                    .addOnSuccessListener { bottleDoc ->
//                        if (!isAdded || _binding == null) return@addOnSuccessListener
//                        if (!bottleDoc.exists()) {
//                            Toast.makeText(requireContext(), "Data botol tidak ditemukan", Toast.LENGTH_SHORT).show()
//                            return@addOnSuccessListener
//                        }
//                        val point = bottleDoc.getLong("point") ?: 0
//                        val bottleName = bottleDoc.getString("name") ?: "Unknown Bottle"
//                        scannedPoints.add(PointItem(bottleName, point))
//                        pointsAdapter.notifyItemInserted(scannedPoints.size - 1)
//                        binding.resultCard.visibility = View.VISIBLE
//                        binding.bottleName.text = bottleName
//                        binding.pointsEarned.text = "Poin yang didapat: $point"
//
//                        // Update transaction status to used
//                        db.collection("transactions").document(qrValue)
//                            .update("is_used", true)
//                            .addOnSuccessListener {
//                                addPointsToUser(point) {
//                                    saveRedemptionHistory(qrValue, bottleId, point)
//                                    if (fromCekHarga) {
//                                        findNavController().navigate(
//                                            R.id.homeFragment,
//                                            null,
//                                            androidx.navigation.NavOptions.Builder()
//                                                .setPopUpTo(R.id.homeFragment, false)
//                                                .build()
//                                        )
//                                    }
//                                }
//                            }
//                            .addOnFailureListener {
//                                if (!isAdded || _binding == null) return@addOnFailureListener
//                                Toast.makeText(requireContext(), "Gagal memperbarui status transaksi", Toast.LENGTH_SHORT).show()
//                            }
//                    }
//                    .addOnFailureListener {
//                        if (!isAdded || _binding == null) return@addOnFailureListener
//                        Toast.makeText(requireContext(), "Gagal mengambil data botol", Toast.LENGTH_SHORT).show()
//                    }
                // end block
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

//    private fun saveRedemptionHistory(transactionId: String, bottleId: String) {// edited, point: Long) {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("bottle_barcodes").document(bottleId)
//            .get()
//            .addOnSuccessListener { bottleDoc ->
//                val bottleName = bottleDoc.getString("name") ?: ""
//                val bottleVolume = bottleDoc.getString("volume") ?: ""
//                val bottlePoint = bottleDoc.getLong("point") ?: 0L
//                val bottleList = listOf(
//                    mapOf(
//                        "nama" to bottleName,
//                        "volume" to bottleVolume,
//                        "point" to bottlePoint
//                    )
//                )
//                val data = hashMapOf(
//                    "transaction_id" to transactionId,
//                    "bottle_list" to bottleList,
//                    "user_id" to uid,
//                    "total_point" to bottlePoint,
//                    "timestamp" to System.currentTimeMillis(),
//                    "location" to "Binus @Bandung - Paskal Campus"
//                )
//                db.collection("redemptions").add(data)
//            }
//    }

    private fun saveRedemptionHistory(transactionId: String, bottleIds: List<String>, totalPoints: Long) {// edited, point: Long) {
        val uid = auth.currentUser?.uid ?: return
//        val bottleMap: MutableMap<String, Any> = mutableMapOf(
//            "nama" to "",
//            "volume" to "",
//            "point" to 0L
//        )
        val bottleList = arrayListOf<MutableMap<String, Any>>()
        Log.d("SendRedemption", bottleIds.toString())
        var fetchCount = 0

        for (bottleId in bottleIds) {
            db.collection("bottle_barcodes").document(bottleId)
                .get()
                .addOnSuccessListener { bottleDoc ->
                    val bottleMap: MutableMap<String, Any> = mutableMapOf(
                        "nama" to "",
                        "volume" to "",
                        "point" to 0L
                    )
                    val bottleName = bottleDoc.getString("name") ?: ""
                    val bottleVolume = bottleDoc.getString("volume") ?: ""
                    val bottlePoint = bottleDoc.getLong("point") ?: 0L
                    bottleMap["nama"] = bottleName
                    bottleMap["volume"] = bottleVolume
                    bottleMap["point"] = bottlePoint
                    bottleList.add(bottleMap)
                    fetchCount++

                    if (fetchCount == bottleIds.size) {
                        val finalList: List<Map<String, Any>> = bottleList.map { it.toMap() }
                        val data = hashMapOf(
                            "transaction_id" to transactionId,
                            "bottle_list" to finalList,
                            "user_id" to uid,
                            "total_point" to totalPoints,
                            "timestamp" to System.currentTimeMillis(),
                            "location" to "Binus @Bandung - Paskal Campus"
                        )
                        db.collection("redemptions").add(data)
                            .addOnSuccessListener { result ->
                                Log.d("SendRedemption", result.id)
                            }
                            .addOnFailureListener { e ->
                                Log.d("SendRedemption", e.toString())
                            }
                    }
                }
        }
//        val finalList: List<Map<String, Any>> = bottleList.map { it.toMap() }
//        Log.d("SendRedemption", bottleList.toString())
//        Log.d("SendRedemption", finalList.toString())
//        val data = hashMapOf(
//            "transaction_id" to transactionId,
//            "bottle_list" to finalList,
//            "user_id" to uid,
//            "total_point" to totalPoints,
//            "timestamp" to System.currentTimeMillis(),
//            "location" to "Binus @Bandung - Paskal Campus"
//        )
//        Log.d("SendRedemption", data["transaction_id"].toString())
////        db.collection("redemptions").add(data)
////            .addOnFailureListener { e ->
////                Log.d("SendRedemption", e.toString())
////            }
//        db.collection("redeptions").document("tes-upload")
//            .set(data)
//            .addOnFailureListener { e ->
//                Log.d("SendRedemption", e.toString())
//            }


//        db.collection("bottle_barcodes").document(bottleId)
//            .get()
//            .addOnSuccessListener { bottleDoc ->
//                val bottleName = bottleDoc.getString("name") ?: ""
//                val bottleVolume = bottleDoc.getString("volume") ?: ""
//                val bottlePoint = bottleDoc.getLong("point") ?: 0L
//                val bottleList = listOf(
//                    mapOf(
//                        "nama" to bottleName,
//                        "volume" to bottleVolume,
//                        "point" to bottlePoint
//                    )
//                )
//                val data = hashMapOf(
//                    "transaction_id" to transactionId,
//                    "bottle_list" to bottleList,
//                    "user_id" to uid,
//                    "total_point" to bottlePoint,
//                    "timestamp" to System.currentTimeMillis(),
//                    "location" to "Binus @Bandung - Paskal Campus"
//                )
//                db.collection("redemptions").add(data)
//            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}