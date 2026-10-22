package com.example.clstock.data.repository

import com.example.clstock.domain.model.Producto
import com.google.firebase.firestore.FirebaseFirestore

class ProductoRepository {

    private val db = FirebaseFirestore.getInstance()


    fun guardarProducto(
        producto: Producto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val codigo = producto.codigoBarra.trim()

        if (codigo.isEmpty()) {
            onError("El código de barras no puede estar vacío")
            return
        }

        db.collection("productos")
            .whereEqualTo("codigoBarra", codigo)
            .limit(1)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener { resultado ->

                if (!resultado.isEmpty) {
                    onError(
                        "Este código de barras ya está registrado en CL Stock"
                    )
                } else {

                    val productoNuevo = producto.copy(
                        codigoBarra = codigo
                    )

                    db.collection("productos")
                        .document(productoNuevo.idProducto)
                        .set(productoNuevo)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { error ->
                            onError(
                                error.message ?: "Error al guardar producto"
                            )
                        }
                }
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo verificar si el producto existe: " +
                            (error.message ?: "Error de conexión")
                )
            }
    }

    fun obtenerProductos(
        onSuccess: (List<Producto>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("productos")
            .get()
            .addOnSuccessListener { resultado ->

                val productos = resultado.documents.mapNotNull { documento ->
                    documento.toObject(Producto::class.java)
                }

                onSuccess(productos)
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Error al obtener productos")
            }
    }

    fun buscarProductoPorCodigo(
        codigoBarra: String,
        onSuccess: (Producto?) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("productos")
            .whereEqualTo("codigoBarra", codigoBarra)
            .limit(1)
            .get()
            .addOnSuccessListener { resultado ->

                val producto = resultado.documents
                    .firstOrNull()
                    ?.toObject(Producto::class.java)

                onSuccess(producto)
            }
            .addOnFailureListener { error ->
                onError(
                    error.message ?: "Error al buscar producto"
                )
            }
    }


}
