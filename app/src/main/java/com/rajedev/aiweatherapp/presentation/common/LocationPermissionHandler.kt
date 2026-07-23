package com.rajedev.aiweatherapp.presentation.common

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Invoked only from an explicit "use current location" action - never on a screen's initial
// composition. Accepts either coarse or fine location, per the location-services convention.
@Composable
fun LocationPermissionHandler(shouldRequest: Boolean, onGranted: () -> Unit, onDenied: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) onGranted() else onDenied()
    }

    LaunchedEffect(shouldRequest) {
        if (!shouldRequest) return@LaunchedEffect
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            onGranted()
        } else {
            launcher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }
}
