package com.example.btl1mobile

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewUseCase by remember { mutableStateOf<Preview?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val executor = ContextCompat.getMainExecutor(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    // Set up the preview use case
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // Set up the image capture use case
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    // Select back camera
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        // Unbind any bound use cases before rebinding
                        cameraProvider.unbindAll()

                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )

                        previewUseCase = preview
                        imageCaptureUseCase = imageCapture

                    } catch (e: Exception) {
                        Toast.makeText(context, "Camera initialization failed: ${e.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Back button at the top left
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back to home",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Round capture button with camera icon
        FloatingActionButton(
            onClick = {
                captureImage(
                    context = context,
                    imageCapture = imageCaptureUseCase,
                    executor = ContextCompat.getMainExecutor(context),
                    onImageCaptured = {Toast.makeText(context, "Photo saved: $it", Toast.LENGTH_SHORT).show()}
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(64.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Take photo",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    executor: Executor,
    onImageCaptured: (Uri?) -> Unit
) {
    imageCapture ?: return

    // Create output options
    val contentValues = ContentValues().apply {
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(System.currentTimeMillis())
        put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_$timestamp")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    // Take the picture
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Image saved successfully
                onImageCaptured(outputFileResults.savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                // Image capture failed
                Toast.makeText(
                    context,
                    "Photo capture failed: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                onImageCaptured(null)
            }
        }
    )
}