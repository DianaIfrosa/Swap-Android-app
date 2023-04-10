package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.diana.bachelorthesis.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private val TAG: String = LoginActivity::class.java.name
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "LoginActivity is onCreate")
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
    }
}