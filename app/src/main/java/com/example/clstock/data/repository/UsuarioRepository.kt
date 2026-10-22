
package com.example.clstock.data.repository

import com.example.clstock.domain.model.Usuario
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class UsuarioRepository {

    private val db = FirebaseFirestore.getInstance()

    fun registrarUsuario(
        nombre: String,
        nombreUsuario: String,
        contrasena: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioNormalizado = nombreUsuario.trim().lowercase()

        if (!Regex("^[a-z0-9._-]{3,30}$").matches(usuarioNormalizado)) {
            onError("El usuario debe tener entre 3 y 30 caracteres: letras, números, punto, guion o guion bajo")
            return
        }

        if (contrasena.length < 6) {
            onError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        // Identificador interno para Firebase Authentication.
        // El empleado seguirá utilizando solamente su nombre de usuario.
        val correoInterno = "$usuarioNormalizado@usuarios.clstock.invalid"

        // Una segunda instancia evita cambiar la sesión principal.
        val appPrincipal = FirebaseApp.getInstance()
        val nombreAppSecundaria = "CLStockRegistro"

        val appSecundaria = FirebaseApp.getApps(appPrincipal.applicationContext)
            .firstOrNull { it.name == nombreAppSecundaria }
            ?: FirebaseApp.initializeApp(
                appPrincipal.applicationContext,
                FirebaseOptions.fromResource(appPrincipal.applicationContext)
                    ?: run {
                        onError("No se pudo cargar la configuración de Firebase")
                        return
                    },
                nombreAppSecundaria
            )

        if (appSecundaria == null) {
            onError("No se pudo iniciar Firebase para el registro")
            return
        }

        val authRegistro = FirebaseAuth.getInstance(appSecundaria)

        authRegistro.createUserWithEmailAndPassword(
            correoInterno,
            contrasena
        ).addOnSuccessListener { resultado ->

            val uid = resultado.user?.uid

            if (uid == null) {
                authRegistro.signOut()
                onError("No se obtuvo el identificador del usuario")
                return@addOnSuccessListener
            }

            val usuario = Usuario(
                idUsuario = uid,
                nombreUsuario = usuarioNormalizado,
                nombre = nombre.trim(),
                idAlmacen = "",
                estado = 1
            )

            db.collection("usuarios")
                .document(uid)
                .set(usuario)
                .addOnSuccessListener {
                    authRegistro.signOut()
                    onSuccess()
                }
                .addOnFailureListener { error ->
                    authRegistro.signOut()
                    onError(
                        "La cuenta se creó en Authentication, pero no se pudo guardar el perfil: ${error.message}"
                    )
                }

        }.addOnFailureListener { error ->
            authRegistro.signOut()
            onError(error.localizedMessage ?: "No se pudo registrar el usuario")
        }
    }

    fun obtenerUsuarios(
        onSuccess: (List<Usuario>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("usuarios")
            .get(Source.SERVER)
            .addOnSuccessListener { resultado ->
                val usuarios = resultado.documents
                    .mapNotNull { documento ->
                        documento.toObject(Usuario::class.java)
                    }
                    .sortedBy { it.nombre.lowercase() }

                onSuccess(usuarios)
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "No se pudieron cargar los usuarios")
            }
    }

    fun iniciarSesion(
        nombreUsuario: String,
        contrasena: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioNormalizado = nombreUsuario.trim().lowercase()

        if (!Regex("^[a-z0-9._-]{3,30}$").matches(usuarioNormalizado)) {
            onError("Nombre de usuario o contraseña incorrectos")
            return
        }

        val correoInterno =
            "$usuarioNormalizado@usuarios.clstock.invalid"

        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(correoInterno, contrasena)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid

                if (uid == null) {
                    auth.signOut()
                    onError("No se pudo identificar al usuario")
                    return@addOnSuccessListener
                }

                db.collection("usuarios")
                    .document(uid)
                    .get(Source.SERVER)
                    .addOnSuccessListener { documento ->
                        val usuario = documento.toObject(Usuario::class.java)

                        if (usuario == null ||
                            usuario.estado != 1 ||
                            usuario.nombreUsuario != usuarioNormalizado
                        ) {
                            auth.signOut()
                            onError("Usuario no autorizado o inactivo")
                        } else {
                            onSuccess(usuario.nombreUsuario)
                        }
                    }
                    .addOnFailureListener {
                        auth.signOut()
                        onError("No se pudo verificar el usuario")
                    }
            }
            .addOnFailureListener {
                onError("Nombre de usuario o contraseña incorrectos")
            }
    }

}
