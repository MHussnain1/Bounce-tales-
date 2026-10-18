package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import com.example.data.LevelProgress
import com.example.data.LevelsRepository
import com.example.data.ScoreDao
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
  scoreDao: ScoreDao,
  audio: RetroAudioSynthesizer,
  onBack: () -> Unit,
  onSelectLevel: (Int) -> Unit
) {
  BackHandler { onBack() }

  val progressList by scoreDao.getAllLevelProgress().collectAsState(initial = emptyList())

  Scaffold(
    containerColor = RetroDarkBg,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = RetroDarkBg,
          titleContentColor = Color.White
        ),
        title = {
          Text(
            text = "SELECT LEVEL",
            fontFamily = PixelFontFamily,
            fontSize = 16.sp,
            color = Color.White
          )
        },
        navigationIcon = {
          IconButton(
            onClick = {
              audio.playClick()
              onBack()
            },
            modifier = Modifier.testTag("level_select_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(vertical = 12.dp)
    ) {
      val allLevels = (1..LevelsRepository.totalLevels).map { LevelsRepository.getLevel(it) }

      items(allLevels) { level ->
        val progress = progressList.find { it.levelNumber == level.number }
        // Level 1 is always unlocked by default
        val isUnlocked = level.number == 1 || (progress?.isUnlocked == true)
        val stars = progress?.starsEarned ?: 0
        val bestScore = progress?.bestScore ?: 0

        LevelCard(
          levelNumber = level.number,
          title = level.title,
          subtitle = level.subtitle,
          targetParTime = level.targetParTimeSeconds,
          isUnlocked = isUnlocked,
          starsEarned = stars,
          bestScore = bestScore,
          onSelect = {
            if (isUnlocked) {
              audio.playClick()
              onSelectLevel(level.number)
            }
          }
        )
      }
    }
  }
}

@Composable
private fun LevelCard(
  levelNumber: Int,
  title: String,
  subtitle: String,
  targetParTime: Int,
  isUnlocked: Boolean,
  starsEarned: Int,
  bestScore: Int,
  onSelect: () -> Unit
) {
  val cardBg = if (isUnlocked) RetroSurface else RetroSurface.copy(alpha = 0.5f)
  val borderColor = if (isUnlocked) BounceRed.copy(alpha = 0.5f) else Color.Transparent

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = cardBg,
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = isUnlocked) { onSelect() }
      .testTag("level_card_$levelNumber")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Level Number Icon or Lock
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
              if (isUnlocked) Brush.linearGradient(listOf(BounceRed, BounceRedDark))
              else Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
            ),
          contentAlignment = Alignment.Center
        ) {
          if (isUnlocked) {
            Text(
              text = "$levelNumber",
              fontFamily = PixelFontFamily,
              fontSize = 16.sp,
              color = Color.White
            )
          } else {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Locked",
              tint = Color.White.copy(alpha = 0.6f),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Column {
          Text(
            text = title,
            fontFamily = PixelFontFamily,
            fontSize = 13.sp,
            color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = subtitle,
            fontFamily = BouncyFontFamily,
            fontSize = 12.sp,
            color = if (isUnlocked) RetroCyan else Color.White.copy(alpha = 0.35f)
          )

          if (isUnlocked && bestScore > 0) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Best: $bestScore pts • Par: ${targetParTime}s",
              fontSize = 11.sp,
              color = RingGoldLight
            )
          }
        }
      }

      // Stars Earned & Play Arrow
      if (isUnlocked) {
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..3) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (i <= starsEarned) RingGold else Color(0xFF475569),
                modifier = Modifier.size(16.dp)
              )
            }
          }
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = RingGold,
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }
  }
}
