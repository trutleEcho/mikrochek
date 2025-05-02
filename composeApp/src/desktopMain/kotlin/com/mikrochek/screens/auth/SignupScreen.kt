package com.mikrochek.screens.auth

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikrochek.server.repository.auth.AuthRepository
import com.mikrochek.components.common.*
import com.mikrochek.data.UserState
import kotlinx.coroutines.launch
import com.mikrochek.server.database.models.User
import com.mikrochek.server.database.enum.AccountType
import com.mikrochek.server.repository.user.UserRepository

@Composable
fun SignupScreen(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                            MaterialTheme.colors.primary,
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
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Create Account",
                    tint = Color.White,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Join Hermesys",
                    style = MaterialTheme.typography.h4,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Create your account and start managing your business efficiently",
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Right side - Signup form
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
                        text = "Create Account",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = "Please fill in your information to create an account",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "First Name",
                        leadingIcon = Icons.Default.Person,
                    )

                    AppTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last Name",
                        leadingIcon = Icons.Default.Person,
                    )

                    AppTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = "Username",
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock
                    )

                    PasswordTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        leadingIcon = Icons.Default.Lock
                    )
                }

                PrimaryButton(
                    text = "Create Account",
                    onClick = {
                        // Validate inputs
                        when {
                            firstName.isBlank() || lastName.isBlank() -> {
                                errorMessage = "Please enter your full name"
                                showErrorDialog = true
                                return@PrimaryButton
                            }

                            userName.isBlank() -> {
                                errorMessage = "Please enter your username"
                                showErrorDialog = true
                                return@PrimaryButton
                            }

                            password.isBlank() -> {
                                errorMessage = "Please enter a password"
                                showErrorDialog = true
                                return@PrimaryButton
                            }

                            password != confirmPassword -> {
                                errorMessage = "Passwords do not match"
                                showErrorDialog = true
                                return@PrimaryButton
                            }

                            password.length < 4 -> {
                                errorMessage = "Password must be at least 4 characters long"
                                showErrorDialog = true
                                return@PrimaryButton
                            }
                        }

                        scope.launch {
                            isLoading = true
                            try {
                                // Check if username already exists
                                val existingUser = userRepository.getUserByUserName(userName)
                                if (existingUser != null) {
                                    errorMessage = "Username already exists"
                                    showErrorDialog = true
                                    return@launch
                                }

                                // Create a new User object
                                val newUser = User(
                                    name = "$firstName $lastName",
                                    userName = userName,
                                    password = password,
                                    accountType = AccountType.KUSH,
                                    createdAt = System.currentTimeMillis(),
                                    lastLoginAt = null,
                                    isActive = true
                                )

                                // Create the user
                                val result = authRepository.signup(newUser)
                                if (result.isSuccess) {
                                    // Set the user in UserState
                                    UserState.setUser(result.getOrNull())
                                    onSignupSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to create user"
                                    showErrorDialog = true
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "An unexpected error occurred. Please try again."
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    TextButton(
                        text = "Sign In",
                        onClick = onNavigateToLogin,
                        contentColor = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            title = "Registration Failed",
            message = errorMessage,
            onDismiss = { showErrorDialog = false },
            onConfirm = { showErrorDialog = false },
            type = AlertDialogType.Error,
            confirmText = "OK",
            dismissText = ""
        )
    }
} 