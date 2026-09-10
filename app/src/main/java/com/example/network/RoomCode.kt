package com.example.network

import java.security.SecureRandom
import java.util.Locale

object RoomCode {
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val random = SecureRandom()
    private val pattern = Regex("(?<![A-Z0-9])AZI[- ]?([A-HJ-NP-Z2-9]{10})(?![A-Z0-9])")
    fun generate(): String = "AZI-" + (1..10).map { ALPHABET[random.nextInt(ALPHABET.length)] }.joinToString("")
    fun normalize(input: String): String? {
        val match = pattern.find(input.trim().uppercase(Locale.ROOT)) ?: return null
        return "AZI-" + match.groupValues[1]
    }
    fun invitation(code: String) = "Играем в Ази! Код комнаты: $code\nОткрой игру → Войти по приглашению → вставь это сообщение."
}
