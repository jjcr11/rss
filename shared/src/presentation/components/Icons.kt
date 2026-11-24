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

private var deleteIconCache: ImageVector? = null

val DeleteIcon: ImageVector
    get() = deleteIconCache ?: ImageVector.Builder(
        name = "Delete",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(value = Color.Black)) {
            moveTo(x = 6f, y = 19f)
            curveTo(
                x1 = 6f,
                y1 = 20.1f,
                x2 = 6.9f,
                y2 = 21f,
                x3 = 8f,
                y3 = 21f,
            )
            horizontalLineTo(x = 16f)
            curveTo(
                x1 = 17.1f,
                y1 = 21f,
                x2 = 18f,
                y2 = 20.1f,
                x3 = 18f,
                y3 = 19f,
            )
            verticalLineTo(y = 7f)
            horizontalLineTo(x = 6f)
            verticalLineTo(y = 19f)
            close()

            moveTo(x = 19f, y = 4f)
            horizontalLineTo(x = 15.5f)
            lineTo(x = 14.5f, y = 3f)
            horizontalLineTo(x = 9.5f)
            lineTo(x = 8.5f, y = 4f)
            horizontalLineTo(x = 5f)
            verticalLineTo(y = 6f)
            horizontalLineTo(x = 19f)
            verticalLineTo(y = 4f)
            close()
        }
    }.build().also { deleteIconCache = it }

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

private var arrowBackIconCache: ImageVector? = null

val ArrowBackIcon: ImageVector
    get() = arrowBackIconCache ?: ImageVector.Builder(
        name = "ArrowBack",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = true,
    ).apply {
        path(fill = SolidColor(value = Color.Black)) {
            moveTo(x = 20f, y = 11f)
            horizontalLineTo(x = 7.83f)
            lineTo(x = 13.42f, y = 5.41f)
            lineTo(x = 12f, y = 4f)
            lineTo(x = 4f, y = 12f)
            lineTo(x = 12f, y = 20f)
            lineTo(x = 13.41f, y = 18.59f)
            lineTo(x = 7.83f, y = 13f)
            horizontalLineTo(x = 20f)
            verticalLineTo(y = 11f)
            close()
        }
    }.build().also { arrowBackIconCache = it }

private var settingsIconCache: ImageVector? = null

val SettingsIcon: ImageVector
    get() = settingsIconCache ?: ImageVector.Builder(
        name = "Settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(value = Color.Black)) {
            moveTo(x = 19.14f, y = 12.94f)
            curveTo(x1 = 19.18f, y1 = 12.63f, x2 = 19.2f, y2 = 12.32f, x3 = 19.2f, y3 = 12f)
            curveTo(x1 = 19.2f, y1 = 11.68f, x2 = 19.18f, y2 = 11.37f, x3 = 19.14f, y3 = 11.06f)
            lineTo(x = 21.43f, y = 9.27f)
            curveTo(x1 = 21.64f, y1 = 9.11f, x2 = 21.7f, y2 = 8.81f, x3 = 21.56f, y3 = 8.57f)
            lineTo(x = 19.4f, y = 4.83f)
            curveTo(x1 = 19.26f, y1 = 4.59f, x2 = 18.97f, y2 = 4.49f, x3 = 18.72f, y3 = 4.59f)
            lineTo(x = 16.02f, y = 5.68f)
            curveTo(x1 = 15.46f, y1 = 5.25f, x2 = 14.85f, y2 = 4.91f, x3 = 14.19f, y3 = 4.67f)
            lineTo(x = 13.78f, y = 1.8f)
            curveTo(x1 = 13.74f, y1 = 1.53f, x2 = 13.51f, y2 = 1.33f, x3 = 13.23f, y3 = 1.33f)
            horizontalLineTo(x = 8.91f)
            curveTo(x1 = 8.63f, y1 = 1.33f, x2 = 8.4f, y2 = 1.53f, x3 = 8.36f, y3 = 1.8f)
            lineTo(x = 7.95f, y = 4.67f)
            curveTo(x1 = 7.29f, y1 = 4.91f, x2 = 6.68f, y2 = 5.25f, x3 = 6.12f, y3 = 5.68f)
            lineTo(x = 3.42f, y = 4.59f)
            curveTo(x1 = 3.17f, y1 = 4.49f, x2 = 2.88f, y2 = 4.59f, x3 = 2.74f, y3 = 4.83f)
            lineTo(x = 0.58f, y = 8.57f)
            curveTo(x1 = 0.44f, y1 = 8.81f, x2 = 0.5f, y2 = 9.11f, x3 = 0.71f, y3 = 9.27f)
            lineTo(x = 3f, y = 11.06f)
            curveTo(x1 = 2.96f, y1 = 11.37f, x2 = 2.94f, y2 = 11.68f, x3 = 2.94f, y3 = 12f)
            curveTo(x1 = 2.94f, y1 = 12.32f, x2 = 2.96f, y2 = 12.63f, x3 = 3f, y3 = 12.94f)
            lineTo(x = 0.71f, y = 14.73f)
            curveTo(x1 = 0.5f, y1 = 14.89f, x2 = 0.44f, y2 = 15.19f, x3 = 0.58f, y3 = 15.43f)
            lineTo(x = 2.74f, y = 19.17f)
            curveTo(x1 = 2.88f, y1 = 19.41f, x2 = 3.17f, y2 = 19.51f, x3 = 3.42f, y3 = 19.41f)
            lineTo(x = 6.12f, y = 18.32f)
            curveTo(x1 = 6.68f, y1 = 18.75f, x2 = 7.29f, y2 = 19.09f, x3 = 7.95f, y3 = 19.33f)
            lineTo(x = 8.36f, y = 22.2f)
            curveTo(x1 = 8.4f, y1 = 22.47f, x2 = 8.63f, y2 = 22.67f, x3 = 8.91f, y3 = 22.67f)
            horizontalLineTo(x = 13.23f)
            curveTo(x1 = 13.51f, y1 = 22.67f, x2 = 13.74f, y2 = 22.47f, x3 = 13.78f, y3 = 22.2f)
            lineTo(x = 14.19f, y = 19.33f)
            curveTo(x1 = 14.85f, y1 = 19.09f, x2 = 15.46f, y2 = 18.75f, x3 = 16.02f, y3 = 18.32f)
            lineTo(x = 18.72f, y = 19.41f)
            curveTo(x1 = 18.97f, y1 = 19.51f, x2 = 19.26f, y2 = 19.41f, x3 = 19.4f, y3 = 19.17f)
            lineTo(x = 21.56f, y = 15.43f)
            curveTo(x1 = 21.7f, y1 = 15.19f, x2 = 21.64f, y2 = 14.89f, x3 = 21.43f, y3 = 14.73f)
            lineTo(x = 19.14f, y = 12.94f)
            close()
            moveTo(x = 11.07f, y = 15.5f)
            curveTo(x1 = 9.14f, y1 = 15.5f, x2 = 7.57f, y2 = 13.93f, x3 = 7.57f, y3 = 12f)
            curveTo(x1 = 7.57f, y1 = 10.07f, x2 = 9.14f, y2 = 8.5f, x3 = 11.07f, y3 = 8.5f)
            curveTo(x1 = 13f, y1 = 8.5f, x2 = 14.57f, y2 = 10.07f, x3 = 14.57f, y3 = 12f)
            curveTo(x1 = 14.57f, y1 = 13.93f, x2 = 13f, y2 = 15.5f, x3 = 11.07f, y3 = 15.5f)
            close()
        }
    }.build().also { settingsIconCache = it }


