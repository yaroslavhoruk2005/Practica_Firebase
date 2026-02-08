package com.example.practica_firebase.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica_firebase.ui.screens.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                productsCollection.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    val productsList = snapshot?.documents?.mapNotNull { doc ->
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            description = doc.getString("description") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: ""
                        )
                    } ?: emptyList()

                    _products.value = productsList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                val productData = hashMapOf(
                    "name" to product.name,
                    "price" to product.price,
                    "description" to product.description,
                    "imageUrl" to product.imageUrl
                )
                productsCollection.add(productData).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                productsCollection.document(productId).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProduct(productId: String, product: Product) {
        viewModelScope.launch {
            try {
                val productData = hashMapOf(
                    "name" to product.name,
                    "price" to product.price,
                    "description" to product.description,
                    "imageUrl" to product.imageUrl
                )
                productsCollection.document(productId).set(productData).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}