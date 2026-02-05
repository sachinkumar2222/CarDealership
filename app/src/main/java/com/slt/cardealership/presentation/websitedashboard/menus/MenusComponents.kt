package com.slt.cardealership.presentation.websitedashboard.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.slt.cardealership.domain.model.DomainMenuItem
import com.slt.cardealership.domain.model.DomainPage
import com.slt.cardealership.presentation.common.LabeledTextField

@Composable
fun RecursiveMenuItem(
    item: DomainMenuItem,
    onUpdate: (DomainMenuItem) -> Unit,
    onRemove: () -> Unit,
    onAddSubMenu: (DomainMenuItem) -> Unit,
    depth: Int = 0
) {
    var isExpanded by remember { mutableStateOf(false) }

    val displayPage = if (!item.pageSlug.isNullOrEmpty()) "Page(${item.pageSlug})" else "Custom Link"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = if (depth > 0) 0.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)), // Light gray/white bg
        shape = RoundedCornerShape(4.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.menuLabel,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayPage,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = Color.Gray
                    )
                }
            }

            // Expanded Content
            if (isExpanded) {
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Column(modifier = Modifier.padding(16.dp)) {
                    // Menu Label
                    LabeledTextField(
                        label = "Menu label*",
                        value = item.menuLabel,
                        onValueChange = { onUpdate(item.copy(menuLabel = it)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        placeholder = "Enter menu label"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Page / URL
                    val label = if (!item.pageSlug.isNullOrEmpty()) "Page*" else "Menu URL"
                    LabeledTextField(
                        label = label,
                        value = item.pageSlug ?: item.customUrl ?: "",
                        onValueChange = {
                            if (!item.pageSlug.isNullOrEmpty()) {
                                onUpdate(item.copy(pageSlug = it))
                            } else {
                                onUpdate(item.copy(customUrl = it))
                            }
                        },
                        readOnly = !item.pageSlug.isNullOrEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .background(if (!item.pageSlug.isNullOrEmpty()) Color.LightGray.copy(alpha = 0.1f) else Color.Transparent),
                        placeholder = if (item.pageSlug.isNullOrEmpty()) "Enter URL" else ""
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Parameter
                    if (!item.pageSlug.isNullOrEmpty()) {
                        LabeledTextField(
                            label = "Parameter",
                            value = item.prms ?: "",
                            onValueChange = { onUpdate(item.copy(prms = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "/new OR ?brands=example"
                        )
                        Text("(For ex: /new OR ?brands=example)", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Remove Link
                    Text(
                        text = "Remove",
                        color = Color.Red,
                        modifier = Modifier.clickable { onRemove() }.padding(vertical = 4.dp),
                        style = TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sub menus - Only allow adding if depth < 1 (Max 1 level of nesting: Root -> Child)
                    if (depth < 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sub menus", fontWeight = FontWeight.Bold)
                            OutlinedButton(
                                onClick = { onAddSubMenu(item) },
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2196F3)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Add Sub-Menu(s)")
                            }
                        }
                    }

                    if (item.childItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        item.childItems.forEachIndexed { index, child ->
                            RecursiveMenuItem(
                                item = child,
                                depth = depth + 1,
                                onUpdate = { updatedChild ->
                                    val newChildren = item.childItems.toMutableList()
                                    newChildren[index] = updatedChild
                                    onUpdate(item.copy(childItems = newChildren))
                                },
                                onRemove = {
                                    val newChildren = item.childItems.toMutableList()
                                    newChildren.removeAt(index)
                                    onUpdate(item.copy(childItems = newChildren))
                                },
                                onAddSubMenu = onAddSubMenu
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemBottomSheet(
    sheetState: SheetState,
    pages: List<DomainPage>,
    selectedPageIds: Set<String>,
    onDismiss: () -> Unit,
    onPageSelectionChange: (String, Boolean) -> Unit,
    onAddPages: () -> Unit,
    onAddCustomLink: (String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Page, 1 = Custom link
    var url by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp) // Add padding for bottom safe area
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add menu item", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider()

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF2196F3),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF2196F3)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Page") },
                    selectedContentColor = Color(0xFF2196F3),
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Custom link") },
                    selectedContentColor = Color(0xFF2196F3),
                    unselectedContentColor = Color.Gray
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (selectedTab == 0) {
                    // Page List
                    Column(
                        modifier = Modifier
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        pages.forEach { page ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val isChecked = !selectedPageIds.contains(page.id)
                                        onPageSelectionChange(page.id, isChecked)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedPageIds.contains(page.id),
                                    onCheckedChange = { isChecked ->
                                        onPageSelectionChange(page.id, isChecked)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
                                )
                                Text(page.pageName, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onAddPages,
                        modifier = Modifier.fillMaxWidth(), // Full width
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(8.dp) // Consistent shape
                    ) {
                        Text("Add To Menu")
                    }
                } else {
                    // Custom Link
                    LabeledTextField(
                        label = "Menu label*",
                        value = label,
                        onValueChange = { label = it },
                        placeholder = "Menu label* (Required)",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    LabeledTextField(
                        label = "Menu URL",
                        value = url,
                        onValueChange = { url = it },
                        placeholder = "Menu Url",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("(Note: \"#\" in the URL will work as label)", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { onAddCustomLink(label, url) },
                        modifier = Modifier.fillMaxWidth(), // Full width
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add To Menu")
                    }
                }
            }
        }
    }
}


@Composable
fun MenuCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null, // Handled by Row click
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
