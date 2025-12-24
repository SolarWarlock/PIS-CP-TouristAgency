package ui.screens.bookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.model.Client
import data.model.Tour
import ui.common.BrandBlue
import ui.common.UserSession
import java.text.DecimalFormat // Импорт для форматирования цены

@Composable
fun BookingDialog(
    tour: Tour,
    viewModel: BookingDialogModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.bookingState.collectAsState()
    val foundClients by viewModel.foundClients.collectAsState()

    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Форматтер цены (чтобы было 30 000 000, а не 3.0E7)
    val priceFormatter = remember { DecimalFormat("#,###.##") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp), // Ограничим высоту, чтобы не вылезало за экран
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            // Добавим скролл для всего контента, если описание длинное
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Бронирование тура", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(Modifier.height(8.dp))

                // Название
                Text("Тур: ${tour.destination}", style = MaterialTheme.typography.titleMedium, color = BrandBlue)

                // Цена (отформатированная)
                Text("Цена: ${priceFormatter.format(tour.cost)} ₽", style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

                // --- ДОБАВЛЕНО ОПИСАНИЕ ---
                if (!tour.description.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Описание:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = tour.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // ---------------------------

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                if (UserSession.isManager) {
                    Text("Оформление для клиента:", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchClient(it)
                        },
                        label = { Text("Поиск (ФИО или телефон)") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Список найденных клиентов (Внимание: скролл внутри скролла может работать плохо,
                    // но так как список клиентов ограничен высотой, должно быть ок)
                    if (foundClients.isNotEmpty() && selectedClient == null) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            foundClients.forEach { client ->
                                ListItem(
                                    headlineContent = { Text("${client.firstName} ${client.lastName}") },
                                    supportingContent = { Text(client.phone ?: "Нет телефона") },
                                    modifier = Modifier.clickable {
                                        selectedClient = client
                                        searchQuery = "${client.firstName} ${client.lastName}"
                                        viewModel.searchClient("")
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }

                    if (selectedClient != null) {
                        Text(
                            text = "Выбран: ${selectedClient!!.firstName} ${selectedClient!!.lastName}",
                            color = BrandBlue,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    Text(
                        "Вы оформляете тур на себя (${UserSession.currentClient?.firstName}).\n" +
                                "Менеджер свяжется с вами для подтверждения.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(24.dp))

                when (val s = state) {
                    is BookingState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is BookingState.Error -> Text(s.message, color = Color.Red)
                    is BookingState.Success -> {
                        Text("Успешно! Номер брони: ${s.bookingId}", color = Color(0xFF2E7D32))
                        Button(onClick = onSuccess, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text("Отлично")
                        }
                    }
                    else -> {
                        Button(
                            onClick = {
                                viewModel.confirmBooking(tour.id, selectedClient)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Text(if (UserSession.isManager) "Оформить продажу" else "Отправить заявку")
                        }
                    }
                }
            }
        }
    }
}