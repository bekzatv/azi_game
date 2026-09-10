package com.example.model

import androidx.compose.ui.graphics.Color

enum class OrnamentPattern {
    QOSHQAR_MYIZ,       // Қошқар мүйіз (Ram horn - symbol of wealth & strength)
    QOSH_DONGGELEK,     // Қосмүйіз & Дөңгелек (Circular solar amulet)
    TUMAR,              // Тұмар (Triangular protection amulet)
    SHANYRAQ,           // Шаңырақ (Sacred home dome & sun rays)
    SYRGHA,             // Сырға / Гүл (Floral steppe ornament)
    AZURE_DIAMOND,      // Лазурный Ромб (Лазурно-голубая мозаика с центральным ромбом и сложным орнаментом)
    BICYCLE_RIDER_RED,  // Классический Красный Райдер (Bicycle Rider Back, два круга с ангелочками)
    VIOLET_GEOMETRIC,   // Фиолетовая Геометрия (Фиолетовый цветок-мандала с шахматной рамкой)
    OBSIDIAN_ACE_SPADES // Обсидиановый Пиковый Туз (Черно-стальная гравировка с коронованным гербовым тузом)
}

data class DeckTheme(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val ornamentColor: Color,
    val pattern: OrnamentPattern,
    val priceTenge: Long = 0L,
    val artworkRes: Int? = null,
    val isUnlocked: Boolean = true
)

object AvailableDecks {
    val SHANYRAK_NOIR = DeckTheme(
        id = "shanyrak_noir", name = "Шаңырақ • Ночь",
        description = "Золотая гравировка и бирюза на глубоком индиго",
        primaryColor = Color(0xFF101B32), secondaryColor = Color(0xFF203653),
        ornamentColor = Color(0xFFDEC28A), pattern = OrnamentPattern.SHANYRAQ,
        artworkRes = com.example.R.drawable.deck_shanyrak
    )
    val STEPPE_SILVER = DeckTheme(
        id = "steppe_silver", name = "Дала • Серебро",
        description = "Серебряная геометрия и оберег степи",
        primaryColor = Color(0xFF183F49), secondaryColor = Color(0xFF0B202C),
        ornamentColor = Color(0xFFD7EBEF), pattern = OrnamentPattern.TUMAR
    )
    val SILK_SUNSET = DeckTheme(
        id = "silk_sunset", name = "Жібек • Закат",
        description = "Тёплое золото на гранатовом шёлке",
        primaryColor = Color(0xFF592C43), secondaryColor = Color(0xFF24172E),
        ornamentColor = Color(0xFFF1CCAA), pattern = OrnamentPattern.SYRGHA
    )

    val ALTYN_ORDA = DeckTheme(
        id = "altyn_orda",
        name = "Золотая Орда",
        description = "Ханская колода с золотым орнаментом Кошкар Муйиз",
        primaryColor = Color(0xFF0F2027),
        secondaryColor = Color(0xFF203A43),
        ornamentColor = Color(0xFFF6C344),
        pattern = OrnamentPattern.QOSHQAR_MYIZ,
        priceTenge = 0L,
        isUnlocked = true
    )

    val KOK_TUMAR = DeckTheme(
        id = "kok_tumar",
        name = "Синий Тумар",
        description = "Небесно-бирюзовая колода с серебряным оберегом Тумар",
        primaryColor = Color(0xFF005C53),
        secondaryColor = Color(0xFF042940),
        ornamentColor = Color(0xFF9FC131),
        pattern = OrnamentPattern.TUMAR,
        priceTenge = 0L,
        isUnlocked = true
    )

    val QYZYL_JIBEK = DeckTheme(
        id = "qyzyl_jibek",
        name = "Красный Шелк",
        description = "Королевский алый шелк с лучезарным узором Шанырак",
        primaryColor = Color(0xFF5B0E2D),
        secondaryColor = Color(0xFF33001B),
        ornamentColor = Color(0xFFFFDF79),
        pattern = OrnamentPattern.SHANYRAQ,
        priceTenge = 0L,
        isUnlocked = true
    )

    val NAURYZ_JASYL = DeckTheme(
        id = "nauryz_jasyl",
        name = "Изумрудный Наурыз",
        description = "Символ обновления и весеннего цветения степи",
        primaryColor = Color(0xFF0B4619),
        secondaryColor = Color(0xFF02230D),
        ornamentColor = Color(0xFFFFD700),
        pattern = OrnamentPattern.SYRGHA,
        priceTenge = 0L,
        isUnlocked = true
    )

    val QARA_ALTYN = DeckTheme(
        id = "qara_altyn",
        name = "Черное Золото VIP",
        description = "Глубокий обсидиановый блеск и тиснение золотом 24 карата",
        primaryColor = Color(0xFF111111),
        secondaryColor = Color(0xFF1E1E1E),
        ornamentColor = Color(0xFFFFB300),
        pattern = OrnamentPattern.QOSH_DONGGELEK,
        priceTenge = 0L,
        isUnlocked = true
    )

    val AZURE_ORNAMENT = DeckTheme(
        id = "azure_ornament",
        name = "Лазурный Узор",
        description = "Изысканный лазурно-голубой узор с центральным ромбом и сложными виньетками",
        primaryColor = Color(0xFF0F172A), // Темная основа
        secondaryColor = Color(0xFF0284C7), // Лазурный градиент
        ornamentColor = Color(0xFF38BDF8), // Яркий небесный циан
        pattern = OrnamentPattern.AZURE_DIAMOND,
        priceTenge = 0L,
        isUnlocked = true
    )

    val BICYCLE_RED = DeckTheme(
        id = "bicycle_red",
        name = "Красный Райдер",
        description = "Культовая классика игральных карт: красный фон, два круглых медальона и симметричные виньетки",
        primaryColor = Color(0xFFD32F2F), // Насыщенный классический красный
        secondaryColor = Color(0xFFB71C1C), // Глубокий бордово-красный
        ornamentColor = Color(0xFFFFFFFF), // Чистый белый орнамент
        pattern = OrnamentPattern.BICYCLE_RIDER_RED,
        priceTenge = 0L,
        isUnlocked = true
    )

    val VIOLET_MANDALA = DeckTheme(
        id = "violet_mandala",
        name = "Сиреневая Мандала",
        description = "Геометрический узор с цветком-мандалой, концентрическими кругами и шахматной рамкой",
        primaryColor = Color(0xFF7C3AED), // Сиренево-фиолетовый
        secondaryColor = Color(0xFF6D28D9), // Глубокий фиолетовый
        ornamentColor = Color(0xFFFFFFFF), // Белая строгая геометрия
        pattern = OrnamentPattern.VIOLET_GEOMETRIC,
        priceTenge = 0L,
        isUnlocked = true
    )

    val OBSIDIAN_SPADE = DeckTheme(
        id = "obsidian_spade",
        name = "Пиковый Туз VIP",
        description = "Премиальная темная гравировка с величественным гербовым пиковым тузом и короной",
        primaryColor = Color(0xFF0A0A0A), // Глубокий черный
        secondaryColor = Color(0xFF171717), // Обсидиановый серый
        ornamentColor = Color(0xFFE2E8F0), // Серебристо-стальной
        pattern = OrnamentPattern.OBSIDIAN_ACE_SPADES,
        priceTenge = 0L,
        isUnlocked = true
    )

    val allDecks = listOf(
        SHANYRAK_NOIR, STEPPE_SILVER, SILK_SUNSET,
        AZURE_ORNAMENT,
        BICYCLE_RED,
        VIOLET_MANDALA,
        OBSIDIAN_SPADE,
        ALTYN_ORDA,
        KOK_TUMAR,
        QYZYL_JIBEK,
        NAURYZ_JASYL,
        QARA_ALTYN
    )
}
