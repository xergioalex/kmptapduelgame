package com.xergioalex.kmptapduelgame.game

import com.xergioalex.kmptapduelgame.game.TapDuelState.Companion.TAPS_TO_WIN

class TapDuelGame {

    fun reset(): TapDuelState = TapDuelState()

    fun startCountdown(state: TapDuelState): TapDuelState =
        if (state.status == GameStatus.Playing) state
        else reset().copy(status = GameStatus.CountingDown)

    fun start(state: TapDuelState): TapDuelState =
        state.copy(status = GameStatus.Playing)

    fun tapPlayerOne(state: TapDuelState): TapDuelState {
        if (state.status != GameStatus.Playing) return state
        return resolve(state.copy(playerOneTaps = state.playerOneTaps + 1))
    }

    fun tapPlayerTwo(state: TapDuelState): TapDuelState {
        if (state.status != GameStatus.Playing) return state
        return resolve(state.copy(playerTwoTaps = state.playerTwoTaps + 1))
    }

    private fun resolve(state: TapDuelState): TapDuelState {
        val netForOne = state.playerOneTaps - state.playerTwoTaps
        return when {
            netForOne >= TAPS_TO_WIN ->
                state.copy(winner = Player.One, status = GameStatus.Finished)
            netForOne <= -TAPS_TO_WIN ->
                state.copy(winner = Player.Two, status = GameStatus.Finished)
            else -> state
        }
    }
}
