package com.camera.ta3bia

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import kotlin.math.abs

/** On-device OCR (ML Kit). No internet and no external API needed. */
object Ocr {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Reads [primary] first (usually the crop inside the green frame).
     * If the 14 digits are not found, tries [fallback] (the whole photo).
     */
    suspend fun read(primary: Bitmap, fallback: Bitmap? = null): RechargeParser.Result {
        val first = recognize(primary)
        if (first.complete || fallback == null) return first
        val second = recognize(fallback)
        return when {
            second.complete -> second
            abs(RechargeParser.CODE_LENGTH - second.digits.length) <
                abs(RechargeParser.CODE_LENGTH - first.digits.length) -> second
            else -> first
        }
    }

    private suspend fun recognize(bitmap: Bitmap): RechargeParser.Result {
        val text = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
        val lines = text.textBlocks.flatMap { block -> block.lines.map { it.text } }
        return RechargeParser.parse(lines)
    }
}
