package com.kafappstore.feshorde.data

import android.content.Context
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    FA("fa", "Persian", "فارسی"),
    EN("en", "English", "English")
}

object LanguageManager {
    private val _currentLanguage = MutableStateFlow(AppLanguage.FA)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val code = prefs.getString("app_language", "fa") ?: "fa"
        _currentLanguage.value = if (code == "en") AppLanguage.EN else AppLanguage.FA
        initialized = true
    }

    fun isLanguageSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.contains("app_language")
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", language.code).apply()
        _currentLanguage.value = language
    }

    fun isEnglish(): Boolean = _currentLanguage.value == AppLanguage.EN

    fun isRtl(): Boolean = _currentLanguage.value == AppLanguage.FA

    fun getLayoutDirection(): LayoutDirection {
        return if (isEnglish()) LayoutDirection.Ltr else LayoutDirection.Rtl
    }

    fun formatNumber(number: Any): String {
        val str = number.toString()
        if (isEnglish()) return str
        val persianNumbers = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val builder = StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                builder.append(persianNumbers[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return if (isEnglish()) "0 Bytes" else "۰ بایت"
        val unitsEn = arrayOf("Bytes", "KB", "MB", "GB", "TB")
        val unitsFa = arrayOf("بایت", "کیلوبایت", "مگابایت", "گیگابایت", "ترابایت")
        val digitGroups = (kotlin.math.log10(bytes.toDouble()) / kotlin.math.log10(1024.0)).toInt().coerceIn(0, unitsEn.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        val formattedValue = String.format(java.util.Locale.US, "%.1f", value)
        return if (isEnglish()) {
            "$formattedValue ${unitsEn[digitGroups]}"
        } else {
            val pValue = formatNumber(formattedValue)
            "$pValue ${unitsFa[digitGroups]}"
        }
    }
}
