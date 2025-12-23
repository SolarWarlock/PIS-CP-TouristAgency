package ui.screens.tours

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.model.Tour
import ui.common.BrandBlue
import ui.common.UserSession
import ui.screens.bookings.BookingDialog
import ui.screens.bookings.BookingDialogModel

object ToursScreen : Screen {
    @Composable
    override fun Content() {
        // Получаем модели через Koin
        val toursViewModel = getScreenModel<ToursScreenModel>()
        val bookingViewModel = getScreenModel<BookingDialogModel>()

        // Состояние списка туров
        val state by toursViewModel.state.collectAsState()

        // Состояния для справочников (нужны для диалога редактирования)
        val types by toursViewModel.types.collectAsState()
        val partners by toursViewModel.partners.collectAsState()

        // --- ЛОКАЛЬНЫЕ СОСТОЯНИЯ ДЛЯ ДИАЛОГОВ ---

        // 1. Бронирование: какой тур выбран?
        var selectedTourForBooking by remember { mutableStateOf<Tour?>(null) }

        // 2. Редактирование/Создание:
        // Если tourToEdit == null, значит создаем новый. Если не null — редактируем.
        var tourToEdit by remember { mutableStateOf<Tour?>(null) }
        var isEditDialogVisible by remember { mutableStateOf(false) }

        // 3. Удаление: какой тур хотим удалить?
        var tourToDelete by remember { mutableStateOf<Tour?>(null) }

        // Загружаем справочники при входе, если это Менеджер
        LaunchedEffect(Unit) {
            if (UserSession.isManager) {
                toursViewModel.loadDictionaries()
            }
        }

        // --- ВЕРСТКА ---
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Заголовок и кнопка обновления
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Каталог туров", style = MaterialTheme.typography.headlineMedium)
                    IconButton(onClick = { toursViewModel.loadTours() }) {
                        Icon(Icons.Default.Refresh, "Обновить")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Контент списка
                when (val currentState = state) {
                    is ToursState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is ToursState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(currentState.message, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    is ToursState.Content -> {
                        if (currentState.tours.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Туров пока нет. Добавьте их в БД.", color = Color.Gray)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 300.dp),
                                contentPadding = PaddingValues(bottom = 80.dp) // Отступ под FAB
                            ) {
                                items(currentState.tours) { tour ->
                                    TourItem(
                                        tour = tour,
                                        onBookClick = { selectedTourForBooking = it },
                                        onEditClick = {
                                            tourToEdit = it
                                            isEditDialogVisible = true
                                        },
                                        onDeleteClick = { tourToDelete = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Кнопка добавления (FAB) - Только для Менеджера
            if (UserSession.isManager) {
                FloatingActionButton(
                    onClick = {
                        tourToEdit = null // Режим создания
                        isEditDialogVisible = true
                    },
                    containerColor = BrandBlue,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(Icons.Default.Add, "Добавить тур")
                }
            }
        }

        // --- ДИАЛОГОВЫЕ ОКНА ---

        // 1. Диалог Бронирования
        if (selectedTourForBooking != null) {
            BookingDialog(
                tour = selectedTourForBooking!!,
                viewModel = bookingViewModel,
                onDismiss = { selectedTourForBooking = null },
                onSuccess = {
                    selectedTourForBooking = null
                    // Можно добавить всплывающее уведомление об успехе
                }
            )
        }

        // 2. Диалог Редактирования / Создания
        if (isEditDialogVisible) {
            TourEditDialog(
                tourToEdit = tourToEdit,
                types = types,
                partners = partners,
                onDismiss = { isEditDialogVisible = false },
                onSave = { id, typeId, partnerId, dest, start, end, cost, desc ->
                    if (id == null) {
                        // Создание
                        toursViewModel.createTour(typeId, partnerId, dest, start, end, cost, desc)
                    } else {
                        // Обновление
                        toursViewModel.updateTour(id, typeId, partnerId, dest, start, end, cost, desc)
                    }
                    isEditDialogVisible = false
                }
            )
        }

        // 3. Диалог Подтверждения удаления
        if (tourToDelete != null) {
            AlertDialog(
                onDismissRequest = { tourToDelete = null },
                title = { Text("Удаление тура") },
                text = { Text("Вы уверены, что хотите удалить тур \"${tourToDelete?.destination}\"?\nЭто действие необратимо.") },
                confirmButton = {
                    Button(
                        onClick = {
                            toursViewModel.deleteTour(tourToDelete!!.id)
                            tourToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tourToDelete = null }) { Text("Отмена") }
                }
            )
        }
    }
}