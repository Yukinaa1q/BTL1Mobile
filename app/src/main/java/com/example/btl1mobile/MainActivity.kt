package com.example.btl1mobile


import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.btl1mobile.ui.theme.Btl1MobileTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


enum class BTL1Screen() {
    Login, Home, Camera, ModelViewer
}


class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
//    private var imageCapture: ImageCapture? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.entries.all { it.value }
        if (!allPermissionsGranted) {
            Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request camera permissions
        requestCameraPermissions()

        // Initialize executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        enableEdgeToEdge()
        setContent {
            Btl1MobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = rememberNavController(),
                        startDestination = BTL1Screen.Login.name,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(route = BTL1Screen.Login.name) {

                        }
                        composable(route = BTL1Screen.Home.name) {

                        }
                        composable(route = BTL1Screen.Camera.name) {
                            CameraScreen(
                                modifier = Modifier.padding(innerPadding),
//                                onImageCapture = { uri ->
//                                    Toast.makeText(MainActivity.this, "Photo saved: $uri", Toast.LENGTH_SHORT).show()
//                                }
                            )
                        }
                        composable(route = BTL1Screen.ModelViewer.name) {
                            ModelViewer()
                        }
                    }
                }
            }
        }
    }

    private fun requestCameraPermissions() {
        val requiredPermissions = mutableListOf(Manifest.permission.CAMERA).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        requestPermissionLauncher.launch(requiredPermissions)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}





