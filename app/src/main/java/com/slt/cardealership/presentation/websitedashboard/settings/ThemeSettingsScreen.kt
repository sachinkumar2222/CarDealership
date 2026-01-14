package com.slt.cardealership.presentation.websitedashboard.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.slt.cardealership.domain.model.DomainFont
import com.slt.cardealership.domain.model.DomainThemeSetting
import com.slt.cardealership.ui.theme.BrandBlue
import com.slt.cardealership.utils.uriToFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    domainId: Int,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Image Picker Logic
    var currentUploadType by remember { mutableStateOf("") }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null && currentUploadType.isNotEmpty()) {
                viewModel.uploadImage(file, currentUploadType, domainId)
            }
        }
    }

    LaunchedEffect(domainId) {
        viewModel.init(domainId)
    }

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandBlue,
        focusedLabelColor = BrandBlue,
        cursorColor = BrandBlue,
        unfocusedBorderColor = Color(0xFFE9ECEF),
        focusedContainerColor = Color(0xFFF8F9FA),
        unfocusedContainerColor = Color(0xFFF8F9FA)
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandBlue)
        }
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                uiState.themeSetting?.let { settings ->
                    // Theme Settings Form
                    ThemeSettingItem(label = "Font family:") {
                        DropdownSelector(
                            selectedItem = uiState.fonts.find { it.id == settings.fontId }?.name ?: "Select Font",
                            items = uiState.fonts.map { it.name },
                            inputColors = inputColors
                        ) { Name ->
                            val font = uiState.fonts.find { it.name == Name }
                            if(font != null) {
                                viewModel.updateSetting(settings.copy(fontId = font.id))
                            }
                        }
                    }

                    ThemeSettingItem(label = "Primary color:") {
                        ColorPickerDisplay(colorCode = settings.primaryColor ?: "#000000", inputColors = inputColors)
                    }

                    ThemeSettingItem(label = "Secondary color:") {
                        ColorPickerDisplay(colorCode = settings.secondaryColor ?: "#000000", inputColors = inputColors)
                    }

                    ThemeSettingItem(label = "Button text color:") {
                        ColorPickerDisplay(colorCode = settings.buttonTextColor ?: "#FFFFFF", isTransparent = settings.buttonTextColor == null, inputColors = inputColors)
                    }

                    ThemeSettingItem(label = "Line height:") {
                        OutlinedTextField(
                            value = settings.lineHeight?.toString() ?: "",
                            onValueChange = {
                                viewModel.updateSetting(settings.copy(lineHeight = it.toDoubleOrNull()))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = inputColors,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    ThemeSettingItem(label = "Font weight:") {
                        DropdownSelector(
                            selectedItem = settings.fontWeight?.toString() ?: "400",
                            items = listOf("100", "200", "300", "400", "500", "600", "700", "800", "900"),
                            inputColors = inputColors
                        ) {
                            viewModel.updateSetting(settings.copy(fontWeight = it.toIntOrNull()))
                        }
                    }

                    ThemeSettingItem(label = "Letter spacing:") {
                        OutlinedTextField(
                            value = settings.letterSpacing?.toString() ?: "0",
                            onValueChange = {
                                viewModel.updateSetting(settings.copy(letterSpacing = it.toDoubleOrNull()))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Text("px", modifier = Modifier.padding(end = 16.dp)) },
                            colors = inputColors,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    ThemeSettingItem(label = "Header type:") {
                        val headerOptions = listOf("Header 1", "Header 2")
                        val displayValue = when(settings.headerType) {
                            "header_1" -> "Header 1"
                            "header_2" -> "Header 2"
                            else -> "Header 1"
                        }

                        DropdownSelector(
                            selectedItem = displayValue,
                            items = headerOptions,
                            inputColors = inputColors
                        ) { selected ->
                            val apiValue = when(selected) {
                                "Header 1" -> "header_1"
                                "Header 2" -> "header_2"
                                else -> "header_1"
                            }
                            viewModel.updateSetting(settings.copy(headerType = apiValue))
                        }
                    }

                    ThemeSettingItem(label = "Show Topbar:") {
                        Switch(
                            checked = settings.showTopBar == true,
                            onCheckedChange = {
                                viewModel.updateSetting(settings.copy(showTopBar = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandBlue)
                        )
                    }

                    ThemeSettingItem(label = "Footer type:") {
                        val footerOptions = listOf("Footer 1", "Footer 2")
                        val displayValue = when(settings.footerType) {
                            "footer_1" -> "Footer 1"
                            "footer_2" -> "Footer 2"
                            else -> "Footer 1"
                        }

                        DropdownSelector(
                            selectedItem = displayValue,
                            items = footerOptions,
                            inputColors = inputColors
                        ) { selected ->
                            val apiValue = when(selected) {
                                "Footer 1" -> "footer_1"
                                "Footer 2" -> "footer_2"
                                else -> "footer_1"
                            }
                            viewModel.updateSetting(settings.copy(footerType = apiValue))
                        }
                    }

                    ThemeSettingItem(label = "Footer social media:") {
                        Switch(
                            checked = settings.showFooterSocialMedia == true,
                            onCheckedChange = {
                                viewModel.updateSetting(settings.copy(showFooterSocialMedia = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandBlue)
                        )
                    }

                    HorizontalDivider()

                    Text("Heading style(s)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    ThemeSettingItem(label = "Font family:") {
                        DropdownSelector(
                            selectedItem = uiState.fonts.find { it.id == settings.headingFontId }?.name ?: "Select Font",
                            items = uiState.fonts.map { it.name },
                            inputColors = inputColors
                        ) { Name ->
                            val font = uiState.fonts.find { it.name == Name }
                            if(font != null) {
                                viewModel.updateSetting(settings.copy(headingFontId = font.id))
                            }
                        }
                    }

                    ThemeSettingItem(label = "Line height:") {
                        OutlinedTextField(
                            value = settings.headingLineHeight?.toString() ?: "",
                            onValueChange = {
                                viewModel.updateSetting(settings.copy(headingLineHeight = it.toDoubleOrNull()))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = inputColors,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    ThemeSettingItem(label = "Font weight:") {
                        DropdownSelector(
                            selectedItem = settings.headingFontWeight?.toString() ?: "400",
                            items = listOf("100", "200", "300", "400", "500", "600", "700", "800", "900"),
                            inputColors = inputColors
                        ) {
                            viewModel.updateSetting(settings.copy(headingFontWeight = it.toIntOrNull()))
                        }
                    }


                    // Logo Section
                    Text("Dark logo", style = MaterialTheme.typography.titleMedium)
                    LogoUploadCard(label = "dark logo", imageUrl = settings.darkLogoUrl, onChange = {
                        currentUploadType = "dark_logo"
                        launcher.launch("image/*")
                    }, onRemove = { viewModel.removeImage("dark_logo") })

                    Text("Light logo", style = MaterialTheme.typography.titleMedium)
                    LogoUploadCard(label = "light logo", imageUrl = settings.lightLogoUrl, onChange = {
                        currentUploadType = "light_logo"
                        launcher.launch("image/*")
                    }, onRemove = { viewModel.removeImage("light_logo") })

                    Text("OG image", style = MaterialTheme.typography.titleMedium)
                    LogoUploadCard(label = "OG image", imageUrl = settings.ogLogoUrl, onChange = {
                        currentUploadType = "og_image"
                        launcher.launch("image/*")
                    }, onRemove = { viewModel.removeImage("og_image") })

                    Text("Favicon", style = MaterialTheme.typography.titleMedium)
                    LogoUploadCard(label = "favicon", imageUrl = settings.faviconUrl, onChange = {
                        currentUploadType = "favicon"
                        launcher.launch("image/*")
                    }, onRemove = { viewModel.removeImage("favicon") })


                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.toggleImportSheet(true) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue)
                        ) {
                            Text("Import Theme", color = BrandBlue)
                        }

                        Button(
                            onClick = { viewModel.saveSettings() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Text("Save")
                        }
                    }
                    if (uiState.isImportSheetVisible) {
                        ModalBottomSheet(
                            onDismissRequest = { viewModel.toggleImportSheet(false) },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                            containerColor = Color.White
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .padding(bottom = 32.dp), // Extra padding for bottom sheet
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Select theme to import",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                var selectedTheme by remember { mutableStateOf<com.slt.cardealership.domain.model.DomainDefaultTheme?>(null) }

                                DropdownSelector(
                                    selectedItem = selectedTheme?.name ?: "Select theme",
                                    items = uiState.defaultThemes.map { it.name },
                                    inputColors = inputColors
                                ) { name ->
                                    selectedTheme = uiState.defaultThemes.find { it.name == name }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
                                ) {
                                    Text(
                                        text = "Note: Importing and saving the new theme will overwrite all current changes and cannot be reverted.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF495057),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            // API call to save IMPORT logic would go here
                                            // For now, let's just close
                                            viewModel.toggleImportSheet(false)
                                        },
                                        enabled = selectedTheme != null,
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BrandBlue,
                                            disabledContainerColor = BrandBlue.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Text("Confirm")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSettingItem(
    label: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    selectedItem: String,
    items: List<String>,
    inputColors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandBlue,
        focusedLabelColor = BrandBlue,
        cursorColor = BrandBlue,
        unfocusedBorderColor = Color(0xFFE9ECEF),
        focusedContainerColor = Color(0xFFF8F9FA),
        unfocusedContainerColor = Color(0xFFF8F9FA)
    ),
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = inputColors,
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            fontWeight = if (item == selectedItem) FontWeight.Bold else FontWeight.Normal,
                            color = if (item == selectedItem) BrandBlue else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        expanded = false
                        onItemSelected(item)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun ColorPickerDisplay(
    colorCode: String,
    isTransparent: Boolean = false,
    inputColors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandBlue,
        focusedLabelColor = BrandBlue,
        cursorColor = BrandBlue,
        unfocusedBorderColor = Color(0xFFE9ECEF),
        focusedContainerColor = Color(0xFFF8F9FA),
        unfocusedContainerColor = Color(0xFFF8F9FA)
    )
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = colorCode,
            onValueChange = {},
            modifier = Modifier.weight(1f),
            colors = inputColors,
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        Box(
            modifier = Modifier
                .size(50.dp) // Match height of text field approx
                .clip(RoundedCornerShape(8.dp))
                .background(if(isTransparent) Color.Transparent else Color(android.graphics.Color.parseColor(if(colorCode.startsWith("#")) colorCode else "#$colorCode")))
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
        )
    }
}




@Composable
fun LogoUploadCard(label: String, imageUrl: String?, onChange: () -> Unit, onRemove: () -> Unit) {
    if (imageUrl.isNullOrEmpty()) {
        Button(
            onClick = onChange,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue.copy(alpha = 0.1f), contentColor = BrandBlue),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue.copy(alpha = 0.3f)),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = "Upload",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload $label", fontWeight = FontWeight.SemiBold)
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Logo",
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = onChange,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue)

                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change")
                    }

                    OutlinedButton(
                        onClick = onRemove,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove")
                    }
                }
            }
        }
    }
}
