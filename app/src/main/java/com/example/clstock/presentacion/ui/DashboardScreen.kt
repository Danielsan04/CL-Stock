package com.example.clstock.presentacion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode


@Composable
fun DashboardScreen(
    nombreUsuario: String,
    onCerrarSesion: () -> Unit
) {
    var pantallaActual by remember { mutableStateOf("inicio") }
    var mostrarAviso by remember { mutableStateOf(false) }

    val azulOscuro = Color(0xFF142D4E)
    val azulPrincipal = Color(0xFF2563EB)


    val context = LocalContext.current

    var codigoEscaneado by remember {
        mutableStateOf("")
    }

    var errorEscaneo by remember {
        mutableStateOf("")
    }

    val opcionesEscaner = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_QR_CODE
            )
            .enableAutoZoom()
            .build()
    }

    val escaner = remember(context) {
        GmsBarcodeScanning.getClient(context, opcionesEscaner)
    }

    fun iniciarEscaneo() {
        errorEscaneo = ""

        escaner.startScan()
            .addOnSuccessListener { codigo ->
                val valor = codigo.rawValue

                if (!valor.isNullOrBlank()) {
                    codigoEscaneado = valor
                    pantallaActual = "productos"
                } else {
                    errorEscaneo = "No se pudo leer el código."
                }
            }
            .addOnFailureListener { error ->
                errorEscaneo =
                    error.localizedMessage ?: "No se pudo abrir el escáner."
            }
    }



    if (pantallaActual == "productos") {
        ProductosScreen(
            onVolver = { pantallaActual = "inicio" },
            onEscanear = { iniciarEscaneo() },
            codigoEscaneado = codigoEscaneado
        )

} else if (pantallaActual == "inventario") {
        InventarioScreen(
            onVolver = { pantallaActual = "inicio" }
        )

    } else if (pantallaActual == "movimientos") {
        MovimientosScreen(
            onVolver = { pantallaActual = "inicio" }
        )


    } else if (pantallaActual == "historial") {
        HistorialMovimientosScreen(
            onVolver = { pantallaActual = "inicio" }
        )

    } else if (pantallaActual == "reportes") {
        ReportesScreen(
            onVolver = { pantallaActual = "inicio" }
        )

    } else if (pantallaActual == "almacenes") {
        AlmacenesScreen(
            onVolver = { pantallaActual = "inicio" }
        )

    } else if (pantallaActual == "registrar_usuario") {
        RegistrarUsuarioScreen(
            onVolver = { pantallaActual = "inicio" }
        )

    } else {



        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F6FB))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "CL Stock",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = azulOscuro
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bienvenido, $nombreUsuario",
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = azulOscuro
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Panel de inventario",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Administra tus productos, almacenes y movimientos.",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { iniciarEscaneo() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = azulPrincipal
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Escanear producto",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Menú principal",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = azulOscuro
            )

            Spacer(modifier = Modifier.height(16.dp))

            OpcionDashboard(
                titulo = "Productos",
                descripcion = "Registrar y consultar productos",
                onClick = { pantallaActual = "productos" }
            )

            OpcionDashboard(
                titulo = "Inventario",
                descripcion = "Consultar existencias disponibles",
                onClick = { pantallaActual = "inventario" }
            )


            OpcionDashboard(
                titulo = "Movimientos",
                descripcion = "Registrar entradas y salidas",
                onClick = { pantallaActual = "movimientos" }
            )

            OpcionDashboard(
                titulo = "Historial de movimientos",
                descripcion = "Consultar entradas y salidas registradas",
                onClick = { pantallaActual = "historial" }
            )

            OpcionDashboard(
                titulo = "Reportes",
                descripcion = "Consultar movimientos por fechas",
                onClick = { pantallaActual = "reportes" }
            )

            OpcionDashboard(
                titulo = "Almacenes",
                descripcion = "Registrar y consultar almacenes",
                onClick = { pantallaActual = "almacenes" }
            )

            OpcionDashboard(
                titulo = "Registrar usuario",
                descripcion = "Crear una nueva cuenta de CL Stock",
                onClick = {
                    pantallaActual = "registrar_usuario"
                }
            )



            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }

    if (errorEscaneo.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { errorEscaneo = "" },
            title = { Text("Error del escáner") },
            text = { Text(errorEscaneo) },
            confirmButton = {
                TextButton(onClick = { errorEscaneo = "" }) {
                    Text("Aceptar")
                }
            }
        )
    }


    if (mostrarAviso) {
        AlertDialog(
            onDismissRequest = { mostrarAviso = false },
            title = { Text("CL Stock") },
            text = {
                Text("Esta función estará disponible próximamente.")
            },
            confirmButton = {
                TextButton(onClick = { mostrarAviso = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun OpcionDashboard(
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        Color(0xFF2563EB),
                        RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF142D4E)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

