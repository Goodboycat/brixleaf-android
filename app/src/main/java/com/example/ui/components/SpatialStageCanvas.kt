package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SpatialStageCanvas(
    calibration: SugarCalibrationResult,
    selectedUnit: SugarUnit,
    onCycleUnit: () -> Unit,
    onSnapIdeal: () -> Unit,
    onOpenInfoScreen: () -> Unit,
    onOpenSearchSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density

    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var stageTwistDeg by remember { mutableFloatStateOf(0f) }

    val smoothPanX by animateFloatAsState(
        targetValue = panX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.78f),
        label = "pan_x"
    )
    val smoothPanY by animateFloatAsState(
        targetValue = panY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.78f),
        label = "pan_y"
    )
    val smoothZoom by animateFloatAsState(
        targetValue = zoomScale,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.8f),
        label = "zoom"
    )
    val smoothTwist by animateFloatAsState(
        targetValue = stageTwistDeg,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "twist"
    )

    val phaseColor by animateColorAsState(
        targetValue = calibration.phaseState.accentColor,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "orb_phase_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "spatial_orbit")
    val floatPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "float_bob"
    )
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "line_pulse"
    )

    val isTransformed = kotlin.math.abs(panX) > 4f ||
        kotlin.math.abs(panY) > 4f ||
        kotlin.math.abs(zoomScale - 1f) > 0.04f ||
        kotlin.math.abs(stageTwistDeg) > 1.5f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = 0.14f),
                        GlassCard.copy(alpha = 0.65f),
                        ObsidianVoid.copy(alpha = 0.96f)
                    )
                )
            )
            .border(1.dp, GlassStroke.copy(alpha = 0.75f), RoundedCornerShape(28.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    panX = (panX + pan.x).coerceIn(-180f, 180f)
                    panY = (panY + pan.y).coerceIn(-140f, 140f)
                    zoomScale = (zoomScale * zoom).coerceIn(0.78f, 1.55f)
                    stageTwistDeg = (stageTwistDeg + rotation).coerceIn(-25f, 25f)
                }
            }
    ) {
        val stageW = constraints.maxWidth.toFloat()
        val stageH = constraints.maxHeight.toFloat()

        // 3D tilt reacts to drag position
        val tiltY = (smoothPanX / 180f) * 14f
        val tiltX = -(smoothPanY / 140f) * 14f

        val cardWidthDp = (maxWidth * 0.43f).coerceIn(138.dp, 174.dp)
        val orbSizeDp = (maxWidth * 0.36f).coerceIn(124.dp, 152.dp)

        // Subtle floating motion
        val bobY = sin(floatPhase) * 6f
        val center = Offset(stageW * 0.5f, stageH * 0.5f + bobY)

        // Anchor points inside the 4 satellite containers for radial connector lines
        val tlAnchor = Offset(stageW * 0.24f, stageH * 0.19f)
        val trAnchor = Offset(stageW * 0.76f, stageH * 0.19f)
        val blAnchor = Offset(stageW * 0.24f, stageH * 0.81f)
        val brAnchor = Offset(stageW * 0.76f, stageH * 0.81f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = smoothPanX
                    translationY = smoothPanY
                    scaleX = smoothZoom
                    scaleY = smoothZoom
                    rotationZ = smoothTwist
                    rotationX = tiltX
                    rotationY = tiltY
                    cameraDistance = 14f * density
                }
        ) {
            // Layer 1: Radial 3D grid, animated sugar crystal ring, and directional connector lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (size.width <= 0f || size.height <= 0f) return@Canvas
                // Subtle radar / scientific laboratory concentric rings
                val maxR = size.minDimension * 0.45f
                listOf(0.55f, 0.80f, 1.04f).forEach { factor ->
                    drawCircle(
                        color = phaseColor.copy(alpha = 0.08f),
                        radius = maxR * factor,
                        center = center,
                        style = Stroke(width = 1.2f)
                    )
                }

                // Orbiting microscopic sucrose crystal particles around plant orb
                val particleCount = (6 + (calibration.densityMultiplier * 8).toInt()).coerceIn(6, 20)
                val orbitRadius = (orbSizeDp.toPx() * 0.58f)
                for (i in 0 until particleCount) {
                    val angle = floatPhase + (i * (2f * PI.toFloat() / particleCount))
                    val px = center.x + cos(angle) * (orbitRadius + (i % 3) * 8f)
                    val py = center.y + sin(angle) * (orbitRadius + (i % 3) * 8f)
                    drawCircle(
                        color = phaseColor.copy(alpha = 0.65f),
                        radius = if (i % 2 == 0) 3.5f else 2.2f,
                        center = Offset(px, py)
                    )
                }

                // Draw glowing curved directional connector lines from center floating plant to each container
                val targets = listOf(
                    Triple(tlAnchor, ChlorophyllEmerald, 0.0f),
                    Triple(trAnchor, CrystalCyan, 0.25f),
                    Triple(blAnchor, phaseColor, 0.5f),
                    Triple(brAnchor, NectarAmber, 0.75f)
                )

                targets.forEach { (anchor, lineColor, phaseOffset) ->
                    val ctrlX = (center.x + anchor.x) * 0.5f
                    val ctrlY = center.y + (anchor.y - center.y) * 0.22f
                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        quadraticTo(ctrlX, ctrlY, anchor.x, anchor.y)
                    }

                    // Soft glow stroke
                    drawPath(
                        path = path,
                        color = lineColor.copy(alpha = 0.20f),
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    // Crisp dashed scientific telemetry line
                    drawPath(
                        path = path,
                        color = lineColor.copy(alpha = 0.72f),
                        style = Stroke(
                            width = 2.4f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = -pulseProgress * 40f)
                        )
                    )

                    // Traveling energy droplet along quadratic bezier
                    val t = (pulseProgress + phaseOffset) % 1f
                    val inv = 1f - t
                    val dotX = inv * inv * center.x + 2 * inv * t * ctrlX + t * t * anchor.x
                    val dotY = inv * inv * center.y + 2 * inv * t * ctrlY + t * t * anchor.y
                    drawCircle(
                        color = MintInk,
                        radius = 4.2f,
                        center = Offset(dotX, dotY)
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 7f,
                        center = Offset(dotX, dotY),
                        alpha = 0.35f
                    )

                    // Endpoint terminal port dot
                    drawCircle(
                        color = lineColor,
                        radius = 5.5f,
                        center = anchor
                    )
                }
            }

            // Layer 2: Four surrounding satellite 3D floating containers
            // 1. TOP-LEFT: Exact Sugar Needed (tap to cycle measurement units)
            SatelliteContainer(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp)
                    .width(cardWidthDp)
                    .testTag("sugar_answer_container")
                    .clickable(onClick = onCycleUnit),
                accentColor = ChlorophyllEmerald
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUGAR (${formatScientificNumber(calibration.servingGrams, 0)}g)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ChlorophyllEmerald
                    )
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = "Cycle unit",
                        tint = ChlorophyllEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedNumberText(
                    targetValue = calibration.valueForUnit(selectedUnit),
                    decimals = selectedUnit.decimals,
                    suffix = selectedUnit.symbol,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MintInk
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${formatScientificNumber(calibration.currentTeaspoons, 2)} tsp",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 2. TOP-RIGHT: Microscopic Scientific Metric (scaled mmol + concentration)
            SatelliteContainer(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = 12.dp)
                    .width(cardWidthDp)
                    .testTag("microscopic_metric_container"),
                accentColor = CrystalCyan
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MICROSCOPIC",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrystalCyan
                    )
                    Icon(
                        imageVector = Icons.Rounded.Biotech,
                        contentDescription = "Microscopic density",
                        tint = CrystalCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedNumberText(
                    targetValue = calibration.totalMillimoles,
                    decimals = 1,
                    suffix = "mmol",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MintInk
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${formatScientificNumber(calibration.molarityMicromolesPerMl, 0)} µmol/mL • ${formatScientificNumber(calibration.brixDensity, 1)} °Bx",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. BOTTOM-LEFT: Dynamic State based on Sugar Density (tap to snap to 100% Ideal)
            SatelliteContainer(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 12.dp)
                    .width(cardWidthDp)
                    .testTag("state_phase_container")
                    .clickable(onClick = onSnapIdeal),
                accentColor = phaseColor
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DENSITY STATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = phaseColor
                    )
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = "Sugar density state",
                        tint = phaseColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = calibration.phaseState.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MintInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${formatScientificNumber(calibration.currentAddedSugarGrams, 2)} / ${formatScientificNumber(calibration.idealAddedSugarGrams, 2)} g",
                    style = MaterialTheme.typography.labelSmall,
                    color = phaseColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 4. BOTTOM-RIGHT: Information Container (connected to Wikipedia/Food DB -> tap to open full Info screen!)
            SatelliteContainer(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 12.dp)
                    .width(cardWidthDp)
                    .testTag("information_container")
                    .clickable(onClick = onOpenInfoScreen),
                accentColor = NectarAmber
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INFO & WIKI DB",
                        style = MaterialTheme.typography.labelSmall,
                        color = NectarAmber
                    )
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Open plant scientific info",
                        tint = NectarAmber,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = calibration.food.scientificName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MintInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        tint = SageSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = calibration.food.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = NectarAmber,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Layer 3: Center 3D Floating Plant Photo Orb
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(0, bobY.roundToInt()) }
                    .size(orbSizeDp)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        ambientColor = phaseColor,
                        spotColor = phaseColor
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GlassCardElevated,
                                ObsidianVoid
                            )
                        )
                    )
                    .border(
                        width = 2.5.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                phaseColor,
                                CrystalCyan,
                                ChlorophyllEmerald,
                                NectarAmber,
                                phaseColor
                            )
                        ),
                        shape = CircleShape
                    )
                    .testTag("center_plant_orb")
                    .clickable(onClick = onOpenSearchSheet)
            ) {
                if (!calibration.food.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = calibration.food.imageUrl,
                        contentDescription = "${calibration.food.name} photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Eco,
                            contentDescription = calibration.food.name,
                            tint = phaseColor,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Bottom glass label overlay inside the orb
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ObsidianVoid.copy(alpha = 0.88f)
                                )
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = calibration.food.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MintInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Top-center subtle hint / reset view chip
        if (isTransformed) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(50))
                    .background(ObsidianVoid.copy(alpha = 0.85f))
                    .border(1.dp, ChlorophyllEmerald.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .clickable {
                        panX = 0f
                        panY = 0f
                        zoomScale = 1f
                        stageTwistDeg = 0f
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("reset_3d_view_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.CenterFocusStrong,
                    contentDescription = "Reset 3D canvas",
                    tint = ChlorophyllEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Reset 3D View",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintInk
                )
            }
        }
    }
}

@Composable
private fun SatelliteContainer(
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = accentColor.copy(alpha = 0.45f),
                spotColor = accentColor.copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GlassCardElevated.copy(alpha = 0.94f),
                        GlassCard.copy(alpha = 0.90f)
                    )
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.42f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        content()
    }
}
