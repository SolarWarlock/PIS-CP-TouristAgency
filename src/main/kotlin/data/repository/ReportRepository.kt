package data.repository

import data.db.dbQuery
import data.model.ManagerKpi
import data.model.MonthlyRevenue
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import data.model.DebtorReportItem
import data.model.PaymentLogItem
import java.time.format.DateTimeFormatter

class ReportRepository {

    // KPI Менеджеров
    suspend fun getManagerPerformance(): List<ManagerKpi> = dbQuery {
        val list = mutableListOf<ManagerKpi>()

        val sql = "SELECT * FROM report_manager_performance('2020-01-01', '2030-12-31')"


        val exposedConn = TransactionManager.current().connection
        val conn = (exposedConn as JdbcConnectionImpl).connection

        val stmt = conn.prepareStatement(sql)
        val rs = stmt.executeQuery()

        while (rs.next()) {
            list.add(
                ManagerKpi(
                    name = rs.getString("ManagerName"),
                    position = rs.getString("ManagerPos"),
                    toursSold = rs.getInt("ToursSold"),
                    revenue = rs.getBigDecimal("TotalRevenue").toDouble()
                )
            )
        }

        rs.close()
        stmt.close()

        return@dbQuery list
    }

    // Финансовая динамика
    suspend fun getMonthlyRevenue(): List<MonthlyRevenue> = dbQuery {
        val list = mutableListOf<MonthlyRevenue>()

        val sql = """
            SELECT TO_CHAR(PaymentDate, 'YYYY-MM') AS Month, 
                   SUM(Amount) AS TotalRevenue, 
                   COUNT(PaymentID) AS TransactionsCount 
            FROM Payments 
            GROUP BY TO_CHAR(PaymentDate, 'YYYY-MM') 
            ORDER BY Month DESC
        """.trimIndent()

        val exposedConn = TransactionManager.current().connection
        val conn = (exposedConn as JdbcConnectionImpl).connection

        val stmt = conn.prepareStatement(sql)
        val rs = stmt.executeQuery()

        while (rs.next()) {
            list.add(
                MonthlyRevenue(
                    month = rs.getString("Month"),
                    revenue = rs.getBigDecimal("TotalRevenue").toDouble(),
                    transactions = rs.getInt("TransactionsCount")
                )
            )
        }

        rs.close()
        stmt.close()

        return@dbQuery list
    }

    // Отчет по должникам
    suspend fun getDebtorsReport(): List<DebtorReportItem> = dbQuery {
        val list = mutableListOf<DebtorReportItem>()
        val sql = "SELECT * FROM report_debtors()"
        val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
        val rs = conn.createStatement().executeQuery(sql)

        while (rs.next()) {
            list.add(DebtorReportItem(
                client = rs.getString("ClientName"),
                tour = rs.getString("TourName"),
                date = rs.getDate("BookingDate").toString(),
                total = rs.getBigDecimal("TotalCost").toDouble(),
                paid = rs.getBigDecimal("PaidAmount").toDouble(),
                debt = rs.getBigDecimal("Debt").toDouble()
            ))
        }
        return@dbQuery list
    }

    // Журнал всех платежей (Детализация)
    suspend fun getAllPaymentsLog(): List<PaymentLogItem> = dbQuery {
        val list = mutableListOf<PaymentLogItem>()
        val sql = "SELECT * FROM report_payments_by_period('2000-01-01', '2100-01-01')"
        val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
        val rs = conn.createStatement().executeQuery(sql)

        while (rs.next()) {
            list.add(PaymentLogItem(
                id = rs.getInt("PaymentID"),
                date = rs.getTimestamp("PaymentDate").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                client = rs.getString("ClientName"),
                amount = rs.getBigDecimal("Amount").toDouble(),
                method = rs.getString("PaymentMethod") ?: "-"
            ))
        }
        return@dbQuery list
    }
}