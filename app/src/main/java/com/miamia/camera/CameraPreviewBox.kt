package com.miamia.camera

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
@Composable
fun CameraPreviewBox(
    onPreviewViewCreated: (PreviewView) -> Unit,
    modifier: Modifier = Modifier
) {
    LocalContext.current
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                contentDescription = "miamia_camera_preview"
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                post { onPreviewViewCreated(this) }
            }
        },
        modifier = modifier,
        update = { }
    )
}
