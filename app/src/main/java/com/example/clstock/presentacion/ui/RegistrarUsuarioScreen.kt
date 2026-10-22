
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clstock.data.repository.UsuarioRepository
import com.example.clstock.domain.model.Usuario

@Composable
fun RegistrarUsuarioScreen(
    onVolver: () -> Unit
) {
    val azulOscuro = Color(0xFF142D4E)
    val azulPrincipal = Color(0xFF2563EB)

    val repository = remember { UsuarioRepository() }

    var nombre by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var cargandoUsuarios by remember { mutableStateOf(false) }
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }

    fun cargarUsuarios() {
        cargandoUsuarios = true

        repository.obtenerUsuarios(
            onSuccess = { lista ->
                usuarios = lista
                cargandoUsuarios = false
            },
            onError = { detalle ->
                error = detalle
                cargandoUsuarios = false
            }
        )
    }

    LaunchedEffect(Unit) {
        cargarUsuarios()
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
            text = "Registrar usuario",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Crear una cuenta para acceder a CL Stock",
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
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nombreUsuario,
                    onValueChange = { nombreUsuario = it },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña") },
                    visualTransformation =
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = { confirmarContrasena = it },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation =
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    enabled = !guardando,
                    onClick = {
                        mensaje = ""
                        error = ""

                        when {
                            nombre.isBlank() ||
                                    nombreUsuario.isBlank() ||
                                    contrasena.isBlank() ||
                                    confirmarContrasena.isBlank() -> {
                                error = "Completa todos los campos"
                            }

                            contrasena.length < 6 -> {
                                error = "La contraseña debe tener al menos 6 caracteres"
                            }

                            contrasena != confirmarContrasena -> {
                                error = "Las contraseñas no coinciden"
                            }

                            else -> {
                                guardando = true

                                repository.registrarUsuario(
                                    nombre = nombre,
                                    nombreUsuario = nombreUsuario,
                                    contrasena = contrasena,
                                    onSuccess = {
                                        guardando = false
                                        mensaje = "Usuario registrado correctamente"

                                        nombre = ""
                                        nombreUsuario = ""
                                        contrasena = ""
                                        confirmarContrasena = ""

                                        cargarUsuarios()
                                    },
                                    onError = { detalle ->
                                        guardando = false
                                        error = detalle
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azulPrincipal
                    )
                ) {
                    if (guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar usuario")
                    }
                }

                if (mensaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = mensaje,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = Color.Red
                    )
                }
            }
        }

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
                    text = "Usuarios registrados",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulOscuro
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Total de usuarios: ${usuarios.size}",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { cargarUsuarios() },
                    enabled = !cargandoUsuarios,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar usuarios")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (cargandoUsuarios) {
                    CircularProgressIndicator()
                } else if (usuarios.isEmpty()) {
                    Text(
                        text = "No hay usuarios registrados",
                        color = Color.Gray
                    )
                } else {
                    usuarios.forEach { usuario ->
                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = usuario.nombre,
                            fontWeight = FontWeight.Bold,
                            color = azulOscuro
                        )

                        Text(
                            text = "Usuario: ${usuario.nombreUsuario}",
                            fontSize = 14.sp
                        )

                        Text(
                            text = if (usuario.estado == 1) {
                                "Estado: Activo"
                            } else {
                                "Estado: Inactivo"
                            },
                            fontSize = 13.sp,
                            color = if (usuario.estado == 1) {
                                Color(0xFF15803D)
                            } else {
                                Color.Red
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

