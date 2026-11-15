package com.example.sistemavet.datos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// Manda el id, rol y si esta activada
// El rol se decide en el servidor
@Serializable
data class RegistroRequest(
    @SerialName("nombreCompleto") val nombre: String,
    @SerialName("identificacion") val ide: String,
    val correo: String,
    val telefono: String,
    val contrasena: String
)

@Serializable
data class RegistroLogin(
    val correo: String,
    val contrasena: String
)

@Serializable
data class UsuarioSesion(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: String,
    val cuentaActivada: Boolean,
    val token: String?
)

// Para notificaciones
@Serializable
data class Notificacion(
    val id: Int,
    val usuarioId: Int,
    val tipo: String,
    val titulo: String,
    val mensaje: String,
    val referenciaId: Int,
    val fechaEnvio: String,
    val leida: Boolean
)