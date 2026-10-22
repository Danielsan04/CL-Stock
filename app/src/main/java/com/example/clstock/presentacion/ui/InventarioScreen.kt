
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

import com.example.clstock.domain.model.Producto
import com.example.clstock.data.repository.ProductoRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

@Composable
fun InventarioScreen(
    onVolver: () -> Unit
) {
    val azulOscuro = Color(0xFF142D4E)
    val azulPrincipal = Color(0xFF2563EB)

    val repositorio = remember { ProductoRepository() }
    val db = remember { FirebaseFirestore.getInstance() }

    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember {
        mutableStateOf("Todas")
    }
    var menuCategoriasAbierto by remember {
        mutableStateOf(false)
    }

    val categorias = listOf(
        "Todas",
        "Sin categoría",
        "Periféricos",
        "Monitores",
        "Accesorios",
        "Muebles",
        "Herramientas",
        "Otros"
    )

    var productos by remember {
        mutableStateOf<List<Producto>>(emptyList())
    }

    // Cantidad de unidades por ID de producto
    var cantidades by remember {
        mutableStateOf<Map<String, Int>>(emptyMap())
    }

    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun cargarInventario() {
        cargando = true
        error = ""

        repositorio.obtenerProductos(
            onSuccess = { lista ->

                val productosOrdenados = lista.sortedBy {
                    it.nombre.lowercase()
                }

                db.collection("inventario")
                    .get(Source.SERVER)
                    .addOnSuccessListener { resultado ->

                        val mapaCantidades = mutableMapOf<String, Int>()

                        for (documento in resultado.documents) {
                            val idProducto =
                                documento.getString("idProducto")
                                    ?: documento.id

                            val cantidad =
                                documento.getLong("cantidad")?.toInt() ?: 0

                            mapaCantidades[idProducto] = cantidad
                        }

                        productos = productosOrdenados
                        cantidades = mapaCantidades
                        cargando = false
                    }
                    .addOnFailureListener { excepcion ->
                        error = "No se pudieron consultar las cantidades: " +
                                (excepcion.message ?: "Error de Firebase")
                        cargando = false
                    }
            },
            onError = { mensaje ->
                error = mensaje
                cargando = false
            }
        )
    }

    LaunchedEffect(Unit) {
        cargarInventario()
    }

    val productosFiltrados = productos.filter { producto ->

        val coincideBusqueda =
            producto.nombre.contains(busqueda, ignoreCase = true) ||
                    producto.codigoBarra.contains(
                        busqueda,
                        ignoreCase = true
                    )

        val categoriaProducto =
            producto.idCategoria.ifBlank { "Sin categoría" }

        val coincideCategoria =
            categoriaSeleccionada == "Todas" ||
                    categoriaProducto == categoriaSeleccionada

        coincideBusqueda && coincideCategoria
    }

    val totalUnidades = productos.sumOf { producto ->
        cantidades[producto.idProducto] ?: 0
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
            text = "Inventario",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Consulta las existencias de tu almacén",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar producto o código") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Filtrar por categoría",
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {

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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { cargarInventario() },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (cargando) "Actualizando..."
                else "Actualizar inventario"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = azulOscuro
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Productos", color = Color.White)

                    Text(
                        text = productos.size.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = azulPrincipal
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Unidades", color = Color.White)

                    Text(
                        text = if (cargando || error.isNotEmpty()) {
                            "—"
                        } else {
                            totalUnidades.toString()
                        },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Existencias de productos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (cargando) {
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(12.dp))

            Text("Cargando inventario de Firebase...")
        }

        if (error.isNotEmpty()) {
            Text(
                text = "Error: $error",
                color = Color.Red
            )
        }

        if (!cargando && error.isEmpty() &&
            productosFiltrados.isEmpty()
        ) {
            Text(
                if (productos.isEmpty()) {
                    "Todavía no hay productos registrados."
                } else {
                    "No se encontraron productos con esos filtros."
                }
            )
        }

        if (!cargando && error.isEmpty()) {

            productosFiltrados.forEach { producto ->

                val cantidadDisponible =
                    cantidades[producto.idProducto] ?: 0

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
                            text = producto.nombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = azulOscuro
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Código: ${producto.codigoBarra}")

                        Text(
                            text = "Categoría: ${
                                producto.idCategoria.ifBlank {
                                    "Sin categoría"
                                }
                            }"
                        )

                        if (producto.descripcion.isNotBlank()) {
                            Text(
                                "Descripción: ${producto.descripcion}"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Cantidad disponible: $cantidadDisponible unidades",
                            color = if (cantidadDisponible > 0) {
                                Color(0xFF15803D)
                            } else {
                                Color(0xFFD97706)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
