package com.stateai.export

import android.content.Context
import android.util.Log
import com.stateai.domain.export.SegmentCsv
import com.stateai.domain.segment.SegmentRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Writes all segment summaries to `files/exports/segments.csv` in the app's private storage.
 * Pull it from a debug build with `adb shell run-as com.stateai cat files/exports/segments.csv`.
 */
class SummaryExporter(
    private val context: Context,
    private val segments: SegmentRepository,
    private val csv: SegmentCsv = SegmentCsv(),
) {
    suspend fun export(): File = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "$DIRECTORY/$FILE_NAME")
        file.parentFile?.mkdirs()
        file.writeText(csv.write(segments.all()))
        Log.i(LOG_TAG, "exported ${file.absolutePath}")
        file
    }

    private companion object {
        const val DIRECTORY = "exports"
        const val FILE_NAME = "segments.csv"
        const val LOG_TAG = "Export"
    }
}
