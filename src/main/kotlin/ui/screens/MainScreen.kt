package ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ui.common.BrandBlue
import ui.common.UserSession
import ui.screens.auth.LoginScreen
import ui.screens.tours.ToursScreen
import ui.screens.bookings.BookingsScreen
import ui.screens.finance.FinanceScreen
import ui.screens.clients.ClientsScreen
import ui.screens.admin.EmployeesScreen
import ui.screens.admin.AuditScreen
import ui.screens.reports.ReportsScreen

object MainScreen : Screen {
    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow

        // Определяем стартовый экран в зависимости от роли
        val startScreen = when {
            UserSession.isFinancier -> FinanceScreen
            UserSession.isAdmin -> EmployeesScreen
            else -> ToursScreen // Клиенты и Менеджеры начинают с Туров
        }

        Navigator(startScreen) { navigator ->
            Row(modifier = Modifier.fillMaxSize()) {

                // --- МЕНЮ ---
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Spacer(Modifier.height(16.dp))
                        Image(
                            painter = painterResource("logo.png"),
                            contentDescription = "Логотип",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                    },
                    modifier = Modifier.width(80.dp)
                ) {
                    // 1. ТУРЫ (Видят Клиенты и Менеджеры)
                    if (UserSession.isClient || UserSession.isManager) {
                        NavRailItem(
                            icon = Icons.Default.DateRange,
                            label = "Туры",
                            isSelected = navigator.lastItem is ToursScreen,
                            onClick = { navigator.replace(ToursScreen) }
                        )
                    }

                    // 2. БАЗА КЛИЕНТОВ (Видят Менеджеры и Админы)
                    if (UserSession.isManager) {
                        NavRailItem(
                            icon = Icons.Default.Person,
                            label = "Клиенты",
                            isSelected = navigator.lastItem is ClientsScreen,
                            onClick = { navigator.replace(ClientsScreen) }
                        )
                    }

                    // 3. ЗАКАЗЫ (Видят Клиенты и Менеджеры)
                    // Для клиента это "Мои покупки", для менеджера "Заявки"
                    if (UserSession.isClient || UserSession.isManager) {
                        NavRailItem(
                            icon = Icons.Default.ShoppingCart,
                            label = if (UserSession.isClient) "Поездки" else "Заказы",
                            isSelected = navigator.lastItem is BookingsScreen,
                            onClick = { navigator.replace(BookingsScreen) }
                        )
                    }

                    // 4. ПЛАТЕЖИ (Видят Финансисты и Админы)
                    if (UserSession.isFinancier) {
                        NavRailItem(
                            icon = Icons.Default.ThumbUp,
                            label = "Платежи",
                            isSelected = navigator.lastItem is FinanceScreen,
                            onClick = { navigator.replace(FinanceScreen) }
                        )

                        NavRailItem(
                            icon = Icons.Default.Menu,
                            label = "Отчеты",
                            isSelected = navigator.lastItem is ReportsScreen,
                            onClick = { navigator.replace(ReportsScreen) }
                        )
                    }

                    // 5. ПАНЕЛЬ АДМИНИСТРАТОРА
                    if (UserSession.isAdmin) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))

                        NavRailItem(
                            icon = Icons.Default.AccountBox,
                            label = "Штат",
                            isSelected = navigator.lastItem is EmployeesScreen,
                            onClick = { navigator.replace(EmployeesScreen) }
                        )

                        NavRailItem(
                            icon = Icons.Default.Lock,
                            label = "Аудит",
                            isSelected = navigator.lastItem is AuditScreen,
                            onClick = { navigator.replace(AuditScreen) }
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // КНОПКА ВЫХОДА
                    NavigationRailItem(
                        selected = false,
                        onClick = {
                            UserSession.clear()
                            rootNavigator.replace(LoginScreen)
                        },
                        icon = { Icon(Icons.Default.ExitToApp, null, tint = Color.Gray) },
                        label = { Text("Выход", style = MaterialTheme.typography.labelSmall) }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // --- КОНТЕНТ ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    CurrentScreen()
                }
            }
        }
    }

    @Composable
    private fun NavRailItem(
        icon: ImageVector,
        label: String,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        NavigationRailItem(
            selected = isSelected,
            onClick = onClick,
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) BrandBlue else Color.Gray
                )
            },
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) BrandBlue else Color.Gray
                )
            },
            colors = NavigationRailItemDefaults.colors(
                indicatorColor = BrandBlue.copy(alpha = 0.1f)
            )
        )
    }
}