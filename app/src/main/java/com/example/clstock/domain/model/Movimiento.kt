
package com.example.clstock.domain.model

data class Movimiento(
    val idMovimiento: String = "",
    val idUsuario: String = "",
    val tipo: String = "",
    val fecha: Long = 0L,
    val observacion: String = ""
)
