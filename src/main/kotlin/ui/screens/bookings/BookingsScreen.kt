package ui.screens.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.model.Booking
import data.model.Debtor
import ui.common.BrandBlue
import ui.common.BrandOrange
import ui.common.UserSession
import ui.screens.finance.PaymentDialog

object BookingsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<BookingsScreenModel>()
        val state by viewModel.state.collectAsState()

        // Состояние для диалога оплаты (Клиент)
        var bookingToPay by remember { mutableStateOf<Booking?>(null) }

        // Состояние для диалога отзыва (Клиент)
        var bookingToReview by remember { mutableStateOf<Booking?>(null) }

        // Состояние вкладок (Менеджер)
        var selectedTabIndex by remember { mutableStateOf(0) }
        val managerTabs = listOf("Новые (Входящие)", "Архив / В работе")

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Заголовок и кнопка обновления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (UserSession.isManager) "Управление заказами" else "Мои поездки",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = { viewModel.loadData() }) {
                    Icon(Icons.Default.Refresh, "Обновить")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Вкладки для Менеджера
            if (UserSession.isManager) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = BrandBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = BrandBlue
                        )
                    }
                ) {
                    managerTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Список
            when (val s = state) {
                is BookingsListState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is BookingsListState.Error -> {
                    Text(s.message, color = Color.Red)
                }
                is BookingsListState.Content -> {
                    // Фильтрация списка в зависимости от роли и вкладки
                    val filteredList = remember(s.bookings, selectedTabIndex, UserSession.isManager) {
                        if (UserSession.isManager) {
                            if (selectedTabIndex == 0) {
                                // Вкладка "Новые": только те, что "В обработке" (требуют действия)
                                s.bookings.filter { it.status == "В обработке" }
                            } else {
                                // Вкладка "Архив": все остальные (Подтверждено, Оплачено, Аннулировано)
                                s.bookings.filter { it.status != "В обработке" }
                            }
                        } else {
                            // Клиент видит всё
                            s.bookings
                        }
                    }

                    if (filteredList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Список пуст", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredList) { booking ->
                                BookingItem(
                                    booking = booking,
                                    viewModel = viewModel,
                                    onPayClick = { bookingToPay = it },
                                    onReviewClick = { bookingToReview = it }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- ДИАЛОГИ ---

        // 1. Диалог оплаты (через костыль с Debtor, чтобы переиспользовать PaymentDialog)
        if (bookingToPay != null) {
            val debt = bookingToPay!!.price - bookingToPay!!.paidAmount
            val fakeDebtor = Debtor(
                bookingId = bookingToPay!!.id,
                clientName = "Я",
                tourName = bookingToPay!!.tourName,
                totalPrice = bookingToPay!!.price,
                paidAmount = bookingToPay!!.paidAmount,
                debt = debt
            )

            PaymentDialog(
                debtor = fakeDebtor,
                onDismiss = { bookingToPay = null },
                onConfirm = { amount, _ ->
                    // Метод оплаты всегда "Карта" для онлайн-оплаты
                    viewModel.payBooking(bookingToPay!!.id, amount)
                    bookingToPay = null
                }
            )
        }

        // 2. Диалог отзыва
        if (bookingToReview != null) {
            ReviewDialog(
                tourName = bookingToReview!!.tourName,
                onDismiss = { bookingToReview = null },
                onSubmit = { rating, comment ->
                    viewModel.sendReview(bookingToReview!!.tourId, rating, comment)
                    bookingToReview = null
                }
            )
        }
    }
}

@Composable
fun BookingItem(
    booking: Booking,
    viewModel: BookingsScreenModel,
    onPayClick: (Booking) -> Unit,
    onReviewClick: (Booking) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            // Верхняя строка: ID и Дата
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Заказ #${booking.id}", fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(booking.date, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(4.dp))

            // Название тура
            Text(booking.tourName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Имя клиента (видит только менеджер)
            if (booking.clientName != null) {
                Text("Клиент: ${booking.clientName}", color = BrandBlue, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))

            // Статусы
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                StatusBadge(booking.status)
                StatusBadge(booking.paymentStatus)
            }

            // Цена и Долг
            Column(
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                when (booking.paymentStatus) {
                    "Оплачено" -> {
                        Text(
                            text = "${booking.price} ₽",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    "Частично" -> {
                        val debt = booking.price - booking.paidAmount
                        Text(
                            text = "Долг: $debt ₽",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "из ${booking.price} ₽",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    else -> {
                        Text(
                            text = "${booking.price} ₽",
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- КНОПКИ ДЕЙСТВИЙ ---

            // 1. ДЛЯ МЕНЕДЖЕРА
            if (UserSession.isManager) {
                // Если новая заявка -> Подтвердить / Отклонить
                if (booking.status == "В обработке") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { viewModel.changeStatus(booking.id, "Аннулировано") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Отклонить")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.changeStatus(booking.id, "Подтверждено") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Подтвердить")
                        }
                    }
                }

                // Если отменена -> Удалить
                if (booking.status == "Аннулировано") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = { viewModel.deleteBooking(booking.id) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Удалить из истории")
                        }
                    }
                }
            }

            // 2. ДЛЯ КЛИЕНТА
            if (!UserSession.isManager) {

                // Кнопка ОПЛАТЫ (показываем, если подтверждено и есть долг)
                if (booking.status == "Подтверждено" && booking.paymentStatus != "Оплачено") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Button(
                        onClick = { onPayClick(booking) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Оплатить онлайн")
                    }
                }

                // Кнопка ОТЗЫВА (показываем, если оплачено и отзыва нет)
                if (booking.paymentStatus == "Оплачено" && !booking.hasReview) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    OutlinedButton(
                        onClick = { onReviewClick(booking) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Оценить поездку")
                    }
                } else if (booking.hasReview) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "✓ Отзыв оставлен",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(text: String) {
    val color = when (text) {
        "Подтверждено", "Оплачено" -> Color(0xFF2E7D32) // Зеленый
        "В обработке", "Частично" -> Color(0xFFF9A825) // Желтый
        "Аннулировано", "Не оплачено" -> Color(0xFFC62828) // Красный
        else -> Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}