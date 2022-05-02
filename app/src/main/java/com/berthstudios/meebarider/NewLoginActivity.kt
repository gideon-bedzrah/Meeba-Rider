package com.berthstudios.meebarider

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NewLoginActivity : AppCompatActivity() {

    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var auth: FirebaseAuth
    private lateinit var loginTapped: Button
    private lateinit var signupButton: TextView
    private lateinit var terms: TextView
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_login)
        auth = Firebase.auth
        signupButton = findViewById(R.id.text_view_signup)

        signupButton.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        })

        val currentUser = auth.currentUser


        if(currentUser != null) {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        emailField = findViewById(R.id.edit_text_email_login)
        passwordField = findViewById(R.id.edit_text_password_login)
        terms = findViewById(R.id.text_view_terms_and_conditions)

        terms.setOnClickListener {
            val url = "https://www.meeba-sisia.com/legal/terms-conditions"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }


        loginTapped = findViewById(R.id.button_login)
        loginTapped.setOnClickListener {

            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        val TAG = "this"
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()

                        }
                    }
            }else {
                Toast.makeText(this, "Email and password are required for authentication.",
                    Toast.LENGTH_SHORT).show()
            }

        }
    }


}