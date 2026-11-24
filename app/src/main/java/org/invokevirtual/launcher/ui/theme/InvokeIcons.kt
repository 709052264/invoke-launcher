package org.invokevirtual.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object InvokeIcons {

    val Search: ImageVector by lazy {
        vectorIcon("InvokeSearch") {
            moveTo(15.5f, 14f)
            horizontalLineToRelative(-0.79f)
            lineToRelative(-0.28f, -0.27f)
            curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
            curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
            reflectiveCurveTo(3f, 5.91f, 3f, 9.5f)
            reflectiveCurveTo(5.91f, 16f, 9.5f, 16f)
            curveToRelative(1.61f, 0f, 3.09f, -0.59f, 4.23f, -1.57f)
            lineToRelative(0.27f, 0.28f)
            verticalLineToRelative(0.79f)
            lineToRelative(5f, 4.99f)
            lineTo(20.49f, 19f)
            lineToRelative(-4.99f, -5f)
            close()
            moveTo(9.5f, 14f)
            curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
            reflectiveCurveTo(7.01f, 5f, 9.5f, 5f)
            reflectiveCurveTo(14f, 7.01f, 14f, 9.5f)
            reflectiveCurveTo(11.99f, 14f, 9.5f, 14f)
            close()
        }
    }

    val Spark: ImageVector by lazy {
        vectorIcon("InvokeSpark") {
            moveTo(12f, 2f)
            lineTo(14.5f, 9.5f)
            lineTo(22f, 12f)
            lineTo(14.5f, 14.5f)
            lineTo(12f, 22f)
            lineTo(9.5f, 14.5f)
            lineTo(2f, 12f)
            lineTo(9.5f, 9.5f)
            close()
        }
    }

    val Close: ImageVector by lazy {
        vectorIcon("InvokeClose") {
            moveTo(19f, 6.41f)
            lineTo(17.59f, 5f)
            lineTo(12f, 10.59f)
            lineTo(6.41f, 5f)
            lineTo(5f, 6.41f)
            lineTo(10.59f, 12f)
            lineTo(5f, 17.59f)
            lineTo(6.41f, 19f)
            lineTo(12f, 13.41f)
            lineTo(17.59f, 19f)
            lineTo(19f, 17.59f)
            lineTo(13.41f, 12f)
            close()
        }
    }

    val Phone: ImageVector by lazy {
        vectorIcon("InvokePhone") {
            moveTo(6.62f, 10.79f)
            curveToRelative(1.44f, 2.83f, 3.76f, 5.14f, 6.59f, 6.59f)
            lineToRelative(2.2f, -2.2f)
            curveToRelative(0.27f, -0.27f, 0.67f, -0.36f, 1.02f, -0.24f)
            curveToRelative(1.12f, 0.37f, 2.33f, 0.57f, 3.57f, 0.57f)
            curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
            verticalLineTo(20f)
            curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f)
            curveToRelative(-9.39f, 0f, -17f, -7.61f, -17f, -17f)
            curveToRelative(0f, -0.55f, 0.45f, -1f, 1f, -1f)
            horizontalLineToRelative(3.5f)
            curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
            curveToRelative(0f, 1.25f, 0.2f, 2.45f, 0.57f, 3.57f)
            curveToRelative(0.11f, 0.35f, 0.03f, 0.74f, -0.25f, 1.02f)
            lineToRelative(-2.2f, 2.2f)
            close()
        }
    }

    val Chat: ImageVector by lazy {
        vectorIcon("InvokeChat") {
            moveTo(20f, 2f)
            horizontalLineTo(4f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(18f)
            lineToRelative(4f, -4f)
            horizontalLineToRelative(14f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(4f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(18f, 14f)
            horizontalLineTo(6f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(12f)
            verticalLineToRelative(2f)
            close()
            moveTo(18f, 11f)
            horizontalLineTo(6f)
            verticalLineTo(9f)
            horizontalLineToRelative(12f)
            verticalLineToRelative(2f)
            close()
            moveTo(18f, 8f)
            horizontalLineTo(6f)
            verticalLineTo(6f)
            horizontalLineToRelative(12f)
            verticalLineToRelative(2f)
            close()
        }
    }

    val Camera: ImageVector by lazy {
        vectorIcon("InvokeCamera") {
            moveTo(9f, 2f)
            lineTo(7.17f, 4f)
            horizontalLineTo(4f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(12f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(16f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(6f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            horizontalLineToRelative(-3.17f)
            lineTo(15f, 2f)
            horizontalLineTo(9f)
            close()
            moveTo(12f, 17f)
            curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
            reflectiveCurveToRelative(2.24f, -5f, 5f, -5f)
            reflectiveCurveToRelative(5f, 2.24f, 5f, 5f)
            reflectiveCurveToRelative(-2.24f, 5f, -5f, 5f)
            close()
            moveTo(12f, 9f)
            curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
            reflectiveCurveToRelative(1.34f, 3f, 3f, 3f)
            reflectiveCurveToRelative(3f, -1.34f, 3f, -3f)
            reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f)
            close()
        }
    }

    val Apps: ImageVector by lazy {
        vectorIcon("InvokeApps") {
            moveTo(4f, 8f); horizontalLineToRelative(4f); verticalLineTo(4f); horizontalLineTo(4f); close()
            moveTo(10f, 20f); horizontalLineToRelative(4f); verticalLineToRelative(-4f); horizontalLineToRelative(-4f); close()
            moveTo(4f, 20f); horizontalLineToRelative(4f); verticalLineToRelative(-4f); horizontalLineTo(4f); close()
            moveTo(4f, 14f); horizontalLineToRelative(4f); verticalLineToRelative(-4f); horizontalLineTo(4f); close()
            moveTo(10f, 14f); horizontalLineToRelative(4f); verticalLineToRelative(-4f); horizontalLineToRelative(-4f); close()
            moveTo(16f, 4f); verticalLineToRelative(4f); horizontalLineToRelative(4f); verticalLineTo(4f); close()
            moveTo(10f, 8f); horizontalLineToRelative(4f); verticalLineTo(4f); horizontalLineToRelative(-4f); close()
            moveTo(16f, 14f); horizontalLineToRelative(4f); verticalLineToRelative(-4f); horizontalLineToRelative(-4f); close()
            moveTo(16f, 20f); horizontalLineToRelative(4f); verticalLineToRelative(-4f); horizontalLineToRelative(-4f); close()
        }
    }

    val Focus: ImageVector by lazy {
        vectorIcon("InvokeFocus") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            reflectiveCurveToRelative(4.48f, 10f, 10f, 10f)
            reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
            reflectiveCurveTo(17.52f, 2f, 12f, 2f)
            close()
            moveTo(12f, 20f)
            curveToRelative(-4.42f, 0f, -8f, -3.58f, -8f, -8f)
            reflectiveCurveToRelative(3.58f, -8f, 8f, -8f)
            reflectiveCurveToRelative(8f, 3.58f, 8f, 8f)
            reflectiveCurveToRelative(-3.58f, 8f, -8f, 8f)
            close()
            moveTo(12f, 7f)
            curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
            reflectiveCurveToRelative(2.24f, 5f, 5f, 5f)
            reflectiveCurveToRelative(5f, -2.24f, 5f, -5f)
            reflectiveCurveToRelative(-2.24f, -5f, -5f, -5f)
            close()
            moveTo(12f, 14f)
            curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
            reflectiveCurveToRelative(0.9f, -2f, 2f, -2f)
            reflectiveCurveToRelative(2f, 0.9f, 2f, 2f)
            reflectiveCurveToRelative(-0.9f, 2f, -2f, 2f)
            close()
        }
    }

    val Night: ImageVector by lazy {
        vectorIcon("InvokeNight") {
            moveTo(12f, 3f)
            curveToRelative(-4.97f, 0f, -9f, 4.03f, -9f, 9f)
            reflectiveCurveToRelative(4.03f, 9f, 9f, 9f)
            reflectiveCurveToRelative(9f, -4.03f, 9f, -9f)
            curveToRelative(0f, -0.46f, -0.04f, -0.92f, -0.1f, -1.36f)
            curveToRelative(-0.98f, 1.37f, -2.58f, 2.26f, -4.4f, 2.26f)
            curveToRelative(-2.98f, 0f, -5.4f, -2.42f, -5.4f, -5.4f)
            curveToRelative(0f, -1.81f, 0.89f, -3.42f, 2.26f, -4.4f)
            curveTo(12.92f, 3.04f, 12.46f, 3f, 12f, 3f)
            close()
        }
    }

    val Sun: ImageVector by lazy {
        vectorIcon("InvokeSun") {
            moveTo(12f, 7f)
            curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
            reflectiveCurveToRelative(2.24f, 5f, 5f, 5f)
            reflectiveCurveToRelative(5f, -2.24f, 5f, -5f)
            reflectiveCurveToRelative(-2.24f, -5f, -5f, -5f)
            close()
            moveTo(11f, 2f); verticalLineToRelative(2f); horizontalLineToRelative(2f); verticalLineTo(2f); close()
            moveTo(11f, 20f); verticalLineToRelative(2f); horizontalLineToRelative(2f); verticalLineToRelative(-2f); close()
            moveTo(2f, 11f); verticalLineToRelative(2f); horizontalLineToRelative(2f); verticalLineToRelative(-2f); close()
            moveTo(20f, 11f); verticalLineToRelative(2f); horizontalLineToRelative(2f); verticalLineToRelative(-2f); close()
            moveTo(5.64f, 4.22f); lineToRelative(-1.42f, 1.42f); lineToRelative(1.42f, 1.42f); lineToRelative(1.41f, -1.42f); close()
            moveTo(18.36f, 16.95f); lineToRelative(-1.41f, 1.41f); lineToRelative(1.41f, 1.42f); lineToRelative(1.42f, -1.42f); close()
            moveTo(18.36f, 4.22f); lineToRelative(-1.41f, 1.42f); lineToRelative(1.41f, 1.41f); lineToRelative(1.42f, -1.41f); close()
            moveTo(5.64f, 16.95f); lineToRelative(-1.42f, 1.41f); lineToRelative(1.42f, 1.42f); lineToRelative(1.41f, -1.42f); close()
        }
    }

    val Play: ImageVector by lazy {
        vectorIcon("InvokePlay") {
            moveTo(8f, 5.14f)
            verticalLineToRelative(14f)
            lineToRelative(11f, -7f)
            close()
        }
    }

    val OpenInNew: ImageVector by lazy {
        vectorIcon("InvokeOpenInNew") {
            moveTo(19f, 19f)
            horizontalLineTo(5f)
            verticalLineTo(5f)
            horizontalLineToRelative(7f)
            verticalLineTo(3f)
            horizontalLineTo(5f)
            curveToRelative(-1.11f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(14f)
            curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f)
            horizontalLineToRelative(14f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineToRelative(-7f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(7f)
            close()
            moveTo(14f, 3f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(3.59f)
            lineToRelative(-9.83f, 9.83f)
            lineToRelative(1.41f, 1.41f)
            lineTo(19f, 6.41f)
            verticalLineTo(10f)
            horizontalLineToRelative(2f)
            verticalLineTo(3f)
            horizontalLineToRelative(-7f)
            close()
        }
    }

    val Tune: ImageVector by lazy {
        vectorIcon("InvokeTune") {
            moveTo(3f, 17f); verticalLineToRelative(2f); horizontalLineToRelative(6f); verticalLineToRelative(-2f); close()
            moveTo(3f, 5f); verticalLineToRelative(2f); horizontalLineToRelative(10f); verticalLineTo(5f); close()
            moveTo(13f, 21f); verticalLineToRelative(-2f); horizontalLineToRelative(8f); verticalLineToRelative(-2f); horizontalLineToRelative(-8f); verticalLineToRelative(-2f); horizontalLineToRelative(-2f); verticalLineToRelative(6f); close()
            moveTo(7f, 9f); verticalLineToRelative(2f); horizontalLineTo(3f); verticalLineToRelative(2f); horizontalLineToRelative(4f); verticalLineToRelative(2f); horizontalLineToRelative(2f); verticalLineTo(9f); close()
            moveTo(21f, 13f); verticalLineToRelative(-2f); horizontalLineTo(11f); verticalLineToRelative(2f); close()
            moveTo(15f, 9f); horizontalLineToRelative(2f); verticalLineTo(7f); horizontalLineToRelative(4f); verticalLineTo(5f); horizontalLineToRelative(-4f); verticalLineTo(3f); horizontalLineToRelative(-2f); close()
        }
    }

    val Pin: ImageVector by lazy {
        vectorIcon("InvokePin") {
            moveTo(16f, 12f)
            verticalLineTo(4f)
            horizontalLineToRelative(1f)
            verticalLineTo(2f)
            horizontalLineTo(7f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(1f)
            verticalLineToRelative(8f)
            lineToRelative(-2f, 2f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(5.2f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(1.6f)
            verticalLineToRelative(-6f)
            horizontalLineTo(18f)
            verticalLineToRelative(-2f)
            lineToRelative(-2f, -2f)
            close()
        }
    }

    val Info: ImageVector by lazy {
        vectorIcon("InvokeInfo") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            reflectiveCurveToRelative(4.48f, 10f, 10f, 10f)
            reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
            reflectiveCurveTo(17.52f, 2f, 12f, 2f)
            close()
            moveTo(13f, 17f); horizontalLineToRelative(-2f); verticalLineToRelative(-6f); horizontalLineToRelative(2f); verticalLineToRelative(6f); close()
            moveTo(13f, 9f); horizontalLineToRelative(-2f); verticalLineTo(7f); horizontalLineToRelative(2f); verticalLineToRelative(2f); close()
        }
    }

    val Delete: ImageVector by lazy {
        vectorIcon("InvokeDelete") {
            moveTo(6f, 19f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(8f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(7f)
            horizontalLineTo(6f)
            verticalLineToRelative(12f)
            close()
            moveTo(19f, 4f)
            horizontalLineToRelative(-3.5f)
            lineToRelative(-1f, -1f)
            horizontalLineToRelative(-5f)
            lineToRelative(-1f, 1f)
            horizontalLineTo(5f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(14f)
            verticalLineTo(4f)
            close()
        }
    }

    val BubbleWindow: ImageVector by lazy {
        vectorIcon("InvokeBubbleWindow") {
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(14f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(14f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(5f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(19f, 19f); horizontalLineTo(5f); verticalLineTo(5f); horizontalLineToRelative(14f); verticalLineToRelative(14f); close()
            moveTo(17f, 11f); horizontalLineToRelative(-6f); verticalLineToRelative(6f); horizontalLineToRelative(6f); verticalLineToRelative(-6f); close()
        }
    }

    val ShortcutBolt: ImageVector by lazy {
        vectorIcon("InvokeShortcutBolt") {
            moveTo(11f, 21f)
            horizontalLineToRelative(-1f)
            lineToRelative(1f, -7f)
            horizontalLineTo(7.5f)
            curveToRelative(-0.58f, 0f, -0.57f, -0.32f, -0.38f, -0.66f)
            lineTo(12.97f, 3f)
            horizontalLineToRelative(1f)
            lineToRelative(-1f, 7f)
            horizontalLineToRelative(3.51f)
            curveToRelative(0.49f, 0f, 0.56f, 0.33f, 0.47f, 0.51f)
            lineTo(11f, 21f)
            close()
        }
    }

    val Swap: ImageVector by lazy {
        vectorIcon("InvokeSwap") {
            moveTo(6.99f, 11f); lineTo(3f, 15f); lineToRelative(3.99f, 4f); verticalLineToRelative(-3f); horizontalLineTo(14f); verticalLineToRelative(-2f); horizontalLineTo(6.99f); verticalLineToRelative(-3f); close()
            moveTo(21f, 9f); lineToRelative(-3.99f, -4f); verticalLineToRelative(3f); horizontalLineTo(10f); verticalLineToRelative(2f); horizontalLineToRelative(7.01f); verticalLineToRelative(3f); lineTo(21f, 9f); close()
        }
    }

    val Lock: ImageVector by lazy {
        vectorIcon("InvokeLock") {
            moveTo(18f, 8f)
            horizontalLineToRelative(-1f)
            verticalLineTo(6f)
            curveToRelative(0f, -2.76f, -2.24f, -5f, -5f, -5f)
            reflectiveCurveTo(7f, 3.24f, 7f, 6f)
            verticalLineToRelative(2f)
            horizontalLineTo(6f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(10f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(12f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(10f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(12f, 17f)
            curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
            reflectiveCurveToRelative(0.9f, -2f, 2f, -2f)
            reflectiveCurveToRelative(2f, 0.9f, 2f, 2f)
            reflectiveCurveToRelative(-0.9f, 2f, -2f, 2f)
            close()
            moveTo(15.1f, 8f)
            horizontalLineTo(8.9f)
            verticalLineTo(6f)
            curveToRelative(0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
            reflectiveCurveToRelative(3.1f, 1.39f, 3.1f, 3.1f)
            verticalLineToRelative(2f)
            close()
        }
    }

    val LockOpen: ImageVector by lazy {
        vectorIcon("InvokeLockOpen") {
            moveTo(12f, 17f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
            reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f)
            reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
            close()
            moveTo(18f, 8f)
            horizontalLineToRelative(-1f)
            verticalLineTo(6f)
            curveToRelative(0f, -2.76f, -2.24f, -5f, -5f, -5f)
            curveToRelative(-2.28f, 0f, -4.27f, 1.54f, -4.84f, 3.75f)
            lineToRelative(1.94f, 0.5f)
            curveTo(9.45f, 3.92f, 10.64f, 3f, 12f, 3f)
            curveToRelative(1.66f, 0f, 3f, 1.34f, 3f, 3f)
            verticalLineToRelative(2f)
            horizontalLineTo(6f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(10f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(12f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(10f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(18f, 20f)
            horizontalLineTo(6f)
            verticalLineTo(10f)
            horizontalLineToRelative(12f)
            verticalLineToRelative(10f)
            close()
        }
    }

    val Work: ImageVector by lazy {
        vectorIcon("InvokeWork") {
            moveTo(20f, 6f)
            horizontalLineToRelative(-4f)
            verticalLineTo(4f)
            curveToRelative(0f, -1.11f, -0.89f, -2f, -2f, -2f)
            horizontalLineToRelative(-4f)
            curveToRelative(-1.11f, 0f, -2f, 0.89f, -2f, 2f)
            verticalLineToRelative(2f)
            horizontalLineTo(4f)
            curveToRelative(-1.11f, 0f, -1.99f, 0.89f, -1.99f, 2f)
            lineTo(2f, 19f)
            curveToRelative(0f, 1.11f, 0.89f, 2f, 2f, 2f)
            horizontalLineToRelative(16f)
            curveToRelative(1.11f, 0f, 2f, -0.89f, 2f, -2f)
            verticalLineTo(8f)
            curveToRelative(0f, -1.11f, -0.89f, -2f, -2f, -2f)
            close()
            moveTo(14f, 6f)
            horizontalLineToRelative(-4f)
            verticalLineTo(4f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(2f)
            close()
        }
    }

    val Calculate: ImageVector by lazy {
        vectorIcon("InvokeCalculate") {
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(14f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(14f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(5f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(13.03f, 7.06f); lineTo(14.09f, 6f); lineToRelative(1.41f, 1.41f); lineTo(16.91f, 6f); lineToRelative(1.06f, 1.06f); lineToRelative(-1.41f, 1.41f); lineToRelative(1.41f, 1.41f); lineToRelative(-1.06f, 1.06f); lineTo(15.5f, 9.54f); lineToRelative(-1.41f, 1.41f); lineToRelative(-1.06f, -1.06f); lineToRelative(1.41f, -1.41f); lineToRelative(-1.41f, -1.42f); close()
            moveTo(6.25f, 7.72f); horizontalLineToRelative(5f); verticalLineToRelative(1.5f); horizontalLineToRelative(-5f); verticalLineToRelative(-1.5f); close()
            moveTo(11.5f, 16f); horizontalLineToRelative(-2f); verticalLineToRelative(2f); horizontalLineTo(8f); verticalLineToRelative(-2f); horizontalLineTo(6f); verticalLineToRelative(-1.5f); horizontalLineToRelative(2f); verticalLineToRelative(-2f); horizontalLineToRelative(1.5f); verticalLineToRelative(2f); horizontalLineToRelative(2f); verticalLineTo(16f); close()
            moveTo(18f, 17.25f); horizontalLineToRelative(-5f); verticalLineToRelative(-1.5f); horizontalLineToRelative(5f); verticalLineToRelative(1.5f); close()
            moveTo(18f, 14.75f); horizontalLineToRelative(-5f); verticalLineToRelative(-1.5f); horizontalLineToRelative(5f); verticalLineToRelative(1.5f); close()
        }
    }

    val Copy: ImageVector by lazy {
        vectorIcon("InvokeCopy") {
            moveTo(16f, 1f)
            horizontalLineTo(4f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(14f)
            horizontalLineToRelative(2f)
            verticalLineTo(3f)
            horizontalLineToRelative(12f)
            verticalLineTo(1f)
            close()
            moveTo(19f, 5f)
            horizontalLineTo(8f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(14f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(11f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(7f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(19f, 21f)
            horizontalLineTo(8f)
            verticalLineTo(7f)
            horizontalLineToRelative(11f)
            verticalLineToRelative(14f)
            close()
        }
    }

    val Colorize: ImageVector by lazy {
        vectorIcon("InvokeColorize") {
            moveTo(20.71f, 5.63f)
            lineToRelative(-2.34f, -2.34f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
            lineToRelative(-3.12f, 3.12f)
            lineToRelative(-1.93f, -1.91f)
            lineToRelative(-1.41f, 1.41f)
            lineToRelative(1.42f, 1.42f)
            lineTo(3f, 16.25f)
            verticalLineTo(21f)
            horizontalLineToRelative(4.75f)
            lineToRelative(8.92f, -8.92f)
            lineToRelative(1.42f, 1.42f)
            lineToRelative(1.41f, -1.41f)
            lineToRelative(-1.92f, -1.92f)
            lineToRelative(3.12f, -3.12f)
            curveToRelative(0.4f, -0.4f, 0.4f, -1.03f, 0.01f, -1.42f)
            close()
            moveTo(6.92f, 19f)
            lineTo(5f, 17.08f)
            lineToRelative(8.06f, -8.06f)
            lineToRelative(1.92f, 1.92f)
            lineTo(6.92f, 19f)
            close()
        }
    }

    val Widgets: ImageVector by lazy {
        vectorIcon("InvokeWidgets") {
            moveTo(13f, 13f); verticalLineToRelative(8f); horizontalLineToRelative(8f); verticalLineToRelative(-8f); horizontalLineToRelative(-8f); close()
            moveTo(3f, 21f); horizontalLineToRelative(8f); verticalLineToRelative(-8f); horizontalLineTo(3f); verticalLineToRelative(8f); close()
            moveTo(3f, 3f); verticalLineToRelative(8f); horizontalLineToRelative(8f); verticalLineTo(3f); horizontalLineTo(3f); close()
            moveTo(16.66f, 1.69f); lineTo(11f, 7.34f); lineTo(16.66f, 13f); lineToRelative(5.66f, -5.66f); lineToRelative(-5.66f, -5.65f); close()
        }
    }

    val Add: ImageVector by lazy {
        vectorIcon("InvokeAdd") {
            moveTo(19f, 13f)
            horizontalLineToRelative(-6f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-6f)
            horizontalLineTo(5f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(6f)
            verticalLineTo(5f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(6f)
            verticalLineToRelative(2f)
            close()
        }
    }

    val Check: ImageVector by lazy {
        vectorIcon("InvokeCheck") {
            moveTo(9f, 16.17f)
            lineTo(4.83f, 12f)
            lineToRelative(-1.42f, 1.41f)
            lineTo(9f, 19f)
            lineTo(21f, 7f)
            lineToRelative(-1.41f, -1.41f)
            close()
        }
    }

    val PersonAdd: ImageVector by lazy {
        vectorIcon("InvokePersonAdd") {
            moveTo(15f, 12f)
            curveToRelative(2.21f, 0f, 4f, -1.79f, 4f, -4f)
            reflectiveCurveToRelative(-1.79f, -4f, -4f, -4f)
            reflectiveCurveToRelative(-4f, 1.79f, -4f, 4f)
            reflectiveCurveToRelative(1.79f, 4f, 4f, 4f)
            close()
            moveTo(6f, 10f); verticalLineTo(7f); horizontalLineTo(4f); verticalLineToRelative(3f); horizontalLineTo(1f); verticalLineToRelative(2f); horizontalLineToRelative(3f); verticalLineToRelative(3f); horizontalLineToRelative(2f); verticalLineToRelative(-3f); horizontalLineToRelative(3f); verticalLineToRelative(-2f); horizontalLineTo(6f); close()
            moveTo(15f, 14f)
            curveToRelative(-2.67f, 0f, -8f, 1.34f, -8f, 4f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(16f)
            verticalLineToRelative(-2f)
            curveToRelative(0f, -2.66f, -5.33f, -4f, -8f, -4f)
            close()
        }
    }

    val Edit: ImageVector by lazy {
        vectorIcon("InvokeEdit") {
            moveTo(3f, 17.25f)
            verticalLineTo(21f)
            horizontalLineToRelative(3.75f)
            lineTo(17.81f, 9.94f)
            lineToRelative(-3.75f, -3.75f)
            lineTo(3f, 17.25f)
            close()
            moveTo(20.71f, 7.04f)
            curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
            lineToRelative(-2.34f, -2.34f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
            lineToRelative(-1.83f, 1.83f)
            lineToRelative(3.75f, 3.75f)
            lineToRelative(1.83f, -1.83f)
            close()
        }
    }

    val ArrowUp: ImageVector by lazy {
        vectorIcon("InvokeArrowUp") {
            moveTo(7.41f, 15.41f)
            lineTo(12f, 10.83f)
            lineToRelative(4.59f, 4.58f)
            lineTo(18f, 14f)
            lineToRelative(-6f, -6f)
            lineToRelative(-6f, 6f)
            close()
        }
    }

    val ArrowDown: ImageVector by lazy {
        vectorIcon("InvokeArrowDown") {
            moveTo(7.41f, 8.59f)
            lineTo(12f, 13.17f)
            lineToRelative(4.59f, -4.58f)
            lineTo(18f, 10f)
            lineToRelative(-6f, 6f)
            lineToRelative(-6f, -6f)
            close()
        }
    }

    val ChevronDown: ImageVector by lazy { ArrowDown }

    val Shapes: ImageVector by lazy {
        vectorIcon("InvokeShapes") {
            moveTo(12f, 2f)
            lineTo(6.5f, 11f)
            horizontalLineToRelative(11f)
            lineTo(12f, 2f)
            close()
            moveTo(17.5f, 13f)
            curveToRelative(-2.49f, 0f, -4.5f, 2.01f, -4.5f, 4.5f)
            reflectiveCurveToRelative(2.01f, 4.5f, 4.5f, 4.5f)
            reflectiveCurveToRelative(4.5f, -2.01f, 4.5f, -4.5f)
            reflectiveCurveToRelative(-2.01f, -4.5f, -4.5f, -4.5f)
            close()
            moveTo(3f, 21.5f)
            horizontalLineToRelative(8f)
            verticalLineToRelative(-8f)
            horizontalLineTo(3f)
            verticalLineToRelative(8f)
            close()
        }
    }

    val Alarm: ImageVector by lazy {
        vectorIcon("InvokeAlarm") {
            moveTo(22f, 5.72f)
            lineToRelative(-4.6f, -3.86f)
            lineToRelative(-1.29f, 1.53f)
            lineToRelative(4.6f, 3.86f)
            lineTo(22f, 5.72f)
            close()
            moveTo(7.88f, 3.39f)
            lineTo(6.6f, 1.86f)
            lineTo(2f, 5.71f)
            lineToRelative(1.29f, 1.53f)
            lineToRelative(4.59f, -3.85f)
            close()
            moveTo(12.5f, 8f)
            horizontalLineTo(11f)
            verticalLineToRelative(6f)
            lineToRelative(4.75f, 2.85f)
            lineToRelative(0.75f, -1.23f)
            lineToRelative(-4f, -2.37f)
            verticalLineTo(8f)
            close()
            moveTo(12f, 4f)
            curveToRelative(-4.97f, 0f, -9f, 4.03f, -9f, 9f)
            reflectiveCurveToRelative(4.02f, 9f, 9f, 9f)
            curveToRelative(4.97f, 0f, 9f, -4.03f, 9f, -9f)
            reflectiveCurveToRelative(-4.03f, -9f, -9f, -9f)
            close()
            moveTo(12f, 20f)
            curveToRelative(-3.87f, 0f, -7f, -3.13f, -7f, -7f)
            reflectiveCurveToRelative(3.13f, -7f, 7f, -7f)
            reflectiveCurveToRelative(7f, 3.13f, 7f, 7f)
            reflectiveCurveToRelative(-3.13f, 7f, -7f, 7f)
            close()
        }
    }

    val Battery: ImageVector by lazy {
        vectorIcon("InvokeBattery") {
            moveTo(15.67f, 4f)
            horizontalLineTo(14f)
            verticalLineTo(2f)
            horizontalLineToRelative(-4f)
            verticalLineToRelative(2f)
            horizontalLineTo(8.33f)
            curveTo(7.6f, 4f, 7f, 4.6f, 7f, 5.33f)
            verticalLineToRelative(15.33f)
            curveTo(7f, 21.4f, 7.6f, 22f, 8.33f, 22f)
            horizontalLineToRelative(7.33f)
            curveToRelative(0.74f, 0f, 1.34f, -0.6f, 1.34f, -1.33f)
            verticalLineTo(5.33f)
            curveTo(17f, 4.6f, 16.4f, 4f, 15.67f, 4f)
            close()
        }
    }

    val Favorite: ImageVector by lazy {
        vectorIcon("InvokeFavorite") {
            moveTo(12f, 21.35f)
            lineToRelative(-1.45f, -1.32f)
            curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
            curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
            curveToRelative(1.74f, 0f, 3.41f, 0.81f, 4.5f, 2.09f)
            curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
            curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
            curveToRelative(0f, 3.78f, -3.4f, 6.86f, -8.55f, 11.54f)
            lineTo(12f, 21.35f)
            close()
        }
    }

    private inline fun vectorIcon(
        name: String,
        crossinline pathBuilder: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit,
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            pathBuilder()
        }
    }.build()
}
