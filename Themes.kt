package com.camera.ta3bia

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The three interfaces the user can choose from in the settings. The order is the stored index. */
enum class ThemeId { ZELLIGE, GLASS, POMEGRANATE }

/** A "surface": fill + optional border + corner radius + text colour. Used for cards and buttons. */
class Surf(
    val fill: Brush,
    val border: Brush?,
    val borderWidth: Dp,
    val radius: Dp,
    val text: Color,
    val shadow: Shadow? = null
)

class UiTheme(
    val id: ThemeId,
    val statusBar: Color,
    val outerPad: Dp,
    val title: Color,
    val titleShadow: Shadow?,
    val step: Color,
    val stepShadow: Shadow?,
    val link: Color,
    val muted: Color,
    val ok: Color,
    val err: Color,
    val accent: Color,
    val onAccent: Color,
    val card: Surf,
    val titleBar: Surf?,
    val primary: Surf,
    val option: Surf,
    val normal: Surf,
    val bar: Surf,
    val fieldFill: Color,
    val fieldBorder: Color,
    val fieldText: Color,
    val fieldHint: Color
)

fun styleOf(shadow: Shadow?): TextStyle = if (shadow == null) TextStyle.Default else TextStyle(shadow = shadow)

fun Modifier.surf(s: Surf): Modifier {
    val shape = RoundedCornerShape(s.radius)
    val base = this.clip(shape).background(s.fill, shape)
    val b = s.border
    return if (b != null) base.border(s.borderWidth, b, shape) else base
}

private fun solid(c: Color): Brush = SolidColor(c)
private fun vert(colors: List<Color>): Brush = Brush.verticalGradient(colors)

private val softShadow = Shadow(Color(0x66000000), Offset(1f, 2f), 4f)
private val darkShadow = Shadow(Color(0x88401020), Offset(1f, 2f), 5f)

fun themeOf(index: Int): UiTheme = when (ThemeId.values()[index.coerceIn(0, ThemeId.values().size - 1)]) {
    ThemeId.ZELLIGE -> UiTheme(
        id = ThemeId.ZELLIGE,
        statusBar = Color(0xFF13408F),
        outerPad = 34.dp,
        title = Color(0xFF0B2A66), titleShadow = null,
        step = Color(0xFF0B2A66), stepShadow = null,
        link = Color(0xFF0B2A66), muted = Color(0xFF33466F),
        ok = Color(0xFF1B8A3B), err = Color(0xFFC92A2A),
        accent = Color(0xFF1E5BC6), onAccent = Color.White,
        card = Surf(solid(Color(0xFFF8F7F3)), solid(Color.White), 1.5.dp, 20.dp, Color(0xFF0B2A66)),
        titleBar = null,
        primary = Surf(vert(listOf(Color(0xFF2C66D1), Color(0xFF0D3A96))), solid(Color(0xFF0A2C75)), 1.dp, 18.dp, Color.White, softShadow),
        option = Surf(vert(listOf(Color(0xFFA3E0C4), Color(0xFF72C3A0))), solid(Color(0xFF54A585)), 1.dp, 16.dp, Color.White, softShadow),
        normal = Surf(vert(listOf(Color(0xFF8DA6C4), Color(0xFF6F8BB0))), solid(Color(0xFF5B7599)), 1.dp, 16.dp, Color.White, softShadow),
        bar = Surf(solid(Color(0xFFF8F7F3)), solid(Color.White), 1.5.dp, 20.dp, Color(0xFF0B2A66)),
        fieldFill = Color(0xFFEAEAEA), fieldBorder = Color(0xFFCFCFCF),
        fieldText = Color(0xFF0B2A66), fieldHint = Color(0xFF7A7A7A)
    )

    ThemeId.GLASS -> {
        val glassFill = vert(listOf(Color(0x66FFFFFF), Color(0x336AA8D8)))
        val glassBorder = vert(listOf(Color(0xD9FFFFFF), Color(0x40FFFFFF)))
        val smallFill = vert(listOf(Color(0x8C7DBDE8), Color(0x80316F9F)))
        val smallBorder = vert(listOf(Color(0xB3FFFFFF), Color(0x66FFFFFF)))
        UiTheme(
            id = ThemeId.GLASS,
            statusBar = Color(0xFF5E9BD6),
            outerPad = 14.dp,
            title = Color.White, titleShadow = softShadow,
            step = Color.White, stepShadow = softShadow,
            link = Color.White, muted = Color(0xE6FFFFFF),
            ok = Color(0xFFB8FFC9), err = Color(0xFFFFC9C2),
            accent = Color(0xFFFFD54F), onAccent = Color(0xFF1B2A41),
            card = Surf(glassFill, glassBorder, 1.5.dp, 26.dp, Color.White, softShadow),
            titleBar = Surf(glassFill, glassBorder, 1.5.dp, 30.dp, Color.White, softShadow),
            primary = Surf(vert(listOf(Color(0xFF5AA5F8), Color(0xFF1F63D1))), glassBorder, 1.5.dp, 24.dp, Color.White, softShadow),
            option = Surf(smallFill, smallBorder, 1.5.dp, 18.dp, Color.White, softShadow),
            normal = Surf(smallFill, smallBorder, 1.5.dp, 18.dp, Color.White, softShadow),
            bar = Surf(glassFill, glassBorder, 1.5.dp, 30.dp, Color.White, softShadow),
            fieldFill = Color(0x401B3F66), fieldBorder = Color(0x8CFFFFFF),
            fieldText = Color.White, fieldHint = Color(0xB3FFFFFF)
        )
    }

    ThemeId.POMEGRANATE -> {
        val cardFill = vert(listOf(Color(0xD9FFF1F3), Color(0xBFF6C8D0)))
        val cardBorder = vert(listOf(Color(0xE6FFFFFF), Color(0x80FFFFFF)))
        val red = vert(listOf(Color(0xFFD22D4A), Color(0xFF8A1228)))
        val redBorder = solid(Color(0xFF5E0A1B))
        UiTheme(
            id = ThemeId.POMEGRANATE,
            statusBar = Color(0xFFA32644),
            outerPad = 14.dp,
            title = Color.White, titleShadow = darkShadow,
            step = Color(0xFF6B0F24), stepShadow = null,
            link = Color(0xFF6B0F24), muted = Color(0xFF6B3A44),
            ok = Color(0xFF1B7A3A), err = Color(0xFFB3141F),
            accent = Color(0xFFC0213D), onAccent = Color.White,
            card = Surf(cardFill, cardBorder, 1.5.dp, 26.dp, Color(0xFF6B0F24)),
            titleBar = Surf(vert(listOf(Color(0xCCC0314B), Color(0xCC861528))), vert(listOf(Color(0x99FFFFFF), Color(0x33FFFFFF))), 1.5.dp, 34.dp, Color.White, darkShadow),
            primary = Surf(red, redBorder, 1.5.dp, 22.dp, Color.White, darkShadow),
            option = Surf(red, redBorder, 1.5.dp, 18.dp, Color.White, darkShadow),
            normal = Surf(red, redBorder, 1.5.dp, 18.dp, Color.White, darkShadow),
            bar = Surf(cardFill, cardBorder, 1.5.dp, 34.dp, Color(0xFF6B0F24)),
            fieldFill = Color(0xB3FFFFFF), fieldBorder = Color(0xFFE8A3AF),
            fieldText = Color(0xFF6B0F24), fieldHint = Color(0xFF8A6A70)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Scaffold + shared components
// ---------------------------------------------------------------------------------------------

/**
 * Full-screen scaffold (the background is drawn behind the status/navigation bars).
 * scroll = false -> the content must fit the screen (main screen: no scrolling at all).
 */
@Composable
fun ThemedScaffold(t: UiTheme, scroll: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        ThemeBackground(t.id)
        val base = Modifier.fillMaxSize().systemBarsPadding()
        val body = if (scroll) base.imePadding().verticalScroll(rememberScrollState()) else base
        Column(
            body.padding(t.outerPad),
            verticalArrangement = Arrangement.spacedBy(if (scroll) 12.dp else 8.dp),
            content = content
        )
    }
}

@Composable
fun TCard(
    t: UiTheme,
    modifier: Modifier = Modifier,
    pad: Dp = 12.dp,
    gap: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .surf(t.card)
            .padding(pad),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content
    )
}

@Composable
fun StepTitle(t: UiTheme, text: String, size: TextUnit = 17.sp) {
    Text(
        text, color = t.step, fontSize = size, fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center, style = styleOf(t.stepShadow),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TButton(
    s: Surf,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .then(if (height.isSpecified) Modifier.height(height) else Modifier)
            .alpha(if (enabled) 1f else 0.45f)
            .surf(s)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun TField(
    t: UiTheme,
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    center: Boolean = false,
    vPad: Dp = 14.dp
) {
    val shape = RoundedCornerShape(14.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle.copy(color = t.fieldText),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(t.fieldText),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(t.fieldFill, shape)
                    .border(1.5.dp, t.fieldBorder, shape)
                    .padding(horizontal = 10.dp, vertical = vPad),
                contentAlignment = if (center) Alignment.Center else Alignment.CenterStart
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, style = textStyle.copy(color = t.fieldHint, fontWeight = FontWeight.Normal))
                }
                inner()
            }
        }
    )
}

// ---------------------------------------------------------------------------------------------
// Backgrounds (drawn in code, no big image files)
// ---------------------------------------------------------------------------------------------

@Composable
fun ThemeBackground(id: ThemeId) {
    Canvas(Modifier.fillMaxSize()) {
        when (id) {
            ThemeId.ZELLIGE -> drawZellige()
            ThemeId.GLASS -> drawGlass()
            ThemeId.POMEGRANATE -> drawPomegranate()
        }
    }
}

private fun DrawScope.blob(cx: Float, cy: Float, r: Float, c: Color, a: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(c.copy(alpha = a), c.copy(alpha = 0f)),
            center = Offset(cx, cy), radius = r
        ),
        radius = r, center = Offset(cx, cy)
    )
}

private fun DrawScope.drawZellige() {
    val w = size.width
    val h = size.height
    val frame = 26.dp.toPx()
    val navy = Color(0xFF13408F)
    val navyDark = Color(0xFF0C2D6E)
    val teal = Color(0xFF34B3A3)
    val line = Color(0xFFF2EFE6)

    drawRect(navy)

    val s = 14.dp.toPx()
    val cols = (w / s).toInt() + 2
    val rows = (h / s).toInt() + 2
    val stroke = Stroke(width = 1.4.dp.toPx())
    val p = Path()
    for (r in 0..rows) {
        for (c in 0..cols) {
            if ((r + c) % 2 != 0) continue
            val cx = c * s
            val cy = r * s
            val i = (c + r) / 2
            val j = (c - r) / 2
            val isTeal = (i and 1) == 0 && (j and 1) == 0
            val fill = when {
                isTeal -> teal
                ((i + j) and 1) == 0 -> navy
                else -> navyDark
            }
            p.reset()
            p.moveTo(cx, cy - s)
            p.lineTo(cx + s, cy)
            p.lineTo(cx, cy + s)
            p.lineTo(cx - s, cy)
            p.close()
            drawPath(p, fill)
            drawPath(p, line, style = stroke)
        }
    }

    // light panel with a cream bevel
    val panelTopLeft = Offset(frame, frame)
    val panelSize = Size(w - 2 * frame, h - 2 * frame)
    val radius = CornerRadius(12.dp.toPx())
    drawRoundRect(Color(0xFFD6E4F0), panelTopLeft, panelSize, radius)
    drawRoundRect(Color(0xFFF3EFE4), panelTopLeft, panelSize, radius, style = Stroke(5.dp.toPx()))
    drawRoundRect(
        Color(0xFFF3EFE4), Offset(1.5.dp.toPx(), 1.5.dp.toPx()),
        Size(w - 3.dp.toPx(), h - 3.dp.toPx()), CornerRadius(6.dp.toPx()), style = Stroke(3.dp.toPx())
    )
}

private fun DrawScope.drawGlass() {
    val w = size.width
    val h = size.height
    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFF6BA6DE),
            0.30f to Color(0xFFB7D5EE),
            0.55f to Color(0xFF8DB7B4),
            1f to Color(0xFF3F6F57)
        )
    )
    // soft clouds
    blob(0.15f * w, 0.10f * h, 0.55f * w, Color.White, 0.75f)
    blob(0.80f * w, 0.04f * h, 0.45f * w, Color.White, 0.55f)
    blob(0.45f * w, 0.30f * h, 0.40f * w, Color.White, 0.35f)
    // blurred trees
    blob(1.00f * w, 0.30f * h, 0.60f * w, Color(0xFF2E7D32), 0.70f)
    blob(0.00f * w, 0.58f * h, 0.55f * w, Color(0xFF3E8E5A), 0.60f)
    blob(0.95f * w, 0.85f * h, 0.70f * w, Color(0xFF1F5E3A), 0.75f)
    blob(0.10f * w, 0.97f * h, 0.55f * w, Color(0xFF2B6E4B), 0.65f)
    // bokeh
    blob(0.30f * w, 0.55f * h, 0.12f * w, Color.White, 0.30f)
    blob(0.70f * w, 0.62f * h, 0.10f * w, Color(0xFFFFF3B0), 0.30f)
    blob(0.55f * w, 0.78f * h, 0.14f * w, Color.White, 0.22f)
    blob(0.15f * w, 0.35f * h, 0.09f * w, Color(0xFFFFF3B0), 0.28f)
}

private fun DrawScope.drawPomegranate() {
    val w = size.width
    val h = size.height
    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFFB8465C),
            0.5f to Color(0xFF8F2036),
            1f to Color(0xFF4F0B1B)
        )
    )
    blob(0.10f * w, 0.08f * h, 0.65f * w, Color(0xFFFFB3C0), 0.50f)
    blob(0.80f * w, 0.55f * h, 0.50f * w, Color(0xFFFF8FA3), 0.18f)

    fun fruit(cx: Float, cy: Float, r: Float) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFE5667A), Color(0xFFB02744), Color(0xFF5C0D20)),
                center = Offset(cx - r * 0.3f, cy - r * 0.3f), radius = r * 1.3f
            ),
            radius = r, center = Offset(cx, cy)
        )
    }
    fruit(1.02f * w, 0.30f * h, 0.26f * w)
    fruit(-0.04f * w, 0.52f * h, 0.20f * w)
    fruit(0.98f * w, 0.72f * h, 0.28f * w)
    fruit(0.02f * w, 0.97f * h, 0.30f * w)
    fruit(0.90f * w, 0.00f * h, 0.20f * w)

    // loose seeds (arils)
    val seeds = listOf(
        Triple(0.05f, 0.06f, 20f), Triple(0.30f, 0.03f, -35f), Triple(0.96f, 0.10f, 50f),
        Triple(0.04f, 0.30f, 70f), Triple(0.93f, 0.49f, -20f), Triple(0.07f, 0.74f, 35f),
        Triple(0.90f, 0.90f, -60f), Triple(0.50f, 0.99f, 15f), Triple(0.22f, 0.93f, -45f),
        Triple(0.97f, 0.62f, 80f), Triple(0.03f, 0.50f, -10f), Triple(0.70f, 0.015f, 30f)
    )
    val sw = 9.dp.toPx()
    val sh = 12.dp.toPx()
    for ((fx, fy, angle) in seeds) {
        val cx = fx * w
        val cy = fy * h
        rotate(angle, Offset(cx, cy)) {
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFF6B80), Color(0xFFB01234), Color(0xFF6E0A22)),
                    center = Offset(cx - sw * 0.2f, cy - sh * 0.2f), radius = sh
                ),
                topLeft = Offset(cx - sw, cy - sh), size = Size(sw * 2, sh * 2)
            )
            drawCircle(Color(0x99FFFFFF), radius = sw * 0.28f, center = Offset(cx - sw * 0.35f, cy - sh * 0.4f))
        }
    }
}
