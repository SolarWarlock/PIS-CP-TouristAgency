package ui.screens.tours

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.model.Tour
import ui.common.BrandBlue
import ui.common.BrandOrange
import ui.common.UserSession

@Composable
fun TourItem(
    tour: Tour,
    onBookClick: (Tour) -> Unit,
    onEditClick: (Tour) -> Unit,   // Новый колбэк
    onDeleteClick: (Tour) -> Unit  // Новый колбэк
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Верхняя часть: Заголовок + Кнопки управления (для менеджера)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tour.destination,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                    Text(
                        text = tour.typeName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }

                // Цена и кнопки
                Column(horizontalAlignment = Alignment.End) {
                    // РЕЙТИНГ
                    if (tour.rating != null) {
                        val ratingColor = when {
                            tour.rating >= 4.0 -> Color(0xFF2E7D32) // Зеленый
                            tour.rating >= 3.0 -> Color(0xFFF9A825) // Желтый
                            else -> Color(0xFFC62828)               // Красный
                        }

                        Surface(
                            color = ratingColor,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "★ ${tour.rating}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Нет оценок",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // ЦЕНА
                    Text(
                        text = "${tour.cost} ₽",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange
                    )

                    // Кнопки управления для менеджера
                    if (UserSession.isManager) {
                        Row {
                            IconButton(onClick = { onEditClick(tour) }) {
                                Icon(Icons.Default.Edit, "Ред.", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { onDeleteClick(tour) }) {
                                Icon(Icons.Default.Delete, "Уд.", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Даты
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = tour.dates, style = MaterialTheme.typography.bodyMedium)
            }

            // Партнер
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = "Партнер: ${tour.partnerName}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f)) // Divider -> HorizontalDivider
            Spacer(Modifier.height(8.dp))

            // Кнопка
            Button(
                onClick = { onBookClick(tour) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Забронировать")
            }
        }
    }
}