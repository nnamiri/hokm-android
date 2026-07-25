package eu.amiri.hokm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.amiri.hokm.engine.Card
import eu.amiri.hokm.engine.Rank
import eu.amiri.hokm.engine.Suit
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 5

/**
 * Five-step tutorial shown on first launch (and on demand from the menu).
 * Pages can be swiped or clicked through, and skipped at any time.
 * Port of the iOS `OnboardingView`.
 */
@Composable
fun TutorialScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    FeltBackground {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    De.SKIP,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onFinish).padding(4.dp),
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { page ->
                Box(
                    Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.widthIn(max = 460.dp)) { TutorialPage(page) }
                }
            }

            // Page dots
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(PAGE_COUNT) { index ->
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) {
                                    TableStyle.gold
                                } else {
                                    Color.White.copy(alpha = 0.35f)
                                }
                            ),
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        },
                    ) { Text("‹  ${De.BACK}", color = Color.White) }
                }
                Spacer(Modifier.weight(1f))
                val isLast = pagerState.currentPage == PAGE_COUNT - 1
                Button(
                    onClick = {
                        if (isLast) {
                            onFinish()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E9E5B),
                        contentColor = Color.White,
                    ),
                ) {
                    Text(if (isLast) "▶  ${De.LETS_GO}" else "${De.NEXT}  ›", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TutorialPage(page: Int) {
    when (page) {
        0 -> PageBody(De.OB1_TITLE, De.OB1_TEXT) {
            // The four seats: you and your partner face each other.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SeatBubble(De.PARTNER, TableStyle.teamMine)
                Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
                    SeatBubble(De.OPPONENTS, TableStyle.teamOpponent)
                    SeatBubble(De.OPPONENTS, TableStyle.teamOpponent)
                }
                SeatBubble(De.YOU, TableStyle.teamMine)
            }
        }

        1 -> PageBody(De.OB2_TITLE, De.OB2_TEXT) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Centered overlapping stack
                Box(contentAlignment = Alignment.Center) {
                    val overlap = 22.dp
                    val count = 5
                    val totalWidth = OverlayCardWidth + overlap * (count - 1)
                    
                    // We wrap in a Box of totalWidth to ensure Box(Alignment.Center) has a base
                    Box(Modifier.width(totalWidth)) {
                        listOf(
                            Card(Rank.ACE, Suit.SPADES),
                            Card(Rank.KING, Suit.SPADES),
                            Card(Rank.NINE, Suit.HEARTS),
                            Card(Rank.QUEEN, Suit.DIAMONDS),
                            Card(Rank.THREE, Suit.CLUBS),
                        ).forEachIndexed { index, card ->
                            CardFace(card, OverlayCardWidth, Modifier.offset(x = overlap * index))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("♛", color = TableStyle.gold, fontSize = 16.sp)
                    Text(
                        "${De.TRUMP_LABEL}: ♠ ${Suit.SPADES.germanName}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        2 -> PageBody(De.OB3_TITLE, De.OB3_TEXT) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CardFace(Card(Rank.KING, Suit.HEARTS), 56.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(De.LED_LABEL, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
                Text("→", color = Color.White.copy(alpha = 0.7f), fontSize = 20.sp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CardFace(Card(Rank.TWO, Suit.SPADES), 56.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(De.TRUMP_BEATS, color = TableStyle.gold, fontSize = 11.sp)
                }
            }
        }

        3 -> PageBody(De.OB4_TITLE, De.OB4_TEXT) {
            Column(
                Modifier.widthIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ScoreExampleRow(De.ROUND_WON_ROW, De.POINTS_ONE)
                ScoreExampleRow(De.KOT_ROW, De.POINTS_TWO)
                ScoreExampleRow(De.HAKEM_KOT_ROW, De.POINTS_THREE)
            }
        }

        else -> PageBody(De.OB5_TITLE, De.OB5_TEXT) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                IconLabel("♟", De.SOLO_WORD)
                IconLabel("⏸", De.PAUSE)
                IconLabel("💾", De.SAVED_WORD)
            }
        }
    }
}

/** One tutorial page: illustration, headline, body text. */
@Composable
private fun PageBody(title: String, text: String, illustration: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.fillMaxWidth().height(130.dp),
            contentAlignment = Alignment.Center,
        ) { illustration() }

        Spacer(Modifier.height(22.dp))
        Text(
            title,
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SeatBubble(text: String, color: Color) {
    Box(
        modifier = Modifier
            .widthIn(min = 100.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.9f))
            .border(1.5.dp, Color.White.copy(alpha = 0.55f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ScoreExampleRow(label: String, points: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Text(points, color = TableStyle.gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun IconLabel(icon: String, label: String) {
    Column(
        Modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(Modifier.height(34.dp), contentAlignment = Alignment.BottomCenter) {
            Text(
                icon, 
                fontSize = 28.sp, 
                color = Color.White,
                modifier = if (icon == "♟") Modifier.offset(y = 2.dp) else Modifier
            )
        }
        Text(label, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}
