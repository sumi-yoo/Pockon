package com.sumi.pockon.data.local.gift

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class GiftPhotoCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun save(id: String, bytes: ByteArray): String {
        val directory = File(context.filesDir, "gift_images").apply { mkdirs() }
        return File(directory, "$id.png").apply { writeBytes(bytes) }.absolutePath
    }
}
