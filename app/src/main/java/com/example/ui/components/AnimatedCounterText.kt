package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun AnimatedNumberText(
    targetValue: Double,
    decimals: Int,
    suffix: String = "",
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    durationMillis: Int = 140
) {
    val animatedVal by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "animated_metric_number"
    )

    val formatted = formatScientificNumber(animatedVal.toDouble(), decimals)
    Text(
        text = if (suffix.isNotEmpty()) "$formatted $suffix" else formatted,
        style = style,
        color = color,
        maxLines = 1,
        modifier = modifier
    )
}

fun formatScientificNumber(value: Double, decimals: Int): String {
    return if (decimals <= 0) {
        NumberFormat.getIntegerInstance(Locale.US).format(value.roundToLong())
    } else {
        String.format(Locale.US, "%.${decimals}f", value)
    }
}
