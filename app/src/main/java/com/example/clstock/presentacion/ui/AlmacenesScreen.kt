
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

import com.example.clstock.data.repository.AlmacenRepository
import com.example.clstock.domain.model.Almacen

@Composable
fun AlmacenesScreen(
    onVolver: () -> Unit
) {
    val azulOscuro = Color(0xFF142D4E)
    val azulPrincipal = Color(0xFF2563EB)

    val repositorio = remember {
        AlmacenRepository()
    }

    var nombre by remember {
        mutableStateOf("")
    }

    var descripcion by remember {
        mutableStateOf("")
    }

    var almacenes by remember {
        mutableStateOf<List<Almacen>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    fun cargarAlmacenes() {
        cargando = true
        error = ""

        repositorio.obtenerAlmacenes(
            onSuccess = { lista ->
                almacenes = lista
                cargando = false
            },
            onError = { mensajeError ->
                error = mensajeError
                cargando = false
            }
        )
    }

    LaunchedEffect(Unit) {
        cargarAlmacenes()
    }

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
            text = "Almacenes",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Administra los almacenes de CL Stock",
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
            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "Registrar nuevo almacén",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulOscuro
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        mensaje = ""
                    },
                    label = {
                        Text("Nombre del almacén")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                    },
                    label = {
                        Text("Descripción")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        mensaje = ""
                        error = ""
                        guardando = true

                        repositorio.guardarAlmacen(
                            nombre = nombre,
                            descripcion = descripcion,
                            onSuccess = {
                                nombre = ""
                                descripcion = ""
                                guardando = false
                                mensaje = "Almacén guardado correctamente"
                                cargarAlmacenes()
                            },
                            onError = { mensajeError ->
                                guardando = false
                                error = mensajeError
                            }
                        )
                    },
                    enabled = !guardando &&
                            nombre.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azulPrincipal
                    )
                ) {
                    Text(
                        if (guardando) {
                            "Guardando..."
                        } else {
                            "Guardar almacén"
                        }
                    )
                }

                if (mensaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = mensaje,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Almacenes registrados",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                cargarAlmacenes()
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizar almacenes")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (cargando) {
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(8.dp))

            Text("Consultando Firebase...")
        }

        if (error.isNotEmpty()) {
            Text(
                text = "Error: $error",
                color = Color.Red
            )
        }

        if (!cargando && error.isEmpty()) {

            Text(
                text = "Total de almacenes: ${almacenes.size}",
                color = azulOscuro,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (almacenes.isEmpty()) {
                Text(
                    text = "Todavía no hay almacenes registrados.",
                    color = Color.Gray
                )
            }

            almacenes.forEach { almacen ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = almacen.nombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = azulOscuro
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (almacen.descripcion.isNotBlank()) {
                            Text(
                                text = "Descripción: ${almacen.descripcion}"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (almacen.estado == 1) {
                                "● Activo"
                            } else {
                                "● Inactivo"
                            },
                            color = if (almacen.estado == 1) {
                                Color(0xFF15803D)
                            } else {
                                Color.Red
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
