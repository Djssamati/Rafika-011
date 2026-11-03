package com.camera.ta3bia

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val themeThumbs = listOf(
    ThemeId.ZELLIGE to R.drawable.theme_zellige,
    ThemeId.GLASS to R.drawable.theme_glass,
    ThemeId.POMEGRANATE to R.drawable.theme_pomegranate
)

@Composable
fun SettingsScreen(
    tr: Tr,
    t: UiTheme,
    themeIndex: Int,
    dest: String,
    endCode: String,
    onLang: (Lang) -> Unit,
    onTheme: (Int) -> Unit,
    onDest: (String) -> Unit,
    onEndCode: (String) -> Unit,
    onBack: () -> Unit
) {
    ThemedScaffold(t) {
        // ---- header: back + title
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TButton(t.bar, Modifier.width(60.dp), 50.dp, onClick = onBack) {
                Text(
                    if (tr.lang.rtl) "→" else "←", color = t.bar.text,
                    fontSize = 26.sp, fontWeight = FontWeight.Bold
                )
            }
            Text(
                tr.settingsTitle, color = t.title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center, style = styleOf(t.titleShadow),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(60.dp))
        }

        // ---- language
        TCard(t) {
            StepTitle(t, tr.languageSection)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Lang.values().forEach { l ->
                    val selected = l == tr.lang
                    val surf = if (selected) t.primary else t.bar
                    TButton(surf, Modifier.weight(1f), 56.dp, onClick = { onLang(l) }) {
                        Text(
                            (if (selected) "✓ " else "") + l.label, color = surf.text, fontSize = 19.sp,
                            fontWeight = FontWeight.Bold, style = styleOf(surf.shadow)
                        )
                    }
                }
            }
        }

        // ---- interface (3 looks)
        TCard(t) {
            StepTitle(t, tr.interfaceSection)
            Text(
                tr.interfaceHint, color = t.muted, fontSize = 14.sp, textAlign = TextAlign.Center,
                style = styleOf(t.card.shadow), modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                themeThumbs.forEachIndexed { index, (id, res) ->
                    ThemeThumb(
                        t = t, res = res, name = tr.themeName(id), selected = index == themeIndex,
                        modifier = Modifier.weight(1f), onClick = { onTheme(index) }
                    )
                }
            }
        }

        // ---- sending
        TCard(t) {
            StepTitle(t, tr.sendSection)
            FieldLabel(t, tr.destLabel)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                TField(
                    t = t, value = dest, onValueChange = onDest,
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    keyboardType = KeyboardType.Phone
                )
            }
            FieldLabel(t, tr.endCodeLabel)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                TField(
                    t = t, value = endCode, onValueChange = onEndCode,
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    keyboardType = KeyboardType.Text, placeholder = "#"
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(t: UiTheme, text: String) {
    Text(
        text, color = t.step, fontSize = 14.sp, fontWeight = FontWeight.Bold,
        style = styleOf(t.stepShadow), modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ThemeThumb(
    t: UiTheme,
    res: Int,
    name: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box {
            Image(
                painterResource(res), contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(340f / 508f)
                    .clip(shape)
                    .border(
                        if (selected) 4.dp else 1.dp,
                        if (selected) t.accent else t.step.copy(alpha = 0.35f),
                        shape
                    )
            )
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(26.dp)
                        .background(t.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("✓", color = t.onAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Text(
            name, color = t.step, fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
            textAlign = TextAlign.Center, style = styleOf(t.stepShadow)
        )
    }
}
