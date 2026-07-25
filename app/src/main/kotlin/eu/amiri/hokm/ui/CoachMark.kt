package eu.amiri.hokm.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** One focus hint of the in-game mini tutorial. */
enum class CoachStep { HEADER, DRAW, HAND }

val CoachStep.text: String
    get() = when (this) {
        CoachStep.HEADER -> De.COACH_HEADER
        CoachStep.DRAW -> De.COACH_DRAW
        CoachStep.HAND -> De.COACH_HAND
    }

/**
 * Spotlight coach mark for the first game of a mode: the table darkens, only
 * the focus field stays lit (with a soft golden glow), and a short explanation
 * appears next to it. Tapping anywhere advances.
 *
 * Port of the iOS `CoachMarkOverlay`.
 */
@Composable
fun CoachMarkOverlay(text: String, target: Rect, onDismiss: () -> Unit) {
    val density = LocalDensity.current

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .pointerInput(text) { detectTapGestures { onDismiss() } },
    ) {
        // Breathing room around the measured frame, as on iOS.
        val rect = Rect(
            left = target.left - with(density) { 10.dp.toPx() },
            top = target.top - with(density) { 8.dp.toPx() },
            right = target.right + with(density) { 10.dp.toPx() },
            bottom = target.bottom + with(density) { 8.dp.toPx() },
        )
        val corner = with(density) { 18.dp.toPx() }

        Canvas(
            Modifier
                .fillMaxSize()
                // The hole is punched with BlendMode.Clear, which needs the
                // scrim to live on its own layer.
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
        ) {
            drawRect(Color.Black.copy(alpha = 0.6f))
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width, rect.height),
                cornerRadius = CornerRadius(corner, corner),
                blendMode = BlendMode.Clear,
            )
        }

        // The "light": a golden ring, softened by two wider, fainter passes.
        Canvas(Modifier.fillMaxSize()) {
            listOf(10f to 0.10f, 5f to 0.25f, 2f to 0.95f).forEach { (width, alpha) ->
                drawRoundRect(
                    color = TableStyle.gold.copy(alpha = alpha),
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    cornerRadius = CornerRadius(corner, corner),
                    style = Stroke(width = width),
                )
            }
        }

        // The explanation sits opposite the spotlight: below when the focus is
        // in the upper half, above otherwise.
        val heightPx = with(density) { maxHeight.toPx() }
        val below = rect.center.y < heightPx / 2
        val cardY = with(density) {
            if (below) {
                minOf(rect.bottom + 40.dp.toPx(), heightPx - 260.dp.toPx()).toDp()
            } else {
                maxOf(rect.top - 260.dp.toPx(), 40.dp.toPx()).toDp()
            }
        }

        Column(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = cardY)
                .widthIn(max = 340.dp)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF14301F).copy(alpha = 0.96f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("💡", fontSize = 22.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E9E5B),
                    contentColor = Color.White,
                ),
            ) { Text(De.GOT_IT, fontWeight = FontWeight.Bold) }
        }
    }
}

/**
 * Keeps the measured focus regions of the table in root coordinates. The
 * frames are Compose state, so the overlay appears as soon as its target
 * has been laid out.
 */
class CoachFrames {
    var header by mutableStateOf<Rect?>(null)
    var center by mutableStateOf<Rect?>(null)
    var hand by mutableStateOf<Rect?>(null)

    operator fun get(step: CoachStep): Rect? = when (step) {
        CoachStep.HEADER -> header
        CoachStep.DRAW -> center
        CoachStep.HAND -> hand
    }
}
