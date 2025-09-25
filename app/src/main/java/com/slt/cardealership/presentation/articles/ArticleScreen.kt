package com.slt.cardealership.presentation.articles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.slt.cardealership.domain.model.Article
import com.slt.cardealership.presentation.home.HomeRoutes // <-- IMPORT THE ROUTES
import com.slt.cardealership.ui.theme.CarDealershipTheme
import kotlin.String
import kotlin.collections.List

@Composable
fun ArticleScreen(
    navController: NavController,
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
            .padding(14.dp)
    ) {
        // Header Section
        Text(
            "Articles",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Manage your article content from here",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = { /* TODO: Implement filter logic */ }) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filter",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filters")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.navigate(HomeRoutes.AddEditArticle()) }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add New Article",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Article")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is ArticleUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ArticleUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ArticleUiState.Success -> {
                    if (state.articles.isEmpty()) {
                        Text(
                            text = "No articles found. Add a new one to get started!",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        ArticleList(
                            articles = state.articles,
                            onEditClick = { article ->
                                navController.navigate(HomeRoutes.AddEditArticle(articleId = article.id))
                            },
                            onDeleteClick = { viewModel.deleteArticle(it.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleList(
    articles: List<Article>,
    onEditClick: (Article) -> Unit,
    onDeleteClick: (Article) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        LazyColumn {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(text = "Sr No.", weight = 0.15f, fontWeight = FontWeight.Bold)
                    TableCell(text = "Title", weight = 0.4f, fontWeight = FontWeight.Bold)
                    TableCell(text = "Domain Name", weight = 0.25f, fontWeight = FontWeight.Bold)
                    TableCell(text = "Status", weight = 0.2f, fontWeight = FontWeight.Bold)
                    TableCell(text = "Actions", weight = 0.3f, fontWeight = FontWeight.Bold)
                }
                Divider()
            }

            // Table Rows
            itemsIndexed(articles) { index, article ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(text = (index + 1).toString(), weight = 0.15f)
                    TableCell(text = article.title, weight = 0.4f)
                    TableCell(text = article.domainName, weight = 0.25f)
                    TableCell(text = article.status, weight = 0.2f)
                    Row(
                        modifier = Modifier.weight(0.2f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        IconButton(
                            onClick = { onEditClick(article) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                        }
                        IconButton(
                            onClick = { onDeleteClick(article) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Gray
                            )
                        }
                    }
                }
                if (index < articles.size - 1) {
                    Divider()
                }
            }
        }
    }
}

@Composable
fun ArticleFilterDialog(
    currentNameFilter: String,
    currentStatusFilter: String,
    onDismiss: () -> Unit,
    onResetFilters: () -> Unit,
    onApplyFilters: (name: String, status: String) -> Unit
) {
    // Internal state for the dialog's fields
    var articleName by remember { mutableStateOf(currentNameFilter) }
    var status by remember { mutableStateOf(currentStatusFilter) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Page filters", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Form Fields
                Text("Article Name", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = articleName,
                    onValueChange = { articleName = it },
                    placeholder = { Text("Article name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Status", style = MaterialTheme.typography.bodyMedium)
                FormDropdown(
                    selectedValue = status,
                    options = listOf("All", "Draft", "Published"),
                    onValueChange = { status = it },
                    label = ""
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onResetFilters,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Text("Reset Filters")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onApplyFilters(articleName, status) }) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(end = 8.dp),
        fontWeight = fontWeight,
        fontSize = 14.sp,
        color = color
    )
}

@Preview(showBackground = true)
@Composable
fun ArticleScreenPreview() {
    CarDealershipTheme {
        ArticleScreen(navController = rememberNavController())
    }
}



