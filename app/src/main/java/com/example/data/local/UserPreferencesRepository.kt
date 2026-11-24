package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.SugarUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "brixleaf_user_prefs")

data class PersonalProfilePrefs(
    val userName: String = "Personal Profile",
    val preferredUnit: SugarUnit = SugarUnit.GRAMS,
    val personalSweetnessBias: Double = 1.0,
    val lastFoodId: String = "aloe_vera"
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val PREFERRED_UNIT = stringPreferencesKey("preferred_unit")
        val SWEETNESS_BIAS = doublePreferencesKey("sweetness_bias")
        val LAST_FOOD_ID = stringPreferencesKey("last_food_id")
    }

    val profileFlow: Flow<PersonalProfilePrefs> = context.dataStore.data.map { prefs ->
        val unitName = prefs[Keys.PREFERRED_UNIT] ?: SugarUnit.GRAMS.name
        val unit = SugarUnit.entries.find { it.name == unitName } ?: SugarUnit.GRAMS
        PersonalProfilePrefs(
            userName = prefs[Keys.USER_NAME] ?: "Personal Profile",
            preferredUnit = unit,
            personalSweetnessBias = prefs[Keys.SWEETNESS_BIAS] ?: 1.0,
            lastFoodId = prefs[Keys.LAST_FOOD_ID] ?: "aloe_vera"
        )
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name.trim().ifEmpty { "Personal Profile" } }
    }

    suspend fun updatePreferredUnit(unit: SugarUnit) {
        context.dataStore.edit { it[Keys.PREFERRED_UNIT] = unit.name }
    }

    suspend fun updateSweetnessBias(bias: Double) {
        context.dataStore.edit { it[Keys.SWEETNESS_BIAS] = bias.coerceIn(0.75, 1.25) }
    }

    suspend fun updateLastFoodId(foodId: String) {
        context.dataStore.edit { it[Keys.LAST_FOOD_ID] = foodId }
    }
}
