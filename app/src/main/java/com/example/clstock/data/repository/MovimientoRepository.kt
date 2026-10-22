package com.example.clstock.data.repository

import com.example.clstock.domain.model.Movimiento
import com.example.clstock.domain.model.DetalleMovimiento
import com.example.clstock.domain.model.Inventario
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class MovimientoRepository {

    private val db = FirebaseFirestore.getInstance()

    fun registrarMovimiento(
        idProducto: String,
        tipo: String,
        cantidad: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        if (idProducto.isBlank()) {
            onError("Selecciona un producto")
            return
        }

        if (tipo != "Entrada" && tipo != "Salida") {
            onError("Tipo de movimiento no válido")
            return
        }

        if (cantidad <= 0) {
            onError("La cantidad debe ser mayor a cero")
            return
        }

        val idMovimiento = UUID.randomUUID().toString()
        val idUbicacion = "GENERAL"

        val referenciaInventario = db.collection("inventario")
            .document(idProducto)

        val referenciaMovimiento = db.collection("movimientos")
            .document(idMovimiento)

        val referenciaDetalle = db.collection("detalleMovimientos")
            .document(idMovimiento)

        db.runTransaction { transaccion ->

            val documentoInventario =
                transaccion.get(referenciaInventario)

            val cantidadActual = if (documentoInventario.exists()) {
                documentoInventario.getLong("cantidad")?.toInt() ?: 0
            } else {
                0
            }

            val nuevaCantidad = when (tipo) {
                "Entrada" -> cantidadActual + cantidad
                "Salida" -> {
                    if (cantidad > cantidadActual) {
                        throw IllegalStateException(
                            "No hay suficientes unidades. Stock actual: $cantidadActual"
                        )
                    }
                    cantidadActual - cantidad
                }
                else -> throw IllegalStateException(
                    "Tipo de movimiento no válido"
                )
            }

            val inventario = Inventario(
                idInventario = idProducto,
                idProducto = idProducto,
                idUbicacion = idUbicacion,
                cantidad = nuevaCantidad
            )

            val movimiento = Movimiento(
                idMovimiento = idMovimiento,
                idUsuario = "",
                tipo = tipo,
                fecha = System.currentTimeMillis(),
                observacion = ""
            )

            val detalle = DetalleMovimiento(
                idDetalle = idMovimiento,
                idMovimiento = idMovimiento,
                idProducto = idProducto,
                idUbicacion = idUbicacion,
                cantidad = cantidad
            )

            transaccion.set(referenciaInventario, inventario)
            transaccion.set(referenciaMovimiento, movimiento)
            transaccion.set(referenciaDetalle, detalle)

            nuevaCantidad
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(
                    error.message ?: "Error al registrar movimiento"
                )
            }
    }
}
