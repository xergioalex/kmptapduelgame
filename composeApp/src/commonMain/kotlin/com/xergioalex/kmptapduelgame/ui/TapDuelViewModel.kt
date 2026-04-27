package com.xergioalex.kmptapduelgame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xergioalex.kmptapduelgame.game.GameStatus
import com.xergioalex.kmptapduelgame.game.TapDuelGame
import com.xergioalex.kmptapduelgame.game.TapDuelState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TapDuelViewModel(
    private val game: TapDuelGame = TapDuelGame(),
) : ViewModel() {

    private val _state = MutableStateFlow(game.reset())
    val state: StateFlow<TapDuelState> = _state.asStateFlow()

    private val _countdown = MutableStateFlow<Int?>(null)
    val countdown: StateFlow<Int?> = _countdown.asStateFlow()

    private var countdownJob: Job? = null

    fun start() {
        if (_state.value.status == GameStatus.Playing) return
        countdownJob?.cancel()
        _state.value = game.startCountdown(_state.value)
        countdownJob = viewModelScope.launch {
            for (n in COUNTDOWN_FROM downTo 1) {
                _countdown.value = n
                delay(COUNTDOWN_TICK_MS)
            }
            _countdown.value = 0
            delay(COUNTDOWN_TICK_MS)
            _countdown.value = null
            _state.value = game.start(_state.value)
        }
    }

    fun tapPlayerOne() {
        _state.value = game.tapPlayerOne(_state.value)
    }

    fun tapPlayerTwo() {
        _state.value = game.tapPlayerTwo(_state.value)
    }

    fun reset() {
        countdownJob?.cancel()
        _countdown.value = null
        _state.value = game.reset()
    }

    private companion object {
        const val COUNTDOWN_FROM = 3
        const val COUNTDOWN_TICK_MS = 700L
    }
}
