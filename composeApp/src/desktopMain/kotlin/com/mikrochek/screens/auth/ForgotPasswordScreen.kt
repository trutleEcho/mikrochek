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
import com.mikrochek.components.common.AlertDialog
import com.mikrochek.components.common.AlertDialogType
import com.mikrochek.components.common.TextButton
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    authRepository: AuthRepository,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
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
                    imageVector = Icons.Default.LockReset,
                    contentDescription = "Reset Password",
                    tint = Color.White,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Reset Your Password",
                    style = MaterialTheme.typography.h4,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Enter your email address and we'll send you instructions to reset your password",
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Right side - Reset Password form
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
                        text = "Forgot Password",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = "Don't worry! It happens. Please enter your email address.",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                PrimaryButton(
                    text = "Send Reset Link",
                    onClick = {
                        if (email.isBlank()) {
                            errorMessage = "Please enter your email address"
                            showErrorDialog = true
                            return@PrimaryButton
                        }
                        
                        scope.launch {
                            isLoading = true
                            try {
                                val result = authRepository.sendVerificationEmail(email)
                                result.fold(
                                    onSuccess = {
                                        showSuccessDialog = true
                                    },
                                    onFailure = { error ->
                                        errorMessage = error.message ?: "Failed to send reset link. Please try again."
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

                TextButton(
                    text = "Back to Login",
                    onClick = onNavigateToLogin,
                    contentColor = MaterialTheme.colors.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            title = "Reset Link Sent",
            message = "We've sent a password reset link to your email address. Please check your inbox and follow the instructions.",
            onDismiss = {
                showSuccessDialog = false
                onNavigateToLogin()
            },
            onConfirm = {
                showSuccessDialog = false
                onNavigateToLogin()
            },
            type = AlertDialogType.Success,
            confirmText = "OK",
            dismissText = ""
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            title = "Error",
            message = errorMessage,
            onDismiss = { showErrorDialog = false },
            onConfirm = { showErrorDialog = false },
            type = AlertDialogType.Error,
            confirmText = "OK",
            dismissText = ""
        )
    }
} 