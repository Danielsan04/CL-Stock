
package com.example.clstock.domain.model

data class Ubicacion(
    val idUbicacion: String = "",
    val idAlmacen: String = "",
    val pasillo: String = "",
    val estante: String = "",
    val nivel: String = "",
    val estado: Int = 1
)
