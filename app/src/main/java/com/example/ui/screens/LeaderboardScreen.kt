package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.RetroAudioSynthesizer
import com.example.data.ScoreDao
import com.example.data.ScoreRecord
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
  scoreDao: ScoreDao,
  audio: RetroAudioSynthesizer,
  onBack: () -> Unit,
  onPlayLevel: (Int) -> Unit
) {
  BackHandler { onBack() }

  var selectedFilter by remember { mutableIntStateOf(0) } // 0 = All Levels, 1..5 = Specific Level

  val scoresFlow = remember(selectedFilter) {
    if (selectedFilter == 0) {
      scoreDao.getAllHighScores()
    } else {
      scoreDao.getScoresForLevel(selectedFilter)
    }
  }

  val scores by scoresFlow.collectAsState(initial = emptyList())

  Scaffold(
    containerColor = RetroDarkBg,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = RetroDarkBg,
          titleContentColor = Color.White
        ),
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = RingGold,
              modifier = Modifier.size(28.dp)
            )
            Text(
              text = "HALL OF FAME",
              fontFamily = PixelFontFamily,
              fontSize = 16.sp,
              color = RingGold
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = {
              audio.playClick()
              onBack()
            },
            modifier = Modifier.testTag("leaderboard_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      // Filter Tabs Row
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp)
          .testTag("leaderboard_filters"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val filters = listOf("ALL", "LVL 1", "LVL 2", "LVL 3", "LVL 4", "LVL 5")
        itemsIndexed(filters) { index, label ->
          val isSelected = selectedFilter == index
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) BounceRed else RetroSurface,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isSelected) RingGold else RetroSurfaceLight
            ),
            modifier = Modifier
              .clickable {
                audio.playClick()
                selectedFilter = index
              }
              .testTag("filter_tab_$index")
          ) {
            Text(
              text = label,
              color = Color.White,
              fontFamily = PixelFontFamily,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (scores.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .testTag("leaderboard_empty_state"),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = RetroSurfaceLight,
              modifier = Modifier.size(64.dp)
            )
            Text(
              text = "NO HIGH SCORES YET",
              fontFamily = PixelFontFamily,
              fontSize = 14.sp,
              color = Color.White.copy(alpha = 0.6f)
            )
            Text(
              text = "Complete a level to claim your spot in the Hall of Fame!",
              color = Color.White.copy(alpha = 0.7f),
              fontSize = 13.sp,
              modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(
              onClick = {
                audio.playClick()
                onPlayLevel(if (selectedFilter > 0) selectedFilter else 1)
              },
              colors = ButtonDefaults.buttonColors(containerColor = BounceRed),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("empty_play_button")
            ) {
              Text("PLAY NOW", fontFamily = PixelFontFamily, fontSize = 12.sp)
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("leaderboard_list"),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 24.dp)
        ) {
          itemsIndexed(scores) { index, record ->
            ScoreCardItem(rank = index + 1, record = record)
          }
        }
      }
    }
  }
}

@Composable
private fun ScoreCardItem(rank: Int, record: ScoreRecord) {
  val rankColor = when (rank) {
    1 -> RingGold
    2 -> Color(0xFFE0E0E0) // Silver
    3 -> Color(0xFFCD7F32) // Bronze
    else -> Color(0xFF94A3B8)
  }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = RetroSurface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (rank <= 3) rankColor.copy(alpha = 0.6f) else Color.Transparent
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("score_item_$rank")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Rank & Initials
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(rankColor.copy(alpha = 0.2f))
            .border(1.dp, rankColor, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "#$rank",
            fontFamily = PixelFontFamily,
            fontSize = 11.sp,
            color = rankColor
          )
        }

        Column {
          Text(
            text = record.playerName.uppercase(),
            fontFamily = PixelFontFamily,
            fontSize = 15.sp,
            color = Color.White
          )
          Text(
            text = "LVL ${record.levelNumber}: ${record.levelName}",
            fontFamily = BouncyFontFamily,
            fontSize = 12.sp,
            color = RetroCyan
          )
        }
      }

      // Score & Stars
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${record.score} PTS",
          fontFamily = PixelFontFamily,
          fontSize = 14.sp,
          color = RingGold
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Stars count
          repeat(record.stars) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = RingGold,
              modifier = Modifier.size(13.dp)
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(12.dp)
          )
          Text(
            text = "${record.timeSeconds}s",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f)
          )
        }
      }
    }
  }
}
