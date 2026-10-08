package com.example.firebasegads

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImageUtils {
    fun compress(context: Context, uri: Uri, maxEdge: Int = 720, quality: Int = 70): ByteArray {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Could not open that image")

        input.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("The selected file is not a readable image")
        }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample >
            maxEdge * 2) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
        }

        val decoded =
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: throw IOException("Could not read that image")

        val scale = maxEdge.toFloat() / maxOf(decoded.width,
            decoded.height)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).toInt(),
                (decoded.height * scale).toInt(),
                true
            )
        } else {
            decoded
        }

        val bytes = ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }

        if (scaled !== decoded) decoded.recycle()
        scaled.recycle()

        require(bytes.size <= 900_000) {
            "Image is too large. Choose a smaller image."
        }

        return bytes
    }
}