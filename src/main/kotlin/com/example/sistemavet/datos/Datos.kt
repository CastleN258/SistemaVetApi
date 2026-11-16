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

// Consultas del propietario (solo lectura)
@Serializable
data class MascotaDto(
    val id: Int, val propietarioId: Int, val nombre: String, val especie: String,
    val raza: String, val sexo: String, val fechaNacimiento: String, val pesoAproximado: Double
)

@Serializable
data class CitaDto(
    val id: Int, val mascotaId: Int, val mascota: String, val veterinarioId: Int,
    val veterinario: String, val fecha: String, val hora: String, val motivo: String,
    val estado: String, val estadoVista: String
)

@Serializable
data class AtencionDto(
    val id: Int, val citaId: Int, val mascotaId: Int, val mascota: String,
    val veterinarioId: Int, val veterinario: String, val fechaAtencion: String,
    val hora: String, val motivo: String, val diagnostico: String, val tratamiento: String,
    val peso: Double, val observaciones: String
)

@Serializable
data class AlergiaDto(val id: Int, val mascotaId: Int, val nombre: String, val observaciones: String)

@Serializable
data class VacunaDto(
    val id: Int, val mascotaId: Int, val nombre: String,
    val fechaAplicacion: String, val fechaProximaDosis: String
)

@Serializable
data class RecetaDto(
    val id: Int, val consultaId: Int, val medicamento: String, val dosis: String,
    val frecuencia: String, val duracion: String, val instrucciones: String
)


// Veterinario: citas
@Serializable
data class CitaVetDto(
    val id: Int, val mascotaId: Int, val mascota: String, val propietarioId: Int,
    val propietario: String, val fecha: String, val hora: String,
    val motivo: String, val estado: String
)

@Serializable
data class CambioEstadoRequest(val estado: String)          // CONFIRMADA | CANCELADA

@Serializable
data class ReprogramarRequest(val fecha: String, val hora: String)   // dd/MM/yyyy y HH:mm

@Serializable
data class CompletarCitaRequest(
    val fechaAtencion: String, val motivo: String, val diagnostico: String,
    val tratamiento: String, val peso: Double, val observaciones: String? = null
)

@Serializable
data class IdCreado(val id: Int)

// Veterinario: expedientes, vacunas, alergias y recetas
@Serializable
data class MascotaVetDto(
    val id: Int, val propietarioId: Int, val propietario: String, val nombre: String,
    val especie: String, val raza: String, val sexo: String,
    val fechaNacimiento: String, val pesoAproximado: Double
)

@Serializable
data class ConsultaVetDto(
    val id: Int, val mascotaId: Int, val mascota: String,
    val propietario: String, val fechaAtencion: String
)

@Serializable
data class VacunaRequest(
    val mascotaId: Int, val nombre: String,
    val fechaAplicacion: String, val fechaProximaDosis: String     // dd/MM/yyyy
)

@Serializable
data class AlergiaRequest(
    val mascotaId: Int, val nombre: String,
    val observaciones: String? = null, val consultaId: Int? = null
)

@Serializable
data class RecetaRequest(
    val consultaId: Int, val medicamento: String, val dosis: String,
    val frecuencia: String, val duracion: String, val instrucciones: String? = null
)

