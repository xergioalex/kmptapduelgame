package com.xergioalex.kmptapduelgame.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xergioalex.kmptapduelgame.game.GameStatus
import com.xergioalex.kmptapduelgame.game.Player
import com.xergioalex.kmptapduelgame.game.TapDuelState
import kmptapduelgame.composeapp.generated.resources.Res
import kmptapduelgame.composeapp.generated.resources.action_play_again
import kmptapduelgame.composeapp.generated.resources.action_reset
import kmptapduelgame.composeapp.generated.resources.action_start
import kmptapduelgame.composeapp.generated.resources.app_subtitle
import kmptapduelgame.composeapp.generated.resources.app_title
import kmptapduelgame.composeapp.generated.resources.countdown_go
import kmptapduelgame.composeapp.generated.resources.player_one_label
import kmptapduelgame.composeapp.generated.resources.player_one_taps_a11y
import kmptapduelgame.composeapp.generated.resources.player_one_zone_a11y
import kmptapduelgame.composeapp.generated.resources.player_two_label
import kmptapduelgame.composeapp.generated.resources.player_two_taps_a11y
import kmptapduelgame.composeapp.generated.resources.player_two_zone_a11y
import kmptapduelgame.composeapp.generated.resources.ready_prompt
import kmptapduelgame.composeapp.generated.resources.taps_label
import kmptapduelgame.composeapp.generated.resources.winner_overlay_title
import org.jetbrains.compose.resources.stringResource

private const val MIN_WEIGHT = 0.001f
private const val P1_GOAL_FRACTION = 0.90f
private const val P2_GOAL_FRACTION = 0.10f

@Composable
fun TapDuelScreen(viewModel: TapDuelViewModel = viewModel { TapDuelViewModel() }) {
    val state by viewModel.state.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val animatedDivider by animateFloatAsState(
        targetValue = state.dividerPosition,
        animationSpec = tween(durationMillis = 80),
        label = "divider",
    )

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
            Header(
                showResetButton = state.status == GameStatus.Playing ||
                    state.status == GameStatus.CountingDown,
                onReset = viewModel::reset,
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val isWide = maxWidth >= 600.dp
                    if (isWide) {
                        HorizontalArena(
                            state = state,
                            dividerPosition = animatedDivider,
                            onTapPlayerOne = viewModel::tapPlayerOne,
                            onTapPlayerTwo = viewModel::tapPlayerTwo,
                        )
                    } else {
                        VerticalArena(
                            state = state,
                            dividerPosition = animatedDivider,
                            onTapPlayerOne = viewModel::tapPlayerOne,
                            onTapPlayerTwo = viewModel::tapPlayerTwo,
                        )
                    }
                    GoalMarkers(isHorizontal = isWide, modifier = Modifier.fillMaxSize())
                }

                ReadyOverlay(
                    visible = state.status == GameStatus.Ready,
                    onStart = viewModel::start,
                )
                CountdownOverlay(value = countdown)
                WinnerOverlay(
                    state = state,
                    onPlayAgain = viewModel::start,
                    onReset = viewModel::reset,
                )
            }
        }
    }
}

@Composable
private fun Header(showResetButton: Boolean, onReset: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.app_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(Res.string.app_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (showResetButton) {
            TextButton(
                onClick = onReset,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Text(stringResource(Res.string.action_reset))
            }
        }
    }
}

@Composable
private fun HorizontalArena(
    state: TapDuelState,
    dividerPosition: Float,
    onTapPlayerOne: () -> Unit,
    onTapPlayerTwo: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        PlayerZone(
            player = Player.One,
            taps = state.playerOneTaps,
            interactive = state.status == GameStatus.Playing,
            onTap = onTapPlayerOne,
            modifier = Modifier
                .weight(dividerPosition.coerceAtLeast(MIN_WEIGHT))
                .fillMaxHeight(),
        )
        VerticalDivider()
        PlayerZone(
            player = Player.Two,
            taps = state.playerTwoTaps,
            interactive = state.status == GameStatus.Playing,
            onTap = onTapPlayerTwo,
            modifier = Modifier
                .weight((1f - dividerPosition).coerceAtLeast(MIN_WEIGHT))
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun VerticalArena(
    state: TapDuelState,
    dividerPosition: Float,
    onTapPlayerOne: () -> Unit,
    onTapPlayerTwo: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PlayerZone(
            player = Player.Two,
            taps = state.playerTwoTaps,
            interactive = state.status == GameStatus.Playing,
            onTap = onTapPlayerTwo,
            modifier = Modifier
                .weight((1f - dividerPosition).coerceAtLeast(MIN_WEIGHT))
                .fillMaxWidth(),
        )
        HorizontalDivider()
        PlayerZone(
            player = Player.One,
            taps = state.playerOneTaps,
            interactive = state.status == GameStatus.Playing,
            onTap = onTapPlayerOne,
            modifier = Modifier
                .weight(dividerPosition.coerceAtLeast(MIN_WEIGHT))
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun PlayerZone(
    player: Player,
    taps: Int,
    interactive: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val (containerColor, contentColor, label, zoneA11y, tapsA11y) = when (player) {
        Player.One -> ZoneStyle(
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
            label = stringResource(Res.string.player_one_label),
            zoneA11y = stringResource(Res.string.player_one_zone_a11y),
            tapsA11y = stringResource(Res.string.player_one_taps_a11y, taps),
        )
        Player.Two -> ZoneStyle(
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
            label = stringResource(Res.string.player_two_label),
            zoneA11y = stringResource(Res.string.player_two_zone_a11y),
            tapsA11y = stringResource(Res.string.player_two_taps_a11y, taps),
        )
    }

    Box(
        modifier = modifier
            .background(containerColor)
            .then(
                if (interactive) {
                    Modifier.pointerInput(player) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            onTap()
                        }
                    }
                } else Modifier,
            )
            .semantics {
                role = Role.Button
                contentDescription = "$zoneA11y. $tapsA11y"
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                ),
                color = contentColor,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = taps.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 72.sp,
                ),
                color = contentColor,
            )
            Text(
                text = stringResource(Res.string.taps_label),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.75f),
            )
        }
    }
}

private data class ZoneStyle(
    val container: Color,
    val content: Color,
    val label: String,
    val zoneA11y: String,
    val tapsA11y: String,
)

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(4.dp)
            .background(MaterialTheme.colorScheme.onBackground),
    )
}

@Composable
private fun HorizontalDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(MaterialTheme.colorScheme.onBackground),
    )
}

@Composable
private fun GoalMarkers(isHorizontal: Boolean, modifier: Modifier = Modifier) {
    val playerOneColor = MaterialTheme.colorScheme.primary
    val playerTwoColor = MaterialTheme.colorScheme.error
    Canvas(modifier = modifier) {
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 14f), 0f)
        val strokeWidth = 4.dp.toPx()
        if (isHorizontal) {
            // Horizontal split: P1 on the left, P2 on the right.
            // P1's goal sits deep in P2's territory; P2's goal sits deep in P1's territory.
            val playerOneGoalX = size.width * P1_GOAL_FRACTION
            val playerTwoGoalX = size.width * P2_GOAL_FRACTION
            drawLine(
                color = playerOneColor,
                start = Offset(playerOneGoalX, 0f),
                end = Offset(playerOneGoalX, size.height),
                strokeWidth = strokeWidth,
                pathEffect = dashEffect,
            )
            drawLine(
                color = playerTwoColor,
                start = Offset(playerTwoGoalX, 0f),
                end = Offset(playerTwoGoalX, size.height),
                strokeWidth = strokeWidth,
                pathEffect = dashEffect,
            )
        } else {
            // Vertical split: P2 on top, P1 at the bottom.
            // P1's goal is near the top (the divider must move up to reach it).
            // P2's goal is near the bottom (the divider must move down to reach it).
            val playerOneGoalY = size.height * (1f - P1_GOAL_FRACTION)
            val playerTwoGoalY = size.height * (1f - P2_GOAL_FRACTION)
            drawLine(
                color = playerOneColor,
                start = Offset(0f, playerOneGoalY),
                end = Offset(size.width, playerOneGoalY),
                strokeWidth = strokeWidth,
                pathEffect = dashEffect,
            )
            drawLine(
                color = playerTwoColor,
                start = Offset(0f, playerTwoGoalY),
                end = Offset(size.width, playerTwoGoalY),
                strokeWidth = strokeWidth,
                pathEffect = dashEffect,
            )
        }
    }
}

@Composable
private fun ReadyOverlay(visible: Boolean, onStart: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.85f),
        exit = fadeOut() + scaleOut(targetScale = 0.85f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.padding(24.dp),
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.ready_prompt),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = onStart) {
                        Text(stringResource(Res.string.action_start))
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownOverlay(value: Int?) {
    AnimatedVisibility(
        visible = value != null,
        enter = fadeIn() + scaleIn(initialScale = 0.6f),
        exit = fadeOut() + scaleOut(targetScale = 1.4f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            val text = when (value) {
                null -> ""
                0 -> stringResource(Res.string.countdown_go)
                else -> value.toString()
            }
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                ),
            )
        }
    }
}

@Composable
private fun WinnerOverlay(
    state: TapDuelState,
    onPlayAgain: () -> Unit,
    onReset: () -> Unit,
) {
    val winner = state.winner
    AnimatedVisibility(
        visible = state.status == GameStatus.Finished && winner != null,
        enter = fadeIn() + scaleIn(initialScale = 0.85f),
        exit = fadeOut(),
    ) {
        if (winner != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                ElevatedCard(
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.padding(24.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        WinnerBadge(winner)
                        Text(
                            text = stringResource(
                                Res.string.winner_overlay_title,
                                stringResource(
                                    when (winner) {
                                        Player.One -> Res.string.player_one_label
                                        Player.Two -> Res.string.player_two_label
                                    },
                                ),
                            ),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            ScoreLine(
                                label = stringResource(Res.string.player_one_label),
                                taps = state.playerOneTaps,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            ScoreLine(
                                label = stringResource(Res.string.player_two_label),
                                taps = state.playerTwoTaps,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(onClick = onReset) {
                                Text(stringResource(Res.string.action_reset))
                            }
                            Button(onClick = onPlayAgain) {
                                Text(stringResource(Res.string.action_play_again))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WinnerBadge(winner: Player) {
    val color = when (winner) {
        Player.One -> MaterialTheme.colorScheme.primary
        Player.Two -> MaterialTheme.colorScheme.error
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = when (winner) {
                Player.One -> "1"
                Player.Two -> "2"
            },
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
        )
    }
}

@Composable
private fun ScoreLine(label: String, taps: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = taps.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
        )
    }
}
