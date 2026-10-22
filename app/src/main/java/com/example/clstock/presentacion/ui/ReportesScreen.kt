
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

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.clstock.domain.model.Producto

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast



private data class RegistroReporte(
    val movimiento: Movimiento,
    val detalle: DetalleMovimiento,
    val nombreProducto: String,
    val codigoBarra: String
)


@Composable
fun ReportesScreen(
    onVolver: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }

    val context = LocalContext.current

    var filasParaPdf by remember {
        mutableStateOf<List<FilaPdfMovimiento>>(emptyList())
    }

    var periodoParaPdf by remember {
        mutableStateOf("")
    }

    var inicioParaPdf by remember {
        mutableLongStateOf(0L)
    }

    var finParaPdf by remember {
        mutableLongStateOf(0L)
    }

    val guardarPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/pdf"
        )
    ) { uri ->

        if (uri != null) {
            try {
                PdfReporteHelper.generarPdf(
                    context = context,
                    uri = uri,
                    periodo = periodoParaPdf,
                    fechaDesde = inicioParaPdf,
                    fechaHasta = finParaPdf,
                    movimientos = filasParaPdf
                )

                Toast.makeText(
                    context,
                    "PDF generado correctamente",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al generar PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    val azulOscuro = Color(0xFF142D4E)
    val azulPrincipal = Color(0xFF2563EB)

    val periodos = listOf(
        "Hoy",
        "Últimos 7 días",
        "Últimos 14 días",
        "Este mes",
        "Personalizado"
    )

    var periodoSeleccionado by remember {
        mutableStateOf("Hoy")
    }

    var menuAbierto by remember {
        mutableStateOf(false)
    }

    var fechaInicio by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    var fechaFin by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    var mostrarFechaInicio by remember {
        mutableStateOf(false)
    }

    var mostrarFechaFin by remember {
        mutableStateOf(false)
    }

    var registros by remember {
        mutableStateOf<List<RegistroReporte>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    fun cargarReportes() {
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

                                val productosPorId = productos.associateBy {
                                    it.idProducto
                                }

                                registros = movimientos.flatMap { movimiento ->
                                    detalles.filter {
                                        it.idMovimiento == movimiento.idMovimiento
                                    }.map { detalle ->

                                        val producto = productosPorId[detalle.idProducto]

                                        RegistroReporte(
                                            movimiento = movimiento,
                                            detalle = detalle,
                                            nombreProducto = producto?.nombre
                                                ?: "Producto no encontrado",
                                            codigoBarra = producto?.codigoBarra
                                                ?: "No disponible"
                                        )
                                    }
                                }

                                cargando = false
                            }
                            .addOnFailureListener { e ->
                                error = e.message
                                    ?: "Error al consultar los productos"
                                cargando = false
                            }

                    }
                    .addOnFailureListener { e ->
                        error = e.message ?: "Error al cargar detalles"
                        cargando = false
                    }
            }
            .addOnFailureListener { e ->
                error = e.message ?: "Error al cargar movimientos"
                cargando = false
            }
    }

    LaunchedEffect(Unit) {
        cargarReportes()
    }

    fun inicioDelDia(fecha: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = fecha
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun finDelDia(fecha: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = fecha
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    val ahora = System.currentTimeMillis()

    val inicioPeriodo = when (periodoSeleccionado) {
        "Hoy" -> inicioDelDia(ahora)

        "Últimos 7 días" -> Calendar.getInstance().apply {
            timeInMillis = inicioDelDia(ahora)
            add(Calendar.DAY_OF_YEAR, -6)
        }.timeInMillis

        "Últimos 14 días" -> Calendar.getInstance().apply {
            timeInMillis = inicioDelDia(ahora)
            add(Calendar.DAY_OF_YEAR, -13)
        }.timeInMillis

        "Este mes" -> Calendar.getInstance().apply {
            timeInMillis = inicioDelDia(ahora)
            set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        else -> inicioDelDia(fechaInicio)
    }

    val finPeriodo = if (periodoSeleccionado == "Personalizado") {
        finDelDia(fechaFin)
    } else {
        ahora
    }

    val rangoValido = inicioPeriodo <= finPeriodo

    val registrosFiltrados = if (rangoValido) {
        registros.filter {
            it.movimiento.fecha in inicioPeriodo..finPeriodo
        }.sortedByDescending {
            it.movimiento.fecha
        }
    } else {
        emptyList()
    }

    val totalEntradas = registrosFiltrados
        .filter { it.movimiento.tipo == "Entrada" }
        .sumOf { it.detalle.cantidad }

    val totalSalidas = registrosFiltrados
        .filter { it.movimiento.tipo == "Salida" }
        .sumOf { it.detalle.cantidad }

    val cantidadMovimientos = registrosFiltrados
        .map { it.movimiento.idMovimiento }
        .distinct()
        .size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6FB))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(onClick = onVolver) {
            Text("← Volver al inicio")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Reportes",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Resumen de entradas y salidas por fecha",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Seleccionar período",
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { menuAbierto = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("$periodoSeleccionado ▼")
            }

            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = { menuAbierto = false }
            ) {
                periodos.forEach { periodo ->
                    DropdownMenuItem(
                        text = { Text(periodo) },
                        onClick = {
                            periodoSeleccionado = periodo
                            menuAbierto = false
                        }
                    )
                }
            }
        }

        if (periodoSeleccionado == "Personalizado") {

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { mostrarFechaInicio = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Desde: ${formatoFecha.format(Date(fechaInicio))}"
                )
            }

            OutlinedButton(
                onClick = { mostrarFechaFin = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Hasta: ${formatoFecha.format(Date(fechaFin))}"
                )
            }

            if (!rangoValido) {
                Text(
                    text = "La fecha inicial no puede ser posterior a la final",
                    color = Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { cargarReportes() },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (cargando) "Actualizando..."
                else "Actualizar reportes"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (cargando) {
            CircularProgressIndicator()
            Text("Consultando Firebase...")
        }

        if (error.isNotEmpty()) {
            Text(
                text = "Error: $error",
                color = Color.Red
            )
        }

        if (!cargando && error.isEmpty() && rangoValido) {

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = azulOscuro
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {

                    Text(
                        text = "Movimientos registrados",
                        color = Color.White
                    )

                    Text(
                        text = cantidadMovimientos.toString(),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF15803D)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Entradas", color = Color.White)
                        Text(
                            "$totalEntradas",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("unidades", color = Color.White)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDC2626)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Salidas", color = Color.White)
                        Text(
                            "$totalSalidas",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("unidades", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Balance del período: ${
                    totalEntradas - totalSalidas
                } unidades",
                fontWeight = FontWeight.Bold,
                color = azulPrincipal
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {

                    filasParaPdf = registrosFiltrados.map { registro ->
                        FilaPdfMovimiento(
                            fecha = registro.movimiento.fecha,
                            nombreProducto = registro.nombreProducto,
                            codigoBarra = registro.codigoBarra,
                            tipo = registro.movimiento.tipo,
                            cantidad = registro.detalle.cantidad
                        )
                    }

                    periodoParaPdf = periodoSeleccionado
                    inicioParaPdf = inicioPeriodo
                    finParaPdf = finPeriodo

                    val nombreArchivo =
                        "CLStock_Reporte_${
                            SimpleDateFormat(
                                "dd-MM-yyyy_HH-mm",
                                Locale.getDefault()
                            ).format(Date())
                        }.pdf"

                    guardarPdf.launch(nombreArchivo)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = azulPrincipal
                )
            ) {
                Text("Generar PDF")
            }


            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Detalle de movimientos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = azulOscuro
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (registrosFiltrados.isEmpty()) {
                Text(
                    "No hay movimientos registrados en este período."
                )
            }

            registrosFiltrados.forEach { registro ->

                val esEntrada =
                    registro.movimiento.tipo == "Entrada"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Column(modifier = Modifier.padding(14.dp)) {

                        Text(
                            text = registro.nombreProducto,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = azulOscuro
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Código: ${registro.codigoBarra}"
                        )

                        Text(
                            text = registro.movimiento.tipo,
                            fontWeight = FontWeight.Bold,
                            color = if (esEntrada) {
                                Color(0xFF15803D)
                            } else {
                                Color(0xFFDC2626)
                            }
                        )

                        Text(
                            text = "Cantidad: ${
                                if (esEntrada) "+" else "-"
                            }${registro.detalle.cantidad} unidades"
                        )

                        val formatoFechaHora = remember {
                            SimpleDateFormat(
                                "dd/MM/yyyy HH:mm",
                                Locale.getDefault()
                            )
                        }

                        Text(
                            text = "Fecha: ${
                                formatoFechaHora.format(
                                    Date(registro.movimiento.fecha)
                                )
                            }"
                        )
                    }

                }
            }
        }
    }

    if (mostrarFechaInicio) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = fechaInicio
        )

        DatePickerDialog(
            onDismissRequest = { mostrarFechaInicio = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estadoFecha.selectedDateMillis?.let {
                            fechaInicio = it
                        }
                        mostrarFechaInicio = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarFechaInicio = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = estadoFecha)
        }
    }

    if (mostrarFechaFin) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = fechaFin
        )

        DatePickerDialog(
            onDismissRequest = { mostrarFechaFin = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estadoFecha.selectedDateMillis?.let {
                            fechaFin = it
                        }
                        mostrarFechaFin = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarFechaFin = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = estadoFecha)
        }
    }
}


