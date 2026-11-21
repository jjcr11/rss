package com.jjcr11.rss.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private var addIconCache: ImageVector? = null

val AddIcon: ImageVector
    get() = addIconCache ?: ImageVector.Builder(
        name = "Add",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(value = Color.Black)) {
            moveTo(x = 19f, y = 13f)
            horizontalLineTo(x = 13f)
            verticalLineTo(y = 19f)
            horizontalLineTo(x = 11f)
            verticalLineTo(y = 13f)
            horizontalLineTo(x = 5f)
            verticalLineTo(y = 11f)
            horizontalLineTo(x = 11f)
            verticalLineTo(y = 5f)
            horizontalLineTo(x = 13f)
            verticalLineTo(y = 11f)
            horizontalLineTo(x = 19f)
            verticalLineTo(y = 13f)
            close()
        }
    }.build().also { addIconCache = it }

private var closeIconCache: ImageVector? = null

val CloseIcon: ImageVector
    get() = closeIconCache ?: ImageVector.Builder(
        name = "Close",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(value = Color.Black)) {
            moveTo(x = 19f, y = 6.41f)
            lineTo(x = 17.59f, y = 5f)
            lineTo(x = 12f, y = 10.59f)
            lineTo(x = 6.41f, y = 5f)
            lineTo(x = 5f, y = 6.41f)
            lineTo(x = 10.59f, y = 12f)
            lineTo(x = 5f, y = 17.59f)
            lineTo(x = 6.41f, y = 19f)
            lineTo(x = 12f, y = 13.41f)
            lineTo(x = 17.59f, y = 19f)
            lineTo(x = 19f, y = 17.59f)
            lineTo(x = 13.41f, y = 12f)
            close()
        }
    }.build().also { closeIconCache = it }

private var rssFeedIconCache: ImageVector? = null

val RssFeedIcon: ImageVector
    get() = rssFeedIconCache ?: ImageVector.Builder(
        name = "RssFeed",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(value = Color.Black)) {
            moveTo(x = 6.18f, y = 15.64f)
            curveTo(
                x1 = 4.98f,
                y1 = 15.64f,
                x2 = 4f,
                y2 = 16.62f,
                x3 = 4f,
                y3 = 17.82f,
            )
            curveTo(
                x1 = 4f,
                y1 = 19.02f,
                x2 = 4.98f,
                y2 = 20f,
                x3 = 6.18f,
                y3 = 20f,
            )
            curveTo(
                x1 = 7.38f,
                y1 = 20f,
                x2 = 8.36f,
                y2 = 19.02f,
                x3 = 8.36f,
                y3 = 17.82f,
            )
            curveTo(
                x1 = 8.36f,
                y1 = 16.62f,
                x2 = 7.38f,
                y2 = 15.64f,
                x3 = 6.18f,
                y3 = 15.64f,
            )
            close()

            moveTo(x = 4f, y = 10.1f)
            verticalLineTo(y = 12.6f)
            curveTo(
                x1 = 6.84f,
                y1 = 12.6f,
                x2 = 9.14f,
                y2 = 14.9f,
                x3 = 9.14f,
                y3 = 17.74f,
            )
            horizontalLineTo(x = 11.64f)
            curveTo(
                x1 = 11.64f,
                y1 = 13.52f,
                x2 = 8.22f,
                y2 = 10.1f,
                x3 = 4f,
                y3 = 10.1f,
            )
            close()

            moveTo(x = 4f, y = 4.47f)
            verticalLineTo(y = 6.97f)
            curveTo(
                x1 = 9.94f,
                y1 = 6.97f,
                x2 = 14.77f,
                y2 = 11.8f,
                x3 = 14.77f,
                y3 = 17.74f,
            )
            horizontalLineTo(x = 17.27f)
            curveTo(
                x1 = 17.27f,
                y1 = 10.42f,
                x2 = 11.32f,
                y2 = 4.47f,
                x3 = 4f,
                y3 = 4.47f,
            )
            close()
        }
    }.build().also { rssFeedIconCache = it }
