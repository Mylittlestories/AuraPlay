package com.auraplay.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.auraplay.player.ui.navigation.Navigation
import com.auraplay.player.ui.theme.AuraPlayTheme
import com.auraplay.player.ui.viewmodel.MainViewModel
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        viewModel.setPermissionGranted(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        setContent {
            val appearance by viewModel.appearance.collectAsState()
            AuraPlayTheme(appearance = appearance) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Navigation(viewModel)
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val granted = ContextCompat.checkSelfPermission(this, audioPermission) ==
                PackageManager.PERMISSION_GRANTED

        viewModel.setPermissionGranted(granted)

        if (!granted) {
            val permissions = mutableListOf(audioPermission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
