package ui.common

import data.model.Client
import data.model.Employee

object UserSession {
    var currentClient: Client? = null
    var currentEmployee: Employee? = null


    // 1. Это Клиент? (Если есть объект клиента)
    val isClient: Boolean
        get() = currentClient != null

    // 2. Это Сотрудник?
    val isEmployee: Boolean
        get() = currentEmployee != null

    // 3. Это Администратор?
    val isAdmin: Boolean
        get() = currentEmployee?.position == "Администратор"

    // 4. Это Финансист?
    val isFinancier: Boolean
        get() = currentEmployee?.position == "Финансист"

    // 5. Это Менеджер? (Любой сотрудник, который не Админ и не Финансист)
    // Либо можно перечислить конкретные должности: "Менеджер", "Старший" и т.д.
    val isManager: Boolean
        get() = currentEmployee != null &&
                (currentEmployee?.position == "Менеджер" || currentEmployee?.position == "Старший")

    // Очистка при выходе
    fun clear() {
        currentClient = null
        currentEmployee = null
    }
}

