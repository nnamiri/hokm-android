package eu.amiri.hokm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.amiri.hokm.SoloGameViewModel
import eu.amiri.hokm.engine.GamePhase
import eu.amiri.hokm.engine.GameSnapshot
import eu.amiri.hokm.engine.Seat
import kotlinx.coroutines.delay

/**
 * The main table: opponents around the trick area, own hand at the bottom,
 * overlays for the declaration, discarding, hand results and the pause menu.
 * Port of the iOS `GameTableView`.
 */
@Composable
fun GameScreen(vm: SoloGameViewModel) {
    val snapshot = vm.snapshot ?: return

    // First-game spotlight tutorial, one focus field at a time.
    val frames = remember { CoachFrames() }
    var coachStep by remember { mutableStateOf<CoachStep?>(null) }
    val coachDone = remember { mutableSetOf<CoachStep>() }
    var coachTick by remember { mutableStateOf(0) }

    LaunchedEffect(snapshot, vm.paused, coachTick) {
        if (coachStep != null) return@LaunchedEffect
        if (vm.hasSeenTableCoach(snapshot.playerCount)) return@LaunchedEffect
        if (snapshot.phase is GamePhase.GameOver) {
            vm.markTableCoachSeen(snapshot.playerCount)
            return@LaunchedEffect
        }
        val steps = if (snapshot.playerCount == 2) {
            listOf(CoachStep.HEADER, CoachStep.DRAW, CoachStep.HAND)
        } else {
            listOf(CoachStep.HEADER, CoachStep.HAND)
        }
        val next = steps.firstOrNull { it !in coachDone && coachAllowed(it, snapshot, vm.paused) }
        if (next != null) {
            // Breathing room before a follow-up step, as on iOS.
            if (coachTick > 0) delay(500)
            coachStep = next
        } else if (steps.all { it in coachDone }) {
            vm.markTableCoachSeen(snapshot.playerCount)
        }
    }

    FeltBackground {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp)
                    .onGloballyPositioned { frames.header = it.boundsInRoot() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PauseButton(onClick = vm::pause)
                ScoreHeader(
                    snapshot = snapshot,
                    hakemName = vm.name(snapshot.hakem),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(8.dp))

            // Top: the partner (4P) or the single opponent (2P)
            val top = topSeat(snapshot)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PlayerBadge(top, vm.name(top), snapshot)
            }

            // Middle: the draw area (2P) or the trick, flanked in 4P.
            Row(
                Modifier.fillMaxWidth().weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (snapshot.playerCount == 4) {
                    val left = seatAt(snapshot, TablePosition.LEFT)
                    Box(Modifier.width(76.dp), contentAlignment = Alignment.Center) {
                        PlayerBadge(left, vm.name(left), snapshot)
                    }
                }

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .onGloballyPositioned { frames.center = it.boundsInRoot() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (snapshot.phase == GamePhase.Drawing) {
                        DrawArea(
                            snapshot = snapshot,
                            opponentName = vm.name(top),
                            onTake = vm::takeCard,
                            onReject = vm::rejectCard,
                        )
                    } else {
                        TrickArea(snapshot, Modifier.fillMaxSize())
                    }
                }

                if (snapshot.playerCount == 4) {
                    val right = seatAt(snapshot, TablePosition.RIGHT)
                    Box(Modifier.width(76.dp), contentAlignment = Alignment.Center) {
                        PlayerBadge(right, vm.name(right), snapshot)
                    }
                }
            }

            // Own row: badge, turn hint, fanned hand.
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                PlayerBadge(snapshot.seat, vm.name(snapshot.seat), snapshot, showCards = false)
                turnHint(snapshot, vm)?.let {
                    Text(
                        it,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            HandFan(
                cards = snapshot.hand,
                legalCards = snapshot.legalCards.toSet(),
                isMyTurn = snapshot.isMyTurn && snapshot.phase == GamePhase.Playing,
                onPlay = vm::play,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(158.dp)
                    .padding(bottom = 8.dp)
                    .onGloballyPositioned { frames.hand = it.boundsInRoot() },
            )
        }

        Overlays(snapshot, vm)

        val step = coachStep
        val frame = step?.let { frames[it] }
        if (step != null && frame != null) {
            CoachMarkOverlay(step.text, frame) {
                coachDone += step
                coachStep = null
                coachTick++
            }
        }
    }
}

/**
 * The pause control: a Material 3 tonal icon button, with the pause glyph
 * drawn as two rounded bars (the core icon set has no pause symbol).
 */
@Composable
private fun PauseButton(onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .semantics { contentDescription = De.PAUSE },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.35f),
            contentColor = Color.White,
        ),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(2) {
                Box(
                    Modifier
                        .size(width = 4.dp, height = 15.dp)
                        .background(Color.White, RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}

/**
 * A coach step may only appear in a calm moment (no modal overlay in the way)
 * and during the phase it explains.
 */
private fun coachAllowed(step: CoachStep, snapshot: GameSnapshot, paused: Boolean): Boolean {
    if (paused) return false
    if (snapshot.phase == GamePhase.ChoosingTrump && snapshot.iAmHakem) return false
    if (snapshot.needsDiscard) return false
    if (snapshot.phase is GamePhase.HandOver || snapshot.phase is GamePhase.GameOver) return false

    return when (step) {
        CoachStep.HEADER -> true
        CoachStep.DRAW -> snapshot.phase == GamePhase.Drawing
        CoachStep.HAND -> snapshot.phase == GamePhase.Playing
    }
}

@Composable
private fun Overlays(snapshot: GameSnapshot, vm: SoloGameViewModel) {
    if (vm.paused) {
        DimmedOverlay { PauseMenu(vm.stats, onResume = vm::resume, onLeave = vm::leaveTable) }
        return
    }
    when (val phase = snapshot.phase) {
        GamePhase.ChoosingTrump -> if (snapshot.iAmHakem) DimmedOverlay {
            TrumpPicker(snapshot.hand, vm::chooseMode)
        }

        GamePhase.Discarding -> if (snapshot.needsDiscard) DimmedOverlay {
            DiscardPicker(snapshot.hand, hakemDeclaration(snapshot, vm), vm::discard)
        }

        is GamePhase.HandOver -> DimmedOverlay {
            HandOverBanner(phase.winner, snapshot, vm::nextHand)
        }

        is GamePhase.GameOver -> DimmedOverlay {
            GameOverBanner(phase.winner, snapshot, vm::leaveTable)
        }

        else -> Unit
    }
}

/** 4P: the partner across the table. 2P: the single opponent. */
private fun topSeat(snapshot: GameSnapshot): Seat =
    if (snapshot.playerCount == 2) {
        if (snapshot.seat == Seat.SOUTH) Seat.WEST else Seat.SOUTH
    } else {
        seatAt(snapshot, TablePosition.TOP)
    }

private fun seatAt(snapshot: GameSnapshot, position: TablePosition): Seat =
    Seat.entries.firstOrNull { TablePosition.of(it, snapshot.seat) == position } ?: snapshot.seat

private fun turnHint(snapshot: GameSnapshot, vm: SoloGameViewModel): String? = when {
    snapshot.phase == GamePhase.Playing && snapshot.isMyTurn -> De.YOUR_TURN
    snapshot.phase == GamePhase.Playing ->
        De.turnOf(vm.name(snapshot.turn ?: snapshot.seat))
    snapshot.phase == GamePhase.ChoosingTrump && !snapshot.iAmHakem ->
        De.choosingTrumpBy(vm.name(snapshot.hakem))
    snapshot.phase == GamePhase.Discarding && !snapshot.needsDiscard &&
        snapshot.pendingDiscards.isNotEmpty() -> De.DISCARD_WAITING
    else -> null
}

/**
 * When a bot is the hakem, spell out its declaration so the player knows it
 * while discarding (2-player variant).
 */
private fun hakemDeclaration(snapshot: GameSnapshot, vm: SoloGameViewModel): String? {
    if (snapshot.iAmHakem) return null
    val choice = snapshot.trumpChoice ?: return null
    val hakem = vm.name(snapshot.hakem)
    // A suit is announced "as trump"; high/low has no trump.
    return if (choice.suit != null) {
        De.hakemChoseTrump(hakem, choice.germanText)
    } else {
        De.hakemChose(hakem, choice.germanText)
    }
}
