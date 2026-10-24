package com.example.inventariomovilti

data class ErroresProducto(
    val nombre: String = "",
    val stock: String = "",
    val minimo: String = ""
) {
    val esValido: Boolean
        get() = nombre.isEmpty() &&
                stock.isEmpty() &&
                minimo.isEmpty()
}

object ValidadorProducto {

    fun validar(
        nombre: String,
        stock: String,
        minimo: String,
        nombresExistentes: List<String>
    ): ErroresProducto {

        var errorNombre = ""
        var errorStock = ""
        var errorMinimo = ""

        if (nombre.isBlank()) {
            errorNombre = "Ingresa el nombre"
        } else if (
            nombresExistentes.any {
                it.equals(nombre.trim(), ignoreCase = true)
            }
        ) {
            errorNombre = "Este producto ya existe"
        }

        val cantidad = stock.toIntOrNull()

        if (cantidad == null) {
            errorStock = "Ingresa un numero valido"
        } else if (cantidad < 0) {
            errorStock = "El stock no puede ser negativo"
        }

        val cantidadMinima = minimo.toIntOrNull()

        if (cantidadMinima == null) {
            errorMinimo = "Ingresa un numero valido"
        } else if (cantidadMinima < 0) {
            errorMinimo = "El minimo no puede ser negativo"
        }

        return ErroresProducto(
            nombre = errorNombre,
            stock = errorStock,
            minimo = errorMinimo
        )
    }
}