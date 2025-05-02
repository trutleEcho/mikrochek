package com.mikrochek.screens.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.QuickStatCard
import com.mikrochek.theme.AppColors
import com.mikrochek.navigation.NavDestination
import com.mikrochek.data.UserState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MainDashboard(
    onNavigate: (NavDestination) -> Unit,
) {
    var showNotifications by remember { mutableStateOf(false) }
    val currentDate = remember {
        LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        )
    }

    LaunchedEffect(Unit) {
        if (!UserState.isUserValid()) {
            onNavigate(NavDestination.Login)
            return@LaunchedEffect
        }
    }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
        // Remove SideBar
        Box(modifier = Modifier.weight(1f)) {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface
                    )
                }
                Divider()
                // Stats section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickStatCard(
                        icon = Icons.Default.ShoppingCart,
                        title = "Total Sales",
                        value = "$24,780",
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
                        name = "sales"
                    )
                    QuickStatCard(
                        icon = Icons.Default.LocalShipping,
                        title = "Orders",
                        value = "45",
                        backgroundColor = MaterialTheme.colors.secondary,
                        contentColor = MaterialTheme.colors.onSecondary,
                        name = "orders"
                    )
                    QuickStatCard(
                        icon = Icons.Default.FolderOpen,
                        title = "Projects",
                        value = "12",
                        backgroundColor = AppColors.Success,
                        contentColor = AppColors.SuccessLight,
                        name = "projects"
                    )
                    QuickStatCard(
                        icon = Icons.Default.AssignmentTurnedIn,
                        title = "Tasks",
                        value = "25/48",
                        backgroundColor = Color.LightGray,
                        contentColor = Color.Gray,
                        name = "tasks"
                    )
                }

                // Content section
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Recent Activity
                    Surface(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(16.dp),
                        elevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            // Activity content will go here
                        }
                    }

                    // Quick Actions
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(16.dp),
                        elevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            ActionButton(
                                icon = Icons.Default.Add,
                                text = "New Order",
                                onClick = { /* Handle click */ }
                            )
                            ActionButton(
                                icon = Icons.Default.Person,
                                text = "Add Customer",
                                onClick = { /* Handle click */ }
                            )
                            ActionButton(
                                icon = Icons.Default.Inventory,
                                text = "Add Product",
                                onClick = { /* Handle click */ }
                            )
                            ActionButton(
                                icon = Icons.Default.Assignment,
                                text = "Create Invoice",
                                onClick = { /* Handle click */ }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add notifications dropdown or dialog here when showNotifications is true
    if (showNotifications) {
        NotificationsDropdown(
            onDismiss = { showNotifications = false }
        )
    }
}

@Composable
private fun NotificationsDropdown(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications") },
        text = {
            Column {
                NotificationItem("New quotation request received", "10 minutes ago")
                Divider()
                NotificationItem("User John Doe updated their profile", "1 hour ago")
                Divider()
                NotificationItem("System update available", "2 hours ago")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun NotificationItem(
    message: String,
    timeAgo: String
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = timeAgo,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}
