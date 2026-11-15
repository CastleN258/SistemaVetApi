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

// Para solicitar citas
@Serializable
data class Opcion(
      val id: Int
    , val nombre: String)

@Serializable
data class SolicitudCitaRequest(
    val propietarioId: Int,
    val mascotaId: Int,
    val veterinarioId: Int,
    val fecha: String,    // dd/MM/yyyy
    val hora: String,     // HH:mm
    val motivo: String
)

@Serializable
data class CitaCreada(
    val id: Int
)

// Para registrar la mascota
@Serializable
data class MascotaRequest(
    val propietarioId: Int, // Se manda el id pero se ignora
    val nombre: String,
    val especie: String,
    val raza: String,
    val sexo: String,              // "MACHO" o "HEMBRA"
    val fechaNacimiento: String,   // dd/MM/yyyy
    val pesoAproximado: Double
)

@Serializable
data class MascotaCreada(
    val id: Int,
    val propietarioId: Int,
    val nombre: String,
    val especie: String,
    val raza: String,
    val sexo: String,
    val fechaNacimiento: String,
    val pesoAproximado: Double
)

