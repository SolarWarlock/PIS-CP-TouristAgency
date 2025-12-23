package ui.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterScreenModel(
    private val repository: AuthRepository
) : ScreenModel {

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state = _state.asStateFlow()

    fun register(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        password: String,
        passport: String
    ) {
        // Простая валидация
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = RegisterState.Error("Заполните обязательные поля")
            return
        }

        screenModelScope.launch {
            _state.value = RegisterState.Loading
            try {
                // В реальном проекте пароль надо хешировать тут, но мы передаем как есть,
                // так как в БД процедура ждет строку
                repository.registerClient(firstName, lastName, phone, email, password, passport)
                _state.value = RegisterState.Success
            } catch (e: Exception) {
                // Обработка ошибки (например, если email занят)
                _state.value = RegisterState.Error(e.message ?: "Ошибка регистрации")
            }
        }
    }
}