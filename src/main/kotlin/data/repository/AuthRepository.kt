package data.repository

import data.db.dbQuery
import data.model.Client
import data.model.Employee
import data.table.ClientsTable
import data.table.ManagersTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.ResultSet

class AuthRepository {

    // 1. Вход для Клиента (вызов функции check_credentials)
    suspend fun loginClient(email: String, passHash: String): Client? = dbQuery {
        // Exposed не имеет красивой обертки для хранимых функций с возвратом скалярного значения,
        // поэтому используем чистый SQL для вызова функции из пункта 3.5.
        val query = "SELECT check_credentials('$email', '$passHash')"

        var clientId = -1

        TransactionManager.current().exec(query) { rs: ResultSet ->
            if (rs.next()) {
                clientId = rs.getInt(1)
            }
        }

        if (clientId > 0) {
            // Если ID > 0, загружаем данные клиента
            ClientsTable.select { ClientsTable.id eq clientId }
                .map {
                    Client(
                        id = it[ClientsTable.id],
                        firstName = it[ClientsTable.firstName],
                        lastName = it[ClientsTable.lastName],
                        email = it[ClientsTable.email],
                        phone = it[ClientsTable.phone]
                    )
                }
                .singleOrNull()
        } else {
            null
        }
    }

    // 2. Вход для Сотрудника (получение инфо по текущему DB пользователю)
    suspend fun getCountEmployeeInfo(dbLogin: String): Employee? = dbQuery {
        ManagersTable.select { ManagersTable.dbLogin eq dbLogin }
            .map {
                Employee(
                    id = it[ManagersTable.id],
                    fio = "${it[ManagersTable.firstName]} ${it[ManagersTable.lastName]}",
                    position = it[ManagersTable.position],
                    dbLogin = it[ManagersTable.dbLogin] ?: ""
                )
            }
            .singleOrNull()
    }

    // 3. Регистрация (вызов процедуры register_client)
    suspend fun registerClient(
        firstName: String, lastName: String, phone: String,
        email: String, passHash: String, passport: String
    ) = dbQuery {
        // Вызов процедуры через CALL
        val sql = "CALL register_client('$firstName', '$lastName', '$phone', '$email', '$passHash', '$passport')"
        TransactionManager.current().exec(sql)
    }
}

