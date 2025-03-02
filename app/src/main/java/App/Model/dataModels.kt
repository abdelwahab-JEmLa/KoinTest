package App.Model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val description: String = ""
)

data class User(val id: String, val name: String)

// Navigation helper
class Navigator(val navigate: (String) -> Unit)
