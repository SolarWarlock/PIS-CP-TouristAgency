package data.repository

import data.db.dbQuery
import data.model.AuditLogEntry
import data.model.Employee
import data.table.AuditLogTable
import data.table.ManagersTable
import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class AdminRepository {

    // --- СОТРУДНИКИ ---

    suspend fun getEmployees(): List<Employee> = dbQuery {
        ManagersTable.selectAll().map {
            Employee(
                id = it[ManagersTable.id],
                fio = "${it[ManagersTable.firstName]} ${it[ManagersTable.lastName]}",
                position = it[ManagersTable.position],
                dbLogin = it[ManagersTable.dbLogin] ?: "-"
            )
        }
    }

    suspend fun addEmployee(
        firstNameParam: String,
        lastNameParam: String,
        phoneParam: String,
        emailParam: String,
        positionParam: String,
        dbLoginParam: String
    ) = dbQuery {
        ManagersTable.insert {
            // Теперь конфликтов имен нет:
            // Слева - колонка таблицы, Справа - аргумент функции
            it[firstName] = firstNameParam
            it[lastName] = lastNameParam
            it[phone] = phoneParam
            it[email] = emailParam
            it[position] = positionParam
            it[hireDate] = LocalDate.now()
            it[dbLogin] = dbLoginParam
        }
    }

    // Обновление данных сотрудника
    suspend fun updateEmployee(
        id: Int,
        firstNameParam: String,
        lastNameParam: String,
        phoneParam: String,
        emailParam: String,
        positionParam: String
    ) = dbQuery {
        ManagersTable.update({ ManagersTable.id eq id }) {
            it[firstName] = firstNameParam
            it[lastName] = lastNameParam
            it[phone] = phoneParam
            it[email] = emailParam
            it[position] = positionParam
        }
    }

    // Удаление сотрудника
    suspend fun deleteEmployee(id: Int) = dbQuery {
        ManagersTable.deleteWhere { ManagersTable.id eq id }
    }

    // --- ЖУРНАЛ АУДИТА ---

    suspend fun getAuditLogs(): List<AuditLogEntry> = dbQuery {
        AuditLogTable.selectAll()
            .orderBy(AuditLogTable.eventDate, SortOrder.DESC)
            .limit(100)
            .map {
                AuditLogEntry(
                    id = it[AuditLogTable.id],
                    date = it[AuditLogTable.eventDate].format(DateTimeFormatter.ofPattern("dd.MM HH:mm:ss")),
                    user = it[AuditLogTable.dbUser],
                    operation = it[AuditLogTable.operation],
                    table = it[AuditLogTable.targetTable],
                    details = it[AuditLogTable.details]
                )
            }
    }
}