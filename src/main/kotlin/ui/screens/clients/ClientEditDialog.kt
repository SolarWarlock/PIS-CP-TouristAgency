package ui.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.model.Client
import ui.common.BrandBlue

@Composable
fun ClientEditDialog(
    client: Client,
    initialPassport: String,
    onDismiss: () -> Unit,
    onSave: (Client, String) -> Unit
) {
    var firstName by remember { mutableStateOf(client.firstName) }
    var lastName by remember { mutableStateOf(client.lastName) }
    var phone by remember { mutableStateOf(client.phone ?: "") }
    var passport by remember { mutableStateOf(initialPassport) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Редактирование клиента", style = MaterialTheme.typography.headlineSmall, color = BrandBlue)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Фамилия") },
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
                    value = passport,
                    onValueChange = { passport = it },
                    label = { Text("Паспортные данные") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updatedClient = client.copy(
                                firstName = firstName,
                                lastName = lastName,
                                phone = phone
                            )
                            onSave(updatedClient, passport)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}