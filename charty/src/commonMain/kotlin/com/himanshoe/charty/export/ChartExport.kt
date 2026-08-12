package com.himanshoe.charty.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * The outcome of an image export started by [exportChartImage] or [ChartExporter.export].
 *
 * Exporting is a best-effort, platform-specific operation: some platforms can only hand the image to
 * the system share sheet, others can only write it to disk. Every branch is therefore terminal and
 * callers are expected to render a message rather than retry.
 */
@Immutable
sealed interface ChartExportResult {
    /**
     * The image was written to persistent storage.
     *
     * @property location A human-readable pointer to the written image, typically an absolute file
     *   path on file-backed platforms or the download file name in the browser.
     */
    @Immutable
    data class Saved(
        val location: String,
    ) : ChartExportResult

    /** The image was handed to the platform share sheet and the user took over from there. */
    @Immutable
    data object Shared : ChartExportResult

    /**
     * The image could not be exported.
     *
     * @property reason A human-readable explanation suitable for showing in the UI.
     */
    @Immutable
    data class Failed(
        val reason: String,
    ) : ChartExportResult
}

/**
 * Encodes [bitmap] as a PNG and hands it to the platform: the share sheet on Android and iOS, a
 * browser download on JS and WasmJs, and the user's Downloads folder on JVM desktop.
 *
 * On Android this needs the application `Context` that [rememberChartExporter] registers while
 * composing; prefer calling [rememberChartExporter] and using [ChartExporter.export] so the context
 * is always available.
 *
 * @param bitmap The captured chart image, usually produced by [ChartCaptureController.capture].
 * @param fileName The desired file name; it is normalised by [sanitizeExportFileName] before use.
 * @return A [ChartExportResult] describing what the platform did with the image.
 */
expect suspend fun exportChartImage(
    bitmap: ImageBitmap,
    fileName: String,
): ChartExportResult

/**
 * Exports a captured chart image. Obtain one with [rememberChartExporter] so that platforms needing
 * ambient composition state — Android needs a `Context` — can capture it while composing.
 */
fun interface ChartExporter {
    /**
     * Encodes [bitmap] as a PNG and hands it to the platform.
     *
     * @param bitmap The captured chart image.
     * @param fileName The desired file name; it is normalised before use.
     * @return A [ChartExportResult] describing what the platform did with the image.
     */
    suspend fun export(
        bitmap: ImageBitmap,
        fileName: String,
    ): ChartExportResult
}

/**
 * Remembers the [ChartExporter] for the current platform, capturing whatever composition-local state
 * that platform needs to export an image.
 *
 * @return A [ChartExporter] stable for as long as its platform dependencies are unchanged.
 */
@Composable
expect fun rememberChartExporter(): ChartExporter

private const val PNG_EXTENSION = ".png"
private const val DEFAULT_EXPORT_FILE_NAME = "chart.png"
private const val MAX_EXPORT_FILE_NAME_LENGTH = 96

private const val ILLEGAL_FILE_NAME_CHARS = "/\\:*?\"<>| \t"

/**
 * Normalises [fileName] into a safe PNG file name: path separators and other characters rejected by
 * common file systems become underscores, surrounding whitespace is dropped, the name is capped at a
 * conservative length, a `.png` extension is appended when missing, and a blank input falls back to
 * `chart.png`.
 *
 * @param fileName The caller-supplied file name, possibly blank or containing illegal characters.
 * @return A non-blank file name ending in `.png`.
 */
internal fun sanitizeExportFileName(fileName: String): String {
    val cleaned = replaceIllegalFileNameChars(fileName)
    val withoutExtension =
        if (cleaned.endsWith(suffix = PNG_EXTENSION, ignoreCase = true)) {
            cleaned.dropLast(PNG_EXTENSION.length)
        } else {
            cleaned
        }
    val trimmed = withoutExtension.trim('_', '.', ' ').take(MAX_EXPORT_FILE_NAME_LENGTH)
    if (trimmed.isEmpty()) {
        return DEFAULT_EXPORT_FILE_NAME
    }
    return trimmed + PNG_EXTENSION
}

private fun replaceIllegalFileNameChars(fileName: String): String =
    buildString {
        fileName.trim().forEach { character ->
            if (ILLEGAL_FILE_NAME_CHARS.indexOf(character) >= 0 || character.isISOControl()) {
                append('_')
            } else {
                append(character)
            }
        }
    }

private const val BASE64_GROUP_BYTES = 3
private const val BASE64_GROUP_CHARS = 4
private const val BASE64_SHIFT_FIRST = 18
private const val BASE64_SHIFT_SECOND = 12
private const val BASE64_SHIFT_THIRD = 6
private const val BASE64_MASK = 0x3F
private const val BYTE_MASK = 0xFF
private const val BITS_PER_BYTE = 8
private const val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
private const val BASE64_PAD = '='

/**
 * Encodes this byte array as standard, padded Base64. Used to build `data:` URLs for the browser
 * download path without depending on a platform codec.
 *
 * @return The Base64 text for the receiver, padded with `=` to a multiple of four characters.
 */
internal fun ByteArray.encodeChartBase64(): String {
    val builder = StringBuilder(size / BASE64_GROUP_BYTES * BASE64_GROUP_CHARS + BASE64_GROUP_CHARS)
    var index = 0
    while (index < size) {
        val remaining = size - index
        val byteOne = this[index].toInt() and BYTE_MASK
        val byteTwo = byteAtOrZero(index = index + 1, available = remaining > 1)
        val byteThree = byteAtOrZero(index = index + 2, available = remaining > 2)
        val chunk = (byteOne shl (BITS_PER_BYTE * 2)) or (byteTwo shl BITS_PER_BYTE) or byteThree
        builder.append(BASE64_ALPHABET[(chunk shr BASE64_SHIFT_FIRST) and BASE64_MASK])
        builder.append(BASE64_ALPHABET[(chunk shr BASE64_SHIFT_SECOND) and BASE64_MASK])
        if (remaining > 1) {
            builder.append(BASE64_ALPHABET[(chunk shr BASE64_SHIFT_THIRD) and BASE64_MASK])
        } else {
            builder.append(BASE64_PAD)
        }
        if (remaining > 2) {
            builder.append(BASE64_ALPHABET[chunk and BASE64_MASK])
        } else {
            builder.append(BASE64_PAD)
        }
        index += BASE64_GROUP_BYTES
    }
    return builder.toString()
}

private fun ByteArray.byteAtOrZero(
    index: Int,
    available: Boolean,
): Int {
    if (!available) {
        return 0
    }
    return this[index].toInt() and BYTE_MASK
}
