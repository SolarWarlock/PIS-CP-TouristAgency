import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import data.db.DatabaseFactory
import di.appModule
import org.koin.core.context.startKoin
import ui.common.AppTheme
import ui.screens.MainScreen

fun main() = application {
    startKoin { modules(appModule) }

    // ВАЖНО: Убедись, что база подключена (имя пользователя/пароль из твоего pgAdmin)
    DatabaseFactory.connect("client_app_bot", "secure_app_pass_2025")

    Window(onCloseRequest = ::exitApplication, title = "Туристическое Агентство") {
        AppTheme {
            // Запускаем навигатор с Главного экрана
            // (Позже сюда добавим логику: если не вошел -> LoginScreen)
            Navigator(ui.screens.auth.LoginScreen)
        }
    }
}