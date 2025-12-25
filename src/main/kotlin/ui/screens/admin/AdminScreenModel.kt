package ui.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.model.AuditLogEntry
import data.model.Employee
import data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import util.ExcelService


sealed class AdminState {
    object Loading : AdminState()
    data class EmployeesContent(val list: List<Employee>) : AdminState()
    data class LogsContent(val list: List<AuditLogEntry>) : AdminState()
    data class Error(val message: String) : AdminState()
}

class AdminScreenModel(
    private val repository: AdminRepository
) : ScreenModel {

    private val _state = MutableStateFlow<AdminState>(AdminState.Loading)
    val state = _state.asStateFlow()

    // Загрузка сотрудников
    fun loadEmployees() {
        screenModelScope.launch {
            _state.value = AdminState.Loading
            try {
                val list = repository.getEmployees()
                _state.value = AdminState.EmployeesContent(list)
            } catch (e: Exception) {
                _state.value = AdminState.Error(e.message ?: "Ошибка")
            }
        }
    }

    // Добавление сотрудника
    fun createEmployee(f: String, l: String, p: String, e: String, pos: String, login: String) {
        screenModelScope.launch {
            try {
                repository.addEmployee(f, l, p, e, pos, login)
                loadEmployees() // Обновляем список
            } catch (ex: Exception) {
                _state.value = AdminState.Error(ex.message ?: "Ошибка добавления")
            }
        }
    }

    // Загрузка логов
    fun loadLogs() {
        screenModelScope.launch {
            _state.value = AdminState.Loading
            try {
                val list = repository.getAuditLogs()
                _state.value = AdminState.LogsContent(list)
            } catch (e: Exception) {
                _state.value = AdminState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun editEmployee(id: Int, f: String, l: String, p: String, e: String, pos: String) {
        screenModelScope.launch {
            try {
                repository.updateEmployee(id, f, l, p, e, pos)
                loadEmployees() // Обновляем список
            } catch (ex: Exception) {
                _state.value = AdminState.Error(ex.message ?: "Ошибка обновления")
            }
        }
    }

    fun deleteEmployee(id: Int) {
        screenModelScope.launch {
            try {
                repository.deleteEmployee(id)
                loadEmployees() // Обновляем список
            } catch (ex: Exception) {
                // Если есть связанные записи (брони), база вернет ошибку, и мы покажем её
                _state.value = AdminState.Error("Нельзя удалить: у сотрудника есть связанные данные (туры или брони).")
            }
        }
    }

    fun exportLogs(path: String) {
        screenModelScope.launch {
            try {
                // 1. Загружаем свежие данные из БД
                val logs = repository.getAuditLogs()

                // 2. Генерируем файл
                ExcelService.generateAuditReport(logs, path)

                // (Опционально) Можно обновить состояние или вывести лог в консоль
                println("Логи успешно выгружены в $path")
            } catch (e: Exception) {
                println("Ошибка экспорта: ${e.message}")
            }
        }
    }

    fun createBackup(path: String) {
        screenModelScope.launch {
            try {
                // Сообщаем, что процесс пошел
                _state.value = AdminState.Loading // Или можно сделать отдельное состояние для сообщений

                val isSuccess = util.BackupService.makeBackup(path)

                if (isSuccess) {
                    // После успеха перезагружаем логи, чтобы вернуть состояние контента
                    loadLogs()
                    println("Бэкап успешно создан: $path")
                } else {
                    _state.value = AdminState.Error("Ошибка создания бэкапа (см. консоль). Проверьте, установлен ли pg_dump в PATH.")
                }
            } catch (e: Exception) {
                _state.value = AdminState.Error("Ошибка: ${e.message}")
            }
        }
    }
}