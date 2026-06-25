package com.example.settlementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.settlementapp.ui.SettlementViewModel
import com.example.settlementapp.ui.SettlementViewModelFactory
import com.example.settlementapp.ui.navigation.SettlementNavHost
import com.example.settlementapp.ui.theme.SettlementAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SettlementViewModel by viewModels {
        SettlementViewModelFactory((application as SettlementApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SettlementAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettlementNavHost(viewModel)
                }
            }
        }
    }
}
