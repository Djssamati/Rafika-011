package com.camera.ta3bia

import android.graphics.Bitmap
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.roundToInt

private const val GUIDE_ASPECT = 3.1f

/** The green frame, in the coordinates of the on-screen preview. Used both to draw and to crop. */
private fun guideRect(viewW: Float, viewH: Float, maxW: Float): Rect {
    val w = minOf(viewW * 0.90f, maxW)
    val h = w / GUIDE_ASPECT
    val cx = viewW / 2f
    val cy = viewH * 0.38f
    return Rect(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)
}

/**
 * THE FIX for the wrong/misplaced reading.
 *
 * The preview uses FILL_CENTER, so the screen shows only the centre part of the photo (zoomed).
 * The old code applied the frame's percentages directly on the full camera frame, so it cropped
 * a different area than the one the user had put inside the green frame.
 * Here we map the frame from screen coordinates back to photo pixels exactly.
 */
private fun cropToGuide(full: Bitmap, viewW: Int, viewH: Int, maxGuideW: Float): Bitmap {
    val g = guideRect(viewW.toFloat(), viewH.toFloat(), maxGuideW)
    val scale = max(viewW / full.width.toFloat(), viewH / full.height.toFloat())
    val offX = (full.width * scale - viewW) / 2f
    val offY = (full.height * scale - viewH) / 2f

    val padX = g.width * 0.06f
    val padY = g.height * 0.12f
    val left = ((g.left - padX + offX) / scale).coerceIn(0f, full.width - 2f)
    val top = ((g.top - padY + offY) / scale).coerceIn(0f, full.height - 2f)
    val right = ((g.right + padX + offX) / scale).coerceIn(left + 1f, full.width.toFloat())
    val bottom = ((g.bottom + padY + offY) / scale).coerceIn(top + 1f, full.height.toFloat())

    return Bitmap.createBitmap(
        full, left.roundToInt(), top.roundToInt(),
        (right - left).roundToInt().coerceAtLeast(1),
        (bottom - top).roundToInt().coerceAtLeast(1)
    )
}

// Same 4:3 ratio for preview and capture => what you see is what gets captured.
private fun selector(maxSize: Size? = null): ResolutionSelector =
    ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
        .apply {
            if (maxSize != null) setResolutionStrategy(
                ResolutionStrategy(maxSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)
            )
        }
        .build()

@Composable
fun CameraScreen(
    guideText: String,
    onCaptured: (crop: Bitmap, full: Bitmap) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val density = LocalDensity.current
    val maxGuideW = with(density) { 400.dp.toPx() }

    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setResolutionSelector(selector(Size(3264, 2448))) // ~8MP is plenty for OCR
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }
    val worker = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        future.addListener({
            val p = future.get()
            provider = p
            val preview = Preview.Builder()
                .setResolutionSelector(selector())
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            p.unbindAll()
            camera = p.bindToLifecycle(
                activity, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
            )
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            provider?.unbindAll()
            worker.shutdown()
        }
    }

    fun capture() {
        if (busy || viewSize == IntSize.Zero) return
        busy = true
        val size = viewSize
        imageCapture.takePicture(worker, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val result = try {
                    val full = image.toBitmap().rotate(image.imageInfo.rotationDegrees)
                    cropToGuide(full, size.width, size.height, maxGuideW) to full
                } catch (e: Throwable) {
                    null
                } finally {
                    image.close()
                }
                ContextCompat.getMainExecutor(context).execute {
                    busy = false
                    if (result != null) onCaptured(result.first, result.second)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                ContextCompat.getMainExecutor(context).execute { busy = false }
            }
        })
    }

    fun toggleTorch() {
        val cam = camera ?: return
        if (cam.cameraInfo.hasFlashUnit()) {
            torchOn = !torchOn
            cam.cameraControl.enableTorch(torchOn)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { viewSize = it }
            .pointerInput(Unit) {
                // tap to focus
                detectTapGestures { offset ->
                    val point = previewView.meteringPointFactory.createPoint(offset.x, offset.y)
                    camera?.cameraControl?.startFocusAndMetering(FocusMeteringAction.Builder(point).build())
                }
            }
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // dark overlay with a transparent window + green border
        Canvas(Modifier.fillMaxSize()) {
            val g = guideRect(size.width, size.height, maxGuideW)
            val radius = CornerRadius(12.dp.toPx())
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(Offset.Zero, size))
                addRoundRect(RoundRect(g, radius))
            }
            drawPath(path, Color.Black.copy(alpha = 0.58f))
            drawRoundRect(Color(0xFF00E676), g.topLeft, g.size, radius, style = Stroke(3.dp.toPx()))
        }

        if (viewSize != IntSize.Zero) {
            val g = guideRect(viewSize.width.toFloat(), viewSize.height.toFloat(), maxGuideW)
            Text(
                guideText,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset { IntOffset(0, (g.bottom + 16.dp.toPx()).toInt()) }
            )
        }

        // top buttons
        RoundButton("✕", Color(0x73000000), Color.White, Modifier.align(Alignment.TopStart).statusBarsPadding().padding(14.dp)) { onClose() }
        RoundButton(
            "⚡",
            if (torchOn) Color(0xFFFFD600) else Color(0x73000000),
            if (torchOn) Color.Black else Color.White,
            Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(14.dp)
        ) { toggleTorch() }

        // capture button
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp)
                .size(84.dp)
                .clip(CircleShape)
                .background(if (busy) Color(0xFF9E9E9E) else Color(0xFF00E676))
                .border(5.dp, Color.White, CircleShape)
                .clickable(enabled = !busy) { capture() },
            contentAlignment = Alignment.Center
        ) { Text("📷", fontSize = 34.sp) }
    }
}

@Composable
private fun RoundButton(
    label: String, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit
) {
    Box(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(label, color = fg, fontSize = 22.sp) }
}
