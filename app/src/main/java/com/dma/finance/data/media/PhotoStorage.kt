package com.dma.finance.data.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère le stockage local des photos de pièces justificatives, dans le répertoire privé
 * de l'application (non accessible aux autres apps), avec exposition via [FileProvider]
 * pour permettre à l'application caméra du système d'écrire directement dedans.
 */
@Singleton
class PhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val receiptsDir: File
        get() = File(context.filesDir, "receipts").apply { mkdirs() }

    /** Crée un nouveau fichier destiné à recevoir une photo, et retourne son Uri (pour l'intent caméra) et son chemin absolu. */
    fun createReceiptDestination(): Pair<Uri, String> {
        val file = File(receiptsDir, "receipt_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return uri to file.absolutePath
    }

    fun deleteReceipt(path: String?) {
        if (path.isNullOrBlank()) return
        val file = File(path)
        if (file.exists()) file.delete()
    }
}
