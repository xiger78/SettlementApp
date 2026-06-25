package com.example.settlementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.settlementapp.ui.SettlementViewModel
import com.example.settlementapp.ui.SettlementViewModelFactory
import com.example.settlementapp.ui.i18n.LocalStrings
import com.example.settlementapp.ui.i18n.stringsFor
import com.example.settlementapp.ui.navigation.SettlementNavHost
import com.example.settlementapp.ui.theme.SettlementAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SettlementViewModel by viewModels {
        val app = application as SettlementApplication
        SettlementViewModelFactory(app.repository, app.settingsStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val language by viewModel.language.collectAsStateWithLifecycle()
            SettlementAppTheme {
                CompositionLocalProvider(LocalStrings provides stringsFor(language)) {
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

    override fun onResume() {
        super.onResume()
        viewModel.refreshLanguageFromSystem()
    }
}
