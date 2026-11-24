package com.example.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.CalibrationLog
import com.example.data.science.SugarCalibrationResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportFormat(val extension: String, val mimeType: String, val label: String) {
    CSV("csv", "text/csv", "CSV Spreadsheet"),
    TXT("txt", "text/plain", "Readable Daily Report")
}

object ReportExporter {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    fun buildReadableReport(
        userName: String,
        currentResult: SugarCalibrationResult,
        logs: List<CalibrationLog>
    ): String {
        val now = dateFormatter.format(Date())
        return buildString {
            appendLine("====================================================")
            appendLine("  BRIXLEAF PERSONAL SUGAR CALIBRATION REPORT")
            appendLine("====================================================")
            appendLine("Prepared for : $userName")
            appendLine("Generated at : $now")
            appendLine()
            appendLine("--- CURRENT ACTIVE CALIBRATION ---------------------")
            appendLine("Plant / Food : ${currentResult.food.name} (${currentResult.food.scientificName})")
            appendLine("Serving Size : ${"%.0f".format(currentResult.servingGrams)} g")
            appendLine("Quick Answer : Add ${"%.2f".format(currentResult.currentAddedSugarGrams)} g (${"%.2f".format(currentResult.currentTeaspoons)} tsp) sugar")
            appendLine("Ideal Target : ${"%.2f".format(currentResult.idealAddedSugarGrams)} g")
            appendLine("Microscopic  : ${"%.1f".format(currentResult.molarityMicromolesPerMl)} µmol/mL  |  ${"%.1f".format(currentResult.brixDensity)} °Brix  |  ${"%.3f".format(currentResult.solutionDensityGPerCm3)} g/cm³")
            appendLine("Phase State  : ${currentResult.phaseState.title} (${currentResult.phaseState.plainVerdict})")
            appendLine()
            appendLine("--- PERSONAL CALIBRATION LOGS (${logs.size}) -----------------")
            if (logs.isEmpty()) {
                appendLine("No saved entries yet. Tap 'Save to Report' on any food to log it here.")
            } else {
                logs.forEachIndexed { idx, log ->
                    val ts = dateFormatter.format(Date(log.createdAtMillis))
                    appendLine("${idx + 1}. ${log.foodName} [${ts}]")
                    appendLine("   Serving: ${"%.0f".format(log.servingGrams)}g | Added: ${"%.2f".format(log.userAdjustedSugarGrams)}g (Ideal: ${"%.2f".format(log.recommendedSugarGrams)}g)")
                    appendLine("   Density: ${"%.1f".format(log.brixDensity)} °Bx | ${"%.1f".format(log.micromolesPerMl)} µmol/mL | State: ${log.phaseStateName}")
                    if (log.note.isNotBlank()) appendLine("   Note: ${log.note}")
                }
            }
            appendLine("====================================================")
        }
    }

    fun buildCsvReport(
        userName: String,
        currentResult: SugarCalibrationResult,
        logs: List<CalibrationLog>
    ): String = buildString {
        appendLine("user_profile,timestamp,food_name,serving_g,ideal_sugar_g,calibrated_sugar_g,brix_density,micromoles_per_ml,phase_state,note")
        val now = dateFormatter.format(Date())
        appendLine(
            "\"${escapeCsv(userName)}\",\"$now\",\"${escapeCsv(currentResult.food.name)}\"," +
                "${"%.1f".format(currentResult.servingGrams)},${"%.2f".format(currentResult.idealAddedSugarGrams)}," +
                "${"%.2f".format(currentResult.currentAddedSugarGrams)},${"%.2f".format(currentResult.brixDensity)}," +
                "${"%.1f".format(currentResult.molarityMicromolesPerMl)},\"${escapeCsv(currentResult.phaseState.title)}\",\"Active Session\""
        )
        logs.forEach { log ->
            val ts = dateFormatter.format(Date(log.createdAtMillis))
            appendLine(
                "\"${escapeCsv(userName)}\",\"$ts\",\"${escapeCsv(log.foodName)}\"," +
                    "${"%.1f".format(log.servingGrams)},${"%.2f".format(log.recommendedSugarGrams)}," +
                    "${"%.2f".format(log.userAdjustedSugarGrams)},${"%.2f".format(log.brixDensity)}," +
                    "${"%.1f".format(log.micromolesPerMl)},\"${escapeCsv(log.phaseStateName)}\",\"${escapeCsv(log.note)}\""
            )
        }
    }

    fun shareReport(
        context: Context,
        format: ReportFormat,
        userName: String,
        currentResult: SugarCalibrationResult,
        logs: List<CalibrationLog>
    ) {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(reportsDir, "brixleaf_personal_report.${format.extension}")
        val content = when (format) {
            ReportFormat.CSV -> buildCsvReport(userName, currentResult, logs)
            ReportFormat.TXT -> buildReadableReport(userName, currentResult, logs)
        }
        file.writeText(content)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_SUBJECT, "BrixLeaf Personal Sugar Report — $userName")
            putExtra(Intent.EXTRA_TEXT, "Personal botanical sugar calibration report for $userName.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(
                Intent.createChooser(shareIntent, "Export Personal Report (${format.label})")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            // No compatible share handler available on device
        }
    }

    private fun escapeCsv(raw: String): String = raw.replace("\"", "\"\"")
}
