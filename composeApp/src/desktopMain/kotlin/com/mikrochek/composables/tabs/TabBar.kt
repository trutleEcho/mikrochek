package com.mikrochek.composables.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikrochek.navigation.*
import com.mikrochek.theme.AppColors

@Composable
fun TabBar(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.background,
        elevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
                .padding(8.dp)
        ) {
            // Single row containing all tabs and the new tab button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = TabManager.tabs
                val selectedTabId = TabManager.selectedTabId
                
                // Existing tabs
                tabs.forEach { tab ->
                    TabItem(
                        tab = tab,
                        isSelected = tab.id == selectedTabId,
                        onSelect = { TabManager.selectTab(tab.id) },
                        onClose = { TabManager.removeTab(tab.id) }
                    )
                }
                
                // New tab button - directly next to the last tab
                NewTabButton()
            }
        }
    }
}

@Composable
private fun NewTabButton() {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
            .clickable {
                // Explicitly create a new tab with the dashboard
                TabManager.addTab(
                    Tab(
                        title = "New Tab",
                        destination = NavDestination.MainDashboard
                    )
                )
            }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New Tab",
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TabItem(
    tab: Tab,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    // Define styles based on selection state
    val backgroundColor = when {
        isSelected -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colors.surface
    }
    
    val borderColor = when {
        isSelected -> MaterialTheme.colors.primary
        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
    }
    
    val textColor = when {
        isSelected -> MaterialTheme.colors.primary
        else -> MaterialTheme.colors.onSurface
    }
    
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab title with limited width
            Text(
                text = tab.title,
                style = MaterialTheme.typography.subtitle2,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 150.dp)
            )
            
            // Only show close button for closable tabs
            if (tab.canClose) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close tab",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
} 