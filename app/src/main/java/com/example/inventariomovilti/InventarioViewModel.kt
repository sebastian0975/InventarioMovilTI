package com.example.inventariomovilti

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class InventarioViewModel : ViewModel() {

    var productos by mutableStateOf<List<Producto>>(emptyList())
        private set

    fun cargarProductos(lista: List<Producto>) {
        productos = lista
    }

    fun registrarProducto(producto: Producto) {
        productos = productos + producto
    }

    fun aumentarStock(nombre: String) {
        productos = productos.map {
            if (it.nombre == nombre) {
                it.copy(stock = it.stock + 1)
            } else {
                it
            }
        }
    }

    fun disminuirStock(nombre: String) {
        productos = productos.map {
            if (it.nombre == nombre && it.stock > 0) {
                it.copy(stock = it.stock - 1)
            } else {
                it
            }
        }
    }
}