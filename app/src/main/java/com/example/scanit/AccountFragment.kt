package com.example.scanit

import ScanItSharedPreferences
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.scanit.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var sharedPreferences: ScanItSharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        sharedPreferences = ScanItSharedPreferences.getInstance(requireContext())

        // Set the password field to display asterisks
        binding.editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        // Disable clicking and editing for the username and password fields
        binding.editTextUsername.isClickable = false
        binding.editTextUsername.isFocusable = false
        binding.editTextUsername.isFocusableInTouchMode = false
        binding.editTextPassword.isClickable = false
        binding.editTextPassword.isFocusable = false
        binding.editTextPassword.isFocusableInTouchMode = false

        // Load the username and password from ScanItSharedPreferences
        val username = sharedPreferences.getUsername()
        val password = sharedPreferences.getPassword()
        binding.editTextUsername.setText(username.toString())
        binding.editTextPassword.setText(password.toString())

        // Attach logout button click listener
        binding.logoutButton.setOnClickListener {
            onLogout()
        }

        return binding.root
    }

    private fun onLogout() {
        // Clear the user's login status in shared preferences
        sharedPreferences.setLoginStatus(false)

        // Clear all activities and start a fresh instance of the Login activity
        val intent = Intent(requireContext(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
