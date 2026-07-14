package com.example.flashcardstudy.fileimport

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FileTextExtractor {
    suspend fun extractRawText(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        when {
            mimeType == "application/pdf" -> extractPdfText(context, uri)
            mimeType.startsWith("image/") -> extractImageText(context, uri)
            else -> throw IllegalArgumentException("Unsupported file type. Pick a PDF or image file.")
        }
    }

    private fun extractPdfText(context: Context, uri: Uri): String {
        PDFBoxResourceLoader.init(context.applicationContext)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                val stripper = PDFTextStripper().apply {
                    sortByPosition = true
                }
                return stripper.getText(document).trim()
            }
        }
        throw IllegalStateException("Unable to open the selected PDF file.")
    }

    private suspend fun extractImageText(context: Context, uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = recognizer.process(image).await()
        return result.text.trim()
    }
}
