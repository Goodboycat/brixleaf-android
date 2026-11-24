package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.SugarUnit
import com.example.data.science.SugarCalibrationResult
import com.example.ui.theme.ChlorophyllEmerald
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassCardElevated
import com.example.ui.theme.GlassStroke
import com.example.ui.theme.MintInk
import com.example.ui.theme.NectarAmber
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SageSecondary

@Composable
fun PlantInfoOverlayScreen(
    calibration: SugarCalibrationResult,
    selectedUnit: SugarUnit,
    onDensityScrub: (Double) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val food = calibration.food
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top bar with prominent X close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(GlassCardElevated)
                        .border(1.dp, GlassStroke, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        tint = NectarAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${food.source} Botanical & Food Record",
                        style = MaterialTheme.typography.labelSmall,
                        color = NectarAmber
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassCardElevated)
                        .border(1.dp, GlassStroke, CircleShape)
                        .testTag("close_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close plant information",
                        tint = MintInk,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real Wikipedia/DB Hero Photo Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassCard)
                    .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            ) {
                if (!food.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = food.imageUrl,
                        contentDescription = "${food.name} real botanical photograph",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Eco,
                            contentDescription = null,
                            tint = ChlorophyllEmerald.copy(alpha = 0.6f),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, ObsidianVoid.copy(alpha = 0.92f))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.displayMedium,
                            color = MintInk
                        )
                        Text(
                            text = "${food.scientificName} • ${food.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SageSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Microscopic Sugar Density Diagram
            TitrationDiagramCard(
                calibration = calibration,
                onDensityScrub = onDensityScrub
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Microscopic Breakdown Grid
            Text(
                text = "MICROSCOPIC & CHEMICAL PROFILE",
                style = MaterialTheme.typography.labelSmall,
                color = SageSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoMetricTile(
                    title = "Sucrose Molarity",
                    value = "${formatScientificNumber(calibration.molarityMicromolesPerMl, 1)} µmol/mL",
                    subtitle = "Dissolved particle density",
                    accent = CrystalCyan,
                    modifier = Modifier.weight(1f)
                )
                InfoMetricTile(
                    title = "Refractive Brix",
                    value = "${formatScientificNumber(calibration.brixDensity, 1)} °Bx",
                    subtitle = "${formatScientificNumber(calibration.solutionDensityGPerCm3, 3)} g/cm³",
                    accent = NectarAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoMetricTile(
                    title = "Micro-Weight",
                    value = "${formatScientificNumber(calibration.currentSugarMicrograms, 0)} µg",
                    subtitle = "${formatScientificNumber(calibration.valueForUnit(selectedUnit), selectedUnit.decimals)} ${selectedUnit.symbol} active",
                    accent = ChlorophyllEmerald,
                    modifier = Modifier.weight(1f)
                )
                InfoMetricTile(
                    title = "Bitterness & pH",
                    value = "${formatScientificNumber(food.bitternessIndex, 1)}/10",
                    subtitle = "Natural pH ${formatScientificNumber(food.acidityPh, 1)} • ${(food.waterFraction * 100).toInt()}% water",
                    accent = calibration.phaseState.accentColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Database Summary
            if (!food.wikipediaSummary.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassCard)
                        .border(1.dp, GlassStroke, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Biotech,
                            contentDescription = null,
                            tint = NectarAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = food.source,
                            style = MaterialTheme.typography.titleMedium,
                            color = MintInk
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = food.wikipediaSummary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MintInk.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InfoMetricTile(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(GlassCard)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = accent
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MintInk,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = SageSecondary,
            maxLines = 1
        )
    }
}
