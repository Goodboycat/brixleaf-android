package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PersonalProfilePrefs
import com.example.data.model.CalibrationLog
import com.example.data.repository.ReportFormat
import com.example.data.science.SugarCalibrationResult
import com.example.ui.theme.BitterCoral
import com.example.ui.theme.ChlorophyllEmerald
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassCardElevated
import com.example.ui.theme.GlassStroke
import com.example.ui.theme.MintInk
import com.example.ui.theme.NectarAmber
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SageMuted
import com.example.ui.theme.SageSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalReportSheet(
    profile: PersonalProfilePrefs,
    currentCalibration: SugarCalibrationResult,
    logs: List<CalibrationLog>,
    onUpdateUserName: (String) -> Unit,
    onUpdateSweetnessBias: (Double) -> Unit,
    onSaveCurrentLog: () -> Unit,
    onDeleteLog: (Long) -> Unit,
    onExportReport: (ReportFormat) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nameDraft by remember { mutableStateOf(profile.userName) }
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.US) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = MintInk
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Report",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MintInk
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close personal report",
                        tint = SageSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Single User Profile Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassCard)
                    .border(1.dp, GlassStroke, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = {
                        nameDraft = it
                        onUpdateUserName(it)
                    },
                    label = { Text("Your Name (Report Owner)") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = ChlorophyllEmerald)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChlorophyllEmerald,
                        unfocusedBorderColor = GlassStroke,
                        focusedTextColor = MintInk,
                        unfocusedTextColor = MintInk
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Personal Sweetness Preference",
                        style = MaterialTheme.typography.labelMedium,
                        color = SageSecondary
                    )
                    Text(
                        text = "${(profile.personalSweetnessBias * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = NectarAmber
                    )
                }
                Slider(
                    value = profile.personalSweetnessBias.toFloat(),
                    onValueChange = { onUpdateSweetnessBias(it.toDouble()) },
                    valueRange = 0.75f..1.25f,
                    colors = SliderDefaults.colors(
                        thumbColor = NectarAmber,
                        activeTrackColor = NectarAmber
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Save Active + Export CSV + Export Report
            Button(
                onClick = onSaveCurrentLog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChlorophyllEmerald,
                    contentColor = ObsidianVoid
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_to_report_btn")
            ) {
                Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log ${currentCalibration.food.name} (+${formatScientificNumber(currentCalibration.currentAddedSugarGrams, 2)} g)")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onExportReport(ReportFormat.CSV) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_csv_btn")
                ) {
                    Icon(Icons.Rounded.TableChart, contentDescription = null, tint = CrystalCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", color = MintInk)
                }
                OutlinedButton(
                    onClick = { onExportReport(ReportFormat.TXT) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_txt_btn")
                ) {
                    Icon(Icons.Rounded.Description, contentDescription = null, tint = NectarAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Report", color = MintInk)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SAVED CALIBRATION HISTORY (${logs.size})",
                style = MaterialTheme.typography.labelSmall,
                color = SageSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (logs.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GlassCard)
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "No logs yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SageSecondary
                            )
                        }
                    }
                } else {
                    items(logs, key = { it.id }) { log ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GlassCardElevated.copy(alpha = 0.72f))
                                .border(1.dp, GlassStroke, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${log.foodName} • ${formatScientificNumber(log.servingGrams, 0)}g",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MintInk
                                )
                                Text(
                                    text = "+${formatScientificNumber(log.userAdjustedSugarGrams, 2)} g • ${formatScientificNumber(log.brixDensity, 1)} °Bx • ${log.phaseStateName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ChlorophyllEmerald
                                )
                                Text(
                                    text = dateFormat.format(Date(log.createdAtMillis)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SageMuted
                                )
                            }
                            IconButton(onClick = { onDeleteLog(log.id) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete log",
                                    tint = BitterCoral
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
