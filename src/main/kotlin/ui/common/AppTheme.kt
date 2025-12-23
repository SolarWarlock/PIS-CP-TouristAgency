package ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Цвета из твоего логотипа
val BrandBlue = Color(0xFF00A3E0)    // Цвет самолетика
val BrandDarkBlue = Color(0xFF005B96) // Темная часть крыла
val BrandOrange = Color(0xFFFFA000)   // Цвет букв "ик"
val BackgroundLight = Color(0xFFF5F7FA) // Очень светлый фон для экранов

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    secondary = BrandOrange,
    onSecondary = Color.White,
    background = BackgroundLight,
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E) // Почти черный для текста
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}

