package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.PlantFood
import com.example.ui.theme.ChlorophyllEmerald
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassCardElevated
import com.example.ui.theme.GlassStroke
import com.example.ui.theme.MintInk
import com.example.ui.theme.NectarAmber
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SageMuted
import com.example.ui.theme.SageSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFoodSheet(
    query: String,
    localFoods: List<PlantFood>,
    externalResults: List<PlantFood>,
    isSearchingExternal: Boolean,
    onQueryChange: (String) -> Unit,
    onSelectFood: (PlantFood) -> Unit,
    onOpenQuickEntry: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filteredLocal = remember(query, localFoods) {
        if (query.isBlank()) localFoods
        else {
            val q = query.trim().lowercase()
            localFoods.filter {
                it.name.lowercase().contains(q) ||
                    it.scientificName.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
            }
        }
    }

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
                    text = "Select Plant or Food",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MintInk
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close search",
                        tint = SageSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Type any plant or food (e.g. Aloe Vera, Matcha, Fig...)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SageMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = ChlorophyllEmerald
                    )
                },
                trailingIcon = {
                    if (isSearchingExternal) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = CrystalCyan
                        )
                    } else if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = SageSecondary
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChlorophyllEmerald,
                    unfocusedBorderColor = GlassStroke,
                    focusedContainerColor = GlassCard,
                    unfocusedContainerColor = GlassCard,
                    focusedTextColor = MintInk,
                    unfocusedTextColor = MintInk
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("food_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            val localIds = remember(filteredLocal) { filteredLocal.map { it.id }.toSet() }
            val localNames = remember(filteredLocal) { filteredLocal.map { it.name.lowercase() }.toSet() }
            val uniqueExternal = remember(externalResults, localIds, localNames) {
                externalResults.filter {
                    it.id !in localIds && it.name.lowercase() !in localNames
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (filteredLocal.isNotEmpty()) {
                    item(key = "header_local") {
                        Text(
                            text = "OFFLINE BOTANICAL CACHE (${filteredLocal.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChlorophyllEmerald
                        )
                    }
                    items(filteredLocal, key = { "local_${it.id}" }) { food ->
                        FoodResultRow(food = food, onClick = { onSelectFood(food) })
                    }
                }

                if (uniqueExternal.isNotEmpty()) {
                    item(key = "header_external") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = NectarAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WIKIPEDIA & OPEN FOOD FACTS LIVE (${uniqueExternal.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = NectarAmber
                            )
                        }
                    }
                    items(uniqueExternal, key = { "ext_${it.id}" }) { food ->
                        FoodResultRow(food = food, onClick = { onSelectFood(food) })
                    }
                }

                if (filteredLocal.isEmpty() && uniqueExternal.isEmpty() && !isSearchingExternal) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(GlassCard)
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "No exact match found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MintInk
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Use Offline Entry to calibrate \"$query\" immediately without internet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SageSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodResultRow(
    food: PlantFood,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassCardElevated.copy(alpha = 0.72f))
            .border(1.dp, GlassStroke, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("food_row_${food.id}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(GlassCard)
                .border(1.dp, ChlorophyllEmerald.copy(alpha = 0.4f), CircleShape)
        ) {
            if (!food.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = food.imageUrl,
                    contentDescription = food.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(50.dp).clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = null,
                    tint = ChlorophyllEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.titleMedium,
                color = MintInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${food.scientificName} • ${food.category}",
                style = MaterialTheme.typography.bodyMedium,
                color = SageSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "+${formatScientificNumber(food.optimalAddedSugarPer100g, 2)} g",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = ChlorophyllEmerald
            )
            Text(
                text = "${food.source} • 100g",
                style = MaterialTheme.typography.labelSmall,
                color = SageMuted
            )
        }
    }
}
