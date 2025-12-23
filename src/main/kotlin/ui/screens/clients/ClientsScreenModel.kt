package ui.screens.clients

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.model.Client
import data.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ClientsState {
    object Loading : ClientsState()
    data class Content(val clients: List<Client>) : ClientsState()
    data class Error(val message: String) : ClientsState()
}

class ClientsScreenModel(
    private val repository: ClientRepository
) : ScreenModel {

    private val _state = MutableStateFlow<ClientsState>(ClientsState.Loading)
    val state = _state.asStateFlow()

    init {
        loadClients()
    }

    fun loadClients() {
        screenModelScope.launch {
            _state.value = ClientsState.Loading
            try {
                val list = repository.getAllClients()
                _state.value = ClientsState.Content(list)
            } catch (e: Exception) {
                _state.value = ClientsState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun search(query: String) {
        screenModelScope.launch {
            try {
                val list = if (query.isBlank()) repository.getAllClients() else repository.searchClients(query)
                _state.value = ClientsState.Content(list)
            } catch (e: Exception) {
                _state.value = ClientsState.Error(e.message ?: "")
            }
        }
    }

    // Сохранение изменений
    fun saveClient(client: Client, passport: String) {
        screenModelScope.launch {
            try {
                repository.updateClient(client, passport)
                loadClients() // Обновляем список
            } catch (e: Exception) {
                println("Ошибка сохранения: ${e.message}")
            }
        }
    }

    // Получить паспорт для редактирования (callback-style для простоты)
    fun loadPassport(clientId: Int, onResult: (String) -> Unit) {
        screenModelScope.launch {
            val passport = repository.getPassportData(clientId)
            onResult(passport)
        }
    }
}