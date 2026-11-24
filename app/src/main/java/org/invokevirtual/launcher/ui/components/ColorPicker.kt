package org.invokevirtual.launcher.ui.components

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.ui.theme.InvokeIcons

/**
 * Interactive 2D Saturation/Value + Hue spectrum HSV color picker with live hex readout,
 * optional reset, and Android 17 EyeDropper sampling when available.
 */
@Composable
fun InteractiveColorPicker(
    currentArgb: Int?,
    onSelectColor: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onReset: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val haptic = LocalHapticFeedback.current

    val initialHsv = remember {
        FloatArray(3).apply {
            if (currentArgb != null) {
                android.graphics.Color.colorToHSV(currentArgb, this)
            } else {
                this[0] = 28f
                this[1] = 0.70f
                this[2] = 0.85f
            }
        }
    }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2]) }

    LaunchedEffect(currentArgb) {
        if (currentArgb != null) {
            val currentComputed = android.graphics.Color.HSVToColor(
                floatArrayOf(hue, saturation, brightness),
            )
            if ((currentComputed and 0xFFFFFF) != (currentArgb and 0xFFFFFF)) {
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(currentArgb, hsv)
                hue = hsv[0]
                saturation = hsv[1]
                brightness = hsv[2]
            }
        }
    }

    val supportsSystemEyeDropper = remember(context, inPreview) {
        !inPreview &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            Intent(Intent.ACTION_OPEN_EYE_DROPPER).resolveActivity(context.packageManager) != null
    }
    val eyeDropperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            val picked = result.data?.getIntExtra(Intent.EXTRA_COLOR, 0) ?: 0
            if (picked != 0) {
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(picked, hsv)
                hue = hsv[0]
                saturation = hsv[1]
                brightness = hsv[2]
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelectColor(picked or (0xFF shl 24))
            }
        }
    }

    val activeArgb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    val activeColor = Color(activeArgb)
    val pureHueColor = Color.hsv(hue.coerceIn(0f, 360f), 1f, 1f)
    val hexText = remember(activeArgb) {
        "#%06X".format(activeArgb and 0xFFFFFF)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(activeColor)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            Text(
                text = hexText,
                style = MaterialTheme.typography.labelLargeEmphasized.copy(
                    fontFeatureSettings = "tnum",
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (supportsSystemEyeDropper && Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        eyeDropperLauncher.launch(Intent(Intent.ACTION_OPEN_EYE_DROPPER))
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 36.dp),
                ) {
                    Icon(
                        imageVector = InvokeIcons.Colorize,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Eyedropper",
                        style = MaterialTheme.typography.labelMediumEmphasized,
                    )
                }
            }
            if (onReset != null && currentArgb != null) {
                TextButton(
                    onClick = onReset,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.heightIn(min = 36.dp),
                ) {
                    Text("Reset", style = MaterialTheme.typography.labelMediumEmphasized)
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        fun updateSv(pos: Offset) {
                            val w = size.width.coerceAtLeast(1).toFloat()
                            val h = size.height.coerceAtLeast(1).toFloat()
                            saturation = (pos.x / w).coerceIn(0f, 1f)
                            brightness = (1f - pos.y / h).coerceIn(0f, 1f)
                            onSelectColor(
                                android.graphics.Color.HSVToColor(
                                    floatArrayOf(hue, saturation, brightness),
                                ),
                            )
                        }
                        updateSv(down.position)
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            updateSv(change.position)
                            change.consume()
                        }
                    }
                },
        ) {
            drawRect(brush = Brush.horizontalGradient(listOf(Color.White, pureHueColor)))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))

            val thumbX = (saturation * size.width).coerceIn(0f, size.width)
            val thumbY = ((1f - brightness) * size.height).coerceIn(0f, size.height)
            val center = Offset(thumbX, thumbY)
            drawCircle(
                color = Color.Black.copy(alpha = 0.55f),
                radius = 10.dp.toPx(),
                center = center,
                style = Stroke(width = 3.dp.toPx()),
            )
            drawCircle(
                color = Color.White,
                radius = 9.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
            drawCircle(
                color = activeColor,
                radius = 7.dp.toPx(),
                center = center,
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        fun updateHue(pos: Offset) {
                            val w = size.width.coerceAtLeast(1).toFloat()
                            hue = ((pos.x / w).coerceIn(0f, 1f)) * 360f
                            onSelectColor(
                                android.graphics.Color.HSVToColor(
                                    floatArrayOf(hue, saturation, brightness),
                                ),
                            )
                        }
                        updateHue(down.position)
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            updateHue(change.position)
                            change.consume()
                        }
                    }
                },
        ) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red,
                    ),
                ),
            )
            val thumbX = ((hue / 360f) * size.width).coerceIn(10.dp.toPx(), (size.width - 10.dp.toPx()).coerceAtLeast(10.dp.toPx()))
            val center = Offset(thumbX, size.height / 2f)
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = 10.dp.toPx(),
                center = center,
                style = Stroke(width = 3.dp.toPx()),
            )
            drawCircle(
                color = pureHueColor,
                radius = 8.dp.toPx(),
                center = center,
            )
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
