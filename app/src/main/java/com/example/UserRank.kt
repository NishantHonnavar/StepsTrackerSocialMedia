package com.example

/**
 * Experience points (XP) rank tiers for Scroll Tax.
 * Users accumulate lifetime steps and earn XP, leveling up from
 * 'Novice Scroller' to the ultimate 'Walking Master'.
 */
enum class UserRank(
    val level: Int,
    val title: String,
    val minXp: Long,
    val maxXp: Long,
    val badgeEmoji: String,
    val iconSymbol: String,
    val colorHex: Long,
    val secondaryColorHex: Long,
    val rankPerk: String,
    val loreDescription: String
) {
    NOVICE_SCROLLER(
        level = 1,
        title = "Novice Scroller",
        minXp = 0L,
        maxXp = 1000L,
        badgeEmoji = "🌱",
        iconSymbol = "📱",
        colorHex = 0xFF64748B, // Slate Silver
        secondaryColorHex = 0xFF94A3B8,
        rankPerk = "Base conversion rate & morning lock active",
        loreDescription = "Just starting your physical activity journey. Break free from mindless scrolling one step at a time."
    ),
    CASUAL_STRIDER(
        level = 2,
        title = "Casual Strider",
        minXp = 1000L,
        maxXp = 3000L,
        badgeEmoji = "👟",
        iconSymbol = "⚡",
        colorHex = 0xFF00E5FF, // Cyber Cyan
        secondaryColorHex = 0xFF0284C7,
        rankPerk = "+5m extra banked vault capacity",
        loreDescription = "Consistent daily steps begin paying off. Instagram feeds no longer dictate your attention."
    ),
    PACE_SETTER(
        level = 3,
        title = "Pace Setter",
        minXp = 3000L,
        maxXp = 7500L,
        badgeEmoji = "🏃‍♂️",
        iconSymbol = "🔥",
        colorHex = 0xFF10B981, // Emerald Neon
        secondaryColorHex = 0xFF059669,
        rankPerk = "+10m extra banked vault capacity & 1.1x Golden Hour XP",
        loreDescription = "Setting a brisk pace. Walking has become your natural daily anchor."
    ),
    DISTANCE_CRUSHER(
        level = 4,
        title = "Distance Crusher",
        minXp = 7500L,
        maxXp = 15000L,
        badgeEmoji = "⚡",
        iconSymbol = "🛡️",
        colorHex = 0xFFFFB800, // Gold / Amber
        secondaryColorHex = 0xFFEA580C,
        rankPerk = "+15m extra vault capacity & 1.25x Active Walk XP",
        loreDescription = "Demolishing step milestones daily. Turning physical movement into a digital fortress."
    ),
    SCROLL_TAX_ELITE(
        level = 5,
        title = "Scroll Tax Elite",
        minXp = 15000L,
        maxXp = 30000L,
        badgeEmoji = "🛡️",
        iconSymbol = "💎",
        colorHex = 0xFFA855F7, // Electric Purple
        secondaryColorHex = 0xFF7C3AED,
        rankPerk = "+20m extra vault capacity & elite aura badge",
        loreDescription = "Uncompromising digital discipline. You wield your screen time with razor-sharp focus."
    ),
    WALKING_MASTER(
        level = 6,
        title = "Walking Master",
        minXp = 30000L,
        maxXp = 60000L,
        badgeEmoji = "👑",
        iconSymbol = "🌟",
        colorHex = 0xFFFF2A85, // Cosmic Holographic Rose
        secondaryColorHex = 0xFFFFD700,
        rankPerk = "Pinnacle Prestige • Maximum vault efficiency & Master flair",
        loreDescription = "The undisputed legend of mindful balance. Social media algorithms bow to your stride."
    );

    val isMaxRank: Boolean get() = this == WALKING_MASTER

    val nextRank: UserRank?
        get() = entries.getOrNull(ordinal + 1)

    fun progressFraction(currentXp: Long): Float {
        if (isMaxRank) return 1f
        val range = (maxXp - minXp).toFloat()
        if (range <= 0f) return 1f
        val currentInRange = (currentXp - minXp).toFloat()
        return (currentInRange / range).coerceIn(0f, 1f)
    }

    fun xpNeededForNextLevel(currentXp: Long): Long {
        if (isMaxRank) return 0L
        return (maxXp - currentXp).coerceAtLeast(0L)
    }

    fun xpInCurrentLevel(currentXp: Long): Long {
        if (isMaxRank) return (currentXp - minXp).coerceAtLeast(0L)
        return (currentXp - minXp).coerceIn(0L, maxXp - minXp)
    }

    fun totalXpRequiredForLevel(): Long {
        return maxXp - minXp
    }

    companion object {
        fun fromXp(xp: Long): UserRank {
            val clampedXp = xp.coerceAtLeast(0L)
            return entries.lastOrNull { clampedXp >= it.minXp } ?: NOVICE_SCROLLER
        }
    }
}
