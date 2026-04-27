package com.xergioalex.kmptapduelgame.game

import com.xergioalex.kmptapduelgame.game.TapDuelState.Companion.INITIAL_POSITION
import com.xergioalex.kmptapduelgame.game.TapDuelState.Companion.TAPS_TO_WIN
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TapDuelGameTest {

    private val game = TapDuelGame()

    @Test
    fun resetReturnsCenteredReadyState() {
        val state = game.reset()
        assertEquals(INITIAL_POSITION, state.dividerPosition)
        assertEquals(0, state.playerOneTaps)
        assertEquals(0, state.playerTwoTaps)
        assertNull(state.winner)
        assertEquals(GameStatus.Ready, state.status)
    }

    @Test
    fun playerOneTapMovesDividerRight() {
        val playing = game.start(game.reset())
        val tapped = game.tapPlayerOne(playing)
        assertTrue(tapped.dividerPosition > playing.dividerPosition)
        assertEquals(1, tapped.playerOneTaps)
        assertEquals(0, tapped.playerTwoTaps)
    }

    @Test
    fun playerTwoTapMovesDividerLeft() {
        val playing = game.start(game.reset())
        val tapped = game.tapPlayerTwo(playing)
        assertTrue(tapped.dividerPosition < playing.dividerPosition)
        assertEquals(1, tapped.playerTwoTaps)
        assertEquals(0, tapped.playerOneTaps)
    }

    @Test
    fun tapsBeforeStartAreIgnored() {
        val ready = game.reset()
        val attempted = game.tapPlayerOne(ready)
        assertEquals(ready, attempted)
    }

    @Test
    fun tapsDuringCountdownAreIgnored() {
        val countingDown = game.startCountdown(game.reset())
        assertEquals(GameStatus.CountingDown, countingDown.status)
        val attempted = game.tapPlayerOne(countingDown)
        assertEquals(countingDown, attempted)
    }

    @Test
    fun playerOneWinsAfterEnoughNetTaps() {
        var state = game.start(game.reset())
        repeat(TAPS_TO_WIN) { state = game.tapPlayerOne(state) }
        assertEquals(GameStatus.Finished, state.status)
        assertEquals(Player.One, state.winner)
    }

    @Test
    fun playerTwoWinsAfterEnoughNetTaps() {
        var state = game.start(game.reset())
        repeat(TAPS_TO_WIN) { state = game.tapPlayerTwo(state) }
        assertEquals(GameStatus.Finished, state.status)
        assertEquals(Player.Two, state.winner)
    }

    @Test
    fun opponentTapsCancelOutAndPostponeWin() {
        var state = game.start(game.reset())
        repeat(TAPS_TO_WIN - 1) { state = game.tapPlayerOne(state) }
        state = game.tapPlayerTwo(state)
        assertNull(state.winner)
        assertEquals(GameStatus.Playing, state.status)
        // Net is now (TAPS_TO_WIN - 1) - 1 = TAPS_TO_WIN - 2 → need 2 more
        state = game.tapPlayerOne(state)
        assertNull(state.winner)
        state = game.tapPlayerOne(state)
        assertEquals(Player.One, state.winner)
    }

    @Test
    fun tapsAfterFinishedAreIgnored() {
        var state = game.start(game.reset())
        repeat(TAPS_TO_WIN) { state = game.tapPlayerOne(state) }
        val frozen = state
        assertEquals(frozen, game.tapPlayerOne(state))
        assertEquals(frozen, game.tapPlayerTwo(state))
    }

    @Test
    fun startCountdownAfterFinishResetsScores() {
        var state = game.start(game.reset())
        repeat(TAPS_TO_WIN) { state = game.tapPlayerOne(state) }
        assertNotNull(state.winner)

        val nextRound = game.startCountdown(state)
        assertEquals(GameStatus.CountingDown, nextRound.status)
        assertEquals(0, nextRound.playerOneTaps)
        assertEquals(0, nextRound.playerTwoTaps)
        assertEquals(INITIAL_POSITION, nextRound.dividerPosition)
        assertNull(nextRound.winner)
    }
}
