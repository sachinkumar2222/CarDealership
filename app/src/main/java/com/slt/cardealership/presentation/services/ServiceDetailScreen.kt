package com.slt.cardealership.presentation.services

// ... (Imports are fine) ...
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceTitle: String,
    onBackClick: () -> Unit,
    viewModel: ServicesViewModel
) {
    val customColor = Color(0xFF11233c)
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val serviceItem = remember(uiState.serviceItems, serviceTitle) {
        uiState.serviceItems.find { it.name == serviceTitle }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ServicesEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ServicesEvent.SaveSuccessAndNavBack -> {
                    onBackClick()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(serviceTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.shadow(4.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = customColor,
                    navigationIconContentColor = customColor
                )
            )
        },

        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->

        if (serviceItem == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                if(uiState.isLoading) CircularProgressIndicator() else Text("Error: Service '$serviceTitle' not found.")
            }
        } else {
            Column(
                modifier = Modifier.padding(paddingValues).fillMaxSize().verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                ServiceToggleCard(
                    isServiceEnabled = serviceItem.isEnabled,
                    onToggle = { isEnabled -> viewModel.onServiceToggled(serviceItem.id, isEnabled) },
                    customColor = customColor
                )
                Spacer(modifier = Modifier.height(20.dp))

                AnimatedVisibility(
                    visible = serviceItem.isEnabled,
                    enter = slideInVertically { fullHeight -> fullHeight / 10 } + fadeIn(),
                    exit = slideOutVertically { fullHeight -> -fullHeight / 10 } + fadeOut()
                ) {
                    Column {
                        ServiceContentPage(
                            serviceItem = serviceItem,
                            onLeadFormToggle = { isChecked -> viewModel.onLeadFormToggled(serviceItem.id, isChecked) },
                            // --- CONNECT ALL NEW FUNCTIONS ---
                            onSettingChange = { newEmail, newAdfEmail, newPhone ->
                                viewModel.onPrimarySettingChanged(serviceItem.id, newEmail, newAdfEmail, newPhone)
                            },

                            // Lead Emails
                            onAddLeadEmail = { viewModel.addLeadEmail(serviceItem.id) },
                            onUpdateLeadEmail = { idx, txt -> viewModel.updateLeadEmail(serviceItem.id, idx, txt) },
                            onDeleteLeadEmail = { idx -> viewModel.deleteLeadEmail(serviceItem.id, idx) },

                            // ADF Emails
                            onAddAdfEmail = { viewModel.addAdfEmail(serviceItem.id) },
                            onUpdateAdfEmail = { idx, txt -> viewModel.updateAdfEmail(serviceItem.id, idx, txt) },
                            onDeleteAdfEmail = { idx -> viewModel.deleteAdfEmail(serviceItem.id, idx) },

                            // Lead Phones
                            onAddLeadPhone = { viewModel.addLeadPhone(serviceItem.id) },
                            onUpdateLeadPhone = { idx, txt -> viewModel.updateLeadPhone(serviceItem.id, idx, txt) },
                            onDeleteLeadPhone = { idx -> viewModel.deleteLeadPhone(serviceItem.id, idx) },

                            customColor = customColor
                        )

                    }
                }

                AnimatedVisibility(
                    visible = !serviceItem.isEnabled,
                    enter = slideInVertically { fullHeight -> -fullHeight / 10 } + fadeIn(),
                    exit = slideOutVertically { fullHeight -> -fullHeight / 10 } + fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 20.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = "Disabled", tint = Color.Gray.copy(alpha = 0.7f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("This service is currently disabled.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = customColor)
                            Text("Click 'Enable' at the top to configure this service.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.saveServices() },
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(text = "Save", modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(40.dp)) // Extra padding at bottom
            }
        }
    }
}

// (ServiceToggleCard composable unchanged)
@Composable
fun ServiceToggleCard(isServiceEnabled: Boolean, onToggle: (Boolean) -> Unit, customColor: Color) {
    /* ... Same as before ... */
    val backgroundColor = if (isServiceEnabled) Color(0xFFE0F7FA) else Color(0xFFFDECEA)
    val buttonColor = if (isServiceEnabled) Color(0xFFDC3545) else Color(0xFF007BFF)
    val buttonText = if (isServiceEnabled) "Disable" else "Enable"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(2.dp, customColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Toggle if you provide this service",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = { onToggle(!isServiceEnabled) },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Text(buttonText, color = Color.White)
        }
    }
}


@Composable
fun ServiceContentPage(
    serviceItem: ServiceUiItem,
    onLeadFormToggle: (Boolean) -> Unit,

    // Updated Signature
    onSettingChange: (newEmail: String, newAdfEmail: String, newPhone: String) -> Unit,

    onAddLeadEmail: () -> Unit,
    onUpdateLeadEmail: (Int, String) -> Unit,
    onDeleteLeadEmail: (Int) -> Unit,

    onAddAdfEmail: () -> Unit,
    onUpdateAdfEmail: (Int, String) -> Unit,
    onDeleteAdfEmail: (Int) -> Unit,

    onAddLeadPhone: () -> Unit,
    onUpdateLeadPhone: (Int, String) -> Unit,
    onDeleteLeadPhone: (Int) -> Unit,

    customColor: Color
) {
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        focusedIndicatorColor = Color(0xFF2196F3),
        unfocusedIndicatorColor = Color.LightGray,
        focusedTextColor = customColor,
        unfocusedTextColor = customColor,
        cursorColor = Color(0xFF2196F3),
        focusedLabelColor = Color(0xFF2196F3)
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ... (Info Box is same) ...
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, customColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .background(customColor.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Info",
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Copy & Paste to replicate the email address across all services.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = customColor
                )
                Text(
                    "Note: Doing this will clear old values and replace them with new values.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
        }

        // Lead Form Toggle
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .background(if (serviceItem.leadFormEnabled) Color.Transparent else Color(0xFFFFFBE6))
                .border(1.dp, if (serviceItem.leadFormEnabled) Color(0xFFFFFBE6) else Color.Transparent, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Turn On Lead Form", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = customColor)
                CustomSwitch(
                    checked = serviceItem.leadFormEnabled,
                    onCheckedChange = { isChecked -> if (serviceItem.supportsLeadForms) onLeadFormToggle(isChecked) },
                    enabled = serviceItem.supportsLeadForms
                )
            }
            if (!serviceItem.leadFormEnabled) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "Note", tint = Color(0xFFF9A825), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Turn this toggle ON if you want to receive leads from Finance websites.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF616161))
                        Text("Note: If you turn the lead form ON or OFF, the change will take 24 to 48 hours to reflect on the websites.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF9A825))
                    }
                }
            }
        }

        // Primary Fields
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = serviceItem.primaryEmail,
                    onValueChange = { onSettingChange(it, serviceItem.primaryAdfEmail, serviceItem.primaryPhone) },
                    label = { Text("Primary Email *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
                OutlinedTextField(
                    value = serviceItem.primaryAdfEmail,
                    onValueChange = { onSettingChange(serviceItem.primaryEmail, it, serviceItem.primaryPhone) },
                    label = { Text("Primary ADF Email") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
            }
            // --- UPDATED: Primary Phone is now enabled ---
            OutlinedTextField(
                value = serviceItem.primaryPhone,
                onValueChange = { onSettingChange(serviceItem.primaryEmail, serviceItem.primaryAdfEmail, it) },
                label = { Text("Primary Phone") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                singleLine = true
            )
        }

        // --- CONNECTED DYNAMIC LISTS ---
        ServiceInputField(
            title = "Lead Emails",
            items = serviceItem.leadEmails,
            onAddItem = onAddLeadEmail,
            onUpdateItem = onUpdateLeadEmail,
            onDeleteItem = onDeleteLeadEmail,
            customColor = customColor
        )

        ServiceInputField(
            title = "ADF Lead Emails",
            items = serviceItem.adfLeadEmails,
            onAddItem = onAddAdfEmail,
            onUpdateItem = onUpdateAdfEmail,
            onDeleteItem = onDeleteAdfEmail,
            customColor = customColor
        )

        ServiceInputField(
            title = "Lead Phone",
            items = serviceItem.leadPhones,
            onAddItem = onAddLeadPhone,
            onUpdateItem = onUpdateLeadPhone,
            onDeleteItem = onDeleteLeadPhone,
            customColor = customColor
        )
    }
}

// (ServiceInputField and CustomSwitch unchanged)
@Composable
fun ServiceInputField(
    title: String,
    items: List<String>,
    onAddItem: () -> Unit,
    onUpdateItem: (index: Int, text: String) -> Unit,
    onDeleteItem: (index: Int) -> Unit,
    customColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = customColor
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, customColor, RoundedCornerShape(12.dp))
                    .clickable { onAddItem() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add $title",
                    tint = customColor
                )
            }
        }

        if (items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        items.forEachIndexed { index, text ->
            OutlinedTextField(
                value = text,
                onValueChange = { onUpdateItem(index, it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter $title") },
                trailingIcon = {
                    IconButton(onClick = { onDeleteItem(index) }) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color(0xFF2196F3),
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedTextColor = customColor,
                    unfocusedTextColor = customColor,
                    cursorColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                )
            )
        }
    }
}

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val trackWidth = 60.dp
    val trackHeight = 30.dp
    val trackBorder = 4.dp
    val thumbDiameter = trackHeight - (trackBorder * 2)

    val thumbPaddingStart = trackBorder
    val thumbPaddingEnd = trackWidth - thumbDiameter - trackBorder

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) thumbPaddingEnd else thumbPaddingStart,
        label = "thumbOffset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (!enabled) Color.Gray.copy(alpha = 0.5f)
        else if (checked) Color(0xFF2196F3)
        else Color.LightGray,
        label = "trackColor"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (!enabled) Color.LightGray else Color.White,
        label = "thumbColor"
    )

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (enabled) onCheckedChange(!checked) }
            )
            .padding(trackBorder)
    ) {
        Box(
            modifier = Modifier
                .size(thumbDiameter)
                .offset(x = thumbOffset - thumbPaddingStart)
                .clip(CircleShape)
                .background(thumbColor)
                .shadow(elevation = 4.dp, shape = CircleShape)
        )
    }
}