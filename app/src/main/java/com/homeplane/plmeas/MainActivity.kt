package com.homeplane.plmeas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import com.homeplane.plmeas.navigation.AppNavigation
import com.homeplane.plmeas.ui.theme.HomePlannerTheme
import com.homeplane.plmeas.viewmodel.AppViewModel
import com.homeplane.plmeas.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(application)
        )[AppViewModel::class.java]

        setContent {
            val prefs by viewModel.preferences.collectAsStateWithLifecycle()
            HomePlannerTheme(darkTheme = prefs.isDarkTheme) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
