package ui.screens.bookings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.model.Booking
import data.repository.BookingRepository
import data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ui.common.UserSession
import data.repository.ReviewRepository

sealed class BookingsListState {
    data object Loading : BookingsListState()
    data class Content(val bookings: List<Booking>) : BookingsListState()
    data class Error(val message: String) : BookingsListState()
}

class BookingsScreenModel(
    private val repository: BookingRepository,
    private val paymentRepository: PaymentRepository,
    private val reviewRepository: ReviewRepository
) : ScreenModel {

    private val _state = MutableStateFlow<BookingsListState>(BookingsListState.Loading)
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        screenModelScope.launch {
            _state.value = BookingsListState.Loading
            try {
                val list = if (UserSession.isManager) {
                    val managerId = UserSession.currentEmployee?.id ?: 0
                    repository.getBookingsForManager(managerId)
                } else {
                    val clientId = UserSession.currentClient?.id ?: 0
                    repository.getBookingsForClient(clientId)
                }
                _state.value = BookingsListState.Content(list)
            } catch (e: Exception) {
                _state.value = BookingsListState.Error("Ошибка: ${e.message}")
            }
        }
    }

    // Только для менеджера
    fun changeStatus(bookingId: Int, newStatus: String) {
        screenModelScope.launch {
            try {
                // Берем ID текущего менеджера из сессии
                val currentManagerId = UserSession.currentEmployee?.id
                    ?: throw Exception("Ошибка сессии")

                // Вызываем обновленный метод репозитория
                repository.updateStatus(bookingId, newStatus, currentManagerId)

                loadData()
            } catch (e: Exception) {
                // ... обработка ошибок ...
                println(e)
            }
        }
    }

    fun deleteBooking(bookingId: Int) {
        screenModelScope.launch {
            try {
                // Опционально: можно добавить состояние Loading
                repository.deleteBooking(bookingId)
                loadData() // Перезагружаем список, чтобы запись исчезла
            } catch (e: Exception) {
                _state.value = BookingsListState.Error("Не удалось удалить: ${e.message}")
            }
        }
    }

    fun payBooking(bookingId: Int, amount: Double) {
        screenModelScope.launch {
            try {
                // Симулируем оплату картой
                paymentRepository.addPayment(
                    bookingId = bookingId,
                    amount = amount,
                    method = "Карта *${(1000..9999).random()}"
                )
                // После оплаты обновляем список, чтобы увидеть новый статус
                loadData()
            } catch (e: Exception) {
                _state.value = BookingsListState.Error("Ошибка оплаты: ${e.message}")
            }
        }
    }

    fun sendReview(tourId: Int, rating: Int, comment: String) {
        screenModelScope.launch {
            try {
                val clientId = UserSession.currentClient?.id ?: throw Exception("Нет авторизации")

                // 1. Отправляем отзыв в базу
                reviewRepository.addReview(tourId, clientId, rating, comment)

                // 2. ВАЖНО: Перезагружаем список, чтобы обновить флаги hasReview
                loadData()

            } catch (e: Exception) {
                _state.value = BookingsListState.Error("Ошибка: ${e.message}")
            }
        }
    }
}