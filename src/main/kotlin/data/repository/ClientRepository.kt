package data.repository

import data.db.dbQuery
import data.model.Client
import data.table.ClientsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ClientRepository {

    // 1. Поиск
    suspend fun searchClients(query: String): List<Client> = dbQuery {
        ClientsTable.selectAll()
            .where {
                (ClientsTable.firstName like "%$query%") or
                        (ClientsTable.lastName like "%$query%") or
                        (ClientsTable.phone like "%$query%")
            }
            .limit(20)
            .map { mapRowToClient(it) }
    }

    // 2. Получить всех клиентов
    suspend fun getAllClients(): List<Client> = dbQuery {
        ClientsTable.selectAll()
            .orderBy(ClientsTable.registrationDate, SortOrder.DESC)
            .limit(100)
            .map { mapRowToClient(it) }
    }

    // 3. Обновить данные клиента
    suspend fun updateClient(client: Client, passportData: String) = dbQuery {
        ClientsTable.update({ ClientsTable.id eq client.id }) {
            it[firstName] = client.firstName
            it[lastName] = client.lastName
            it[phone] = client.phone
            // ИСПРАВЛЕНИЕ: Явно указываем таблицу ClientsTable, чтобы не путать с аргументом функции
            it[ClientsTable.passportData] = passportData
        }
    }

    // 4. Получить паспортные данные
    suspend fun getPassportData(clientId: Int): String = dbQuery {
        // Используем select (выбор колонок), а не slice (который устарел)
        ClientsTable.select(ClientsTable.passportData)
            .where { ClientsTable.id eq clientId }
            .singleOrNull()
            ?.get(ClientsTable.passportData) ?: ""
    }

    // Вспомогательный маппер
    private fun mapRowToClient(row: ResultRow) = Client(
        id = row[ClientsTable.id],
        firstName = row[ClientsTable.firstName],
        lastName = row[ClientsTable.lastName],
        email = row[ClientsTable.email],
        phone = row[ClientsTable.phone]
    )
}