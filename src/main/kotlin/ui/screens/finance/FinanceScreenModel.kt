package ui.screens.finance

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.model.Debtor
import data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FinanceState {
    object Loading : FinanceState()
    data class Content(val debtors: List<Debtor>) : FinanceState()
    data class Error(val message: String) : FinanceState()
}

class FinanceScreenModel(
    private val repository: PaymentRepository
) : ScreenModel {

    private val _state = MutableStateFlow<FinanceState>(FinanceState.Loading)
    val state = _state.asStateFlow()

    init {
        loadDebtors()
    }

    fun loadDebtors() {
        screenModelScope.launch {
            _state.value = FinanceState.Loading
            try {
                val list = repository.getDebtors()
                _state.value = FinanceState.Content(list)
            } catch (e: Exception) {
                _state.value = FinanceState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun processPayment(bookingId: Int, amount: Double, method: String) {
        screenModelScope.launch {
            try {
                repository.addPayment(bookingId, amount, method)
                loadDebtors() // Обновляем список (долг должен уменьшиться или исчезнуть)
            } catch (e: Exception) {
                _state.value = FinanceState.Error("Ошибка оплаты: ${e.message}")
            }
        }
    }
}