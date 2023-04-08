package com.diana.bachelorthesis.view

import android.R
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.diana.bachelorthesis.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private val TAG: String = LoginActivity::class.java.name
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        Log.d(TAG, "LoginActivity is onCreate")
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
    }
}