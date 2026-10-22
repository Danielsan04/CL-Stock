
package com.example.clstock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clstock.ui.theme.CLStockTheme
import com.example.clstock.presentacion.ui.DashboardScreen
import com.example.clstock.data.repository.UsuarioRepository


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CLStockTheme {

                var sesionIniciada by remember {
                    mutableStateOf(false)
                }

                var usuarioActual by remember {
                    mutableStateOf("")
                }

                if (sesionIniciada) {

                    DashboardScreen(
                        nombreUsuario = usuarioActual,
                        onCerrarSesion = {

                            com.google.firebase.auth.FirebaseAuth
                                .getInstance()
                                .signOut()

                            sesionIniciada = false
                            usuarioActual = ""
                        }
                    )

                } else {

                    PantallaLogin(
                        onIngresar = { nombre ->

                            usuarioActual = nombre
                            sesionIniciada = true

                        }
                    )

                }
            }
        }
    }
}

@Composable
fun PantallaLogin(
    onIngresar: (String) -> Unit
) {

    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }


    val azulOscuro = Color(0xFF142D4E)
    val azulPrincipal = Color(0xFF2563EB)
    val usuarioRepository = remember { UsuarioRepository() }
    var verificando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6FB))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "CL Stock",
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sistema de Gestión de Inventario",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.almacen),
            contentDescription = "Imagen de almacén",
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Iniciar sesión",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulOscuro
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = usuario,
                    onValueChange = { usuario = it },
                    label = { Text("Nombre de usuario") },
                    placeholder = { Text("Ej: DanielSantos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(


                    onClick = {
                        if (usuario.isBlank() || contrasena.isBlank()) {
                            mensaje = "Complete todos los campos"
                        } else {
                            mensaje = ""
                            verificando = true

                            usuarioRepository.iniciarSesion(
                                nombreUsuario = usuario,
                                contrasena = contrasena,
                                onSuccess = { nombre ->
                                    verificando = false
                                    onIngresar(nombre)
                                },
                                onError = { error ->
                                    verificando = false
                                    mensaje = error
                                }
                            )
                        }
                    },


                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azulPrincipal
                    )
                ) {
                    Text(
                        text = if (verificando) "Verificando..." else "Iniciar sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (mensaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = mensaje,
                        color = azulOscuro,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CL Stock © 2026",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
