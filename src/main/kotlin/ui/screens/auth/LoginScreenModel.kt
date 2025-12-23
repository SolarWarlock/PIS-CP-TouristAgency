package ui.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ui.common.UserSession // <-- Не забудь этот импорт!

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val userType: String, val userName: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginScreenModel(
    private val authRepository: AuthRepository
) : ScreenModel {

    var email = MutableStateFlow("")
    var password = MutableStateFlow("")

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state = _state.asStateFlow()

    fun onLoginClicked() {
        if (email.value.isBlank() || password.value.isBlank()) {
            _state.value = LoginState.Error("Заполните все поля")
            return
        }

        screenModelScope.launch {
            _state.value = LoginState.Loading

            try {
                // 1. Пробуем найти Клиента
                val client = authRepository.loginClient(email.value, password.value)

                if (client != null) {
                    UserSession.currentClient = client // Сохраняем в сессию
                    UserSession.currentEmployee = null // Очищаем сотрудника на всякий случай
                    _state.value = LoginState.Success("Client", "${client.firstName} ${client.lastName}")
                    return@launch
                }

                // 2. Если клиент не найден, пробуем найти Сотрудника
                val employee = authRepository.getCountEmployeeInfo(email.value)

                if (employee != null) {
                    UserSession.currentEmployee = employee // Сохраняем в сессию
                    UserSession.currentClient = null
                    _state.value = LoginState.Success("Employee", employee.fio)
                } else {
                    _state.value = LoginState.Error("Неверный логин или пароль")
                }

            } catch (e: Exception) {
                _state.value = LoginState.Error("Ошибка базы данных: ${e.message}")
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }
}