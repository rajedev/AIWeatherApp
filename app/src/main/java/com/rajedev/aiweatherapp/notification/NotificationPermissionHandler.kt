package com.rajedev.aiweatherapp.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Invoked only from an explicit "enable alerts" toggle in Settings - never on cold launch.
// Below API 33 there is no runtime notification permission, so it resolves as granted directly.
@Composable
fun NotificationPermissionHandler(shouldRequest: Boolean, onResult: (granted: Boolean) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> onResult(granted) }

    LaunchedEffect(shouldRequest) {
        if (!shouldRequest) return@LaunchedEffect
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onResult(true)
            return@LaunchedEffect
        }
        val alreadyGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) onResult(true) else launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
