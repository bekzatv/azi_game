package com.example.audio

/**
 * SoundManager provides unified playback of all game audio events:
 * - Card dealing (раздача карт)
 * - Player moves / card played (ход игрока)
 * - Trick completion (завершение взятки)
 * - Chip bets / raises
 * - Victory / Azi fanfares
 */
object SoundManager {
    var isSoundEnabled: Boolean
        get() = SoundSynthesizer.isSoundEnabled
        set(value) {
            SoundSynthesizer.isSoundEnabled = value
        }

    /**
     * Воспроизведение звука раздачи карты.
     */
    fun playCardDeal(variation: Int = 0) {
        SoundSynthesizer.playCardDeal(variation)
    }

    /**
     * Воспроизведение звука хода игрока (выкладывание карты на стол).
     */
    fun playCardPlay() {
        SoundSynthesizer.playCardPlay()
    }

    /**
     * Воспроизведение звука завершения взятки (сбор карт победителем).
     */
    fun playTrickCompleted(isUserWinner: Boolean = false) {
        SoundSynthesizer.playTrickCompleted(isUserWinner)
    }

    /**
     * Воспроизведение звука ставки / фишек.
     */
    fun playChipBet() {
        SoundSynthesizer.playChipBet()
    }

    /**
     * Воспроизведение звука победы / Ази.
     */
    fun playWinSound() {
        SoundSynthesizer.playWinSound()
    }

    /**
     * Звук гонга свары (ничьей).
     */
    fun playSvaraGong() {
        SoundSynthesizer.playSvaraGong()
    }
}
