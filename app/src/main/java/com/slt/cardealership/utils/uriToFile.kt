package com.slt.cardealership.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun uriToFile(context: Context, uri: Uri): File? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    val outputStream = FileOutputStream(file)
    inputStream.copyTo(outputStream)
    inputStream.close()
    outputStream.close()
    return file
}