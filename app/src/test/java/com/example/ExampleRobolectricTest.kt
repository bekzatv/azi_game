package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AziEvaluator
import com.example.model.Card
import com.example.model.HandCombinationType
import com.example.model.Rank
import com.example.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ази - Карточная игра", appName)
  }

  @Test
  fun `test three aces is Azi`() {
    val threeAces = listOf(
      Card(Suit.HEARTS, Rank.ACE),
      Card(Suit.SPADES, Rank.ACE),
      Card(Suit.CLUBS, Rank.ACE)
    )
    val evaluation = AziEvaluator.evaluate(threeAces)
    assertTrue(evaluation.isAzi)
    assertEquals(33f, evaluation.score)
    assertEquals(HandCombinationType.AZI, evaluation.combinationType)
  }

  @Test
  fun `test trio of kings is Trio`() {
    val threeKings = listOf(
      Card(Suit.HEARTS, Rank.KING),
      Card(Suit.SPADES, Rank.KING),
      Card(Suit.CLUBS, Rank.KING)
    )
    val evaluation = AziEvaluator.evaluate(threeKings)
    assertEquals(HandCombinationType.TRIO, evaluation.combinationType)
  }

  @Test
  fun `test sound manager methods execute without errors`() {
    com.example.audio.SoundManager.isSoundEnabled = true
    assertTrue(com.example.audio.SoundManager.isSoundEnabled)
    com.example.audio.SoundManager.playCardDeal()
    com.example.audio.SoundManager.playCardPlay()
    com.example.audio.SoundManager.playTrickCompleted(isUserWinner = true)
    com.example.audio.SoundManager.playChipBet()
    com.example.audio.SoundManager.playWinSound()
    com.example.audio.SoundManager.playSvaraGong()

    com.example.audio.SoundManager.isSoundEnabled = false
    com.example.audio.SoundManager.playCardDeal()
    com.example.audio.SoundManager.isSoundEnabled = true
  }
}


