
package com.example.clstock.domain.model

data class Producto(
    val idProducto: String = "",
    val idCategoria: String = "",
    val codigoBarra: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val unidadMedida: String = "",
    val estado: Int = 1
)
