package eu.amiri.hokm.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.amiri.hokm.SoloGameViewModel
import eu.amiri.hokm.engine.GamePhase
import eu.amiri.hokm.engine.GameSnapshot
import eu.amiri.hokm.engine.Seat

/**
 * The main table: opponents around the trick area, own hand at the bottom,
 * overlays for the declaration, discarding, hand results and the pause menu.
 * Port of the iOS `GameTableView`.
 */
@Composable
fun GameScreen(vm: SoloGameViewModel) {
    val snapshot = vm.snapshot ?: return

    FeltBackground {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "⏸",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 22.sp,
                    modifier = Modifier.clickable { vm.pause() }.padding(end = 10.dp),
                )
                ScoreHeader(snapshot, hakemName = name(snapshot, snapshot.hakem), modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            // Top: the partner (4P) or the single opponent (2P)
            val top = topSeat(snapshot)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PlayerBadge(top, name(snapshot, top), snapshot)
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
                        PlayerBadge(left, name(snapshot, left), snapshot)
                    }
                }

                Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                    val revealed = snapshot.revealedCard
                    if (snapshot.phase == GamePhase.Drawing && snapshot.isMyTurn && revealed != null) {
                        DrawPanel(revealed, snapshot.stockCount, vm::takeCard, vm::rejectCard)
                    } else if (snapshot.phase == GamePhase.Drawing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(De.drawingTurn(name(snapshot, top)), color = Color.White, fontSize = 14.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${De.STOCK_LABEL}: ${snapshot.stockCount}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                            )
                        }
                    } else {
                        TrickArea(snapshot, Modifier.fillMaxSize())
                    }
                }

                if (snapshot.playerCount == 4) {
                    val right = seatAt(snapshot, TablePosition.RIGHT)
                    Box(Modifier.width(76.dp), contentAlignment = Alignment.Center) {
                        PlayerBadge(right, name(snapshot, right), snapshot)
                    }
                }
            }

            // Own row: badge, turn hint, fanned hand.
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                PlayerBadge(snapshot.seat, De.YOU, snapshot, showCards = false)
                turnHint(snapshot)?.let {
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
                    .padding(bottom = 8.dp),
            )
        }

        Overlays(snapshot, vm)
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
            DiscardPicker(snapshot.hand, hakemDeclaration(snapshot), vm::discard)
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

/** Bot names follow the table position, as on iOS in solo play. */
private fun name(snapshot: GameSnapshot, seat: Seat): String = when {
    seat == snapshot.seat -> De.YOU
    snapshot.playerCount == 2 -> De.OPPONENTS
    seat == snapshot.seat.partner -> De.PARTNER
    TablePosition.of(seat, snapshot.seat) == TablePosition.LEFT -> "Gegner links"
    else -> "Gegner rechts"
}

private fun turnHint(snapshot: GameSnapshot): String? = when {
    snapshot.phase == GamePhase.Playing && snapshot.isMyTurn -> De.YOUR_TURN
    snapshot.phase == GamePhase.Playing ->
        De.turnOf(name(snapshot, snapshot.turn ?: snapshot.seat))
    snapshot.phase == GamePhase.ChoosingTrump && !snapshot.iAmHakem ->
        De.choosingTrumpBy(name(snapshot, snapshot.hakem))
    snapshot.phase == GamePhase.Discarding && !snapshot.needsDiscard &&
        snapshot.pendingDiscards.isNotEmpty() -> De.DISCARD_WAITING
    else -> null
}

/**
 * When a bot is the hakem, spell out its declaration so the player knows it
 * while discarding (2-player variant).
 */
private fun hakemDeclaration(snapshot: GameSnapshot): String? {
    if (snapshot.iAmHakem) return null
    val choice = snapshot.trumpChoice ?: return null
    val hakem = name(snapshot, snapshot.hakem)
    // A suit is announced "as trump"; high/low has no trump.
    return if (choice.suit != null) {
        De.hakemChoseTrump(hakem, choice.germanText)
    } else {
        De.hakemChose(hakem, choice.germanText)
    }
}
