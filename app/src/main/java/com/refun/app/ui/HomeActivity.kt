package com.refun.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.refun.app.R
import com.refun.app.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Hide the default action bar
            supportActionBar?.hide()

            // Setup bottom navigation
            setupBottomNavigation()

            // Set initial user data
            setupUserData()

            // Setup click listeners
            setupClickListeners()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset bottom navigation selection to home and animate it
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
        animateHomeButton()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on home
                    true
                }
                R.id.navigation_location -> {
                    // Navigate to location activity
                    startActivity(Intent(this, LocationActivity::class.java))
                    true
                }
                R.id.navigation_scan -> {
                    // Navigate to QR scanner activity
                    startActivity(Intent(this, QRScannerActivity::class.java))
                    true
                }
                R.id.navigation_history -> {
                    // Navigate to history activity
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    // Navigate to profile activity
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun animateHomeButton() {
        try {
            // Find the home menu item view
            val bottomNav = binding.bottomNavigation
            val homeItemView = bottomNav.findViewById<android.view.View>(R.id.navigation_home)
            
            // Apply scale animation
            val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.nav_item_scale)
            homeItemView?.startAnimation(scaleAnimation)
        } catch (e: Exception) {
            Log.e(TAG, "Error animating home button", e)
        }
    }

    private fun setupUserData() {
        try {
            // Get user data from session
            val userName = sessionManager.getUserName() ?: "User"
            binding.userName.text = userName
            binding.pointsValue.text = "0"
            binding.bottlesCollected.text = "0"
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up user data", e)
        }
    }

    private fun setupClickListeners() {
        try {
            binding.tukerPoinButton.setOnClickListener {
                // Handle points exchange
            }

            binding.cekHargaButton.setOnClickListener {
                startActivity(Intent(this, QRScannerActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 