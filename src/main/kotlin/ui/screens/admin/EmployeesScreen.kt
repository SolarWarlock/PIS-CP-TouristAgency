package ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.model.Employee
import ui.common.BrandBlue

object EmployeesScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<AdminScreenModel>()
        val state by viewModel.state.collectAsState()

        // Состояния диалогов
        var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
        var isDialogOpen by remember { mutableStateOf(false) }
        var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

        // Загрузка данных при входе
        LaunchedEffect(Unit) {
            viewModel.loadEmployees()
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Заголовок и кнопка добавления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Сотрудники", style = MaterialTheme.typography.headlineMedium)
                FloatingActionButton(
                    onClick = {
                        employeeToEdit = null // Режим создания
                        isDialogOpen = true
                    },
                    containerColor = BrandBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Добавить")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Контент
            when (val s = state) {
                is AdminState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AdminState.Error -> {
                    Text(s.message, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
                }
                is AdminState.EmployeesContent -> {
                    if (s.list.isEmpty()) {
                        Text("Список сотрудников пуст", color = Color.Gray)
                    } else {
                        LazyColumn {
                            items(s.list) { emp ->
                                EmployeeItem(
                                    emp = emp,
                                    onEdit = {
                                        employeeToEdit = emp
                                        isDialogOpen = true
                                    },
                                    onDelete = {
                                        employeeToDelete = emp
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {} // Игнорируем состояние логов
            }
        }

        // Диалог Создания / Редактирования
        if (isDialogOpen) {
            EmployeeDialog(
                employee = employeeToEdit,
                onDismiss = { isDialogOpen = false },
                onSave = { id, f, l, p, e, pos, log ->
                    if (id == null) {
                        viewModel.createEmployee(f, l, p, e, pos, log)
                    } else {
                        viewModel.editEmployee(id, f, l, p, e, pos)
                    }
                    isDialogOpen = false
                }
            )
        }

        // Диалог Удаления
        if (employeeToDelete != null) {
            AlertDialog(
                onDismissRequest = { employeeToDelete = null },
                title = { Text("Увольнение сотрудника") },
                text = { Text("Вы уверены, что хотите удалить сотрудника ${employeeToDelete?.fio}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteEmployee(employeeToDelete!!.id)
                            employeeToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { employeeToDelete = null }) { Text("Отмена") }
                }
            )
        }
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ (ВНЕ OBJECT SCREEN) ---

@Composable
fun EmployeeItem(
    emp: Employee,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = BrandBlue)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(emp.fio, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${emp.position} (Login: ${emp.dbLogin})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            // Кнопки действий
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Редактировать", tint = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Удалить", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun EmployeeDialog(
    employee: Employee?,
    onDismiss: () -> Unit,
    // Callback с параметрами: id, firstName, lastName, phone, email, position, dbLogin
    onSave: (Int?, String, String, String, String, String, String) -> Unit
) {
    // Инициализация полей
    var f by remember { mutableStateOf(employee?.fio?.split(" ")?.getOrNull(0) ?: "") }
    var l by remember { mutableStateOf(employee?.fio?.split(" ")?.getOrNull(1) ?: "") }
    // Внимание: если в модели Employee нет полей phone/email, они будут пустыми при редактировании.
    // Это нормально для текущей версии.
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf(employee?.position ?: "Менеджер") }
    var login by remember { mutableStateOf(employee?.dbLogin ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employee == null) "Новый сотрудник" else "Редактирование") },
        text = {
            Column {
                OutlinedTextField(
                    value = f,
                    onValueChange = { f = it },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = l,
                    onValueChange = { l = it },
                    label = { Text("Фамилия") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pos,
                    onValueChange = { pos = it },
                    label = { Text("Должность") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text("DB Login (pgAdmin)") },
                    enabled = employee == null, // Логин менять нельзя
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(employee?.id, f, l, phone, email, pos, login) }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}