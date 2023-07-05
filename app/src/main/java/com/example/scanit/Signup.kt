package com.example.scanit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.scanit.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

public class Signup : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        binding = ActivitySignupBinding.inflate(layoutInflater)

        auth = Firebase.auth
        /*
        val backLogIn = findViewById<Button>(R.id.signup)
        backLogIn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }*/

        val signBtn: Button = findViewById(R.id.signup)
        signBtn.setOnClickListener {
            signUpInit()
        }
    }

    private fun signUpInit(){
        val uName = findViewById<EditText>(R.id.username).text.toString()
        val uPass = findViewById<EditText>(R.id.password).text.toString()
        val uRePass = findViewById<EditText>(R.id.repassword).text.toString()
        if(uName.isEmpty() || uPass.isEmpty() || uRePass.isEmpty()){
            Toast.makeText(this,"Please filled all the fields", Toast.LENGTH_LONG).show()
            return
        }else if(uPass != uRePass){
            Toast.makeText(this,"Both password not the same", Toast.LENGTH_LONG).show()
            return
        }

        val inputEmail = uName + "@ymail.com"
        val inputPass = uPass

        auth.createUserWithEmailAndPassword(inputEmail,inputPass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val singUpBtn = findViewById<Button>(R.id.signup)
                singUpBtn.setOnClickListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    Toast.makeText(
                        baseContext,
                        "Authentication Success.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            } else {
                // If sign in fails, display a message to the user.

                Toast.makeText(
                    baseContext,
                    "Username is already taken.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
            .addOnFailureListener {
                Toast.makeText(this, "Error Occured ${it.localizedMessage}",Toast.LENGTH_SHORT).show()
            }
    }

}