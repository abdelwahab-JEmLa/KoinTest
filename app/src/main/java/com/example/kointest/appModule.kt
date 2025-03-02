package com.example.kointest

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Définir votre module d'injection de dépendances
val appModule = module {
    // Exemple: Factory pour un repository
    factory { YourRepository() }

    // Exemple: ViewModel
    viewModel { YourViewModel(get()) }

    // Injecter le ViewModel CategorieProduitsManagerViewModel dans le module
    viewModel { params -> CategorieProduitsManagerViewModel(get()) }

    // Définir la factory pour CategorieProduitsManagerCoordinator avec dépendance sur le ViewModel
    factory { CategorieProduitsManagerCoordinator(get()) }

    // Ajouter les modules spécifiques
    includes(categorieProduitsModule)
}

// Exemples de classes (à remplacer par vos vraies classes)
class YourRepository()
class YourViewModel(private val repository: YourRepository) : ViewModel()
// Pas besoin de redéclarer CategorieProduitsManagerCoordinator car il est déjà défini dans Categorie.kt
