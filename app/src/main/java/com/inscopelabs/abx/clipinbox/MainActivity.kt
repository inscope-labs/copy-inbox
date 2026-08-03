package com.inscopelabs.abx.clipinbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.inscopelabs.abx.clipinbox.data.db.AppDatabase
import com.inscopelabs.abx.clipinbox.data.repository.ClipRepository
import com.inscopelabs.abx.clipinbox.ui.components.MainScreen
import com.inscopelabs.abx.clipinbox.ui.theme.CopyInboxTheme
import com.inscopelabs.abx.clipinbox.ui.viewmodel.ClipViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ClipViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ClipRepository(database.clipDao())
        ClipViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CopyInboxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
