package com.mikrochek.composables.layout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikrochek.data.UserState
import com.mikrochek.navigation.AppMenuItems
import com.mikrochek.navigation.AppMenuItem
import com.mikrochek.theme.AppColors

@Composable
fun Sidebar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val currentUser = UserState.getCurrentUser()
    val menuItems = AppMenuItems.getMenuItemsByCategory(currentUser)
    val scrollState = rememberScrollState()
    
    // Find current category from route
    val currentCategory = remember(currentRoute) {
        menuItems.entries.find { (_, items) -> 
            items.any { it.title == currentRoute }
        }?.key ?: "Dashboard"
    }
    
    var expandedCategory by remember(currentCategory) { 
        mutableStateOf(currentCategory)
    }
    
    val logoutInteractionSource = remember { MutableInteractionSource() }
    val isLogoutHovered by logoutInteractionSource.collectIsHoveredAsState()

    val menuItemShape = RoundedCornerShape(12.dp)

    // Animation specs
    val transitionSpec = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    Column(
        modifier = Modifier
            .width(250.dp)
            .fillMaxHeight()
            .background(AppColors.Gray900)
    ) {
        // Profile Section with fade-in animation
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500)) + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.Primary)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.name?.firstOrNull()?.toString() ?: "P",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = currentUser?.name ?: "Pradyumna Tanksali",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "kush",
                            color = AppColors.Gray500,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Divider(color = AppColors.Gray800)

        // Scrollable Navigation Menu
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 8.dp)
        ) {
            menuItems.forEach { (category, items) ->
                val isExpanded = category == expandedCategory
                val categoryInteractionSource = remember { MutableInteractionSource() }
                val isCategoryHovered by categoryInteractionSource.collectIsHoveredAsState()
                val hasSelectedChild = items.any { it.title == currentRoute }
                
                // Rotation animation for arrow
                val rotationAngle by animateFloatAsState(
                    targetValue = if (isExpanded) 0f else -90f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
                
                // Category Header with hover animation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = if (isExpanded) 4.dp else 0.dp)
                        .clip(menuItemShape)
                        .clickable(
                            interactionSource = categoryInteractionSource,
                            indication = null
                        ) {
                            expandedCategory = if (isExpanded) "" else category
                        }
                        .hoverable(categoryInteractionSource)
                        .background(
                            when {
                                hasSelectedChild -> AppColors.Gray800
                                isCategoryHovered -> AppColors.Gray800.copy(alpha = 0.5f)
                                else -> Color.Transparent
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = when {
                            hasSelectedChild -> AppColors.Primary
                            isCategoryHovered -> AppColors.Gray300
                            else -> AppColors.Gray500
                        },
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category,
                        color = when {
                            hasSelectedChild -> AppColors.Primary
                            isCategoryHovered -> AppColors.Gray300
                            else -> AppColors.Gray500
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Category Items with slide animation
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        items.forEach { item ->
                            val isSelected = currentRoute == item.title
                            val itemInteractionSource = remember { MutableInteractionSource() }
                            val isItemHovered by itemInteractionSource.collectIsHoveredAsState()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 4.dp)
                                    .clip(menuItemShape)
                                    .clickable(
                                        interactionSource = itemInteractionSource,
                                        indication = null
                                    ) { onNavigate(item.title) }
                                    .hoverable(itemInteractionSource)
                                    .background(
                                        when {
                                            isSelected -> AppColors.Primary.copy(alpha = 0.15f)
                                            isItemHovered -> AppColors.Gray800.copy(alpha = 0.5f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .padding(start = 40.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                                    .animateContentSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = when {
                                        isSelected -> AppColors.Primary
                                        isItemHovered -> AppColors.Gray300
                                        else -> AppColors.Gray500
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.title,
                                    color = when {
                                        isSelected -> AppColors.Primary
                                        isItemHovered -> AppColors.Gray300
                                        else -> AppColors.Gray500
                                    },
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom App Info with slide-up animation
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Column {
                Divider(color = AppColors.Gray800)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "MikroChek",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .hoverable(logoutInteractionSource)
                            .clickable(
                                interactionSource = logoutInteractionSource,
                                indication = null
                            ) { onLogout() }
                            .background(
                                animateColorAsState(
                                    if (isLogoutHovered) AppColors.Gray800 
                                    else Color.Transparent,
                                    animationSpec = tween(300)
                                ).value
                            )
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = animateColorAsState(
                                if (isLogoutHovered) AppColors.Gray300 
                                else AppColors.Gray500,
                                animationSpec = tween(300)
                            ).value
                        )
                    }
                }
            }
        }
    }
} 