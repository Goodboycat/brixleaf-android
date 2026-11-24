package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BitterCoral
import com.example.ui.theme.ChlorophyllEmerald
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassStroke
import com.example.ui.theme.MintInk
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SageSecondary

@Composable
fun QuickOfflineEntryDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        bitterness: Double,
        naturalSugar: Double,
        ph: Double,
        servingGrams: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Plant / Herb") }
    var servingText by remember { mutableStateOf("100") }
    var naturalSugarText by remember { mutableStateOf("1.0") }
    var bitterness by remember { mutableDoubleStateOf(5.5) }
    var ph by remember { mutableDoubleStateOf(5.2) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(ObsidianSurface)
                .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Quick Offline Entry",
                style = MaterialTheme.typography.titleLarge,
                color = MintInk
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant or Food Name") },
                placeholder = { Text("e.g. Moringa Leaf") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChlorophyllEmerald,
                    unfocusedBorderColor = GlassStroke,
                    focusedContainerColor = GlassCard,
                    unfocusedContainerColor = GlassCard,
                    focusedTextColor = MintInk,
                    unfocusedTextColor = MintInk
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = servingText,
                    onValueChange = { servingText = it },
                    label = { Text("Serving (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChlorophyllEmerald,
                        unfocusedBorderColor = GlassStroke,
                        focusedContainerColor = GlassCard,
                        unfocusedContainerColor = GlassCard,
                        focusedTextColor = MintInk,
                        unfocusedTextColor = MintInk
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = naturalSugarText,
                    onValueChange = { naturalSugarText = it },
                    label = { Text("Nat. Sugar/100g") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChlorophyllEmerald,
                        unfocusedBorderColor = GlassStroke,
                        focusedContainerColor = GlassCard,
                        unfocusedContainerColor = GlassCard,
                        focusedTextColor = MintInk,
                        unfocusedTextColor = MintInk
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Bitterness Strength: ${formatScientificNumber(bitterness, 1)} / 10",
                style = MaterialTheme.typography.labelMedium,
                color = BitterCoral
            )
            Slider(
                value = bitterness.toFloat(),
                onValueChange = { bitterness = it.toDouble() },
                valueRange = 1f..10f,
                colors = SliderDefaults.colors(
                    thumbColor = BitterCoral,
                    activeTrackColor = BitterCoral
                )
            )

            Text(
                text = "Acidity (pH): ${formatScientificNumber(ph, 1)}",
                style = MaterialTheme.typography.labelMedium,
                color = ChlorophyllEmerald
            )
            Slider(
                value = ph.toFloat(),
                onValueChange = { ph = it.toDouble() },
                valueRange = 2.0f..7.5f,
                colors = SliderDefaults.colors(
                    thumbColor = ChlorophyllEmerald,
                    activeTrackColor = ChlorophyllEmerald
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = SageSecondary)
                }
                Button(
                    onClick = {
                        onSave(
                            name,
                            category,
                            bitterness,
                            naturalSugarText.replace(',', '.').toDoubleOrNull() ?: 1.0,
                            ph,
                            servingText.replace(',', '.').toDoubleOrNull() ?: 100.0
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChlorophyllEmerald,
                        contentColor = ObsidianVoid
                    ),
                    modifier = Modifier.testTag("save_offline_entry_btn")
                ) {
                    Text("Calculate & Save")
                }
            }
        }
    }
}
