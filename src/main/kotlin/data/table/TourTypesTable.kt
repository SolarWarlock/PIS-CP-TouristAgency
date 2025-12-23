package data.table

import org.jetbrains.exposed.sql.Table

// Объект описывает структуру таблицы в БД
// Название в кавычках должно ТОЧНО совпадать с названием в PostgreSQL (обычно lowercase)
object TourTypesTable : Table("tourtypes") {
    // Колонки
    val id = integer("tourtypeid").autoIncrement()
    val typeName = varchar("typename", 100)
    val description = text("description").nullable()

    // Первичный ключ
    override val primaryKey = PrimaryKey(id)
}