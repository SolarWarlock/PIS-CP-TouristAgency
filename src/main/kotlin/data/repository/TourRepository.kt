package data.repository

import data.db.dbQuery
import data.model.LookupItem
import data.model.Tour
import data.table.PartnersTable
import data.table.TourTypesTable
import data.table.ToursTable
import org.jetbrains.exposed.sql.* // Импорт всего SQL DSL
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq // Важный импорт для eq
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TourRepository {

    // Получение списка туров с фильтрацией
    suspend fun getTours(
        searchQuery: String = "",
        maxPrice: Double? = null
    ): List<Tour> = dbQuery {

        val query = (ToursTable innerJoin TourTypesTable innerJoin PartnersTable)
            .selectAll()

        if (searchQuery.isNotEmpty()) {
            query.andWhere { ToursTable.destination like "%$searchQuery%" }
        }
        if (maxPrice != null) {
            query.andWhere { ToursTable.baseCost lessEq maxPrice.toBigDecimal() }
        }

        query.map { row ->
            val start = row[ToursTable.startDate].format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            val end = row[ToursTable.endDate].format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

            Tour(
                id = row[ToursTable.id],
                destination = row[ToursTable.destination],
                typeName = row[TourTypesTable.typeName],
                partnerName = row[PartnersTable.name],
                cost = row[ToursTable.baseCost].toDouble(),
                dates = "$start - $end",
                description = row[ToursTable.description]
            )
        }
    }

    suspend fun getTourTypes(): List<LookupItem> = dbQuery {
        TourTypesTable.selectAll().map {
            LookupItem(it[TourTypesTable.id], it[TourTypesTable.typeName])
        }
    }

    suspend fun getPartners(): List<LookupItem> = dbQuery {
        PartnersTable.selectAll().map {
            LookupItem(it[PartnersTable.id], it[PartnersTable.name])
        }
    }

    suspend fun createTour(
        typeId: Int, managerId: Int, partnerId: Int, destination: String,
        startDate: LocalDate, endDate: LocalDate, cost: Double, desc: String
    ) = dbQuery {
        val sql = "SELECT create_tour(?, ?, ?, ?, ?, ?, ?, ?)"
        val exposedConn = TransactionManager.current().connection
        val conn = (exposedConn as JdbcConnectionImpl).connection
        val stmt = conn.prepareStatement(sql)

        stmt.setInt(1, typeId)
        stmt.setInt(2, managerId)
        stmt.setInt(3, partnerId)
        stmt.setString(4, destination)
        stmt.setDate(5, Date.valueOf(startDate))
        stmt.setDate(6, Date.valueOf(endDate))
        stmt.setBigDecimal(7, cost.toBigDecimal())
        stmt.setString(8, desc)

        stmt.execute()
        stmt.close()
    }

    // ИСПРАВЛЕННЫЙ МЕТОД UPDATE
    suspend fun updateTour(
        id: Int, typeId: Int, partnerId: Int, destination: String,
        startDate: LocalDate, endDate: LocalDate, cost: Double, desc: String
    ) = dbQuery {
        // Используем явное указание таблицы (ToursTable.поле)
        ToursTable.update({ ToursTable.id eq id }) {
            it[ToursTable.typeId] = typeId
            it[ToursTable.partnerId] = partnerId
            it[ToursTable.destination] = destination
            it[ToursTable.startDate] = startDate
            it[ToursTable.endDate] = endDate
            it[ToursTable.baseCost] = cost.toBigDecimal()
            it[ToursTable.description] = desc
        }
    }

    // ИСПРАВЛЕННЫЙ МЕТОД DELETE
    suspend fun deleteTour(id: Int) = dbQuery {
        ToursTable.deleteWhere { ToursTable.id eq id }
    }
}