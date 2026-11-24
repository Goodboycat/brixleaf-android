package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SugarUnit
import com.example.ui.components.AnimatedNumberText
import com.example.ui.components.PersonalReportSheet
import com.example.ui.components.PlantInfoOverlayScreen
import com.example.ui.components.QuickOfflineEntryDialog
import com.example.ui.components.SearchFoodSheet
import com.example.ui.components.SpatialStageCanvas
import com.example.ui.components.TitrationDiagramCard
import com.example.ui.components.formatScientificNumber
import com.example.ui.theme.ChlorophyllEmerald
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassCardElevated
import com.example.ui.theme.GlassStroke
import com.example.ui.theme.MintInk
import com.example.ui.theme.NectarAmber
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SageSecondary

@Composable
fun BrixLeafMainScreen(
    viewModel: MainViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        containerColor = ObsidianVoid,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ObsidianVoid, ObsidianSurface, ObsidianVoid)
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Top Search Bar + Quick Offline Entry + Personal Report
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GlassCard)
                            .border(1.dp, GlassStroke, RoundedCornerShape(16.dp))
                            .clickable(onClick = viewModel::openSearchSheet)
                            .padding(horizontal = 14.dp)
                            .testTag("open_search_bar")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search plants and foods",
                            tint = ChlorophyllEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.selectedFood.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MintInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = viewModel::openQuickEntry,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GlassCardElevated)
                            .border(1.dp, ChlorophyllEmerald.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .testTag("quick_entry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Quick offline entry",
                            tint = ChlorophyllEmerald
                        )
                    }

                    IconButton(
                        onClick = viewModel::openReportSheet,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GlassCardElevated)
                            .border(1.dp, NectarAmber.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .testTag("personal_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Assessment,
                            contentDescription = "Personal report and export",
                            tint = NectarAmber
                        )
                    }
                }

                // 2. Simple Direct Answer Header Card ("User wants to know how much sugar? Here is the answer.")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    state.calibration.phaseState.accentColor.copy(alpha = 0.16f),
                                    GlassCard.copy(alpha = 0.92f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            state.calibration.phaseState.accentColor.copy(alpha = 0.45f),
                            RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.selectedFood.name.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = state.calibration.phaseState.accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${formatScientificNumber(state.servingGrams, 0)} g",
                            style = MaterialTheme.typography.labelSmall,
                            color = SageSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedNumberText(
                            targetValue = state.calibration.valueForUnit(state.selectedUnit),
                            decimals = state.selectedUnit.decimals,
                            suffix = state.selectedUnit.symbol,
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = MintInk
                        )
                        Text(
                            text = "${formatScientificNumber(state.calibration.currentTeaspoons, 2)} tsp",
                            style = MaterialTheme.typography.titleMedium,
                            color = NectarAmber,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // 3. Interactive 3D Drag & Multi-Touch Spatial Diagram with Floating Photo Orb + Radial Lines
                SpatialStageCanvas(
                    calibration = state.calibration,
                    selectedUnit = state.selectedUnit,
                    onCycleUnit = viewModel::cycleMeasurementUnit,
                    onSnapIdeal = viewModel::snapToIdealSugar,
                    onOpenInfoScreen = viewModel::openInfoScreen,
                    onOpenSearchSheet = viewModel::openSearchSheet,
                    modifier = Modifier.height(355.dp)
                )

                // 4. Measurement Unit Switcher Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SugarUnit.entries.forEach { unit ->
                        val selected = unit == state.selectedUnit
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (selected) ChlorophyllEmerald.copy(alpha = 0.22f)
                                    else GlassCard
                                )
                                .border(
                                    1.dp,
                                    if (selected) ChlorophyllEmerald else GlassStroke,
                                    RoundedCornerShape(50)
                                )
                                .clickable { viewModel.selectMeasurementUnit(unit) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("unit_chip_${unit.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${unit.symbol} (${unit.label})",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) ChlorophyllEmerald else SageSecondary
                            )
                        }
                    }
                }

                // 5. Serving Quantity & Synchronous Sugar Balance Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(GlassCard)
                        .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Quantity: ${formatScientificNumber(state.servingGrams, 0)} g",
                        style = MaterialTheme.typography.titleMedium,
                        color = MintInk
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(50.0, 100.0, 250.0, 500.0).forEach { preset ->
                            val active = kotlin.math.abs(state.servingGrams - preset) < 1.0
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (active) ChlorophyllEmerald.copy(alpha = 0.22f) else GlassCardElevated)
                                    .border(
                                        1.dp,
                                        if (active) ChlorophyllEmerald else GlassStroke,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setServingGrams(preset) }
                                    .padding(vertical = 8.dp)
                                    .testTag("serving_preset_${preset.toInt()}")
                            ) {
                                Text(
                                    text = "${preset.toInt()} g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) ChlorophyllEmerald else SageSecondary
                                )
                            }
                        }
                    }

                    Slider(
                        value = state.servingGrams.toFloat(),
                        onValueChange = { viewModel.setServingGrams(it.toDouble()) },
                        valueRange = 20f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = ChlorophyllEmerald,
                            activeTrackColor = ChlorophyllEmerald
                        ),
                        modifier = Modifier.testTag("serving_size_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val maxSugarSlider = (state.calibration.idealAddedSugarGrams * 2.0).toFloat().coerceAtLeast(1f)
                    val currentSugarSlider = state.calibration.currentAddedSugarGrams.toFloat().coerceIn(0.05f, maxSugarSlider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sugar: ${formatScientificNumber(state.calibration.currentAddedSugarGrams, 2)} g",
                            style = MaterialTheme.typography.titleMedium,
                            color = MintInk,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (state.isFixedSugarMode) NectarAmber.copy(alpha = 0.2f) else GlassCardElevated)
                                    .border(
                                        1.dp,
                                        if (state.isFixedSugarMode) NectarAmber else GlassStroke,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable(onClick = viewModel::toggleFixedSugarMode)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("toggle_fixed_sugar_btn")
                            ) {
                                Text(
                                    text = if (state.isFixedSugarMode) "Fixed g" else "Auto-Sync",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.isFixedSugarMode) NectarAmber else SageSecondary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(GlassCardElevated)
                                    .border(1.dp, GlassStroke, RoundedCornerShape(50))
                                    .clickable(onClick = viewModel::snapToIdealSugar)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Reset to ideal sweet spot",
                                    tint = ChlorophyllEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ideal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ChlorophyllEmerald
                                )
                            }
                        }
                    }

                    Slider(
                        value = currentSugarSlider,
                        onValueChange = { viewModel.setAddedSugarGrams(it.toDouble()) },
                        valueRange = 0.05f..maxSugarSlider,
                        colors = SliderDefaults.colors(
                            thumbColor = state.calibration.phaseState.accentColor,
                            activeTrackColor = state.calibration.phaseState.accentColor
                        ),
                        modifier = Modifier.testTag("sugar_balance_slider")
                    )
                }

                // 6. Interactive Microscopic Sugar Density Diagram
                TitrationDiagramCard(
                    calibration = state.calibration,
                    onDensityScrub = viewModel::setDensityMultiplier
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Full-Screen Info Overlay opened when tapping the Information Container; closed by pressing X
            AnimatedVisibility(
                visible = state.showInfoScreen,
                enter = fadeIn() + scaleIn(initialScale = 0.94f),
                exit = fadeOut() + scaleOut(targetScale = 0.94f)
            ) {
                PlantInfoOverlayScreen(
                    calibration = state.calibration,
                    selectedUnit = state.selectedUnit,
                    onDensityScrub = viewModel::setDensityMultiplier,
                    onClose = viewModel::closeInfoScreen
                )
            }
        }
    }

    if (state.showSearchSheet) {
        SearchFoodSheet(
            query = state.searchQuery,
            localFoods = state.allSavedFoods,
            externalResults = state.externalSearchResults,
            isSearchingExternal = state.isSearchingExternal,
            onQueryChange = viewModel::updateSearchQuery,
            onSelectFood = viewModel::selectFood,
            onOpenQuickEntry = viewModel::openQuickEntry,
            onDismiss = viewModel::closeSearchSheet
        )
    }

    if (state.showQuickEntryDialog) {
        QuickOfflineEntryDialog(
            onDismiss = viewModel::closeQuickEntry,
            onSave = viewModel::addQuickOfflineFood
        )
    }

    if (state.showReportSheet) {
        PersonalReportSheet(
            profile = state.profile,
            currentCalibration = state.calibration,
            logs = state.calibrationLogs,
            onUpdateUserName = viewModel::updateUserName,
            onUpdateSweetnessBias = viewModel::updateSweetnessBias,
            onSaveCurrentLog = { viewModel.saveCurrentToPersonalReport() },
            onDeleteLog = viewModel::deleteLog,
            onExportReport = { format -> viewModel.exportReport(context, format) },
            onDismiss = viewModel::closeReportSheet
        )
    }
}
