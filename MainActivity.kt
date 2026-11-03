package com.camera.ta3bia

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // full screen: the app is drawn behind the status bar and the navigation bar (transparent, light icons)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
private fun App() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = remember { Settings(context) }

    // chosen language + interface (saved on the phone)
    var langCode by rememberSaveable { mutableStateOf(settings.lang) }
    var themeIndex by rememberSaveable { mutableStateOf(settings.theme) }
    val tr = remember(langCode) { Tr(Lang.from(langCode)) }
    val theme = remember(themeIndex) { themeOf(themeIndex) }

    // `digits` is the single source of truth of the 14-digit field.
    var digits by rememberSaveable { mutableStateOf("") }
    var dest by rememberSaveable { mutableStateOf(settings.dest) }
    var endCode by rememberSaveable { mutableStateOf(settings.endCode) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var sentNote by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<Status?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    fun applyResult(r: RechargeParser.Result) {
        sentNote = false
        status = when {
            r.complete -> {
                digits = r.digits // <- goes straight into the 14-digit field
                Status({ it.ocrOk }, Kind.OK)
            }
            r.digits.isNotEmpty() -> {
                digits = r.digits
                val n = r.digits.length
                Status({ it.ocrPartial(n) }, Kind.ERR)
            }
            r.found > RechargeParser.CODE_LENGTH -> Status({ it.ocrTooMany }, Kind.ERR)
            else -> Status({ it.ocrNone }, Kind.ERR)
        }
    }

    fun runOcr(primary: Bitmap, fallback: Bitmap? = null) {
        status = Status({ it.ocrReading }, Kind.INFO)
        scope.launch {
            val result = try {
                Ocr.read(primary, fallback)
            } catch (e: Exception) {
                null
            }
            if (result == null) status = Status({ it.ocrFail }, Kind.ERR)
            else applyResult(result)
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) showCamera = true
        else status = Status({ it.camDenied }, Kind.ERR)
    }
    fun openCamera() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) showCamera = true else permission.launch(Manifest.permission.CAMERA)
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            val bmp = withContext(Dispatchers.IO) { runCatching { loadBitmap(context, uri) }.getOrNull() }
            if (bmp == null) status = Status({ it.imageFail }, Kind.ERR)
            else {
                preview = bmp
                runOcr(bmp)
            }
        }
    }

    val target = dest.trim().ifEmpty { "555" }

    // Arabic => right-to-left, French => left-to-right
    CompositionLocalProvider(
        LocalLayoutDirection provides if (tr.lang.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        when {
            showCamera -> {
                BackHandler { showCamera = false }
                CameraScreen(
                    guideText = tr.camGuide,
                    onCaptured = { crop, full ->
                        showCamera = false
                        preview = crop
                        runOcr(crop, full)
                    },
                    onClose = { showCamera = false }
                )
            }

            settingsOpen -> {
                BackHandler { settingsOpen = false }
                SettingsScreen(
                    tr = tr,
                    t = theme,
                    themeIndex = themeIndex,
                    dest = dest,
                    endCode = endCode,
                    onLang = { langCode = it.code; settings.lang = it.code },
                    onTheme = { themeIndex = it; settings.theme = it },
                    onDest = { dest = it; settings.dest = it },
                    onEndCode = { endCode = it; settings.endCode = it },
                    onBack = { settingsOpen = false }
                )
            }

            else -> {
                MainScreen(
                    tr = tr,
                    t = theme,
                    digits = digits,
                    onDigits = { digits = RechargeParser.normalizeDigits(it); sentNote = false },
                    status = status,
                    preview = preview,
                    sentNote = sentNote,
                    target = target,
                    onCamera = { openCamera() },
                    onGallery = { gallery.launch("image/*") },
                    onSend = { suffix ->
                        openSms(context, target, digits + suffix + endCode, tr.noSmsApp)
                        sentNote = true
                    },
                    onSettings = { settingsOpen = true }
                )
            }
        }
    }
}

private fun openSms(context: Context, dest: String, body: String, noAppMessage: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(dest)))
        .putExtra("sms_body", body)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, noAppMessage, Toast.LENGTH_LONG).show()
    }
}
