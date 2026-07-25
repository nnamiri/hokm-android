package eu.amiri.hokm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The rule book, ported from the iOS `RulesView`. Reading order follows a
 * game's flow: setup (incl. the 2-player deal & draw phase) before trick play,
 * scoring and the high/low variant.
 */
@Composable
fun RulesScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(De.RULES, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            RuleSection(De.RULES_GAME_TITLE, De.RULES_GAME_TEXT)
            RuleSection(De.RULES_HAKEM_TITLE, De.RULES_HAKEM_TEXT)
            RuleSection(De.RULES_2P_TITLE, De.RULES_2P_TEXT)
            RuleSection(De.RULES_TRICK_TITLE, De.RULES_TRICK_TEXT)
            RuleSection(De.RULES_SCORING_TITLE, De.RULES_SCORING_TEXT)
            RuleSection(De.RULES_HIGH_LOW_TITLE, De.RULES_HIGH_LOW_TEXT)
            RuleSection(De.RULES_APP_TITLE, De.RULES_APP_TEXT)
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun RuleSection(title: String, text: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(text, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, lineHeight = 20.sp)
    }
}
