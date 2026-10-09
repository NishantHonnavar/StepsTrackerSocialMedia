package com.example

import org.json.JSONArray
import org.json.JSONObject

data class BlockedAppInfo(
    val packageName: String,
    val appName: String,
    val iconEmoji: String,
    val isBlocked: Boolean = true
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("packageName", packageName)
        put("appName", appName)
        put("iconEmoji", iconEmoji)
        put("isBlocked", isBlocked)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): BlockedAppInfo = BlockedAppInfo(
            packageName = json.optString("packageName", ""),
            appName = json.optString("appName", ""),
            iconEmoji = json.optString("iconEmoji", "📱"),
            isBlocked = json.optBoolean("isBlocked", true)
        )

        val DEFAULT_BLOCKED_APPS = listOf(
            BlockedAppInfo("com.instagram.android", "Instagram", "📸", true),
            BlockedAppInfo("com.google.android.youtube", "YouTube", "▶️", true),
            BlockedAppInfo("com.whatsapp", "WhatsApp", "💬", true),
            BlockedAppInfo("com.pinterest", "Pinterest", "📌", true),
            BlockedAppInfo("com.facebook.katana", "Facebook", "👥", true)
        )
    }
}
