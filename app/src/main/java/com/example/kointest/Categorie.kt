package com.example.kointest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// ========== MODÈLES DE DONNÉES ==========

/**
 * Modèle de données pour une catégorie de produits
 */
data class Categorie(
    val id: String,
    val nom: String,
    val imageUrl: String? = null,
    val parentId: String? = null
)

// ========== ÉTAT & ACTIONS ==========

/**
 * UI State that represents CategorieProduitsManagerScreen
 **/
data class CategorieProduitsManagerState(
    val categories: List<Categorie> = emptyList(),
    val isLoading: Boolean = false,
    val selectedParentId: String? = null,
    val error: String? = null
)

/**
 * CategorieProduitsManager Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/
data class CategorieProduitsManagerActions(
    val onCategorieClick: (Categorie) -> Unit = {},
    val onRetryClick: () -> Unit = {},
    val onBackClick: () -> Unit = {}
)

// ========== VIEW MODEL ==========

/**
 * ViewModel pour gérer l'état et la logique des catégories de produits
 */
class CategorieProduitsManagerViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<CategorieProduitsManagerState> =
        MutableStateFlow(CategorieProduitsManagerState(isLoading = true))

    val stateFlow: StateFlow<CategorieProduitsManagerState> = _stateFlow.asStateFlow()
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                // Simuler le chargement des données
                val categories = listOf(
                    Categorie(id = "1", nom = "Fruits et Légumes", imageUrl = "https://example.com/fruits.jpg"),
                    Categorie(id = "2", nom = "Viandes", imageUrl = "https://example.com/viandes.jpg"),
                    Categorie(id = "3", nom = "Produits Laitiers", imageUrl = "https://example.com/laitiers.jpg"),
                    Categorie(id = "4", nom = "Boissons", imageUrl = "https://example.com/boissons.jpg"),
                    Categorie(id = "5", nom = "Épicerie", imageUrl = "https://example.com/epicerie.jpg"),
                    Categorie(id = "6", nom = "Surgelés", imageUrl = "https://example.com/surgeles.jpg")
                )
                
                _stateFlow.update { currentState ->
                    currentState.copy(
                        categories = categories,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _stateFlow.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Erreur lors du chargement des catégories: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateCatalogueParentId(categorie: Categorie) {
        // Mettre à jour le parentId du catalogue
        _stateFlow.update { currentState ->
            currentState.copy(
                selectedParentId = categorie.id
            )
        }
        
        // Log pour démonstration - à remplacer par votre logique métier
        println("Catalogue parent ID mis à jour avec: ${categorie.id}")
    }
    
    fun retry() {
        _stateFlow.update { it.copy(isLoading = true, error = null) }
        loadCategories()
    }
}

// ========== COORDINATOR ==========

/**
 * Screen's coordinator which is responsible for handling actions from the UI layer
 * and one-shot actions based on the new UI state
 */
class CategorieProduitsManagerCoordinator(
    val viewModel: CategorieProduitsManagerViewModel
) {
    val screenStateFlow = viewModel.stateFlow
    
    fun onCategorieClick(categorie: Categorie) {
        viewModel.updateCatalogueParentId(categorie)
    }
    
    fun onRetryClick() {
        viewModel.retry()
    }
    
    fun onBackClick() {}
}

@Composable
fun rememberCategorieProduitsManagerCoordinator(
    viewModel: CategorieProduitsManagerViewModel = koinViewModel()
): CategorieProduitsManagerCoordinator {
    return remember(viewModel) {
        CategorieProduitsManagerCoordinator(
            viewModel = viewModel
        )
    }
}

// ========== ROUTE ==========

@Composable
fun CategorieProduitsManagerRoute(
    coordinator: CategorieProduitsManagerCoordinator = rememberCategorieProduitsManagerCoordinator()
) {
    // State observing and declarations
    val uiState by coordinator.screenStateFlow.collectAsStateWithLifecycle(
        CategorieProduitsManagerState()
    )

    // UI Actions
    val actions = remember(coordinator) {
        CategorieProduitsManagerActions(
            onCategorieClick = coordinator::onCategorieClick,
            onRetryClick = coordinator::onRetryClick,
            onBackClick = coordinator::onBackClick
        )
    }

    // UI Rendering
    CategorieProduitsManagerScreen(uiState, actions)
}

// ========== SCREEN ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorieProduitsManagerScreen(
    state: CategorieProduitsManagerState,
    actions: CategorieProduitsManagerActions
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catégories de Produits") },
                navigationIcon = {
                    IconButton(onClick = actions.onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
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
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = actions.onRetryClick) {
                            Text("Réessayer")
                        }
                    }
                }
                state.categories.isEmpty() -> {
                    Text(
                        text = "Aucune catégorie disponible",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.categories) { categorie ->
                            CategorieItem(
                                categorie = categorie,
                                isSelected = categorie.id == state.selectedParentId,
                                onClick = { actions.onCategorieClick(categorie) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorieItem(
    categorie: Categorie,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image de la catégorie
            if (categorie.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(categorie.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = categorie.nom,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Image par défaut si aucune image n'est disponible
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Overlay et texte
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Text(
                    text = categorie.nom,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Indicateur de sélection
            if (isSelected) {
                Surface(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.star_on),
                        contentDescription = "Sélectionné",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}

// ========== MODULE KOIN ==========

/**
 * Module Koin pour l'injection de dépendances
 */
val categorieProduitsModule = module {
    viewModel { CategorieProduitsManagerViewModel(get()) }
}
