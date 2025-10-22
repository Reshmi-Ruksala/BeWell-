package com.bewell

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignupActivity : AppCompatActivity() {

    private lateinit var fullNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var fullNameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var signupButton: Button
    private lateinit var signInLink: TextView
    private lateinit var termsCheckbox: CheckBox
    private lateinit var googleSignupButton: Button
    private lateinit var facebookSignupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        fullNameInput = findViewById(R.id.fullNameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)
        signupButton = findViewById(R.id.signupButton)
        signInLink = findViewById(R.id.signInLink)
        termsCheckbox = findViewById(R.id.termsCheckbox)
        googleSignupButton = findViewById(R.id.googleSignupButton)
        facebookSignupButton = findViewById(R.id.facebookSignupButton)
    }

    private fun setupClickListeners() {
        signupButton.setOnClickListener {
            performSignup()
        }

        signInLink.setOnClickListener {
            navigateToLogin()
        }

        googleSignupButton.setOnClickListener {
            performGoogleSignup()
        }

        facebookSignupButton.setOnClickListener {
            performFacebookSignup()
        }
    }

    private fun performSignup() {
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (validateInputs(fullName, email, password, confirmPassword)) {
            // TODO: Implement actual signup logic here
            // For now, just show success message and navigate to home
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
    }

    private fun validateInputs(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Clear previous errors
        fullNameInputLayout.error = null
        emailInputLayout.error = null
        passwordInputLayout.error = null
        confirmPasswordInputLayout.error = null

        // Validate full name
        if (fullName.isEmpty()) {
            fullNameInputLayout.error = "Full name is required"
            isValid = false
        } else if (fullName.length < 2) {
            fullNameInputLayout.error = "Name must be at least 2 characters"
            isValid = false
        }

        // Validate email
        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Please enter a valid email"
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        }

        // Validate terms checkbox
        if (!termsCheckbox.isChecked) {
            Toast.makeText(this, "Please accept the Terms of Service", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun performGoogleSignup() {
        // TODO: Implement Google Sign-Up
        Toast.makeText(this, "Google Sign-Up coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun performFacebookSignup() {
        // TODO: Implement Facebook Sign-Up
        Toast.makeText(this, "Facebook Sign-Up coming soon!", Toast.LENGTH_SHORT).show()
    }
}
