package util

import data.model.ManagerKpi
import data.model.MonthlyRevenue
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
}