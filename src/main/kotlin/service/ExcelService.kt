package util

import data.model.ManagerKpi
import data.model.MonthlyRevenue
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import data.model.AuditLogEntry
import data.model.DebtorReportItem
import data.model.PaymentLogItem

object ExcelService {

    fun generateManagerReport(data: List<ManagerKpi>, filePath: String) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("KPI Менеджеров")

        // Заголовок
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("ФИО")
        headerRow.createCell(1).setCellValue("Должность")
        headerRow.createCell(2).setCellValue("Продано туров")
        headerRow.createCell(3).setCellValue("Выручка (Руб)")

        // Данные
        data.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(item.name)
            row.createCell(1).setCellValue(item.position)
            row.createCell(2).setCellValue(item.toursSold.toDouble())
            row.createCell(3).setCellValue(item.revenue)
        }

        // Автоширина колонок
        for (i in 0..3) sheet.autoSizeColumn(i)

        // Сохранение
        FileOutputStream(filePath).use { workbook.write(it) }
        workbook.close()
    }

    fun generateFinanceReport(data: List<MonthlyRevenue>, filePath: String) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Выручка по месяцам")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Месяц")
        headerRow.createCell(1).setCellValue("Кол-во транзакций")
        headerRow.createCell(2).setCellValue("Выручка (Руб)")

        data.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(item.month)
            row.createCell(1).setCellValue(item.transactions.toDouble())
            row.createCell(2).setCellValue(item.revenue)
        }

        for (i in 0..2) sheet.autoSizeColumn(i)

        FileOutputStream(filePath).use { workbook.write(it) }
        workbook.close()
    }

    fun generateAuditReport(data: List<AuditLogEntry>, filePath: String) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Журнал аудита")

        // Заголовки
        val headerRow = sheet.createRow(0)
        val headers = listOf("ID", "Время", "Пользователь", "Действие", "Таблица", "Детали")
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).setCellValue(title)
        }

        // Данные
        data.forEachIndexed { index, log ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(log.id.toDouble())
            row.createCell(1).setCellValue(log.date)
            row.createCell(2).setCellValue(log.user)
            row.createCell(3).setCellValue(log.operation)
            row.createCell(4).setCellValue(log.table)
            row.createCell(5).setCellValue(log.details ?: "")
        }

        // Автоширина (кроме последней колонки Детали, она может быть длинной)
        for (i in 0..4) sheet.autoSizeColumn(i)
        sheet.setColumnWidth(5, 10000) // Фиксированная ширина для деталей

        FileOutputStream(filePath).use { workbook.write(it) }
        workbook.close()
    }

    fun generateDebtorsReport(data: List<DebtorReportItem>, filePath: String) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Должники")

        val header = sheet.createRow(0)
        listOf("Клиент", "Тур", "Дата брони", "Цена", "Оплачено", "ДОЛГ").forEachIndexed { i, title ->
            header.createCell(i).setCellValue(title)
        }

        data.forEachIndexed { i, item ->
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(item.client)
            row.createCell(1).setCellValue(item.tour)
            row.createCell(2).setCellValue(item.date)
            row.createCell(3).setCellValue(item.total)
            row.createCell(4).setCellValue(item.paid)
            row.createCell(5).setCellValue(item.debt)
        }
        for (i in 0..5) sheet.autoSizeColumn(i)
        FileOutputStream(filePath).use { workbook.write(it) }
        workbook.close()
    }

    fun generatePaymentLogReport(data: List<PaymentLogItem>, filePath: String) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Журнал платежей")

        val header = sheet.createRow(0)
        listOf("ID", "Дата", "Клиент", "Сумма", "Способ").forEachIndexed { i, title ->
            header.createCell(i).setCellValue(title)
        }

        data.forEachIndexed { i, item ->
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(item.id.toDouble())
            row.createCell(1).setCellValue(item.date)
            row.createCell(2).setCellValue(item.client)
            row.createCell(3).setCellValue(item.amount)
            row.createCell(4).setCellValue(item.method)
        }
        for (i in 0..4) sheet.autoSizeColumn(i)
        FileOutputStream(filePath).use { workbook.write(it) }
        workbook.close()
    }
}