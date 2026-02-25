package dev.ajthom.lansingferry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ajthom.lansingferry.ui.navigation.FerryNavigation
import dev.ajthom.lansingferry.ui.theme.LansingFerryTheme
import dev.ajthom.lansingferry.viewmodel.FerryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LansingFerryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: FerryViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsState()

                    when {
                        uiState.isLoading && uiState.ferryInfo == null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        uiState.ferryInfo != null -> {
                            FerryNavigation(
                                ferryInfo = uiState.ferryInfo!!,
                                isRefreshing = uiState.isLoading,
                                onRefresh = { viewModel.refresh() },
                            )
                        }
                        uiState.errorMessage != null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(uiState.errorMessage!!)
                            }
                        }
                    }
                }
            }
        }
    }
}
