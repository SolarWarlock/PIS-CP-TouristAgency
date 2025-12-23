package data.repository

import data.db.dbQuery
import data.model.Debtor
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager

class PaymentRepository {

    // 1. Получение списка неоплаченных броней (Реестр должников)
    suspend fun getDebtors(): List<Debtor> = dbQuery {
        val debtors = mutableListOf<Debtor>()

        val sql = """
            SELECT b.BookingID, 
                   c.FirstName || ' ' || c.LastName AS ClientName, 
                   t.Destination, 
                   b.FinalPrice, 
                   (SELECT COALESCE(SUM(Amount), 0) FROM Payments p WHERE p.BookingID = b.BookingID) AS PaidAmount, 
                   b.FinalPrice - (SELECT COALESCE(SUM(Amount), 0) FROM Payments p WHERE p.BookingID = b.BookingID) AS Debt 
            FROM Bookings b 
            JOIN Clients c ON b.ClientID = c.ClientID 
            JOIN Tours t ON b.TourID = t.TourID 
            WHERE b.PaymentStatus IN ('Не оплачено', 'Частично') 
              AND b.Status != 'Аннулировано' 
            ORDER BY b.BookingDate
        """.trimIndent()

        // ИСПРАВЛЕНИЕ: Достаем "настоящее" соединение JDBC
        val exposedConn = TransactionManager.current().connection
        val conn = (exposedConn as JdbcConnectionImpl).connection

        val stmt = conn.prepareStatement(sql)
        val rs = stmt.executeQuery()

        while (rs.next()) {
            debtors.add(
                Debtor(
                    bookingId = rs.getInt("BookingID"),
                    clientName = rs.getString("ClientName"),
                    tourName = rs.getString("Destination"),
                    totalPrice = rs.getBigDecimal("FinalPrice").toDouble(),
                    paidAmount = rs.getBigDecimal("PaidAmount").toDouble(),
                    debt = rs.getBigDecimal("Debt").toDouble()
                )
            )
        }

        // Закрываем ресурсы
        rs.close()
        stmt.close()

        return@dbQuery debtors
    }

    // 2. Внесение платежа (Вызов процедуры add_payment)
    suspend fun addPayment(bookingId: Int, amount: Double, method: String) = dbQuery {
        val sql = "CALL add_payment(?, ?, ?, ?)"

        // ИСПРАВЛЕНИЕ: Достаем "настоящее" соединение JDBC
        val exposedConn = TransactionManager.current().connection
        val conn = (exposedConn as JdbcConnectionImpl).connection

        val stmt = conn.prepareStatement(sql)

        stmt.setInt(1, bookingId)
        stmt.setBigDecimal(2, amount.toBigDecimal())
        stmt.setString(3, method)
        stmt.setString(4, "Оплата через Desktop App")

        stmt.execute()
        stmt.close()
    }
}