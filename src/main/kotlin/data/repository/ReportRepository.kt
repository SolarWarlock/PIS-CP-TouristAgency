package data.repository

import data.db.dbQuery
import data.model.ManagerKpi
import data.model.MonthlyRevenue
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager

class ReportRepository {

    // 1. KPI Менеджеров
    suspend fun getManagerPerformance(): List<ManagerKpi> = dbQuery {
        val list = mutableListOf<ManagerKpi>()

        val sql = "SELECT * FROM report_manager_performance('2020-01-01', '2030-12-31')"

        // ПОЛУЧАЕМ НАСТОЯЩИЙ JDBC CONNECTION
        val exposedConn = TransactionManager.current().connection
        val conn = (exposedConn as JdbcConnectionImpl).connection

        val stmt = conn.prepareStatement(sql)
        val rs = stmt.executeQuery()

        while (rs.next()) {
            list.add(
                ManagerKpi(
                    name = rs.getString("ManagerName"),

                    // БЫЛО: position = rs.getString("Position"),
                    // СТАЛО:
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

    // 2. Финансовая динамика
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

        // ПОЛУЧАЕМ НАСТОЯЩИЙ JDBC CONNECTION
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
}