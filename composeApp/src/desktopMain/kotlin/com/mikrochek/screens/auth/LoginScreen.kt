package com.mikrochek.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikrochek.server.repository.auth.AuthRepository
import com.mikrochek.server.database.models.User
import com.mikrochek.components.common.*
import com.mikrochek.components.common.AlertDialogType
import com.mikrochek.components.common.AppTextField
import com.mikrochek.components.common.PasswordTextField
import com.mikrochek.components.common.PrimaryButton
import com.mikrochek.data.UserState
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onNavigateToSignup: () -> Unit,
    onUserAuthenticated: (User) -> Unit = {}
) {
    var userName by remember { mutableStateOf("kush") }
    var password by remember { mutableStateOf("kush") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left side - Branding and Welcome message
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colors.primaryVariant,
                            MaterialTheme.colors.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome to StreamLine",
                    style = MaterialTheme.typography.h4,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
//                Text(
//                    text = "Empowering manufacturing since 2000",
//                    style = MaterialTheme.typography.subtitle1,
//                    color = Color.White.copy(alpha = 0.8f),
//                    textAlign = TextAlign.Center
//                )
            }
        }

        // Right side - Login form
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(400.dp)
                    .padding(40.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = "Please enter your credentials to continue",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppTextField(
                        value = userName,
                        onValueChange = {
                            userName = it
                            errorMessage = ""
                        },
                        label = "Username",
                        leadingIcon = Icons.Default.Person,
                        error = if (errorMessage.contains("username", ignoreCase = true)) errorMessage else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    PasswordTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        error = if (errorMessage.contains("password", ignoreCase = true)) errorMessage else null
                    )
                }

                PrimaryButton(
                    text = "Sign In",
                    onClick = {
                        if (userName.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter both username and password"
                            return@PrimaryButton
                        }

                        scope.launch {
                            isLoading = true
                            try {
                                val result = authRepository.login(userName, password)
                                result.fold(
                                    onSuccess = { user ->
                                        // Set the user in UserState
                                        UserState.setUser(user)
                                        // Initialize tabs and navigate
                                        onUserAuthenticated(user)
                                    },
                                    onFailure = { error ->
                                        errorMessage = error.message ?: "Login failed. Please try again."
                                        showErrorDialog = true
                                    }
                                )
                            } catch (e: Exception) {
                                errorMessage = "An unexpected error occurred. Please try again."
                                showErrorDialog = true
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    loading = isLoading,
                    enabled = !isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center  ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    TextButton(
//                        text = "Forgot Password?",
//                        onClick = onNavigateToForgotPassword,
//                        contentColor = MaterialTheme.colors.primary
//                    )

                    TextButton(
                        text = "Create Account",
                        onClick = onNavigateToSignup,
                        contentColor = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            title = "Login Failed",
            message = errorMessage,
            onDismiss = { showErrorDialog = false },
            onConfirm = { showErrorDialog = false },
            type = AlertDialogType.Error,
            confirmText = "OK",
            dismissText = ""
        )
    }
}
