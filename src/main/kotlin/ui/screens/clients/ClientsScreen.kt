package ui.screens.clients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.model.Client
import ui.common.BrandBlue

object ClientsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<ClientsScreenModel>()
        val state by viewModel.state.collectAsState()

        var searchQuery by remember { mutableStateOf("") }
        var clientToEdit by remember { mutableStateOf<Client?>(null) }
        var passportData by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Заголовок и поиск
            Text("База клиентов", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                label = { Text("Поиск...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            when (val s = state) {
                is ClientsState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is ClientsState.Error -> Text(s.message, color = Color.Red)
                is ClientsState.Content -> {
                    LazyColumn {
                        items(s.clients) { client ->
                            ClientItem(client, onEditClick = {
                                // Загружаем паспорт перед открытием окна
                                viewModel.loadPassport(client.id) { passport ->
                                    passportData = passport
                                    clientToEdit = client
                                }
                            })
                        }
                    }
                }
            }
        }

        if (clientToEdit != null) {
            ClientEditDialog(
                client = clientToEdit!!,
                initialPassport = passportData,
                onDismiss = { clientToEdit = null },
                onSave = { updatedClient, newPassport ->
                    viewModel.saveClient(updatedClient, newPassport)
                    clientToEdit = null
                }
            )
        }
    }
}

@Composable
fun ClientItem(client: Client, onEditClick: () -> Unit) {
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
                    Text("${client.firstName} ${client.lastName}", style = MaterialTheme.typography.titleMedium)
                    Text(client.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (client.phone != null) {
                        Text(client.phone, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, "Редактировать", tint = Color.Gray)
            }
        }
    }
}