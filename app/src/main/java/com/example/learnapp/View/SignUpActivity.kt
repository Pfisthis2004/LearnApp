package com.example.learnapp.View

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.View.ui.begin.welcomehome
import com.example.learnapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btncontinue.setOnClickListener {
            val email = binding.edtemail.text.toString()
            val password = binding.edtpassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid ?: return@addOnCompleteListener

//                        // Ghi dữ liệu vào Realtime Database
//                        val database = FirebaseDatabase.getInstance()
//                        val ref = database.getReference("users")

                        val userData = hashMapOf(
                            "email" to email,
                            "displayName" to email.substringBefore("@"),
                            "photoURL" to "https://res.cloudinary.com/djwpvlu9t/image/upload/user-round_1_cx3qfc.png",
                            "createdAt" to System.currentTimeMillis(),
                            "streak" to 0,
                            "totalXP" to 0,
                            "lastLoginAt" to System.currentTimeMillis(),
                            "isPremium" to false,
                            "completedLessons" to emptyList<String>()
                        )


                        firestore.collection("users")
                            .document(uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                            .addOnFailureListener {
                                e -> Toast.makeText(this, "Lỗi Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    else {
                        Toast.makeText(this, "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.btnlogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnback.setOnClickListener {
            startActivity(Intent(this, welcomehome::class.java))
        }
    }
}