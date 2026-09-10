package com.example.model

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderId: String,
    val text: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val isQuickPhrase: Boolean = false,
    val isSystem: Boolean = false
)

object QuickPhrases {
    val russianQuickPhrases = listOf(
        "Всем привет! Удачи! 🃏",
        "Поднимаю ставку! 🔥",
        "У меня Ази! 🏆",
        "Пас, карта не пошла ✋",
        "Похоже, будет АЗИ!",
        "Рискнем! Играем дальше 🎲",
        "Отличная игра, молодец! 👍",
        "Играем реванш! 🔄",
        "Банк сегодня мой! 💰",
        "Не спеши, думай! 🤔"
    )

    val kazakhQuickPhrases = russianQuickPhrases
}

