package com.example.contruction_management_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.core.navigation.AppNavHost
import com.example.contruction_management_app.core.ui.theme.CmsAppTheme
import com.example.contruction_management_app.feature.auth.AuthViewModel

class MainActivity : ComponentActivity() {

    private val sessionDataStore by lazy {
        SessionDataStore(applicationContext)
    }

    private val authViewModel by lazy {
        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(sessionDataStore) as T
                }
            }
        )[AuthViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CmsAppTheme {
                AppNavHost(authViewModel = authViewModel)
            }
        }
    }
}