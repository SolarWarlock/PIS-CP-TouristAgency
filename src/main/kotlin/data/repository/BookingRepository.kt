package data.repository

import data.db.dbQuery
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.ResultSet
import data.db.dbQuery
import data.model.Booking
import data.table.BookingsTable
import data.table.ClientsTable
import data.table.ToursTable
import org.jetbrains.exposed.sql.*
import java.time.format.DateTimeFormatter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl

class BookingRepository {

    suspend fun createBooking(tourId: Int, clientId: Int, managerId: Int?): Int = dbQuery {
        var newBookingId = -1

        // Формируем строку с NULL или числом вручную, так как это безопасно для Int
        val managerVal = managerId?.toString() ?: "NULL"
        val query = "SELECT create_booking($tourId, $clientId, $managerVal)"

        TransactionManager.current().exec(query) { rs: ResultSet ->
            if (rs.next()) {
                newBookingId = rs.getInt(1)
            }
        }

        return@dbQuery newBookingId
    }

    // 1. Получить список бронирований для Менеджера
    suspend fun getBookingsForManager(managerId: Int): List<Booking> = dbQuery {
        val bookings = mutableListOf<Booking>()

        // Добавили подзапрос для PaidAmount
        val sql = """
            SELECT b.BookingID, t.Destination, c.FirstName, c.LastName, b.BookingDate, b.Status, b.PaymentStatus, b.FinalPrice,
                   (SELECT COALESCE(SUM(Amount), 0) FROM Payments p WHERE p.BookingID = b.BookingID) as PaidAmount
            FROM Bookings b
            JOIN Clients c ON b.ClientID = c.ClientID
            JOIN Tours t ON b.TourID = t.TourID
            WHERE b.ManagerID = $managerId
            ORDER BY b.BookingDate DESC
        """.trimIndent()

        val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery(sql)

        while (rs.next()) {
            bookings.add(mapRowToBooking(rs, hasClientName = true))
        }

        return@dbQuery bookings
    }

    // 3. Список для КЛИЕНТА (с подсчетом оплат)
    suspend fun getBookingsForClient(clientId: Int): List<Booking> = dbQuery {
        val bookings = mutableListOf<Booking>()

        val sql = """
            SELECT b.BookingID, t.Destination, b.BookingDate, b.Status, b.PaymentStatus, b.FinalPrice,
                   (SELECT COALESCE(SUM(Amount), 0) FROM Payments p WHERE p.BookingID = b.BookingID) as PaidAmount
            FROM Bookings b
            JOIN Tours t ON b.TourID = t.TourID
            WHERE b.ClientID = $clientId
            ORDER BY b.BookingDate DESC
        """.trimIndent()

        val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery(sql)

        while (rs.next()) {
            bookings.add(mapRowToBooking(rs, hasClientName = false))
        }

        return@dbQuery bookings
    }

    // Вспомогательный метод для маппинга
    private fun mapRowToBooking(rs: ResultSet, hasClientName: Boolean): Booking {
        // Форматируем дату красиво
        val rawDate = rs.getTimestamp("BookingDate").toLocalDateTime()
        val formattedDate = rawDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

        val clientName = if (hasClientName) "${rs.getString("FirstName")} ${rs.getString("LastName")}" else null

        return Booking(
            id = rs.getInt("BookingID"),
            tourName = rs.getString("Destination"),
            clientName = clientName,
            date = formattedDate,
            status = rs.getString("Status"),
            paymentStatus = rs.getString("PaymentStatus"),
            price = rs.getBigDecimal("FinalPrice").toDouble(),
            paidAmount = rs.getBigDecimal("PaidAmount").toDouble() // <--- Читаем реальную сумму!
        )
    }

    // 4. Смена статуса (Без изменений)
    suspend fun updateStatus(bookingId: Int, newStatus: String) = dbQuery {
        val sql = "UPDATE Bookings SET Status = ? WHERE BookingID = ?"
        val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
        val stmt = conn.prepareStatement(sql)
        stmt.setString(1, newStatus)
        stmt.setInt(2, bookingId)
        stmt.executeUpdate()
    }

    // 5. Удаление (Без изменений)
    suspend fun deleteBooking(bookingId: Int) = dbQuery {
        BookingsTable.deleteWhere { BookingsTable.id eq bookingId }
    }
}