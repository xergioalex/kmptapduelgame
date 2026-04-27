package com.xergioalex.kmptapduelgame.game

import androidx.compose.runtime.Immutable

@Immutable
data class TapDuelState(
    val playerOneTaps: Int = 0,
    val playerTwoTaps: Int = 0,
    val winner: Player? = null,
    val status: GameStatus = GameStatus.Ready,
) {
    /** 0.0 = far left, 0.5 = center, 1.0 = far right. Derived from taps so it never drifts. */
    val dividerPosition: Float
        get() = (INITIAL_POSITION + (playerOneTaps - playerTwoTaps) * TAP_STEP).coerceIn(0f, 1f)

    val playerOneTerritory: Float get() = dividerPosition
    val playerTwoTerritory: Float get() = 1f - dividerPosition

    companion object {
        const val INITIAL_POSITION = 0.5f
        const val TAP_STEP = 0.02f
        /** Net taps required to push the divider into the opponent's win zone. */
        const val TAPS_TO_WIN = 20
    }
}
