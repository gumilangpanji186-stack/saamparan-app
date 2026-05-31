package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.SaamparanApp
import com.example.ui.SaamparanViewModel
import com.example.ui.theme.SaamparanTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SaamparanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaamparanTheme {
                SaamparanApp(viewModel = viewModel)
            }
        }
    }
}
