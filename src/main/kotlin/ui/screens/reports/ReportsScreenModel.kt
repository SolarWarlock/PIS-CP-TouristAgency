package ui.screens.reports

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import util.ExcelService

class ReportsScreenModel(
    private val repository: ReportRepository
) : ScreenModel {

    private val _status = MutableStateFlow<String>("")
    val status = _status.asStateFlow()

    fun exportManagerKpi(path: String) {
        screenModelScope.launch {
            try {
                _status.value = "Генерация..."
                val data = repository.getManagerPerformance()
                ExcelService.generateManagerReport(data, path)
                _status.value = "Успешно сохранено: $path"
            } catch (e: Exception) {
                _status.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun exportFinance(path: String) {
        screenModelScope.launch {
            try {
                _status.value = "Генерация..."
                val data = repository.getMonthlyRevenue()
                ExcelService.generateFinanceReport(data, path)
                _status.value = "Успешно сохранено: $path"
            } catch (e: Exception) {
                _status.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun exportDebtors(path: String) {
        screenModelScope.launch {
            try {
                val data = repository.getDebtorsReport()
                ExcelService.generateDebtorsReport(data, path)
                _status.value = "Сохранено: $path"
            } catch (e: Exception) { _status.value = "Ошибка: ${e.message}" }
        }
    }

    fun exportPaymentLog(path: String) {
        screenModelScope.launch {
            try {
                val data = repository.getAllPaymentsLog()
                ExcelService.generatePaymentLogReport(data, path)
                _status.value = "Сохранено: $path"
            } catch (e: Exception) { _status.value = "Ошибка: ${e.message}" }
        }
    }
}