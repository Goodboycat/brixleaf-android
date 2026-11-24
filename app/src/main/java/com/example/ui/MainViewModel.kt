package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BrixLeafDatabase
import com.example.data.local.PersonalProfilePrefs
import com.example.data.local.PrepopulateSeedData
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.CalibrationLog
import com.example.data.model.PlantFood
import com.example.data.model.SugarUnit
import com.example.data.remote.NetworkModule
import com.example.data.repository.PlantFoodRepository
import com.example.data.repository.ReportExporter
import com.example.data.repository.ReportFormat
import com.example.data.science.SugarCalibrationEngine
import com.example.data.science.SugarCalibrationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedFood: PlantFood = PrepopulateSeedData.initialFoods().first(),
    val calibration: SugarCalibrationResult = SugarCalibrationEngine.calculate(
        PrepopulateSeedData.initialFoods().first(),
        100.0,
        1.0
    ),
    val servingGrams: Double = 100.0,
    val densityMultiplier: Double = 1.0,
    val isFixedSugarMode: Boolean = false,
    val selectedUnit: SugarUnit = SugarUnit.GRAMS,
    val profile: PersonalProfilePrefs = PersonalProfilePrefs(),
    val allSavedFoods: List<PlantFood> = emptyList(),
    val calibrationLogs: List<CalibrationLog> = emptyList(),
    val searchQuery: String = "",
    val externalSearchResults: List<PlantFood> = emptyList(),
    val isSearchingExternal: Boolean = false,
    val showSearchSheet: Boolean = false,
    val showInfoScreen: Boolean = false,
    val showQuickEntryDialog: Boolean = false,
    val showReportSheet: Boolean = false,
    val toastMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BrixLeafDatabase.getInstance(application)
    private val repository = PlantFoodRepository(
        plantFoodDao = database.plantFoodDao(),
        calibrationLogDao = database.calibrationLogDao(),
        wikipediaApi = NetworkModule.wikipediaApi,
        openFoodFactsApi = NetworkModule.openFoodFactsApi
    )
    private val prefsRepo = UserPreferencesRepository(application)

    private val _selectedFood = MutableStateFlow(PrepopulateSeedData.initialFoods().first())
    private val _servingGrams = MutableStateFlow(100.0)
    private val _densityMultiplier = MutableStateFlow(1.0)
    private val _isFixedSugarMode = MutableStateFlow(false)
    private val _fixedSugarGrams = MutableStateFlow(6.42)
    private val _searchQuery = MutableStateFlow("")
    private val _externalResults = MutableStateFlow<List<PlantFood>>(emptyList())
    private val _isSearchingExternal = MutableStateFlow(false)
    private val _showSearchSheet = MutableStateFlow(false)
    private val _showInfoScreen = MutableStateFlow(false)
    private val _showQuickEntryDialog = MutableStateFlow(false)
    private val _showReportSheet = MutableStateFlow(false)
    private val _toastMessage = MutableStateFlow<String?>(null)

    private var searchJob: Job? = null

    private val syncParamsFlow = combine(
        _servingGrams,
        _densityMultiplier,
        _isFixedSugarMode,
        _fixedSugarGrams
    ) { serving, mult, fixedMode, fixedGrams ->
        SyncParams(serving, mult, fixedMode, fixedGrams)
    }

    private val coreFlow = combine(
        _selectedFood,
        syncParamsFlow,
        prefsRepo.profileFlow
    ) { food, sync, profile ->
        val idealForServing = (food.optimalAddedSugarPer100g * (sync.servingGrams / 100.0) * profile.personalSweetnessBias)
            .coerceAtLeast(0.05)
        val effectiveMultiplier = if (sync.isFixedSugarMode) {
            (sync.fixedSugarGrams / idealForServing).coerceIn(0.05, 2.2)
        } else {
            sync.densityMultiplier
        }
        val result = SugarCalibrationEngine.calculate(
            food = food,
            servingGrams = sync.servingGrams,
            densityMultiplier = effectiveMultiplier,
            personalSweetnessBias = profile.personalSweetnessBias
        )
        CoreBundle(food, result, profile, sync.servingGrams, effectiveMultiplier, sync.isFixedSugarMode)
    }

    private val listsFlow = combine(
        repository.allFoodsFlow,
        repository.calibrationLogsFlow,
        _searchQuery,
        _externalResults,
        _isSearchingExternal
    ) { foods, logs, query, external, searching ->
        ListsBundle(foods, logs, query, external, searching)
    }

    private val modalsFlow = combine(
        _showSearchSheet,
        _showInfoScreen,
        _showQuickEntryDialog,
        _showReportSheet,
        _toastMessage
    ) { search, info, quick, report, toast ->
        ModalsBundle(search, info, quick, report, toast)
    }

    val uiState: StateFlow<MainUiState> = combine(
        coreFlow,
        listsFlow,
        modalsFlow
    ) { core, lists, modals ->
        MainUiState(
            selectedFood = core.food,
            calibration = core.result,
            servingGrams = core.servingGrams,
            densityMultiplier = core.effectiveMultiplier,
            isFixedSugarMode = core.isFixedSugarMode,
            selectedUnit = core.profile.preferredUnit,
            profile = core.profile,
            allSavedFoods = lists.foods,
            calibrationLogs = lists.logs,
            searchQuery = lists.query,
            externalSearchResults = lists.external,
            isSearchingExternal = lists.searching,
            showSearchSheet = modals.search,
            showInfoScreen = modals.info,
            showQuickEntryDialog = modals.quick,
            showReportSheet = modals.report,
            toastMessage = modals.toast
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            val initial = repository.getFoodById("aloe_vera")
                ?: PrepopulateSeedData.initialFoods().first()
            _selectedFood.value = initial
            _fixedSugarGrams.value = initial.optimalAddedSugarPer100g
            enrichFoodAsync(initial)
        }
    }

    private fun enrichFoodAsync(food: PlantFood) {
        viewModelScope.launch {
            val enriched = repository.enrichFromWikipediaIfNeeded(food)
            if (_selectedFood.value.id == enriched.id) {
                _selectedFood.value = enriched
            }
        }
    }

    fun selectFood(food: PlantFood) {
        viewModelScope.launch {
            repository.saveCustomOrExternalFood(food)
            prefsRepo.updateLastFoodId(food.id)
            _selectedFood.value = food
            _isFixedSugarMode.value = false
            _densityMultiplier.value = 1.0
            _fixedSugarGrams.value = food.optimalAddedSugarPer100g * (_servingGrams.value / 100.0)
            _showSearchSheet.value = false
            enrichFoodAsync(food)
        }
    }

    fun setServingGrams(grams: Double) {
        val safeGrams = grams.coerceIn(10.0, 2000.0)
        _servingGrams.value = safeGrams
        if (!_isFixedSugarMode.value) {
            val currentIdeal = uiState.value.calibration.food.optimalAddedSugarPer100g *
                (safeGrams / 100.0) * uiState.value.profile.personalSweetnessBias
            _fixedSugarGrams.value = currentIdeal * _densityMultiplier.value
        }
    }

    fun setAddedSugarGrams(grams: Double) {
        val ideal = uiState.value.calibration.idealAddedSugarGrams.coerceAtLeast(0.05)
        val safeSugar = grams.coerceIn(0.05, ideal * 2.0)
        _fixedSugarGrams.value = safeSugar
        _densityMultiplier.value = (safeSugar / ideal).coerceIn(0.05, 2.0)
    }

    fun setDensityMultiplier(multiplier: Double) {
        val safeMult = multiplier.coerceIn(0.05, 2.0)
        _densityMultiplier.value = safeMult
        _fixedSugarGrams.value = uiState.value.calibration.idealAddedSugarGrams * safeMult
    }

    fun toggleFixedSugarMode() {
        val next = !_isFixedSugarMode.value
        if (next) {
            _fixedSugarGrams.value = uiState.value.calibration.currentAddedSugarGrams
        } else {
            _densityMultiplier.value = 1.0
        }
        _isFixedSugarMode.value = next
    }

    fun snapToIdealSugar() {
        _isFixedSugarMode.value = false
        _densityMultiplier.value = 1.0
        _fixedSugarGrams.value = uiState.value.calibration.idealAddedSugarGrams
    }

    fun cycleMeasurementUnit() {
        viewModelScope.launch {
            val nextUnit = uiState.value.selectedUnit.next()
            prefsRepo.updatePreferredUnit(nextUnit)
        }
    }

    fun selectMeasurementUnit(unit: SugarUnit) {
        viewModelScope.launch {
            prefsRepo.updatePreferredUnit(unit)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _externalResults.value = emptyList()
            _isSearchingExternal.value = false
            return
        }
        searchJob = viewModelScope.launch {
            delay(360)
            _isSearchingExternal.value = true
            val results = repository.searchExternalDatabases(query)
            _externalResults.value = results
            _isSearchingExternal.value = false
        }
    }

    fun addQuickOfflineFood(
        name: String,
        category: String,
        bitterness: Double,
        naturalSugarPer100g: Double,
        acidityPh: Double,
        servingGrams: Double
    ) {
        val cleanName = name.trim().ifEmpty { "Custom Plant" }
        val slug = "custom_${cleanName.lowercase().replace(Regex("[^a-z0-9]+"), "_")}_${System.currentTimeMillis() % 10000}"
        val created = SugarCalibrationEngine.estimateProfileFromExternalFood(
            id = slug,
            name = cleanName,
            scientificName = "Personal Offline Entry",
            category = category.ifBlank { "Custom Entry" },
            naturalSugarPer100g = naturalSugarPer100g,
            summary = "Offline quick entry logged directly on device. Natural sugar: ${"%.1f".format(naturalSugarPer100g)}g/100g, pH ${"%.1f".format(acidityPh)}.",
            imageUrl = null,
            wikipediaTitle = cleanName.replace(" ", "_"),
            source = "Personal"
        ).copy(
            bitternessIndex = bitterness.coerceIn(0.5, 10.0),
            acidityPh = acidityPh.coerceIn(2.0, 8.0),
            isUserCreated = true
        )
        val targetTotalSweetness = (2.8 + created.bitternessIndex * 0.72 + (6.2 - created.acidityPh).coerceAtLeast(0.0) * 0.85)
        val optimalAdded = (targetTotalSweetness - created.naturalSugarPer100g * 0.75).coerceIn(0.5, 18.0)
        val optimalBrix = ((created.naturalSugarPer100g + optimalAdded) / (100.0 + optimalAdded) * 100.0)
        val finalFood = created.copy(
            optimalAddedSugarPer100g = optimalAdded,
            optimalBrix = optimalBrix,
            microscopicNote = "Offline calibration for $cleanName: ${"%.2f".format(optimalAdded)}g sucrose per 100g neutralizes bitterness ${"%.1f".format(bitterness)}/10."
        )

        viewModelScope.launch {
            repository.saveCustomOrExternalFood(finalFood)
            val safeServing = servingGrams.coerceIn(10.0, 2000.0)
            _servingGrams.value = safeServing
            _selectedFood.value = finalFood
            _isFixedSugarMode.value = false
            _densityMultiplier.value = 1.0
            _fixedSugarGrams.value = optimalAdded * (safeServing / 100.0)
            _showQuickEntryDialog.value = false
            _toastMessage.value = "Saved '$cleanName' offline & calibrated!"
            enrichFoodAsync(finalFood)
        }
    }

    fun saveCurrentToPersonalReport(note: String = "") {
        val current = uiState.value.calibration
        viewModelScope.launch {
            repository.saveCalibrationLog(
                CalibrationLog(
                    foodId = current.food.id,
                    foodName = current.food.name,
                    servingGrams = current.servingGrams,
                    recommendedSugarGrams = current.idealAddedSugarGrams,
                    userAdjustedSugarGrams = current.currentAddedSugarGrams,
                    brixDensity = current.brixDensity,
                    micromolesPerMl = current.molarityMicromolesPerMl,
                    phaseStateName = current.phaseState.title,
                    note = note.ifBlank { current.phaseState.plainVerdict }
                )
            )
            _toastMessage.value = "Saved ${current.food.name} to Personal Report"
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch { repository.deleteCalibrationLog(id) }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch { prefsRepo.updateUserName(name) }
    }

    fun updateSweetnessBias(bias: Double) {
        viewModelScope.launch { prefsRepo.updateSweetnessBias(bias) }
    }

    fun exportReport(context: Context, format: ReportFormat) {
        val state = uiState.value
        ReportExporter.shareReport(
            context = context,
            format = format,
            userName = state.profile.userName,
            currentResult = state.calibration,
            logs = state.calibrationLogs
        )
    }

    fun openSearchSheet() { _showSearchSheet.value = true }
    fun closeSearchSheet() { _showSearchSheet.value = false }
    fun openInfoScreen() { _showInfoScreen.value = true }
    fun closeInfoScreen() { _showInfoScreen.value = false }
    fun openQuickEntry() { _showQuickEntryDialog.value = true }
    fun closeQuickEntry() { _showQuickEntryDialog.value = false }
    fun openReportSheet() { _showReportSheet.value = true }
    fun closeReportSheet() { _showReportSheet.value = false }
    fun clearToast() { _toastMessage.value = null }

    private data class SyncParams(
        val servingGrams: Double,
        val densityMultiplier: Double,
        val isFixedSugarMode: Boolean,
        val fixedSugarGrams: Double
    )

    private data class CoreBundle(
        val food: PlantFood,
        val result: SugarCalibrationResult,
        val profile: PersonalProfilePrefs,
        val servingGrams: Double,
        val effectiveMultiplier: Double,
        val isFixedSugarMode: Boolean
    )

    private data class ListsBundle(
        val foods: List<PlantFood>,
        val logs: List<CalibrationLog>,
        val query: String,
        val external: List<PlantFood>,
        val searching: Boolean
    )

    private data class ModalsBundle(
        val search: Boolean,
        val info: Boolean,
        val quick: Boolean,
        val report: Boolean,
        val toast: String?
    )
}
