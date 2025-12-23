package data.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DriverManager

object DatabaseFactory {
    private var database: Database? = null

    /**
     * Инициализация подключения к БД.
     * @param user Логин пользователя PostgreSQL (например, "client_app_bot")
     * @param password Пароль пользователя
     */
    fun connect(user: String, password: String): Boolean {
        return try {
            // URL подключения.
            // localhost:5432 - стандартный адрес
            // TouristAgency - имя твоей базы (проверь в pgAdmin!)
            val url = "jdbc:postgresql://localhost:5432/TravelAgency"
            val driver = "org.postgresql.Driver"

            // 1. Пытаемся подключиться через JDBC драйвер напрямую, чтобы проверить пароль
            // (Exposed не всегда выбрасывает ошибку сразу при connect, поэтому проверим вручную)
            DriverManager.getConnection(url, user, password).close()

            // 2. Если успех, инициализируем Exposed
            database = Database.connect(
                url = url,
                driver = driver,
                user = user,
                password = password
            )

            // 3. Настраиваем логирование SQL в консоль (для отладки)
            transaction {
                addLogger(StdOutSqlLogger)
            }

            println("Успешное подключение к БД под пользователем: $user")
            true
        } catch (e: Exception) {
            println("Ошибка подключения: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Получение доступа к базе.
     * Если база не инициализирована — выбросит ошибку.
     */
    fun get(): Database {
        return database ?: throw IllegalStateException("База данных не подключена. Сначала вызови connect()")
    }
}