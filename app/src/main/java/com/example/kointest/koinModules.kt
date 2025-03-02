package com.example.kointest

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

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
