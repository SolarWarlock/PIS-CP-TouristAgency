package ui.screens.tours

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.model.Tour
import data.repository.TourRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import data.model.LookupItem
import ui.common.UserSession
import java.time.LocalDate

// Состояние экрана
sealed class ToursState {
    object Loading : ToursState()
    data class Content(val tours: List<Tour>) : ToursState()
    data class Error(val message: String) : ToursState()
}

class ToursScreenModel(
    private val repository: TourRepository
) : ScreenModel {

    private val _state = MutableStateFlow<ToursState>(ToursState.Loading)
    val state = _state.asStateFlow()

    // Загрузка данных при старте
    init {
        loadTours()
    }

    fun loadTours() {
        screenModelScope.launch {
            _state.value = ToursState.Loading
            try {
                val tours = repository.getTours() // Вызываем наш репозиторий
                _state.value = ToursState.Content(tours)
            } catch (e: Exception) {
                _state.value = ToursState.Error("Не удалось загрузить туры: ${e.message}")
            }
        }
    }

    private val _types = MutableStateFlow<List<LookupItem>>(emptyList())
    val types = _types.asStateFlow()

    private val _partners = MutableStateFlow<List<LookupItem>>(emptyList())
    val partners = _partners.asStateFlow()

    fun loadDictionaries() {
        screenModelScope.launch {
            _types.value = repository.getTourTypes()
            _partners.value = repository.getPartners()
        }
    }

    fun createTour(
        typeId: Int,
        partnerId: Int,
        destination: String,
        dateStart: String, // Строка YYYY-MM-DD
        dateEnd: String,
        cost: String,
        description: String
    ) {
        screenModelScope.launch {
            try {
                val managerId = UserSession.currentEmployee?.id ?: throw Exception("Не найден менеджер")

                // Парсим данные
                val start = LocalDate.parse(dateStart)
                val end = LocalDate.parse(dateEnd)
                val price = cost.toDouble()

                repository.createTour(typeId, managerId, partnerId, destination, start, end, price, description)

                // Обновляем список
                loadTours()
            } catch (e: Exception) {
                _state.value = ToursState.Error("Ошибка создания: ${e.message}")
            }
        }
    }

    fun updateTour(
        id: Int,
        typeId: Int,
        partnerId: Int,
        destination: String,
        dateStart: String,
        dateEnd: String,
        cost: String,
        description: String
    ) {
        screenModelScope.launch {
            try {
                val start = LocalDate.parse(dateStart)
                val end = LocalDate.parse(dateEnd)
                val price = cost.toDouble()

                repository.updateTour(id, typeId, partnerId, destination, start, end, price, description)
                loadTours()
            } catch (e: Exception) {
                _state.value = ToursState.Error("Ошибка обновления: ${e.message}")
            }
        }
    }

    fun deleteTour(id: Int) {
        screenModelScope.launch {
            try {
                repository.deleteTour(id)
                loadTours()
            } catch (e: Exception) {
                _state.value = ToursState.Error("Нельзя удалить тур (возможно, есть брони): ${e.message}")
            }
        }
    }
}