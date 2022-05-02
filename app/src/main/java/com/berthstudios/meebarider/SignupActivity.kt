package com.berthstudios.meebarider

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private lateinit var createAccountButton: Button
    private lateinit var firstNameField: EditText
    private lateinit var lastNameField: EditText
    private lateinit var emailField: EditText
    private lateinit var phoneField: EditText
    private lateinit var passwordField: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var backButton: ImageButton
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val sharedPreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        createAccountButton = findViewById(R.id.button_create_account)
        firstNameField = findViewById(R.id.edit_text_first_name)
        lastNameField = findViewById(R.id.edit_text_last_name)
        emailField = findViewById(R.id.edit_text_email)
        phoneField = findViewById(R.id.edit_text_phone)
        passwordField = findViewById(R.id.edit_text_password)
        backButton = findViewById(R.id.button_close)

        backButton.setOnClickListener {
            onBackPressed()
        }

        createAccountButton.setOnClickListener {

            val TAG = "NOW"

            if (!firstNameField.text.toString().isEmpty()|| !lastNameField.text.toString().isEmpty()
                || !emailField.text.toString().isEmpty() || !phoneField.text.toString().isEmpty()
                || !passwordField.text.toString().isEmpty()
            ) {

                val firstname = firstNameField.text.toString()
                val lastname = lastNameField.text.toString()
                val email = emailField.text.toString()
                val phone = phoneField.text.toString()
                val password = passwordField.text.toString()

                val TAG = "signuptag"

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser?.uid

                            val newUser = hashMapOf(
                                "firstname" to firstname,
                                "lastname" to lastname,
                                "email" to email,
                                "phone number" to phone,
                                "userID" to user.toString()
                            )

                            db.collection("riders").document(user.toString())
                                .set(newUser)
                                .addOnSuccessListener {
                                    Log.d(TAG, "DocumentSnapshot successfully written!")
                                    val editor = sharedPreferences?.edit()
                                    editor?.apply{
                                        putString("firstname", newUser["firstname"].toString())
                                        putString("lastname", newUser["lastname"].toString())
                                        putString("email", newUser["email"].toString())
                                        putString("phone number", newUser["phone number"].toString())
                                        putString("userID", newUser["userID"].toString())
                                    }?.apply()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                this, "Authentication failed: " + task.exception,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

//
            } else {
//
                Toast.makeText(
                    this, "Please note that all fields are required!",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }

    }
}