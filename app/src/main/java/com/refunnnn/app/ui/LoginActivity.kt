package com.refunnnn.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.refunnnn.app.R
import com.refunnnn.app.databinding.ActivityLoginBinding
import com.refunnnn.app.viewmodel.LoginViewModel
import com.refunnnn.app.viewmodel.LoginState

class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    private val viewModel: LoginViewModel by viewModels()

    override fun getViewBinding(): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupClickListeners()
        observeLoginState()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this, Observer { state ->
            when (state) {
                is LoginState.Loading -> {
                    showProgress(true)
                }
                is LoginState.Success -> {
                    showProgress(false)
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startHomeActivity()
                }
                is LoginState.Error -> {
                    showProgress(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    private fun startHomeActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showProgress(show: Boolean) {
        binding.loginButton.isEnabled = !show
        binding.emailLayout.isEnabled = !show
        binding.passwordLayout.isEnabled = !show
        binding.registerButton.isEnabled = !show
    }
} 