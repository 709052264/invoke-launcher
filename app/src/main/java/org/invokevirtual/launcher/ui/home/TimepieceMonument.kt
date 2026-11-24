package org.invokevirtual.launcher.ui.home

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import org.invokevirtual.launcher.data.ClockAlignment
import org.invokevirtual.launcher.data.ClockStyle
import org.invokevirtual.launcher.data.LauncherSettings
import org.invokevirtual.launcher.data.TileTone
import org.invokevirtual.launcher.data.WorkspaceMode
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.LocalReducedMotion
import org.invokevirtual.launcher.ui.theme.toContainerShape
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Immutable
data class ClockSnapshot(
    val hourText: String,
    val minuteText: String,
    val secondText: String,
    val amPmText: String,
    val dayOfWeek: String,
    val dateLine: String,
)

@Immutable
data class LiveSystemGlance(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val nextAlarmLabel: String?,
)

@Immutable
private data class ClockPalette(
    val accentColor: Color,
    val containerColor: Color,
    val onContainerColor: Color,
)

@Composable
private fun rememberClockPalette(
    tone: TileTone,
    customArgb: Int?,
): ClockPalette {
    val customColor = customArgb?.let(::Color)
    if (customColor != null) {
        val onColor = if (customColor.luminance() > 0.48f) {
            Color(0xFF141414)
        } else {
            Color.White
        }
        return ClockPalette(
            accentColor = customColor,
            containerColor = customColor,
            onContainerColor = onColor,
        )
    }

    return when (tone) {
        TileTone.SURFACE -> ClockPalette(
            accentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            onContainerColor = MaterialTheme.colorScheme.onSurface,
        )
        TileTone.PRIMARY -> ClockPalette(
            accentColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        TileTone.SECONDARY -> ClockPalette(
            accentColor = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            onContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        TileTone.TERTIARY -> ClockPalette(
            accentColor = MaterialTheme.colorScheme.tertiary,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            onContainerColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
fun TimepieceMonument(
    settings: LauncherSettings,
    onClockClick: () -> Unit,
    onSelectMode: (WorkspaceMode) -> Unit,
    onOpenCustomize: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clock = rememberLiveClockSnapshot(
        use24Hour = settings.clock24Hour,
        showSeconds = settings.showClockSeconds,
    )
    val systemGlance = rememberLiveSystemGlance()
    val reducedMotion = LocalReducedMotion.current
    val clockPalette = rememberClockPalette(
        tone = settings.clockTone,
        customArgb = settings.clockCustomColorArgb,
    )

    val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    val effectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val emblemShape = settings.clockShape.toContainerShape(isHero = true)
    val showAmPm = !settings.clock24Hour && settings.showClockAmPm

    val showGlancePill = settings.showGlanceBattery ||
        (settings.showGlanceAlarm && systemGlance.nextAlarmLabel != null)
    val showGlanceRow = showGlancePill || settings.showClockModeBadge

    if (!settings.showClock && !showGlanceRow) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (settings.showClock) {
            AnimatedContent(
                targetState = settings.clockStyle,
                transitionSpec = {
                    if (reducedMotion) {
                        fadeIn(snap()) togetherWith fadeOut(snap())
                    } else {
                        (fadeIn(effectsSpec) + scaleIn(initialScale = 0.96f, animationSpec = spatialSpec)) togetherWith
                            fadeOut(effectsSpec)
                    }
                },
                label = "clockStyleTransition",
            ) { style ->
                when (style) {
                    ClockStyle.EDITORIAL_STACKED -> EditorialStackedClockFace(
                        clock = clock,
                        workspaceMode = settings.workspaceMode,
                        scale = settings.clockSize.scale,
                        alignment = settings.clockAlignment,
                        palette = clockPalette,
                        showSeconds = settings.showClockSeconds,
                        showAmPm = showAmPm,
                        showDate = settings.showClockDate,
                        showModeBadge = settings.showClockModeBadge,
                        emblemShape = emblemShape,
                        onClockClick = onClockClick,
                    )

                    ClockStyle.KINETIC_SPLIT -> KineticSplitClockFace(
                        clock = clock,
                        scale = settings.clockSize.scale,
                        alignment = settings.clockAlignment,
                        palette = clockPalette,
                        showSeconds = settings.showClockSeconds,
                        showAmPm = showAmPm,
                        showDate = settings.showClockDate,
                        onClockClick = onClockClick,
                    )

                    ClockStyle.SHAPE_DIAL -> ShapeDialClockFace(
                        clock = clock,
                        workspaceMode = settings.workspaceMode,
                        scale = settings.clockSize.scale,
                        alignment = settings.clockAlignment,
                        palette = clockPalette,
                        dialShape = emblemShape,
                        showSeconds = settings.showClockSeconds,
                        showAmPm = showAmPm,
                        showDate = settings.showClockDate,
                        showModeBadge = settings.showClockModeBadge,
                        onClockClick = onClockClick,
                    )

                    ClockStyle.MINIMAL_INLINE -> MinimalInlineClockFace(
                        clock = clock,
                        scale = settings.clockSize.scale,
                        alignment = settings.clockAlignment,
                        palette = clockPalette,
                        showSeconds = settings.showClockSeconds,
                        showAmPm = showAmPm,
                        showDate = settings.showClockDate,
                        onClockClick = onClockClick,
                    )
                }
            }
        }

        if (showGlanceRow) {
            LiveGlanceBar(
                workspaceMode = settings.workspaceMode,
                glance = systemGlance,
                showBattery = settings.showGlanceBattery,
                showAlarm = settings.showGlanceAlarm,
                showModeSwitcher = settings.showClockModeBadge,
                onSelectMode = onSelectMode,
                onOpenCustomize = onOpenCustomize,
            )
        }
    }
}

@Composable
private fun EditorialStackedClockFace(
    clock: ClockSnapshot,
    workspaceMode: WorkspaceMode,
    scale: Float,
    alignment: ClockAlignment,
    palette: ClockPalette,
    showSeconds: Boolean,
    showAmPm: Boolean,
    showDate: Boolean,
    showModeBadge: Boolean,
    emblemShape: Shape,
    onClockClick: () -> Unit,
) {
    val fontSizeSp = (84f * scale).sp
    val lineHeightSp = (74f * scale).sp
    val hourStyle = MaterialTheme.typography.displayLarge.copy(
        fontWeight = FontWeight.Black,
        fontSize = fontSizeSp,
        lineHeight = lineHeightSp,
        letterSpacing = (-3).sp,
        fontFeatureSettings = "tnum",
    )
    val minuteStyle = MaterialTheme.typography.displayLarge.copy(
        fontWeight = FontWeight.Light,
        fontSize = fontSizeSp,
        lineHeight = lineHeightSp,
        letterSpacing = (-2.5).sp,
        fontFeatureSettings = "tnum",
    )

    val isCentered = alignment == ClockAlignment.CENTER
    if (isCentered) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .clickable(onClick = onClockClick)
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.semantics { heading() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-8).dp),
            ) {
                Text(
                    text = clock.hourText,
                    style = hourStyle,
                    color = palette.accentColor,
                    maxLines = 1,
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = clock.minuteText,
                        style = minuteStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    if (showSeconds || showAmPm) {
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            if (showSeconds) {
                                Text(
                                    text = ":${clock.secondText}",
                                    style = MaterialTheme.typography.titleLargeEmphasized.copy(
                                        fontFeatureSettings = "tnum",
                                    ),
                                    color = palette.accentColor,
                                )
                            }
                            if (showAmPm) {
                                Text(
                                    text = clock.amPmText,
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            if (showDate) {
                Text(
                    text = "${clock.dayOfWeek} · ${clock.dateLine}",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .clickable(onClick = onClockClick)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.semantics { heading() },
                verticalArrangement = Arrangement.spacedBy((-8).dp),
            ) {
                Text(
                    text = clock.hourText,
                    style = hourStyle,
                    color = palette.accentColor,
                    maxLines = 1,
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = clock.minuteText,
                        style = minuteStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    if (showSeconds || showAmPm) {
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            if (showSeconds) {
                                Text(
                                    text = ":${clock.secondText}",
                                    style = MaterialTheme.typography.titleLargeEmphasized.copy(
                                        fontFeatureSettings = "tnum",
                                    ),
                                    color = palette.accentColor,
                                )
                            }
                            if (showAmPm) {
                                Text(
                                    text = clock.amPmText,
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            if (showModeBadge || showDate) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (showModeBadge) {
                        Box(
                            modifier = Modifier
                                .size((68f * scale).coerceIn(52f, 82f).dp)
                                .clip(emblemShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.94f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = when (workspaceMode) {
                                    WorkspaceMode.EVERYDAY -> InvokeIcons.Sun
                                    WorkspaceMode.DEEP_FOCUS -> InvokeIcons.Focus
                                    WorkspaceMode.NIGHT_AMOLED -> InvokeIcons.Night
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size((28f * scale).coerceIn(22f, 34f).dp),
                            )
                        }
                    }

                    if (showDate) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = clock.dayOfWeek,
                                style = MaterialTheme.typography.titleLargeEmphasized,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = clock.dateLine,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KineticSplitClockFace(
    clock: ClockSnapshot,
    scale: Float,
    alignment: ClockAlignment,
    palette: ClockPalette,
    showSeconds: Boolean,
    showAmPm: Boolean,
    showDate: Boolean,
    onClockClick: () -> Unit,
) {
    val numeralStyle = MaterialTheme.typography.displayLarge.copy(
        fontWeight = FontWeight.Black,
        fontSize = (64f * scale).sp,
        lineHeight = (68f * scale).sp,
        fontFeatureSettings = "tnum",
    )
    val verticalPad = (16f * scale).coerceIn(10f, 22f).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .clickable(onClick = onClockClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = if (alignment == ClockAlignment.CENTER) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 42.dp,
                    topEnd = 12.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 42.dp,
                ),
                color = palette.containerColor.copy(alpha = 0.94f),
                contentColor = palette.onContainerColor,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = verticalPad),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = clock.hourText,
                        style = numeralStyle,
                        color = palette.onContainerColor,
                        maxLines = 1,
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(palette.accentColor),
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(palette.accentColor),
                )
                if (showSeconds) {
                    Text(
                        text = clock.secondText,
                        style = MaterialTheme.typography.labelMediumEmphasized.copy(
                            fontFeatureSettings = "tnum",
                        ),
                        color = palette.accentColor,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 42.dp,
                    bottomStart = 42.dp,
                    bottomEnd = 12.dp,
                ),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = verticalPad),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = clock.minuteText,
                            style = numeralStyle.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                        if (showAmPm) {
                            Text(
                                text = clock.amPmText,
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    }
                }
            }
        }

        if (showDate) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = if (alignment == ClockAlignment.CENTER) {
                    Arrangement.Center
                } else {
                    Arrangement.SpaceBetween
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (alignment == ClockAlignment.CENTER) {
                    Text(
                        text = "${clock.dayOfWeek} · ${clock.dateLine}",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Text(
                        text = clock.dayOfWeek,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = clock.dateLine,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShapeDialClockFace(
    clock: ClockSnapshot,
    workspaceMode: WorkspaceMode,
    scale: Float,
    alignment: ClockAlignment,
    palette: ClockPalette,
    dialShape: Shape,
    showSeconds: Boolean,
    showAmPm: Boolean,
    showDate: Boolean,
    showModeBadge: Boolean,
    onClockClick: () -> Unit,
) {
    val dialSize = (138f * scale).coerceIn(106f, 164f).dp
    val dialHourStyle = MaterialTheme.typography.displayMedium.copy(
        fontWeight = FontWeight.Black,
        fontSize = (42f * scale).sp,
        lineHeight = (44f * scale).sp,
        fontFeatureSettings = "tnum",
    )
    val dialMinuteStyle = MaterialTheme.typography.displayMedium.copy(
        fontWeight = FontWeight.Light,
        fontSize = (42f * scale).sp,
        lineHeight = (44f * scale).sp,
        fontFeatureSettings = "tnum",
    )

    val dialContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(dialSize)
                .clip(dialShape)
                .background(palette.containerColor)
                .semantics { heading() },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-6).dp),
            ) {
                Text(
                    text = clock.hourText,
                    style = dialHourStyle,
                    color = palette.onContainerColor,
                    maxLines = 1,
                )
                Text(
                    text = clock.minuteText,
                    style = dialMinuteStyle,
                    color = palette.onContainerColor.copy(alpha = 0.88f),
                    maxLines = 1,
                )
                if (showSeconds || showAmPm) {
                    val sub = listOfNotNull(
                        clock.secondText.takeIf { showSeconds }?.let { ":$it" },
                        clock.amPmText.takeIf { showAmPm },
                    ).joinToString(" ")
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.labelSmallEmphasized.copy(
                            fontFeatureSettings = "tnum",
                        ),
                        color = palette.onContainerColor.copy(alpha = 0.78f),
                    )
                }
            }
        }
    }

    if (alignment == ClockAlignment.CENTER) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .clickable(onClick = onClockClick)
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            dialContent()
            if (showDate) {
                Text(
                    text = "${clock.dayOfWeek} · ${clock.dateLine}",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .clickable(onClick = onClockClick)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            dialContent()

            if (showDate || showModeBadge) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (showDate) {
                        Text(
                            text = clock.dayOfWeek,
                            style = MaterialTheme.typography.headlineMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = clock.dateLine,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (showModeBadge) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = when (workspaceMode) {
                                    WorkspaceMode.EVERYDAY -> InvokeIcons.Sun
                                    WorkspaceMode.DEEP_FOCUS -> InvokeIcons.Focus
                                    WorkspaceMode.NIGHT_AMOLED -> InvokeIcons.Night
                                },
                                contentDescription = null,
                                tint = palette.accentColor,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = workspaceMode.label,
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                color = palette.accentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalInlineClockFace(
    clock: ClockSnapshot,
    scale: Float,
    alignment: ClockAlignment,
    palette: ClockPalette,
    showSeconds: Boolean,
    showAmPm: Boolean,
    showDate: Boolean,
    onClockClick: () -> Unit,
) {
    val timeText = buildString {
        append(clock.hourText)
        append(':')
        append(clock.minuteText)
        if (showSeconds) {
            append(':')
            append(clock.secondText)
        }
    }
    val timeStyle = MaterialTheme.typography.displayLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = (54f * scale).sp,
        lineHeight = (58f * scale).sp,
        letterSpacing = (-1.5).sp,
        fontFeatureSettings = "tnum",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClockClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = if (alignment == ClockAlignment.CENTER) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.semantics { heading() },
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = timeText,
                style = timeStyle,
                color = palette.accentColor,
                maxLines = 1,
            )
            if (showAmPm) {
                Text(
                    text = clock.amPmText,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
        if (showDate) {
            Text(
                text = "${clock.dayOfWeek} · ${clock.dateLine}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LiveGlanceBar(
    workspaceMode: WorkspaceMode,
    glance: LiveSystemGlance,
    showBattery: Boolean,
    showAlarm: Boolean,
    showModeSwitcher: Boolean,
    onSelectMode: (WorkspaceMode) -> Unit,
    onOpenCustomize: () -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    var menuExpanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (menuExpanded) 180f else 0f,
        animationSpec = if (reducedMotion) snap() else MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "splitChevronRotation",
    )

    val alarmText = if (showAlarm) glance.nextAlarmLabel else null
    val showSignalPill = showBattery || alarmText != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showSignalPill) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.86f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = when {
                            showBattery && glance.isCharging -> InvokeIcons.ShortcutBolt
                            alarmText != null -> InvokeIcons.Alarm
                            else -> InvokeIcons.Battery
                        },
                        contentDescription = null,
                        tint = if (showBattery && glance.isCharging) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(18.dp),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        val primaryText = when {
                            showBattery && glance.isCharging && alarmText != null ->
                                "$alarmText · Charging ${glance.batteryPercent}%"
                            showBattery && glance.isCharging ->
                                "Charging · ${glance.batteryPercent}%"
                            showBattery && alarmText != null ->
                                "$alarmText · ${glance.batteryPercent}%"
                            alarmText != null -> alarmText
                            else -> "${glance.batteryPercent}% battery"
                        }
                        Text(
                            text = primaryText,
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (showBattery && glance.isCharging) {
                            LinearWavyProgressIndicator(
                                progress = { (glance.batteryPercent / 100f).coerceIn(0.05f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.tertiaryContainer,
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        if (showModeSwitcher) {
            Box {
                SplitButtonLayout(
                    leadingButton = {
                        SplitButtonDefaults.TonalLeadingButton(
                            onClick = {
                                val entries = WorkspaceMode.entries
                                val next = entries[(entries.indexOf(workspaceMode) + 1) % entries.size]
                                onSelectMode(next)
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                        ) {
                            Icon(
                                imageVector = when (workspaceMode) {
                                    WorkspaceMode.EVERYDAY -> InvokeIcons.Sun
                                    WorkspaceMode.DEEP_FOCUS -> InvokeIcons.Focus
                                    WorkspaceMode.NIGHT_AMOLED -> InvokeIcons.Night
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = workspaceMode.label,
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                maxLines = 1,
                            )
                        }
                    },
                    trailingButton = {
                        SplitButtonDefaults.TonalTrailingButton(
                            checked = menuExpanded,
                            onCheckedChange = { menuExpanded = it },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                        ) {
                            Icon(
                                imageVector = InvokeIcons.ChevronDown,
                                contentDescription = "Mode and canvas options",
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer { rotationZ = chevronRotation },
                            )
                        }
                    },
                )

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    WorkspaceMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = mode.label,
                                    style = if (mode == workspaceMode) {
                                        MaterialTheme.typography.titleSmallEmphasized
                                    } else {
                                        MaterialTheme.typography.titleSmall
                                    },
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (mode) {
                                        WorkspaceMode.EVERYDAY -> InvokeIcons.Sun
                                        WorkspaceMode.DEEP_FOCUS -> InvokeIcons.Focus
                                        WorkspaceMode.NIGHT_AMOLED -> InvokeIcons.Night
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            trailingIcon = if (mode == workspaceMode) {
                                {
                                    Icon(
                                        imageVector = InvokeIcons.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            } else {
                                null
                            },
                            onClick = {
                                menuExpanded = false
                                onSelectMode(mode)
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Edit Home Canvas",
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = InvokeIcons.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenCustomize()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberLiveSystemGlance(): LiveSystemGlance {
    val inPreview = LocalInspectionMode.current
    if (inPreview) {
        return remember {
            LiveSystemGlance(
                batteryPercent = 84,
                isCharging = false,
                nextAlarmLabel = "7:00 AM",
            )
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var glance by remember { mutableStateOf(readSystemGlance(context)) }

    DisposableEffect(context, lifecycleOwner) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                glance = readSystemGlance(context)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
        }
        var registered = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    glance = readSystemGlance(context)
                    if (!registered) {
                        context.registerReceiver(receiver, filter)
                        registered = true
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    if (registered) {
                        runCatching { context.unregisterReceiver(receiver) }
                        registered = false
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (registered) {
                runCatching { context.unregisterReceiver(receiver) }
            }
        }
    }

    return glance
}

private val ALARM_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

private fun readSystemGlance(context: Context): LiveSystemGlance {
    val batteryIntent = runCatching {
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }.getOrNull()

    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val pct = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else 100
    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val alarmLabel = runCatching {
        val am = context.getSystemService(AlarmManager::class.java)
        val triggerMs = am?.nextAlarmClock?.triggerTime ?: return@runCatching null
        Instant.ofEpochMilli(triggerMs)
            .atZone(ZoneId.systemDefault())
            .format(ALARM_FORMAT)
    }.getOrNull()

    return LiveSystemGlance(
        batteryPercent = pct,
        isCharging = charging,
        nextAlarmLabel = alarmLabel,
    )
}

@Composable
private fun rememberLiveClockSnapshot(
    use24Hour: Boolean,
    showSeconds: Boolean,
): ClockSnapshot {
    val inPreview = LocalInspectionMode.current
    if (inPreview) {
        return remember(use24Hour) {
            ClockSnapshot(
                hourText = if (use24Hour) "09" else "9",
                minuteText = "41",
                secondText = "08",
                amPmText = "AM",
                dayOfWeek = "Monday",
                dateLine = "Nov 23",
            )
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var snapshot by remember(use24Hour) { mutableStateOf(formatNow(use24Hour)) }

    LaunchedEffect(lifecycleOwner, use24Hour, showSeconds) {
        if (!showSeconds) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                snapshot = formatNow(use24Hour)
                val nowMs = System.currentTimeMillis()
                val delayMs = (1_000L - (nowMs % 1_000L)).coerceIn(50L, 1_000L)
                delay(delayMs.milliseconds)
            }
        }
    }

    DisposableEffect(context, lifecycleOwner, use24Hour) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                snapshot = formatNow(use24Hour)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        var registered = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    snapshot = formatNow(use24Hour)
                    if (!registered) {
                        context.registerReceiver(receiver, filter)
                        registered = true
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    if (registered) {
                        runCatching { context.unregisterReceiver(receiver) }
                        registered = false
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (registered) {
                runCatching { context.unregisterReceiver(receiver) }
            }
        }
    }

    return snapshot
}

private val HOUR_24_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH", Locale.getDefault())
private val HOUR_12_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h", Locale.getDefault())
private val MINUTE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("mm", Locale.getDefault())
private val SECOND_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("ss", Locale.getDefault())
private val AM_PM_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("a", Locale.getDefault())
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

private fun formatNow(use24Hour: Boolean): ClockSnapshot {
    val now = LocalDateTime.now()
    return ClockSnapshot(
        hourText = now.format(if (use24Hour) HOUR_24_FORMAT else HOUR_12_FORMAT),
        minuteText = now.format(MINUTE_FORMAT),
        secondText = now.format(SECOND_FORMAT),
        amPmText = now.format(AM_PM_FORMAT),
        dayOfWeek = now.format(DAY_FORMAT),
        dateLine = now.format(DATE_FORMAT),
    )
}
