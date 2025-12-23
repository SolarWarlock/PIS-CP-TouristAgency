package ui.screens.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.model.Debtor
import ui.common.BrandBlue
import ui.common.UserSession

object FinanceScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<FinanceScreenModel>()
        val state by viewModel.state.collectAsState()

        var selectedDebtor by remember { mutableStateOf<Debtor?>(null) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Касса и Платежи", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = { viewModel.loadDebtors() }) {
                    Icon(Icons.Default.Refresh, "Обновить")
                }
            }

            // Если не финансовый специалист и не админ - показываем заглушку
            if (UserSession.currentEmployee?.position != "Финансист" && UserSession.currentEmployee?.position != "Администратор") {
                // ПРИМЕЧАНИЕ: В реальной системе лучше проверять роль строже
                // Но для теста покажем всем менеджерам или поправим логику позже
            }

            Spacer(Modifier.height(16.dp))

            when (val s = state) {
                is FinanceState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is FinanceState.Error -> Text(s.message, color = Color.Red)
                is FinanceState.Content -> {
                    if (s.debtors.isEmpty()) {
                        Text("Нет неоплаченных бронирований.", color = Color.Gray)
                    } else {
                        LazyColumn {
                            items(s.debtors) { debtor ->
                                DebtorItem(debtor, onPayClick = { selectedDebtor = it })
                            }
                        }
                    }
                }
            }
        }

        if (selectedDebtor != null) {
            PaymentDialog(
                debtor = selectedDebtor!!,
                onDismiss = { selectedDebtor = null },
                onConfirm = { amount, method ->
                    viewModel.processPayment(selectedDebtor!!.bookingId, amount, method)
                    selectedDebtor = null
                }
            )
        }
    }
}

@Composable
fun DebtorItem(debtor: Debtor, onPayClick: (Debtor) -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text("Бронь #${debtor.bookingId}: ${debtor.clientName}", fontWeight = FontWeight.Bold)
                Text(debtor.tourName, style = MaterialTheme.typography.bodyMedium)
                Text("Цена: ${debtor.totalPrice} | Оплачено: ${debtor.paidAmount}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Долг: ${debtor.debt} ₽", color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                /*Button(
                    onClick = { onPayClick(debtor) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("Оплатить")
                }*/
            }
        }
    }
}