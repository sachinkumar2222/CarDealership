package com.slt.cardealership.presentation.articles

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.slt.cardealership.R
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.presentation.home.HomeRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    navController: NavController,
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentBackStackEntry = navController.currentBackStackEntry

    val refreshKey by currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("should_refresh")
        ?.observeAsState(initial = false) ?: remember { mutableStateOf(false) }

    // --- THIS IS THE FIX ---
    // The LaunchedEffect now ONLY re-runs when 'refreshKey' changes its value.
    LaunchedEffect(refreshKey) {
        if (refreshKey == true) {
            viewModel.fetchArticles()
            // Reset the key so it doesn't trigger again
            currentBackStackEntry?.savedStateHandle?.set("should_refresh", false)
        }
    }


    val blueGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2196F3), // Light Blue
            Color(0xFF1565C0)  // Dark Blue
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articles", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F8FF))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(HomeRoutes.AddEditArticle()) },
                containerColor = Color(0xFF2196F3)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Article", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F8FF)),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ArticleUiState.Loading -> LoadingAnimation()
                is ArticleUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is ArticleUiState.Success -> {
                    if (state.articles.isEmpty()) {
                        EmptyArticleState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.articles) { article ->
                                ArticleItemCard(
                                    post = article,
                                    onDeleteClick = {
                                        article.id?.let { viewModel.deleteArticle(it) }
                                    },
                                    onEditClick = {
                                        navController.navigate(HomeRoutes.AddEditArticle(articleId = article.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ArticleItemCard(
    post: Post,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onEditClick), // Make the whole card clickable for editing
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
          // modifier = Modifier.height(IntrinsicSize.Min), // Ensures row children can fill height
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            AsyncImage(
                model = post.image,
                contentDescription = post.name,
                modifier = Modifier
                    .fillMaxHeight() // Fill the height of the row
                    .width(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale = ContentScale.Crop
            )

            // Content Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = post.name ?: "No Title",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.content ?: "No content",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // More Options Menu
            Box(modifier = Modifier.align(Alignment.Top)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = {
                            onEditClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                        onClick = {
                            onDeleteClick()
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyArticleState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Article,
            contentDescription = "No Articles",
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No articles yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Click the '+' button to add your first article.",
            color = Color.Gray
        )
    }
}

@Composable
fun LoadingAnimation() {
    // 1. Load the Lottie animation composition from your assets folder
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lott.json")) // <-- Replace with your JSON file name

    // 2. Display the Lottie animation
    Box(
        modifier = Modifier.fillMaxSize(), // Center the animation if desired
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever, // Loop the animation indefinitely
            modifier = Modifier.size(200.dp) // Adjust size as needed
        )
    }
}