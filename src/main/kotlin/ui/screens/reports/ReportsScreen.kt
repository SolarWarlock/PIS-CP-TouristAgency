package ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import ui.common.BrandBlue
import java.awt.FileDialog
import java.io.File
import javax.swing.JFrame

object ReportsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<ReportsScreenModel>()
        val status by viewModel.status.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("Отчетность", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(32.dp))

            // Карточки отчетов
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReportCard(
                    title = "KPI Менеджеров",
                    desc = "Статистика продаж и выручки по сотрудникам",
                    icon = Icons.Default.Star,
                    onClick = {
                        val path = saveFileDialog("managers_kpi.xlsx")
                        if (path != null) viewModel.exportManagerKpi(path)
                    }
                )

                ReportCard(
                    title = "Финансовый отчет",
                    desc = "Динамика выручки по месяцам",
                    icon = Icons.Default.DateRange,
                    onClick = {
                        val path = saveFileDialog("finance_report.xlsx")
                        if (path != null) viewModel.exportFinance(path)
                    }
                )

                if (ui.common.UserSession.isFinancier) {
                    ReportCard(
                        title = "Реестр должников",
                        desc = "Список неоплаченных бронирований",
                        icon = Icons.Default.Warning, // Импортируй
                        onClick = {
                            val path = saveFileDialog("debtors.xlsx")
                            if (path != null) viewModel.exportDebtors(path)
                        }
                    )

                    ReportCard(
                        title = "Журнал транзакций",
                        desc = "Полная история всех платежей",
                        icon = Icons.Default.List,
                        onClick = {
                            val path = saveFileDialog("payments_full.xlsx")
                            if (path != null) viewModel.exportPaymentLog(path)
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Статус бар
            if (status.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(16.dp),
                        color = BrandBlue
                    )
                }
            }
        }
    }

    // Функция открытия нативного диалога сохранения
    private fun saveFileDialog(defaultName: String): String? {
        val dialog = FileDialog(null as JFrame?, "Сохранить отчет", FileDialog.SAVE)
        dialog.file = defaultName
        dialog.isVisible = true
        return if (dialog.directory != null && dialog.file != null) {
            dialog.directory + dialog.file
        } else {
            null
        }
    }
}

@Composable
fun ReportCard(title: String, desc: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(300.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("Скачать Excel")
            }
        }
    }
}