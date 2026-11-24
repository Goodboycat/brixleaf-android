package com.example.data.model

enum class SugarUnit(
    val symbol: String,
    val label: String,
    val decimals: Int
) {
    GRAMS("g", "Grams", 2),
    TEASPOONS("tsp", "Teaspoons", 2),
    MILLIGRAMS("mg", "Milligrams", 0),
    MICROGRAMS("µg", "Micrograms", 0),
    MICROMOLAR("µmol/mL", "Micro-Molar", 1),
    BRIX("°Bx", "Sugar Brix", 1);

    fun next(): SugarUnit {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }
}
