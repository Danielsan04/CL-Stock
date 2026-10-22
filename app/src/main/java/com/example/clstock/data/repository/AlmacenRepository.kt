
package com.example.clstock.data.repository

import com.example.clstock.domain.model.Almacen
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import java.util.UUID

class AlmacenRepository {

    private val db = FirebaseFirestore.getInstance()

    fun guardarAlmacen(
        nombre: String,
        descripcion: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val nombreLimpio = nombre.trim()
        val descripcionLimpia = descripcion.trim()

        if (nombreLimpio.isEmpty()) {
            onError("Ingresa el nombre del almacén")
            return
        }

        val id = UUID.randomUUID().toString()

        val almacen = Almacen(
            idAlmacen = id,
            nombre = nombreLimpio,
            descripcion = descripcionLimpia,
            estado = 1
        )

        db.collection("almacenes")
            .document(id)
            .set(almacen)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(
                    error.message ?: "Error al guardar almacén"
                )
            }
    }

    fun obtenerAlmacenes(
        onSuccess: (List<Almacen>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("almacenes")
            .get(Source.SERVER)
            .addOnSuccessListener { resultado ->

                val almacenes = resultado.documents
                    .mapNotNull {
                        it.toObject(Almacen::class.java)
                    }
                    .sortedBy {
                        it.nombre.lowercase()
                    }

                onSuccess(almacenes)
            }
            .addOnFailureListener { error ->
                onError(
                    error.message ?: "Error al consultar almacenes"
                )
            }
    }
}
