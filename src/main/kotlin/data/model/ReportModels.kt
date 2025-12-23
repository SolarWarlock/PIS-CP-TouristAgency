package data.model

// Строка отчета по менеджерам
data class ManagerKpi(
    val name: String,
    val position: String,
    val toursSold: Int,
    val revenue: Double
)

// Строка финансового отчета
data class MonthlyRevenue(
    val month: String, // "2025-01"
    val revenue: Double,
    val transactions: Int
)