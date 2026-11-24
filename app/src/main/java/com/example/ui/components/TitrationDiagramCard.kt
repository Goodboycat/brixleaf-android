package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.science.SugarCalibrationResult
import com.example.ui.theme.BitterCoral
import com.example.ui.theme.ChlorophyllEmerald
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassStroke
import com.example.ui.theme.MintInk
import com.example.ui.theme.NectarAmber
import com.example.ui.theme.SageMuted
import com.example.ui.theme.SageSecondary
import com.example.ui.theme.SyrupCaramel

@Composable
fun TitrationDiagramCard(
    calibration: SugarCalibrationResult,
    onDensityScrub: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val phaseColor by animateColorAsState(
        targetValue = calibration.phaseState.accentColor,
        animationSpec = tween(350),
        label = "diagram_phase_color"
    )
    val animatedRatio by animateFloatAsState(
        targetValue = (calibration.densityMultiplier / 2.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "diagram_marker_x"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(GlassCard.copy(alpha = 0.88f))
            .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SUGAR DENSITY (${formatScientificNumber(calibration.servingGrams, 0)} g)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageSecondary
                )
                Text(
                    text = "${formatScientificNumber(calibration.currentAddedSugarGrams, 2)} g / ${formatScientificNumber(calibration.idealAddedSugarGrams, 2)} g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MintInk
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(phaseColor.copy(alpha = 0.16f))
                    .border(1.dp, phaseColor.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(phaseColor)
                )
                Text(
                    text = calibration.phaseState.shortBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = phaseColor
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0.05f, 1.0f)
                        onDensityScrub((ratio * 2.0).coerceIn(0.1, 2.0))
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val ratio = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0.05f, 1.0f)
                        onDensityScrub((ratio * 2.0).coerceIn(0.1, 2.0))
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val topPad = 14f
                val bottomPad = 24f
                val graphH = h - topPad - bottomPad

                // Zone bands (Raw Bitter -> Crisp -> Ideal -> Nectar -> Syrup)
                val zones = listOf(
                    0.00f to 0.275f to BitterCoral,
                    0.275f to 0.425f to CrystalCyan,
                    0.425f to 0.60f to ChlorophyllEmerald,
                    0.60f to 0.825f to NectarAmber,
                    0.825f to 1.00f to SyrupCaramel
                )
                zones.forEach { (bounds, col) ->
                    val xStart = bounds.first * w
                    val xEnd = bounds.second * w
                    drawRect(
                        color = col.copy(alpha = 0.08f),
                        topLeft = Offset(xStart, topPad),
                        size = androidx.compose.ui.geometry.Size(xEnd - xStart, graphH)
                    )
                }

                // Grid lines
                for (i in 0..4) {
                    val y = topPad + (graphH * i / 4f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.06f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                val points = calibration.titrationCurve
                if (points.size > 1) {
                    val palatabilityPath = Path()
                    val fillPath = Path()
                    val maskedPath = Path()

                    points.forEachIndexed { idx, pt ->
                        val px = (pt.densityRatio / 2.0).toFloat().coerceIn(0f, 1f) * w
                        val py = topPad + graphH * (1f - (pt.palatabilityScore / 100.0).toFloat())
                        val my = topPad + graphH * (1f - (pt.bitternessMaskedPct / 100.0).toFloat())
                        if (idx == 0) {
                            palatabilityPath.moveTo(px, py)
                            fillPath.moveTo(px, topPad + graphH)
                            fillPath.lineTo(px, py)
                            maskedPath.moveTo(px, my)
                        } else {
                            palatabilityPath.lineTo(px, py)
                            fillPath.lineTo(px, py)
                            maskedPath.lineTo(px, my)
                        }
                    }
                    fillPath.lineTo(w, topPad + graphH)
                    fillPath.close()

                    // Fill under taste harmony curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                phaseColor.copy(alpha = 0.30f),
                                phaseColor.copy(alpha = 0.01f)
                            ),
                            startY = topPad,
                            endY = topPad + graphH
                        )
                    )

                    // Bitter neutralization dotted-feel secondary curve
                    drawPath(
                        path = maskedPath,
                        color = CrystalCyan.copy(alpha = 0.55f),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )

                    // Main taste balance curve
                    drawPath(
                        path = palatabilityPath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(BitterCoral, CrystalCyan, ChlorophyllEmerald, NectarAmber, SyrupCaramel)
                        ),
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )
                }

                // Ideal center vertical tick (1.0x = 50% width)
                drawLine(
                    color = ChlorophyllEmerald.copy(alpha = 0.45f),
                    start = Offset(w * 0.5f, topPad),
                    end = Offset(w * 0.5f, topPad + graphH),
                    strokeWidth = 2f
                )

                // Active operating marker
                val markerX = animatedRatio * w
                val markerY = topPad + graphH * (1f - (calibration.palatabilityScore / 100.0).toFloat())

                drawLine(
                    color = phaseColor.copy(alpha = 0.7f),
                    start = Offset(markerX, topPad),
                    end = Offset(markerX, topPad + graphH),
                    strokeWidth = 2.5f
                )
                drawCircle(
                    color = phaseColor.copy(alpha = 0.28f),
                    radius = 20f,
                    center = Offset(markerX, markerY)
                )
                drawCircle(
                    color = phaseColor,
                    radius = 9f,
                    center = Offset(markerX, markerY)
                )
                drawCircle(
                    color = MintInk,
                    radius = 4f,
                    center = Offset(markerX, markerY)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0 g", style = MaterialTheme.typography.labelSmall, color = BitterCoral)
            Text("${formatScientificNumber(calibration.idealAddedSugarGrams, 2)} g", style = MaterialTheme.typography.labelSmall, color = ChlorophyllEmerald)
            Text("${formatScientificNumber(calibration.idealAddedSugarGrams * 2.0, 2)} g", style = MaterialTheme.typography.labelSmall, color = SyrupCaramel)
        }
    }
}
