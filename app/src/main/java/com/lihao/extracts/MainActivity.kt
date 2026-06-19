package com.lihao.extracts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lihao.extracts.ui.navigation.AppNavigation
import com.lihao.extracts.ui.theme.ExtractsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExtractsTheme {
                AppNavigation()
            }
        }
    }
}
