package data.model

import java.time.LocalDate
import java.time.LocalDateTime


data class Employee(
    val id: Int,
    val fio: String,
    val position: String,
    val dbLogin: String
)

data class Client(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?
)

data class Debtor(
    val bookingId: Int,
    val clientName: String,
    val tourName: String,
    val totalPrice: Double,
    val paidAmount: Double,
    val debt: Double // Остаток долга
)


data class AuditLogEntry(
    val id: Long,
    val date: String,
    val user: String,
    val operation: String,
    val table: String,
    val details: String?
)

data class LookupItem(
    val id: Int,
    val name: String
)

data class Tour(
    val id: Int,
    val destination: String,
    val typeName: String,
    val partnerName: String,
    val cost: Double,
    val dates: String,
    val description: String?,
    val rating: Double?
)

data class ReviewItem(
    val id: Int,
    val tourName: String,
    val clientName: String,
    val date: String,
    val rating: Int,
    val comment: String
)

data class Booking(
    val id: Int,
    val tourId: Int,
    val tourName: String,
    val clientName: String?,
    val date: String,
    val status: String,
    val paymentStatus: String,
    val price: Double,
    val paidAmount: Double,
    val hasReview: Boolean = false //
)

data class DebtorReportItem(
    val client: String,
    val tour: String,
    val date: String,
    val total: Double,
    val paid: Double,
    val debt: Double
)

data class PaymentLogItem(
    val id: Int,
    val date: String,
    val client: String,
    val amount: Double,
    val method: String
)
