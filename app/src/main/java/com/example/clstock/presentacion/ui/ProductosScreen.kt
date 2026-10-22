
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
import com.example.clstock.data.repository.ProductoRepository
import com.example.clstock.domain.model.Producto
import java.util.UUID



@Composable
fun ProductosScreen(
    onVolver: () -> Unit,
    onEscanear: () -> Unit,
    codigoEscaneado: String = ""
) {
    var nombre by remember { mutableStateOf("") }
    var codigoBarra by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var categoriaSeleccionada by remember { mutableStateOf("Sin categoría") }
    var menuCategoriasAbierto by remember { mutableStateOf(false) }

    val categorias = listOf(
        "Sin categoría",
        "Periféricos",
        "Monitores",
        "Accesorios",
        "Muebles",
        "Herramientas",
        "Otros"
    )

    var mensaje by remember { mutableStateOf("") }

    var guardando by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var buscandoProducto by remember { mutableStateOf(false) }
    var productoExistente by remember { mutableStateOf(false) }

    var productos by remember {
        mutableStateOf<List<Producto>>(emptyList())
    }

    val repositorio = remember { ProductoRepository() }
    val azulOscuro = Color(0xFF142D4E)

    LaunchedEffect(codigoEscaneado) {
        if (codigoEscaneado.isNotBlank()) {

            codigoBarra = codigoEscaneado
            nombre = ""
            descripcion = ""
            productoExistente = false

            buscandoProducto = true
            mensaje = "Buscando producto en Firebase..."

            repositorio.buscarProductoPorCodigo(
                codigoBarra = codigoEscaneado,

                onSuccess = { producto ->
                    buscandoProducto = false

                    if (producto != null) {
                        productoExistente = true
                        nombre = producto.nombre
                        descripcion = producto.descripcion

                        mensaje = "Producto encontrado en Firebase"
                    } else {
                        mensaje = "Producto no registrado. Puedes registrarlo."
                    }
                },

                onError = { error ->
                    buscandoProducto = false
                    mensaje = "Error al buscar producto: $error"
                }
            )
        }
    }



    fun cargarProductos() {
        cargando = true

        repositorio.obtenerProductos(
            onSuccess = { lista ->
                productos = lista.sortedBy { it.nombre.lowercase() }
                cargando = false
            },
            onError = { error ->
                mensaje = "Error al cargar productos: $error"
                cargando = false
            }
        )
    }

    LaunchedEffect(Unit) {
        cargarProductos()
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
            text = "Productos",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registra y consulta productos del almacén",
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
                    text = "Registrar producto",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulOscuro
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = codigoBarra,
                    onValueChange = { codigoBarra = it },
                    label = { Text("Código de barras") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onEscanear,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Escanear código de barras")
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Categoría",
                    fontWeight = FontWeight.Bold,
                    color = azulOscuro
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { menuCategoriasAbierto = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Categoría: $categoriaSeleccionada ▼")
                    }

                    DropdownMenu(
                        expanded = menuCategoriasAbierto,
                        onDismissRequest = {
                            menuCategoriasAbierto = false
                        }
                    ) {
                        categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria) },
                                onClick = {
                                    categoriaSeleccionada = categoria
                                    menuCategoriasAbierto = false
                                }
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (nombre.isBlank() || codigoBarra.isBlank()) {
                            mensaje = "Completa el nombre y código de barras"
                        } else {
                            guardando = true
                            mensaje = "Guardando producto..."

                            val producto = Producto(
                                idProducto = UUID.randomUUID().toString(),
                                idCategoria = categoriaSeleccionada,
                                codigoBarra = codigoBarra.trim(),
                                nombre = nombre.trim(),
                                descripcion = descripcion.trim(),
                                unidadMedida = "Unidad",
                                estado = 1
                            )

                            repositorio.guardarProducto(
                                producto = producto,
                                onSuccess = {
                                    guardando = false
                                    mensaje = "Producto guardado correctamente"

                                    nombre = ""
                                    codigoBarra = ""
                                    descripcion = ""
                                    categoriaSeleccionada = "Sin categoría"

                                    cargarProductos()
                                },
                                onError = { error ->
                                    guardando = false
                                    mensaje = "Error al guardar: $error"
                                }
                            )
                        }
                    },

                    enabled = !guardando && !buscandoProducto && !productoExistente,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    )
                ) {
                    Text(
                        if (guardando) "Guardando..."
                        else "Guardar producto"
                    )
                }

                if (mensaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = mensaje,
                        color = azulOscuro
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Productos registrados",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { cargarProductos() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizar lista")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (cargando) {
            CircularProgressIndicator()
        } else if (productos.isEmpty()) {
            Text(
                text = "No hay productos registrados",
                color = Color.Gray
            )
        } else {
            Text(
                text = "Total de productos: ${productos.size}",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            productos.forEach { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = producto.nombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = azulOscuro
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Código: ${producto.codigoBarra}"
                        )

                        Text(
                            text = "Descripción: ${producto.descripcion}"
                        )

                        Text(
                            text = "Categoría: ${
                                producto.idCategoria.ifBlank { "Sin categoría" }
                            }"
                        )


                        Text(
                            text = if (producto.estado == 1)
                                "Estado: Activo"
                            else
                                "Estado: Inactivo",
                            color = if (producto.estado == 1)
                                Color(0xFF16803C)
                            else
                                Color.Gray
                        )
                    }
                }
            }
        }
    }
}
