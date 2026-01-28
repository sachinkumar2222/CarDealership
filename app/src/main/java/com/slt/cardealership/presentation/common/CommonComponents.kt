package com.slt.cardealership.presentation.common

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slt.cardealership.ui.theme.BrandBlue

@Composable
fun LoadingAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = BrandBlue)
    }
}

@Composable
fun FullScreenError(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Retry", color = Color.White)
                }
            }
        }
    }
}

// --- NEW COMPONENTS ---

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color.Black.copy(alpha = 0.8f),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val blueGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (enabled) Modifier
                    .background(blueGradient)
                    .clickable(onClick = onClick)
                else Modifier
                    .background(Color.Gray.copy(alpha = 0.5f))
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun GoalSelectionCard(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    selectedGoal: String,
    onSelect: (String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect(title) },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selectedGoal == title) BrandBlue else Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
            RadioButton(
                selected = selectedGoal == title,
                onClick = { onSelect(title) },
                colors = RadioButtonDefaults.colors(selectedColor = BrandBlue)
            )
        }
    }
}

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.7f)) },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                disabledBorderColor = Color.LightGray.copy(alpha = 0.3f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF5F5F5), // Slightly gray for disabled
                errorContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            interactionSource = interactionSource ?: remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        )
    }
}

@Composable
fun AnimatedDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "rotation"
    )

    Column(modifier = modifier) {
        Box {
            // Reusing LabeledTextField for consistency
            LabeledTextField(
                label = label,
                value = selectedOption,
                onValueChange = {},
                placeholder = "Select $label",
                readOnly = true,
                enabled = enabled, // NOTE: LabeledTextField internal enabled check might gray it out. 
                           // For dropdowns, we usually want it to look enabled but be read-only text.
                           // But if 'enabled' param is false, it should look disabled.
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Drop Down",
                        modifier = Modifier.rotate(rotationState),
                        tint = if (enabled) Color.Black else Color.Gray
                    )
                }
            )
            
            // Interaction overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 28.dp) // Offset to account for label height approx? 
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled) { expanded = !expanded }
            )
        }

        AnimatedVisibility(
            visible = expanded && enabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .heightIn(max = 250.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(options.size) { index ->
                        val option = options[index]
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (option == selectedOption) BrandBlue else Color.Black
                                )
                            },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        )
                        if (index < options.size - 1) {
                            HorizontalDivider(
                                color = Color.LightGray.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
