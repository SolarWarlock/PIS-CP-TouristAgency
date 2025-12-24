package ui.screens.reports

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.model.ReviewItem
import data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewsScreenModel(
    private val repository: ReviewRepository
) : ScreenModel {

    private val _reviews = MutableStateFlow<List<ReviewItem>>(emptyList())
    val reviews = _reviews.asStateFlow()

    init {
        loadReviews()
    }

    private fun loadReviews() {
        screenModelScope.launch {
            try {
                _reviews.value = repository.getAllReviews()
            } catch (e: Exception) {
                println("Ошибка загрузки отзывов: ${e.message}")
            }
        }
    }
}