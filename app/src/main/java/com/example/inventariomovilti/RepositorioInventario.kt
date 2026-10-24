package com.example.inventariomovilti

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object RepositorioInventario {

    private const val ARCHIVO = "inventario"
    private const val CLAVE = "productos"

    fun guardar(context: Context, productos: List<Producto>) {
        val preferencias = context.getSharedPreferences(
            ARCHIVO,
            Context.MODE_PRIVATE
        )

        val lista = JSONArray()

        productos.forEach { producto ->
            val objeto = JSONObject()

            objeto.put("nombre", producto.nombre)
            objeto.put("stock", producto.stock)
            objeto.put("minimo", producto.minimo)

            lista.put(objeto)
        }

        preferencias.edit()
            .putString(CLAVE, lista.toString())
            .apply()
    }

    fun cargar(context: Context): List<Producto> {
        val preferencias = context.getSharedPreferences(
            ARCHIVO,
            Context.MODE_PRIVATE
        )

        val datos = preferencias.getString(CLAVE, null)
            ?: return listOf(
                Producto("Notebook HP", 10, 3),
                Producto("Mouse Logitech", 25, 5),
                Producto("Teclado mecanico", 5, 5),
                Producto("Monitor Samsung", 3, 4)
            )

        return try {
            val lista = JSONArray(datos)
            val productos = mutableListOf<Producto>()

            for (i in 0 until lista.length()) {
                val objeto = lista.getJSONObject(i)

                productos.add(
                    Producto(
                        nombre = objeto.getString("nombre"),
                        stock = objeto.getInt("stock"),
                        minimo = objeto.getInt("minimo")
                    )
                )
            }

            productos
        } catch (e: Exception) {
            emptyList()
        }
    }
}