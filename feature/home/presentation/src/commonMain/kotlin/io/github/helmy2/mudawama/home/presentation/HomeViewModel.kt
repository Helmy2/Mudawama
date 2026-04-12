package io.github.helmy2.mudawama.home.presentation

import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveAthkarCompletionUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveTasbeehDailyTotalUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveTasbeehGoalUseCase
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.location.Coordinates
import io.github.helmy2.mudawama.core.location.LocationError
import io.github.helmy2.mudawama.core.location.LocationProvider
import io.github.helmy2.mudawama.core.presentation.mvi.MviViewModel
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.habits.domain.usecase.DecrementHabitCountUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.IncrementHabitCountUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.ObserveHabitsWithTodayStatusUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.ToggleHabitCompletionUseCase
import io.github.helmy2.mudawama.home.presentation.model.HomeUiAction
import io.github.helmy2.mudawama.home.presentation.model.HomeUiEvent
import io.github.helmy2.mudawama.home.presentation.model.HomeUiState
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import io.github.helmy2.mudawama.prayer.domain.usecase.ObservePrayersForDateUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.SeedPrayerHabitsUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.ObserveQuranStateUseCase
import io.github.helmy2.mudawama.settings.domain.CalculationMethod
import io.github.helmy2.mudawama.settings.domain.LocationMode
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val observeHabitsUseCase: ObserveHabitsWithTodayStatusUseCase,
    private val toggleCompletionUseCase: ToggleHabitCompletionUseCase,
    private val incrementCountUseCase: IncrementHabitCountUseCase,
    private val decrementCountUseCase: DecrementHabitCountUseCase,
    private val observePrayersForDateUseCase: ObservePrayersForDateUseCase,
    private val seedPrayerHabitsUseCase: SeedPrayerHabitsUseCase,
    private val observeAthkarCompletionUseCase: ObserveAthkarCompletionUseCase,
    private val observeQuranStateUseCase: ObserveQuranStateUseCase,
    private val observeTasbeehGoalUseCase: ObserveTasbeehGoalUseCase,
    private val observeTasbeehDailyTotalUseCase: ObserveTasbeehDailyTotalUseCase,
    private val locationProvider: LocationProvider,
    private val timeProvider: TimeProvider,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val dispatcher: CoroutineDispatcher,
) : MviViewModel<HomeUiState, HomeUiAction, HomeUiEvent>(HomeUiState()) {

    private val today get() = timeProvider.logicalDate()
    private var currentCoordinates: Coordinates = MECCA_COORDINATES
    private var currentLocationMode: LocationMode = LocationMode.Gps
    private var currentCalculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE

    init {
        // Seed prayer habits first to ensure they exist before observing
        intent {
            seedPrayerHabitsUseCase()
        }
        observeSettings()
        observeHabits()
        observePrayers()
        observeAthkar()
        observeQuran()
        observeTasbeeh()
    }

    private fun observeSettings() {
        intent {
            observeSettingsUseCase().collectLatest { settings ->
                currentLocationMode = settings.locationMode
                currentCalculationMethod = settings.calculationMethod
                resolveLocation()
            }
        }
    }

    private suspend fun resolveLocation() {
        when (val mode = currentLocationMode) {
            is LocationMode.Gps -> {
                val location = locationProvider.getCurrentLocation()
                when (location) {
                    is Result.Success -> currentCoordinates = location.data
                    is Result.Failure -> currentCoordinates = MECCA_COORDINATES
                }
            }
            is LocationMode.Manual -> currentCoordinates = Coordinates(mode.latitude, mode.longitude)
        }
        observePrayers()
    }

    // ── Observation coroutines ────────────────────────────────────────────────

    private fun observeHabits() {
        intent {
            observeHabitsUseCase()
                .catch { reduce { copy(isHabitsLoading = false) } }
                .collect { habits ->
                    reduce { copy(habits = habits, isHabitsLoading = false) }
                }
        }
    }

    private fun observePrayers() {
        intent {
            observePrayersForDateUseCase(today, currentCoordinates, currentCalculationMethod)
                .catch { reduce { copy(isPrayerLoading = false, prayerTimesAvailable = false) } }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val prayers = result.data
                            val pending = prayers.filter { it.status == LogStatus.PENDING }
                            val allDone = prayers.isNotEmpty() && pending.isEmpty()
                            val nextPrayer = nextPendingFromNow(pending)
                            reduce {
                                copy(
                                    nextPrayerName = nextPrayer?.name,
                                    nextPrayerTime = nextPrayer?.timeString ?: "",
                                    isPrayerLoading = false,
                                    prayerTimesAvailable = true,
                                    allPrayersDone = allDone,
                                )
                            }
                        }
                        is Result.Failure ->
                            reduce { copy(isPrayerLoading = false, prayerTimesAvailable = false) }
                    }
                }
        }
    }

    /**
     * From the list of pending prayers, return the first one whose scheduled time
     * is still in the future (relative to now). Falls back to the first pending prayer
     * if all pending times are already past (e.g. times unavailable / "—").
     */
    private fun nextPendingFromNow(pending: List<PrayerWithStatus>): PrayerWithStatus? {
        if (pending.isEmpty()) return null
        val now = timeProvider.nowInstant()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time
        val nowMinutes = now.hour * 60 + now.minute
        val upcoming = pending.filter { prayer ->
            val parts = prayer.timeString.split(":")
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull() ?: return@filter false
                val m = parts[1].toIntOrNull() ?: return@filter false
                (h * 60 + m) > nowMinutes
            } else false
        }
        return upcoming.firstOrNull() ?: pending.first()
    }

    private fun observeAthkar() {
        intent {
            observeAthkarCompletionUseCase(today.toString())
                .catch { reduce { copy(isAthkarLoading = false) } }
                .collect { completionMap ->
                    val filtered = completionMap.filterKeys {
                        it == AthkarGroupType.MORNING || it == AthkarGroupType.EVENING
                    }
                    reduce { copy(athkarStatus = filtered, isAthkarLoading = false) }
                }
        }
    }

    private fun observeQuran() {
        intent {
            observeQuranStateUseCase(today)
                .catch { reduce { copy(isQuranLoading = false) } }
                .collect { quranState ->
                    reduce {
                        copy(
                            quranPagesReadToday = quranState.pagesReadToday,
                            quranGoalPages = quranState.goalPages,
                            isQuranLoading = false,
                        )
                    }
                }
        }
    }

    private fun observeTasbeeh() {
        intent {
            observeTasbeehGoalUseCase()
                .catch { reduce { copy(isTasbeehLoading = false) } }
                .collect { goal ->
                    reduce { copy(tasbeehGoal = goal.goalCount) }
                }
        }
        intent {
            observeTasbeehDailyTotalUseCase(today.toString())
                .catch { reduce { copy(isTasbeehLoading = false) } }
                .collect { total ->
                    reduce { copy(tasbeehDailyTotal = total.totalCount, isTasbeehLoading = false) }
                }
        }
    }

    // ── Action handler ────────────────────────────────────────────────────────

    override fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.ToggleCompletion ->
                exclusiveIntent("toggle_${action.habitId}") {
                    toggleCompletionUseCase(action.habitId)
                }
            is HomeUiAction.IncrementCount ->
                exclusiveIntent("increment_${action.habitId}") {
                    incrementCountUseCase(action.habitId)
                }
            is HomeUiAction.DecrementCount ->
                exclusiveIntent("decrement_${action.habitId}") {
                    decrementCountUseCase(action.habitId)
                }
            is HomeUiAction.PrayerCardTapped ->
                intent { emitEvent(HomeUiEvent.Navigate.ToPrayer) }
            is HomeUiAction.AthkarCardTapped ->
                intent { emitEvent(HomeUiEvent.Navigate.ToAthkar) }
            is HomeUiAction.QuranCardTapped ->
                intent { emitEvent(HomeUiEvent.Navigate.ToQuran) }
            is HomeUiAction.TasbeehCardTapped ->
                intent { emitEvent(HomeUiEvent.Navigate.ToTasbeeh) }
            is HomeUiAction.SettingsIconTapped ->
                intent { emitEvent(HomeUiEvent.Navigate.ToSettings) }
            is HomeUiAction.HabitsViewAllTapped ->
                intent { emitEvent(HomeUiEvent.Navigate.ToHabits) }
            is HomeUiAction.QiblaCardTapped ->
                intent { emitEvent(HomeUiEvent.Navigate.ToQibla) }
        }
    }

    companion object {
        private val MECCA_COORDINATES = Coordinates(21.3891, 39.8579)
    }
}
