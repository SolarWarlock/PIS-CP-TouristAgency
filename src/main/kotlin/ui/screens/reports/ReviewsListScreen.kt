package ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.model.ReviewItem
import ui.common.BrandBlue
import ui.common.BrandOrange

object ReviewsListScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<ReviewsScreenModel>()
        val reviews by viewModel.reviews.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Отзывы клиентов", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(Modifier.height(16.dp))

            if (reviews.isEmpty()) {
                Text("Отзывов пока нет", color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reviews) { review ->
                        ReviewCard(review)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: ReviewItem) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.clientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(review.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text("Тур: ${review.tourName}", style = MaterialTheme.typography.bodyMedium, color = BrandBlue)
            Spacer(Modifier.height(8.dp))
            Row {
                repeat(review.rating) {
                    Icon(Icons.Default.Star, null, tint = BrandOrange, modifier = Modifier.size(16.dp))
                }
            }
            if (review.comment.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(review.comment)
            }
        }
    }
}