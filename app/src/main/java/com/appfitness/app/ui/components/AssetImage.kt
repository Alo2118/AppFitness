package com.appfitness.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

private fun decodeAsset(context: Context, path: String): Bitmap? =
    runCatching { context.assets.open(path).use { BitmapFactory.decodeStream(it) } }.getOrNull()

/**
 * Loads and shows a JPEG bundled in `assets`. Decoded once per [assetPath] and
 * cached by [remember]; renders nothing if the asset is missing.
 */
@Composable
fun AssetImage(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val bitmap = remember(assetPath) { decodeAsset(context, assetPath) } ?: return
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}

/**
 * Shows an exercise guide that **animates between two frames** (start ⇄ end
 * position) to suggest the movement. The second frame is derived from the first
 * by convention (`name.jpg` → `name_2.jpg`); if it's missing, the image stays
 * static. Both frames are decoded once and cached.
 */
@Composable
fun AnimatedExerciseImage(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    intervalMs: Long = 750,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val frame0 = remember(assetPath) { decodeAsset(context, assetPath) } ?: return
    val secondPath = remember(assetPath) { assetPath.replace(Regex("\\.jpg$"), "_2.jpg") }
    val frame1 = remember(secondPath) { decodeAsset(context, secondPath) }

    if (frame1 == null) {
        Image(
            bitmap = frame0.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
        return
    }

    var showSecond by remember(assetPath) { mutableStateOf(false) }
    LaunchedEffect(assetPath) {
        while (true) {
            delay(intervalMs)
            showSecond = !showSecond
        }
    }
    Crossfade(targetState = showSecond, animationSpec = tween(350), label = "exercise-frame") { second ->
        Image(
            bitmap = (if (second) frame1 else frame0).asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}
