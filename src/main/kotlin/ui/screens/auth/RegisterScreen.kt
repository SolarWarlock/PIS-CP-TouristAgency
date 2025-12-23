package ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ui.common.BrandBlue

object RegisterScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<RegisterScreenModel>()
        val state by viewModel.state.collectAsState()

        // Поля ввода
        var fName by remember { mutableStateOf("") }
        var lName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }
        var passport by remember { mutableStateOf("") }

        // Если успех — возвращаемся на логин
        LaunchedEffect(state) {
            if (state is RegisterState.Success) {
                navigator.pop() // Вернуться назад
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(450.dp).padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Кнопка назад
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                        }
                        Text("Регистрация", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Форма
                    OutlinedTextField(fName, { fName = it }, label = { Text("Имя*") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(lName, { lName = it }, label = { Text("Фамилия*") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(phone, { phone = it }, label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(email, { email = it }, label = { Text("Email (Логин)*") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(pass, { pass = it }, label = { Text("Пароль*") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(passport, { passport = it }, label = { Text("Паспортные данные") }, modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(24.dp))

                    // Ошибки
                    if (state is RegisterState.Error) {
                        Text((state as RegisterState.Error).message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                    }

                    // Кнопка
                    Button(
                        onClick = { viewModel.register(fName, lName, phone, email, pass, passport) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        enabled = state !is RegisterState.Loading
                    ) {
                        if (state is RegisterState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Создать аккаунт")
                        }
                    }
                }
            }
        }
    }
}