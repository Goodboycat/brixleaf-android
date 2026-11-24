package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BitterCoral
import com.example.ui.theme.ChlorophyllEmerald
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.NectarAmber
import com.example.ui.theme.SyrupCaramel

enum class SugarPhaseState(
    val title: String,
    val shortBadge: String,
    val plainVerdict: String,
    val microscopicMeaning: String,
    val accentColor: Color
) {
    RAW_BITTER(
        title = "Raw & Bitter",
        shortBadge = "BITTER",
        plainVerdict = "Too sharp or bitter for daily taste",
        microscopicMeaning = "T2R bitter receptors dominate unbound; sucrose concentration below masking threshold.",
        accentColor = BitterCoral
    ),
    HERBAL_EDGE(
        title = "Herbal Edge",
        shortBadge = "CRISP",
        plainVerdict = "Lightly sweetened, botanical tang remains",
        microscopicMeaning = "Partial T1R2/T1R3 sweet receptor occupation; subtle astringency persists.",
        accentColor = CrystalCyan
    ),
    BALANCED_SWEET(
        title = "Balanced Harmony",
        shortBadge = "IDEAL",
        plainVerdict = "Exact sweet spot — smooth & naturally pleasant",
        microscopicMeaning = "Optimal sucrose molarity neutralizes bitter/acid molecules without masking plant aroma.",
        accentColor = ChlorophyllEmerald
    ),
    NECTAR_RICH(
        title = "Nectar Rich",
        shortBadge = "NECTAR",
        plainVerdict = "Lush dessert sweetness with velvety finish",
        microscopicMeaning = "Elevated osmotic sugar density creates silky mouthfeel and full bitterness suppression.",
        accentColor = NectarAmber
    ),
    SYRUP_SATURATED(
        title = "Syrup Saturated",
        shortBadge = "SYRUP",
        plainVerdict = "Very dense & syrupy — near crystal saturation",
        microscopicMeaning = "High sucrose refractive density slows volatile aroma release; hyper-sweet dominance.",
        accentColor = SyrupCaramel
    )
}
