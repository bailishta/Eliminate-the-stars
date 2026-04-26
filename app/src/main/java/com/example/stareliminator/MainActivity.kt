package com.example.stareliminator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.stareliminator.ui.navigation.NavGraph
import com.example.stareliminator.ui.theme.StarEliminatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContent {
            StarEliminatorTheme {
                NavGraph()
            }
        }
    }
}
