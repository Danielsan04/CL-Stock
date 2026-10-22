
package com.example.clstock.presentacion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clstock.data.repository.MovimientoRepository
import com.example.clstock.data.repository.ProductoRepository
import com.example.clstock.domain.model.Producto

import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode


@Composable
fun MovimientosScreen(
    onVolver: () -> Unit
) {
    val azulOscuro = Color(0xFF142D4E)
    val azulPrincipal = Color(0xFF2563EB)

    val productoRepository = remember { ProductoRepository() }
    val movimientoRepository = remember { MovimientoRepository() }

    val context = LocalContext.current

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


    var codigoBarra by remember { mutableStateOf("") }
    var productoEncontrado by remember { mutableStateOf<Producto?>(null) }
    var tipoMovimiento by remember { mutableStateOf("Entrada") }
    var cantidad by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var buscando by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

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
            text = "Movimientos",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registra entradas y salidas de mercadería",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text = "Nuevo movimiento",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulOscuro
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = codigoBarra,
                    onValueChange = {
                        codigoBarra = it
                        productoEncontrado = null
                        mensaje = ""
                    },
                    label = { Text("Código de barras") },
                    singleLine = true,
                    enabled = !guardando && !buscando,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        escaner.startScan()
                            .addOnSuccessListener { codigo ->
                                val valor = codigo.rawValue

                                if (!valor.isNullOrBlank()) {
                                    codigoBarra = valor.trim()
                                    productoEncontrado = null
                                    buscando = true
                                    mensaje = "Buscando producto..."

                                    productoRepository.buscarProductoPorCodigo(
                                        codigoBarra = valor.trim(),
                                        onSuccess = { producto ->
                                            buscando = false
                                            productoEncontrado = producto

                                            mensaje = if (producto != null) {
                                                "Producto encontrado en Firebase"
                                            } else {
                                                "Producto no registrado"
                                            }
                                        },
                                        onError = { error ->
                                            buscando = false
                                            productoEncontrado = null
                                            mensaje = "Error al buscar: $error"
                                        }
                                    )
                                } else {
                                    mensaje = "No se pudo leer el código"
                                }
                            }
                            .addOnFailureListener { error ->
                                mensaje = "Error del escáner: ${
                                    error.localizedMessage ?: "No se pudo abrir"
                                }"
                            }
                    },
                    enabled = !buscando && !guardando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azulPrincipal
                    )
                ) {
                    Text("📷 Escanear código de barras")
                }


                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (codigoBarra.isBlank()) {
                            mensaje = "Ingresa un código de barras"
                        } else {
                            buscando = true
                            mensaje = "Buscando producto..."

                            productoRepository.buscarProductoPorCodigo(
                                codigoBarra = codigoBarra.trim(),
                                onSuccess = { producto ->
                                    buscando = false
                                    productoEncontrado = producto

                                    mensaje = if (producto != null) {
                                        "Producto encontrado en Firebase"
                                    } else {
                                        "Producto no registrado"
                                    }
                                },
                                onError = { error ->
                                    buscando = false
                                    productoEncontrado = null
                                    mensaje = "Error al buscar: $error"
                                }
                            )
                        }
                    },
                    enabled = !buscando && !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (buscando) "Buscando..." else "Buscar producto")
                }

                if (productoEncontrado != null) {

                    val producto = productoEncontrado!!

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEFF6FF)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = producto.nombre,
                                fontWeight = FontWeight.Bold,
                                color = azulOscuro
                            )

                            Text("Código: ${producto.codigoBarra}")

                            Text(
                                "Categoría: ${
                                    producto.idCategoria.ifBlank {
                                        "Sin categoría"
                                    }
                                }"
                            )

                            Text("Descripción: ${producto.descripcion}")
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Tipo de movimiento",
                        fontWeight = FontWeight.Bold,
                        color = azulOscuro
                    )

                    Row {
                        RadioButton(
                            selected = tipoMovimiento == "Entrada",
                            onClick = {
                                tipoMovimiento = "Entrada"
                            },
                            enabled = !guardando
                        )

                        Text(
                            "Entrada",
                            modifier = Modifier.padding(top = 12.dp)
                        )

                        Spacer(modifier = Modifier.width(18.dp))

                        RadioButton(
                            selected = tipoMovimiento == "Salida",
                            onClick = {
                                tipoMovimiento = "Salida"
                            },
                            enabled = !guardando
                        )

                        Text(
                            "Salida",
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad de unidades") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        enabled = !guardando,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val unidades = cantidad.toIntOrNull()

                            if (unidades == null || unidades <= 0) {
                                mensaje = "Ingresa una cantidad válida"
                            } else {
                                guardando = true
                                mensaje = "Registrando movimiento..."

                                movimientoRepository.registrarMovimiento(
                                    idProducto = producto.idProducto,
                                    tipo = tipoMovimiento,
                                    cantidad = unidades,
                                    onSuccess = {
                                        guardando = false
                                        mensaje = "Movimiento registrado correctamente"
                                        cantidad = ""
                                        productoEncontrado = null
                                        codigoBarra = ""
                                    },
                                    onError = { error ->
                                        guardando = false
                                        mensaje = "Error: $error"
                                    }
                                )
                            }
                        },
                        enabled = !guardando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = azulPrincipal
                        )
                    ) {
                        Text(
                            if (guardando) "Guardando..."
                            else "Registrar movimiento"
                        )
                    }
                }

                if (mensaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = mensaje,
                        color = azulOscuro
                    )
                }
            }
        }
    }
}
