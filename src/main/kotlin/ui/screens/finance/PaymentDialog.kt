package ui.screens.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.model.Debtor
import ui.common.BrandBlue

@Composable
fun PaymentDialog(
    debtor: Debtor,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf(debtor.debt.toString()) } // По умолчанию полная сумма
    var selectedMethod by remember { mutableStateOf("Наличные") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Заголовок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Внесение оплаты", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Spacer(Modifier.height(16.dp))
                Text("Клиент: ${debtor.clientName}")
                Text("Тур: ${debtor.tourName}")
                Text("Остаток долга: ${debtor.debt} ₽", color = Color.Red, style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(16.dp))

                // Ввод суммы
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Сумма платежа") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Выбор метода (Радиокнопки)
                Text("Способ оплаты:", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedMethod == "Наличные", onClick = { selectedMethod = "Наличные" })
                    Text("Наличные")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = selectedMethod == "Карта", onClick = { selectedMethod = "Карта" })
                    Text("Карта")
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val sum = amountText.toDoubleOrNull()
                        if (sum != null && sum > 0) {
                            onConfirm(sum, selectedMethod)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Провести платеж")
                }
            }
        }
    }
}