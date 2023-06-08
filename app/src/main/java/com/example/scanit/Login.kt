package com.example.scanit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        val username = findViewById<EditText>(R.id.username)
        val pw = findViewById<EditText>(R.id.password)

        val signinButton = findViewById<Button>(R.id.sign_in_btn)
        signinButton.setOnClickListener {
            if(username.text.isEmpty() || pw.text.isEmpty()){
                Toast.makeText(this, "Please fill all the empty fields", Toast.LENGTH_SHORT).show()
            }else{
                loginUser()
            }
        }

        val signupButton = findViewById<Button>(R.id.sign_up_button)
        signupButton.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val uname = findViewById<EditText>(R.id.username).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()

        val unameInp = uname + "@ymail.com"
        auth.signInWithEmailAndPassword(unameInp, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Proceed with any additional actions or navigation
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Welcome, $uname", Toast.LENGTH_SHORT).show()

                } else {
                    // User login failed
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        // Invalid user input or email format
                        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        // Invalid user input or email format
                        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Other exceptions
                        Toast.makeText(this, "Login failed. Please try again later.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
