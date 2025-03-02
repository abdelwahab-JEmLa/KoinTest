package com.example.kointest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.parameter.parametersOf
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// ============== MAIN ACTIVITY ==============

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                KoinAndroidContext {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

// ============== NAVIGATION ==============

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Créer un navigator qui peut être injecté
    val navigator = remember {
        Navigator { route ->
            navController.navigate(route) {
                // Pop back stack jusqu'à la route principale si on navigue vers "main"
                if (route == "main") {
                    popUpTo("main") { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            // Injecter le coordinator avec le navigator comme paramètre
            val coordinator = org.koin.androidx.compose.get<MainCoordinator> { parametersOf(navigator) }
            MainRoute(coordinator)
        }

        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            // Injecter le coordinator avec le productId et le navigator comme paramètres
            val coordinator = org.koin.androidx.compose.get<DetailCoordinator> {
                parametersOf(productId, navigator)
            }
            DetailRoute(coordinator)
        }
    }
}

// ============== ROUTES ==============

// Route principale
@Composable
fun MainRoute(coordinator: MainCoordinator) {
    val state by coordinator.stateFlow.collectAsStateWithLifecycle()

    MainScreen(
        state = state,
        onProductClick = coordinator::onProductClick,
        onRetry = coordinator::onRetry
    )
}

// Route détail
@Composable
fun DetailRoute(coordinator: DetailCoordinator) {
    val state by coordinator.stateFlow.collectAsStateWithLifecycle()

    DetailScreen(
        state = state,
        onBackClick = coordinator::onBackClick,
        onRetry = coordinator::onRetry
    )
}

// ============== SCREENS ==============

// Écran principal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainState,
    onProductClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalogue de Produits") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorContent(
                        message = state.error ?: "Une erreur s'est produite",
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.products.isEmpty() -> {
                    Text(
                        text = "Aucun produit disponible",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.products) { product ->
                            ProductItem(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Écran détail
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.product?.name ?: "Détail Produit") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorContent(
                        message = state.error ?: "Une erreur s'est produite",
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.product == null -> {
                    Text(
                        text = "Produit non trouvé",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    ProductDetail(product = state.product!!)
                }
            }
        }
    }
}

// ============== UI COMPONENTS ==============

@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance("EUR")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Product info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Price
            Text(
                text = currencyFormat.format(product.price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ProductDetail(product: Product) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance("EUR")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Placeholder pour l'image (dans une vraie app, utilisez AsyncImage)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nom du produit
        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Prix
        Text(
            text = currencyFormat.format(product.price),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.description.ifEmpty { "Aucune description disponible." },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton d'action
        Button(
            onClick = { /* Ajouter au panier */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Ajouter au panier")
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Erreur",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Réessayer")
        }
    }
}
