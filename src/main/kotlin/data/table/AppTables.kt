package data.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

// --- СПРАВОЧНИКИ И ЛЮДИ ---

object ClientsTable : Table("clients") {
    val id = integer("clientid").autoIncrement()
    val firstName = varchar("firstname", 50)
    val lastName = varchar("lastname", 50)
    val phone = varchar("phone", 20).nullable()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("passwordhash", 255)
    val passportData = varchar("passportdata", 100).nullable()
    val registrationDate = datetime("registrationdate")

    override val primaryKey = PrimaryKey(id)
}

object ManagersTable : Table("managers") {
    val id = integer("managerid").autoIncrement()
    val firstName = varchar("firstname", 50)
    val lastName = varchar("lastname", 50)
    val phone = varchar("phone", 20).nullable()
    val email = varchar("email", 100).nullable()
    val position = varchar("position", 50)
    val hireDate = date("hiredate")
    val dbLogin = varchar("db_login", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}

object PartnersTable : Table("partners") {
    val id = integer("partnerid").autoIncrement()
    val name = varchar("name", 100)
    val type = varchar("partnertype", 50)
    val contacts = varchar("contacts", 200).nullable()

    override val primaryKey = PrimaryKey(id)
}

// --- ПРОДУКТ (ТУРЫ) ---

object ToursTable : Table("tours") {
    val id = integer("tourid").autoIncrement()
    val typeId = integer("tourtypeid") references TourTypesTable.id
    val managerId = integer("managerid") references ManagersTable.id
    val partnerId = integer("partnerid") references PartnersTable.id

    val destination = varchar("destination", 100)
    val startDate = date("startdate")
    val endDate = date("enddate")
    val baseCost = decimal("basecost", 10, 2)
    val isActive = bool("isactive").default(true)
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

object AccommodationsTable : Table("accommodations") {
    val id = integer("accommodationid").autoIncrement()
    val tourId = integer("tourid") references ToursTable.id
    val type = varchar("type", 100)
    val cost = decimal("cost", 10, 2)
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

// --- ПРОДАЖИ И ФИНАНСЫ ---

object BookingsTable : Table("bookings") {
    val id = integer("bookingid").autoIncrement()
    val tourId = integer("tourid") references ToursTable.id
    val clientId = integer("clientid") references ClientsTable.id

    // ИСПРАВЛЕНИЕ 1: Поменяли порядок. Сначала references, потом nullable
    val managerId = integer("managerid").references(ManagersTable.id).nullable()

    val bookingDate = datetime("bookingdate")
    val status = varchar("status", 20)
    val paymentStatus = varchar("paymentstatus", 20)
    val finalPrice = decimal("finalprice", 10, 2)

    override val primaryKey = PrimaryKey(id)
}

object PaymentsTable : Table("payments") {
    val id = integer("paymentid").autoIncrement()
    val bookingId = integer("bookingid") references BookingsTable.id

    val date = datetime("paymentdate")
    val amount = decimal("amount", 10, 2)
    val method = varchar("paymentmethod", 50).nullable()
    val transactionInfo = varchar("transactioninfo", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

object AuditLogTable : Table("auditlog") {
    val id = long("logid").autoIncrement()
    val eventDate = datetime("eventdate")
    val dbUser = varchar("dbuser", 100)
    val operation = varchar("operationtype", 10)
    val targetTable = varchar("tablename", 50)
    val details = text("changedfields").nullable()

    override val primaryKey = PrimaryKey(id)
}