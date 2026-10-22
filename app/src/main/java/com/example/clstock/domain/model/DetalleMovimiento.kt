
package com.example.clstock.domain.model

data class DetalleMovimiento(
    val idDetalle: String = "",
    val idMovimiento: String = "",
    val idProducto: String = "",
    val idUbicacion: String = "",
    val cantidad: Int = 0
)
