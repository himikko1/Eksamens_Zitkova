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
            // Atļauja piešķirta, tagad varat sūtīt paziņojumus
        } else {
            // Atļauja liegta
            println("Notification permission denied")
        }
    }

    Button(
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                if (!permissionGranted) {
                    println("Notification permission not explicitly requested on this Android version.")
                } else {
                    println("Notification permission already granted.")
                }
            }
        },
        enabled = !permissionGranted // Atslēgt pogu, ja atļauja jau ir piešķirta (nav obligāti)
    ) {
        Text(if (permissionGranted) "Ieslēgti paziņojumi" else "Request Notification Permission")
    }
}

// Šī funkcija pārbauda, vai lietotnei ir piešķirta atļauja rādīt paziņojumus.
// Android 13 (API 33) un jaunākās versijās ir nepieciešama POST_NOTIFICATIONS atļauja.
// Vecākās Android versijās šī atļauja nav nepieciešama, tāpēc funkcija atgriež true.

fun isNotificationPermissionGranted(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}