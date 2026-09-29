package com.sumi.pockon.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream

// Uri -> Bitmap
fun getBitmapFromUri(
    contentResolver: ContentResolver,
    uri: Uri,
    reqWidth: Int = 1024,
    reqHeight: Int = 1024
): Bitmap? {
    val inputStream = contentResolver.openInputStream(uri) ?: return null

    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeStream(inputStream, null, options)
    inputStream.close()

    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false

    val newInputStream = contentResolver.openInputStream(uri) ?: return null
    val bitmap = BitmapFactory.decodeStream(newInputStream, null, options)
    newInputStream.close()

    // EXIF 정보를 기반으로 이미지 회전 보정
    return bitmap?.let { rotateBitmapIfRequired(contentResolver, uri, it) }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun rotateBitmapIfRequired(contentResolver: ContentResolver, uri: Uri, bitmap: Bitmap): Bitmap {
    val inputStream = contentResolver.openInputStream(uri) ?: return bitmap
    val exif = ExifInterface(inputStream)
    inputStream.close()

    val orientation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val rotationAngle = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    return if (rotationAngle != 0f) {
        val matrix = Matrix().apply { postRotate(rotationAngle) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
