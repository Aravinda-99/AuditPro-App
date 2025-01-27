package com.example.auditpro

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun onButtonClick(view: View) {
        if (view.id == R.id.Deadline) {
            val intent = Intent(this, Deadline ::class.java)
            startActivity(intent)
        }
        if (view.id == R.id.Documents) {
            val intent = Intent(this, Document::class.java)
            startActivity(intent)
        }
        if (view.id == R.id.Standards) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
    }
}