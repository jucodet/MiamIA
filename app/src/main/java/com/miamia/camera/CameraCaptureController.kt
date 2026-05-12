package com.miamia.camera

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.miamia.core.FeatureConfig
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraCaptureController(
    private val context: Context,
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null

    private val captureInFlight = AtomicBoolean(false)

    suspend fun bind(lifecycleOwner: LifecycleOwner, previewView: PreviewView): Result<Unit> {
        return try {
            val provider = obtainProvider()
            cameraProvider = provider
            provider.unbindAll()

            val previewUseCase = Preview.Builder().build().also { p ->
                p.surfaceProvider = previewView.surfaceProvider
                preview = p
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            imageCapture = capture

            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                previewUseCase,
                capture
            )
            Result.success(Unit)
        } catch (t: Throwable) {
            Log.e(TAG, "bind_failed", t)
            Result.failure(t)
        }
    }

    fun unbind() {
        try {
            cameraProvider?.unbindAll()
        } catch (_: Throwable) {
            // ignore
        }
        preview = null
        imageCapture = null
        cameraProvider = null
        captureInFlight.set(false)
    }

    suspend fun captureToFile(outputFile: File): Result<Unit> {
        val capture = imageCapture ?: return Result.failure(IllegalStateException("image_capture_null"))
        if (!captureInFlight.compareAndSet(false, true)) {
            return Result.failure(IllegalStateException("capture_already_in_flight"))
        }
        val startFeedback = SystemClock.elapsedRealtime()
        return suspendCoroutine { cont ->
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            capture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val elapsed = SystemClock.elapsedRealtime() - startFeedback
                        Log.d(
                            TAG,
                            "capture_feedback_ms=$elapsed threshold_ms=${FeatureConfig.CAPTURE_FEEDBACK_TARGET_MS}"
                        )
                        captureInFlight.set(false)
                        cont.resume(Result.success(Unit))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        captureInFlight.set(false)
                        Log.e(TAG, "capture_failed", exception)
                        cont.resume(Result.failure(exception))
                    }
                }
            )
        }
    }

    fun shutdown() {
        unbind()
        cameraExecutor.shutdown()
    }

    private suspend fun obtainProvider(): ProcessCameraProvider = suspendCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                try {
                    cont.resume(future.get())
                } catch (e: Throwable) {
                    cont.resumeWithException(e)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private companion object {
        const val TAG = "CameraCapture"
    }
}
