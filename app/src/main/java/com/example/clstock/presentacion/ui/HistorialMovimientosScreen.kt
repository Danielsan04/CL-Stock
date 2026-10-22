
package com.example.clstock.presentacion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.example.clstock.domain.model.Movimiento
import com.example.clstock.domain.model.DetalleMovimiento
import com.example.clstock.domain.model.Producto

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class MovimientoHistorial(
    val movimiento: Movimiento,
    val detalle: DetalleMovimiento,
    val nombreProducto: String
)

@Composable
fun HistorialMovimientosScreen(
    onVolver: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }

    var historial by remember {
        mutableStateOf<List<MovimientoHistorial>>(emptyList())
    }

    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    fun cargarHistorial() {
        cargando = true
        error = ""

        db.collection("movimientos")
            .get(Source.SERVER)
            .addOnSuccessListener { resultadoMovimientos ->

                val movimientos = resultadoMovimientos.documents
                    .mapNotNull {
                        it.toObject(Movimiento::class.java)
                    }

                db.collection("detalleMovimientos")
                    .get(Source.SERVER)
                    .addOnSuccessListener { resultadoDetalles ->

                        val detalles = resultadoDetalles.documents
                            .mapNotNull {
                                it.toObject(DetalleMovimiento::class.java)
                            }

                        db.collection("productos")
                            .get(Source.SERVER)
                            .addOnSuccessListener { resultadoProductos ->

                                val productos = resultadoProductos.documents
                                    .mapNotNull {
                                        it.toObject(Producto::class.java)
                                    }

                                val nombres = productos.associate {
                                    it.idProducto to it.nombre
                                }

                                historial = movimientos.mapNotNull { movimiento ->
                                    val detalle = detalles.firstOrNull {
                                        it.idMovimiento == movimiento.idMovimiento
                                    }

                                    if (detalle == null) {
                                        null
                                    } else {
                                        MovimientoHistorial(
                                            movimiento = movimiento,
                                            detalle = detalle,
                                            nombreProducto = nombres[
                                                detalle.idProducto
                                            ] ?: "Producto no encontrado"
                                        )
                                    }
                                }.sortedByDescending {
                                    it.movimiento.fecha
                                }

                                cargando = false
                            }
                            .addOnFailureListener { e ->
                                error = e.message ?: "Error al consultar productos"
                                cargando = false
                            }
                    }
                    .addOnFailureListener { e ->
                        error = e.message ?: "Error al consultar detalles"
                        cargando = false
                    }
            }
            .addOnFailureListener { e ->
                error = e.message ?: "Error al consultar movimientos"
                cargando = false
            }
    }

    LaunchedEffect(Unit) {
        cargarHistorial()
    }

    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6FB))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onVolver) {
            Text("← Volver")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Historial de movimientos",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF142D4E)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Consulta las entradas y salidas registradas",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { cargarHistorial() },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizar historial")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            CircularProgressIndicator()
            Text("Cargando movimientos...")
        }

        if (error.isNotEmpty()) {
            Text("Error: $error", color = Color.Red)
        }

        if (!cargando && error.isEmpty() && historial.isEmpty()) {
            Text("Todavía no hay movimientos registrados.")
        }

        if (!cargando && error.isEmpty()) {
            historial.forEach { registro ->

                val esEntrada = registro.movimiento.tipo == "Entrada"
                val colorMovimiento = if (esEntrada) {
                    Color(0xFF15803D)
                } else {
                    Color(0xFFDC2626)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = registro.nombreProducto,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = registro.movimiento.tipo,
                            color = colorMovimiento,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Cantidad: ${
                                if (esEntrada) "+" else "-"
                            }${registro.detalle.cantidad} unidades",
                            color = colorMovimiento
                        )

                        Text(
                            text = "Fecha: ${
                                formatoFecha.format(
                                    Date(registro.movimiento.fecha)
                                )
                            }"
                        )
                    }
                }
            }
        }
    }
}
