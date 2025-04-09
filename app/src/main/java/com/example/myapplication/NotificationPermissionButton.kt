package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun NotificationPermissionButton() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(isNotificationPermissionGranted(context)) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
        if (isGranted) {
            // Permission granted, you can now send notifications
            println("Notification permission granted")
            // Implement your notification sending logic here
        } else {
            // Permission denied
            println("Notification permission denied")
            // Optionally, show a message to the user explaining why the permission is needed
        }
    }

    Button(
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Request permission for Android 13 (API level 33) and above
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // For older versions, the permission is often granted by default or requested during install.
                // You might still want to check and inform the user.
                if (!permissionGranted) {
                    println("Notification permission not explicitly requested on this Android version.")
                    // Optionally, guide the user to enable notifications in settings.
                } else {
                    println("Notification permission already granted.")
                    // Implement your notification sending logic here
                }
            }
        },
        enabled = !permissionGranted // Disable the button if permission is already granted (optional)
    ) {
        Text(if (permissionGranted) "Notifications Enabled" else "Request Notification Permission")
    }
}

fun isNotificationPermissionGranted(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // For older versions, the permission is often granted by default.
        // You might need to check AppOpsManager for more precise status if needed.
        true
    }
}