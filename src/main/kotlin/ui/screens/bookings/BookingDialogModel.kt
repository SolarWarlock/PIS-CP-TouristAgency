package ui.screens.bookings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.model.Client
import data.repository.BookingRepository
import data.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ui.common.UserSession

class BookingDialogModel(
    private val clientRepository: ClientRepository,
    private val bookingRepository: BookingRepository
) : ScreenModel {

    // Для поиска (только менеджер)
    private val _foundClients = MutableStateFlow<List<Client>>(emptyList())
    val foundClients = _foundClients.asStateFlow()

    // Результат операции
    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState = _bookingState.asStateFlow()

    fun searchClient(query: String) {
        screenModelScope.launch {
            if (query.length >= 2) {
                _foundClients.value = clientRepository.searchClients(query)
            } else {
                _foundClients.value = emptyList()
            }
        }
    }

    fun confirmBooking(tourId: Int, selectedClient: Client?) {
        screenModelScope.launch {
            _bookingState.value = BookingState.Loading
            try {
                // ОПРЕДЕЛЯЕМ КТО БРОНИРУЕТ
                val clientId: Int
                val managerId: Int?

                if (UserSession.isManager) {
                    // Сценарий Менеджера
                    if (selectedClient == null) {
                        _bookingState.value = BookingState.Error("Выберите клиента из списка!")
                        return@launch
                    }
                    clientId = selectedClient.id
                    managerId = UserSession.currentEmployee?.id
                } else {
                    // Сценарий Клиента
                    clientId = UserSession.currentClient?.id ?: throw Exception("Ошибка сессии")
                    managerId = null
                }

                // Вызываем процедуру в БД
                val id = bookingRepository.createBooking(tourId, clientId, managerId)
                _bookingState.value = BookingState.Success(id)

            } catch (e: Exception) {
                _bookingState.value = BookingState.Error(e.message ?: "Ошибка бронирования")
            }
        }
    }
}

sealed class BookingState {
    object Idle : BookingState()
    object Loading : BookingState()
    data class Success(val bookingId: Int) : BookingState()
    data class Error(val message: String) : BookingState()
}