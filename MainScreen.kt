package com.camera.ta3bia

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Kind { INFO, OK, ERR }

/** The text is a function of the language, so the message changes language together with the app. */
class Status(val text: (Tr) -> String, val kind: Kind)

/**
 * The main screen. NO scrolling: every block has a share of the screen height (weights),
 * so everything always fits on one screen, whatever the phone size.
 */
@Composable
fun MainScreen(
    tr: Tr,
    t: UiTheme,
    digits: String,
    onDigits: (String) -> Unit,
    status: Status?,
    preview: Bitmap?,
    sentNote: Boolean,
    target: String,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onSend: (suffix: String) -> Unit,
    onSettings: () -> Unit
) {
    val complete = digits.length == RechargeParser.CODE_LENGTH

    // sizes follow the screen (small phone -> a bit smaller, big phone -> a bit bigger)
    val conf = LocalConfiguration.current
    val s = minOf(conf.screenHeightDp / 700f, conf.screenWidthDp / 360f).coerceIn(0.8f, 1.1f)
    // no scrolling here, so the system "large text" setting must not push things off screen
    val dens = LocalDensity.current
    // reading result / "SMS opened" note (shown in card 1)
    val msg: String? = if (sentNote) tr.sentNote else status?.let { it.text(tr) }

    CompositionLocalProvider(LocalDensity provides Density(dens.density, minOf(dens.fontScale, 1f))) {
        ThemedScaffold(t, scroll = false) {
            TitleBlock(t, tr.appTitle, s, Modifier.weight(0.9f))

            // ---- 1) camera
            TCard(t, Modifier.weight(if (msg != null) 2.7f else 2.1f), 10.dp, 6.dp) {
                StepTitle(t, tr.step1, (15 * s).sp)
                TButton(t.primary, Modifier.fillMaxWidth().weight(1f).heightIn(min = 40.dp), Dp.Unspecified, onClick = onCamera) {
                    CameraIcon(Modifier.size((32 * s).dp), Color.White, Color(0xFF2B6FD9))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        tr.openCamera, color = t.primary.text, fontSize = (23 * s).sp,
                        fontWeight = FontWeight.Bold, style = styleOf(t.primary.shadow)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(tr.orWord, color = t.link, fontSize = (15 * s).sp, fontWeight = FontWeight.Bold)
                    Text(
                        tr.gallery, color = t.link, fontSize = (15 * s).sp, fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onGallery() }
                    )
                }
                // reading result / "SMS opened" note (small text + small photo)
                if (msg != null) {
                    val msgColor = if (sentNote) t.ok else when (status?.kind) {
                        Kind.OK -> t.ok
                        Kind.ERR -> t.err
                        else -> t.muted
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (!sentNote && preview != null) {
                            Image(
                                preview.asImageBitmap(), contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            msg, color = msgColor, fontSize = (12.5f * s).sp,
                            fontWeight = if (msgColor == t.ok) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            style = styleOf(t.card.shadow), modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ---- 2) the 14 digits
            TCard(t, Modifier, 10.dp, 6.dp) {
                StepTitle(t, tr.step2, (15 * s).sp)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    TField(
                        t = t,
                        value = digits,
                        onValueChange = onDigits,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace, fontSize = (21 * s).sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        ),
                        keyboardType = KeyboardType.Number,
                        placeholder = "0000 0000 0000 00",
                        visualTransformation = CodeTransformation,
                        center = true,
                        vPad = 8.dp
                    )
                }
                Text(
                    when {
                        complete -> tr.digitsComplete + "  \u2022  " + tr.sendTo(target)
                        digits.isNotEmpty() -> tr.digitsCount(digits.length)
                        else -> ""
                    },
                    color = if (complete) t.ok else t.muted, fontSize = (13 * s).sp,
                    textAlign = TextAlign.Center, maxLines = 1, style = styleOf(t.card.shadow),
                    modifier = Modifier.fillMaxWidth().height(18.dp)
                )
            }

            // ---- 3) recharge type -> opens the SMS app
            TCard(t, Modifier.weight(3.3f), 10.dp, 6.dp) {
                StepTitle(t, tr.step3, (15 * s).sp)
                // left-to-right on purpose: *1 *2 / *3 *6 exactly like the design
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            listOf(R.drawable.ic_chicken to "*1", R.drawable.ic_daisy to "*2"),
                            listOf(R.drawable.ic_ie to "*3", R.drawable.ic_wa to "*6")
                        ).forEach { row ->
                            Row(
                                Modifier.fillMaxWidth().weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { (icon, code) ->
                                    TButton(
                                        t.option, Modifier.weight(1f).fillMaxHeight().heightIn(min = 36.dp), Dp.Unspecified,
                                        enabled = complete, onClick = { onSend(code) }
                                    ) {
                                        Image(
                                            painterResource(icon), contentDescription = null,
                                            modifier = Modifier.size((34 * s).dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            code, color = t.option.text, fontSize = (30 * s).sp,
                                            fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                                            style = styleOf(t.option.shadow)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                TButton(
                    t.normal, Modifier.fillMaxWidth().weight(0.9f), Dp.Unspecified,
                    enabled = complete, onClick = { onSend("") }
                ) {
                    Image(
                        painterResource(R.drawable.ic_sun), contentDescription = null,
                        modifier = Modifier.size((28 * s).dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        tr.normal, color = t.normal.text, fontSize = (19 * s).sp,
                        fontWeight = FontWeight.Bold, style = styleOf(t.normal.shadow)
                    )
                }
            }

            // ---- settings bar
            TButton(t.bar, Modifier.fillMaxWidth(), (48 * s).dp, onClick = onSettings) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GearIcon(Modifier.size((26 * s).dp), t.bar.text)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                tr.settings, color = t.bar.text, fontSize = (18 * s).sp,
                                fontWeight = FontWeight.Bold, style = styleOf(t.bar.shadow)
                            )
                        }
                        Box(
                            Modifier
                                .size((34 * s).dp)
                                .clip(CircleShape)
                                .background(t.bar.text.copy(alpha = 0.12f))
                                .border(1.5.dp, t.bar.text.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (tr.lang.rtl) "\u2190" else "\u2192", color = t.bar.text,
                                fontSize = (20 * s).sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleBlock(t: UiTheme, text: String, s: Float, modifier: Modifier) {
    val bar = t.titleBar
    val base = if (bar == null) modifier.fillMaxWidth() else modifier.fillMaxWidth().surf(bar)
    Box(base, contentAlignment = Alignment.Center) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PhoneArrowIcon(Modifier.size(width = (54 * s).dp, height = (44 * s).dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text, color = t.title, fontSize = (28 * s).sp, fontWeight = FontWeight.ExtraBold,
                    style = styleOf(t.titleShadow), modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}

/** Shows 0000 0000 0000 00 while the stored value stays plain digits. */
private object CodeTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val out = buildString {
            text.text.forEachIndexed { i, c ->
                if (i == 4 || i == 8 || i == 12) append(' ')
                append(c)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = when {
                offset <= 4 -> offset
                offset <= 8 -> offset + 1
                offset <= 12 -> offset + 2
                else -> offset + 3
            }

            override fun transformedToOriginal(offset: Int) = when {
                offset <= 4 -> offset
                offset <= 9 -> offset - 1
                offset <= 14 -> offset - 2
                else -> offset - 3
            }
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}
