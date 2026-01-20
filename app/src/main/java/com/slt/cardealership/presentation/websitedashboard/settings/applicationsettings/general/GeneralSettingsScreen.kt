package com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.general

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import  androidx.compose.ui.graphics.SolidColor
import com.slt.cardealership.ui.theme.BrandBlue
import com.slt.cardealership.ui.theme.LightBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    navController: NavController,
    domainId: Int,
    viewModel: GeneralSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(domainId) {
        viewModel.loadSettings(domainId)
    }

    // Mutable state for form fields
    var globalStyle by remember(uiState.settings) { mutableStateOf(uiState.settings?.globalStyle ?: "") }
    var headerScript by remember(uiState.settings) { mutableStateOf(uiState.settings?.headerScript ?: "") }
    var footerScript by remember(uiState.settings) { mutableStateOf(uiState.settings?.footerScript ?: "") }
    var copyrightContent by remember(uiState.settings) { mutableStateOf(uiState.settings?.copyrightContent ?: "") }
    var robotsMetaTags by remember(uiState.settings) { mutableStateOf(uiState.settings?.robotsMetaTags ?: emptyList()) }
    var robotsFileContent by remember(uiState.settings) { mutableStateOf(uiState.settings?.robotsFileContent ?: "") }
    var address by remember(uiState.settings) { mutableStateOf(uiState.settings?.address ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = { Text("General Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = LightBackground
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}", color = Color.Red)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Global Styles
                CodeEditorField(
                    label = "Global styles",
                    value = globalStyle,
                    onValueChange = { globalStyle = it },
                    minHeight = 200.dp
                )

                // Header Script
                CodeEditorField(
                    label = "Header script",
                    value = headerScript,
                    onValueChange = { headerScript = it }
                )

                // Footer Script
                CodeEditorField(
                    label = "Footer script",
                    value = footerScript,
                    onValueChange = { footerScript = it }
                )

                // Copyright Text
                CodeEditorField(
                    label = "Copyright text",
                    value = copyrightContent,
                    onValueChange = { copyrightContent = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Robots meta tag
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Robots meta tag", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        RobotsMetaTagDropdown(
                            selectedTags = robotsMetaTags,
                            onTagsChanged = { robotsMetaTags = it }
                        )
                    }
                }

                // Robots.txt
                CodeEditorField(
                    label = "Robot.txt",
                    value = robotsFileContent,
                    onValueChange = { robotsFileContent = it },
                    minHeight = 150.dp,
                    isLight = true
                )

                // Address
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Address", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = Color(0xFFE9ECEF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.saveSettings(
                            domainId, globalStyle, headerScript, footerScript,
                            copyrightContent, robotsMetaTags, robotsFileContent, address
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CodeEditorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minHeight: androidx.compose.ui.unit.Dp = 300.dp,
    isLight: Boolean = false
) {
    if (isLight) {
        // Simple Version for Robots.txt (Light Mode)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(label, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(minHeight),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFE9ECEF),
                    unfocusedBorderColor = Color(0xFFE9ECEF),
                    cursorColor = BrandBlue
                )
            )
        }
    } else {
        // VS Code-like Editor (Dark Mode)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(label, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(minHeight)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            ) {
                // Re-implementation with unified scroll
                val verticalScrollState = rememberScrollState()
                val horizontalScrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(verticalScrollState)
                ) {
                    // Line Numbers Gutter
                    val lines = value.split("\n")
                    Column(
                        modifier = Modifier
                            .width(48.dp) // Increased width to prevent wrapping of 3+ digit numbers
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        lines.indices.forEach { i ->
                            Text(
                                text = "${i + 1}",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = Color(0xFF858585),
                                    lineHeight = 20.sp
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                            )
                        }
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(with(androidx.compose.ui.platform.LocalDensity.current) { (lines.size * 20).sp.toDp() }) // approx height match
                            .background(Color(0xFF333333))
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Code Area with Horizontal Scroll
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = Color(0xFFD4D4D4),
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(Color.White),
                            // Allow it to grow horizontally by NOT setting width constraint and letting scroll handle it
                            modifier = Modifier.wrapContentWidth(Alignment.Start),
                            visualTransformation = SyntaxHighlightTransformation()
                        )
                    }
                }
            }
        }
    }
}

class SyntaxHighlightTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        return androidx.compose.ui.text.input.TransformedText(
            text = highlightCode(text.text),
            offsetMapping = androidx.compose.ui.text.input.OffsetMapping.Identity
        )
    }

    private fun highlightCode(text: String): androidx.compose.ui.text.AnnotatedString {
        return androidx.compose.ui.text.buildAnnotatedString {
            append(text)

            val keywords = listOf("html", "body", "div", "span", "var", "function", "const", "let", "if", "else", "return", "class", "id")
            val colorMap = mapOf(
                Regex("(\".*?\")|('.*?')") to Color(0xFFCE9178), // Strings (Orange)
                Regex("\\b(${keywords.joinToString("|")})\\b") to Color(0xFF569CD6), // Keywords (Blue)
                Regex("([.#][a-zA-Z0-9_-]+)") to Color(0xFFD7BA7D), // CSS Classes/IDs (Yellow)
                Regex("(:)") to Color.White,
                Regex("(\\{|\\})") to Color(0xFFFFD700) // Brackets (Gold)
            )

            colorMap.forEach { (regex, color) ->
                regex.findAll(text).forEach { result ->
                    addStyle(
                        style = androidx.compose.ui.text.SpanStyle(color = color),
                        start = result.range.first,
                        end = result.range.last + 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RobotsMetaTagDropdown(
    selectedTags: List<String>,
    onTagsChanged: (List<String>) -> Unit
) {
    val options = listOf("index", "noindex", "follow", "nofollow", "noodp", "none")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE9ECEF), RoundedCornerShape(12.dp))
                .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(12.dp)
                .heightIn(min = 56.dp) // Minimum touch target height
        ) {
            if (selectedTags.isEmpty()) {
                Text(
                    text = "Select tags",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(end = 24.dp), // Space for arrow
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedTags.forEach { tag ->
                        InputChip(
                            selected = true,
                            onClick = { onTagsChanged(selectedTags - tag) },
                            label = { Text(tag) },
                            enabled = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = Color(0xFFE9ECEF),
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }

            // Dropdown Arrow
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd)
            )

            // Dropdown Menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Slightly smaller than screen width
                    .background(Color.White)
            ) {
                options.forEach { option ->
                    val isSelected = selectedTags.contains(option)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null, // Handled by item click
                                    colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(option)
                            }
                        },
                        onClick = {
                            val newTags = if (isSelected) {
                                selectedTags - option
                            } else {
                                selectedTags + option
                            }
                            onTagsChanged(newTags)
                            // Keep menu open for multi-select convenience?
                            // Usually better to keep open or close? User didn't specify.
                            // Standard multi-select keeps open.
                        }
                    )
                }
            }
        }
    }
}
