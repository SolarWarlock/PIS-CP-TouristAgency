package data.repository

import data.db.dbQuery
import data.model.ReviewItem
import data.table.ClientsTable
import data.table.ReviewsTable
import data.table.ToursTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReviewRepository {

    // 1. Добавить отзыв (вызывает клиент)
    suspend fun addReview(
        tourId: Int,
        clientId: Int,
        rating: Int,
        comment: String
    ) = dbQuery {
        // Пробуем вставить. Если такой отзыв уже есть, БД выкинет ошибку Unique Constraint,
        // которую мы перехватим в ViewModel
        ReviewsTable.insert {
            it[this.tourId] = tourId
            it[this.clientId] = clientId
            it[this.rating] = rating
            it[this.description] = comment
            it[this.date] = LocalDateTime.now()
        }
    }

    // 2. Получить все отзывы (для менеджера/финансиста)
    suspend fun getAllReviews(): List<ReviewItem> = dbQuery {
        (ReviewsTable innerJoin ToursTable innerJoin ClientsTable)
            .selectAll()
            .orderBy(ReviewsTable.date, SortOrder.DESC)
            .map { row ->
                ReviewItem(
                    id = row[ReviewsTable.id],
                    tourName = row[ToursTable.destination],
                    clientName = "${row[ClientsTable.firstName]} ${row[ClientsTable.lastName]}",
                    date = row[ReviewsTable.date].format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    rating = row[ReviewsTable.rating],
                    comment = row[ReviewsTable.description] ?: ""
                )
            }
    }
}