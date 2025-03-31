package com.refun.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.refun.app.utils.SessionManager

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn() && this !is LoginActivity && this !is RegisterActivity) {
            // Redirect to login if not logged in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
} 