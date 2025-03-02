package com.example.kointest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

// ============== VIEWMODELS ==============

class MainViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val products = repository.getProducts()
                _state.update { it.copy(products = products, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun retry() {
        loadProducts()
    }
}

class DetailViewModel(
    private val productId: String,
    private val repository: ProductRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        loadProductDetails()
    }

    private fun loadProductDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val product = repository.getProductById(productId)
                _state.update { it.copy(product = product, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun retry() {
        loadProductDetails()
    }
}

// ============== UI STATES ==============

data class MainState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DetailState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// ============== COORDINATORS ==============

class MainCoordinator(
    private val viewModel: MainViewModel,
    private val navigator: Navigator
) {
    val stateFlow = viewModel.state

    fun onProductClick(productId: String) {
        navigator.navigate("detail/$productId")
    }

    fun onRetry() {
        viewModel.retry()
    }
}

class DetailCoordinator(
    private val productId: String,
    private val navigator: Navigator
) {
    // Get ViewModel with the productId parameter
    private val viewModel: DetailViewModel by lazy {
        org.koin.core.context.GlobalContext.get().get { parametersOf(productId) }
    }

    val stateFlow = viewModel.state

    fun onBackClick() {
        navigator.navigate("main")
    }

    fun onRetry() {
        viewModel.retry()
    }
}
