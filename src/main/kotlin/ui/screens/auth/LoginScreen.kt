package ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ui.common.BrandBlue
import ui.screens.MainScreen

object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<LoginScreenModel>() // Магия Koin!

        // Подписка на изменение данных
        val state by viewModel.state.collectAsState()
        val email by viewModel.email.collectAsState()
        val password by viewModel.password.collectAsState()

        // Реакция на смену состояния (Успех -> Переход)
        LaunchedEffect(state) {
            if (state is LoginState.Success) {
                // TODO: Сохранить информацию о пользователе (кто вошел)
                navigator.replace(MainScreen)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(400.dp).padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Логотип
                    Image(
                        painter = painterResource("logo.png"),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Вход в систему",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                    Spacer(Modifier.height(32.dp))

                    // Поля ввода
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.email.value = it },
                        label = { Text("Email или Логин") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.password.value = it },
                        label = { Text("Пароль") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(24.dp))

                    // Ошибки
                    if (state is LoginState.Error) {
                        Text(
                            text = (state as LoginState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // Кнопка
                    Button(
                        onClick = { viewModel.onLoginClicked() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        enabled = state !is LoginState.Loading
                    ) {
                        if (state is LoginState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Войти", fontSize = 16.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Ссылка на регистрацию
                    TextButton(
                        onClick = { navigator.push(RegisterScreen) }
                    ) {
                        Text("Нет аккаунта? Зарегистрироваться", color = Color.Gray)
                    }
                }
            }
        }
    }
}