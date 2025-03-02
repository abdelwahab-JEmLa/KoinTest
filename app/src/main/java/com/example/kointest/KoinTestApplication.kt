package com.example.kointest

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

// ============== DATA MODELS ==============

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val description: String = ""
)

data class User(val id: String, val name: String)

// Navigation helper
class Navigator(val navigate: (String) -> Unit)

// ============== REPOSITORIES ==============

// Repository interfaces
interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getProductById(id: String): Product?
}

interface UserRepository {
    fun getCurrentUser(): User?
}

// Repository implementations
class ProductRepositoryImpl : ProductRepository {
    override suspend fun getProducts(): List<Product> = listOf(
        Product("1", "Smartphone Galaxy S23", 899.99, "Un smartphone haut de gamme avec une caméra exceptionnelle"),
        Product("2", "Laptop UltraBook Pro", 1299.99, "Ordinateur portable fin et léger avec une excellente autonomie"),
        Product("3", "Écouteurs sans fil NoiseCancel", 199.99, "Écouteurs avec réduction de bruit active et son immersif"),
        Product("4", "Montre connectée FitTech", 249.99, "Montre connectée avec suivi d'activité et notifications"),
        Product("5", "Tablette MediaTab 10", 349.99, "Tablette 10 pouces avec écran haute définition pour le multimédia")
    )

    override suspend fun getProductById(id: String): Product? =
        getProducts().find { it.id == id }
}

class UserRepositoryImpl(private val productRepository: ProductRepository) : UserRepository {
    override fun getCurrentUser(): User? = User("1", "John Doe")
}

// ============== KOIN MODULES ==============

// Module pour les repositories
val repositoryModule = module {
    // Singleton: une seule instance pour toute l'application
    single<ProductRepository> { ProductRepositoryImpl() }

    // Factory: nouvelle instance à chaque fois
    factory<UserRepository> { UserRepositoryImpl(get()) }
}

// Module pour les ViewModels
val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { parameters -> DetailViewModel(productId = parameters.get(), repository = get()) }
}

val coordinatorModule = module {
    factory { (navigator: Navigator) -> MainCoordinator(get(), navigator) }
    factory { (productId: String, navigator: Navigator) -> DetailCoordinator(productId, navigator) }
}

// Module principal qui regroupe tous les autres modules
val appModule = module {
    // Inclure d'autres modules dans l'ordre correct
    includes(repositoryModule, viewModelModule, coordinatorModule)
}

// ============== APPLICATION ==============

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}
