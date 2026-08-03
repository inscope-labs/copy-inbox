package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.db.AppDatabase
import com.example.data.repository.ClipRepository
import com.example.ui.components.MainScreen
import com.example.ui.theme.CopyInboxTheme
import com.example.ui.viewmodel.ClipViewModel

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
