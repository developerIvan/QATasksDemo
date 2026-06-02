package com.jorge.qatasksdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jorge.qatasksdemo.presentation.navigation.AppNavigation
import com.jorge.qatasksdemo.ui.theme.QATasksDemoTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QATasksDemoTheme {
                AppNavigation()
            }
        }
    }
}