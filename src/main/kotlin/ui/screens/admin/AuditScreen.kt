package ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.model.AuditLogEntry
import ui.common.BrandBlue
import java.awt.FileDialog
import java.io.File
import javax.swing.JFrame

object AuditScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<AdminScreenModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadLogs()
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Журнал безопасности", style = MaterialTheme.typography.headlineMedium)

                // Блок кнопок
                Row {
                    // БЭКАП БД
                    IconButton(onClick = {
                        val path = saveFileDialog("backup_${System.currentTimeMillis()}.sql")
                        if (path != null) {
                            viewModel.createBackup(path)
                        }
                    }) {
                        // Иконка "Сохранить" или "Облако"
                        // Проверь импорт: import androidx.compose.material.icons.filled.Save
                        Icon(Icons.Default.Save, "Сделать бэкап БД", tint = BrandBlue)
                    }


                    // Кнопка СКАЧАТЬ
                    IconButton(onClick = {
                        val path = saveFileDialog("audit_logs.xlsx")
                        if (path != null) {
                            viewModel.exportLogs(path)
                        }
                    }) {
                        Icon(Icons.Default.Share, "Скачать логи")
                    }

                    Spacer(Modifier.width(8.dp))

                    // Кнопка ОБНОВИТЬ
                    IconButton(onClick = { viewModel.loadLogs() }) {
                        Icon(Icons.Default.Refresh, "Обновить")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Контент (Таблица)
            when (val s = state) {
                is AdminState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is AdminState.Error -> Text(s.message, color = Color.Red)
                is AdminState.LogsContent -> {
                    if (s.list.isEmpty()) {
                        Text("Журнал пуст", color = Color.Gray)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                Row(Modifier.padding(horizontal = 8.dp)) {
                                    Text("Время", modifier = Modifier.width(150.dp), fontWeight = FontWeight.Bold)
                                    Text("Пользователь", modifier = Modifier.width(120.dp), fontWeight = FontWeight.Bold)
                                    Text("Действие", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
                                    Text("Детали", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            }

                            items(s.list) { log ->
                                LogItem(log)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    // Хелпер для диалога сохранения
    private fun saveFileDialog(defaultName: String): String? {
        val dialog = FileDialog(null as JFrame?, "Сохранить логи", FileDialog.SAVE)
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
fun LogItem(log: AuditLogEntry) {
    val operationColor = when (log.operation) {
        "INSERT" -> Color(0xFF2E7D32) // Зеленый
        "UPDATE" -> Color(0xFF1565C0) // Синий
        "DELETE" -> Color(0xFFC62828) // Красный
        else -> Color.Gray
    }

    Card(
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Время
            Text(log.date, modifier = Modifier.width(120.dp), fontSize = 14.sp)

            // Пользователь (DB Login)
            Text(log.user, modifier = Modifier.width(150.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium)

            // Тип операции (Плашка)
            Surface(
                color = operationColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.width(100.dp)
            ) {
                Text(
                    text = log.operation,
                    color = operationColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Детали (Таблица и изменения)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Таблица: ${log.table}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                if (log.details != null) {
                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
            }
        }
    }
}