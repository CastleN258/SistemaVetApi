package com.example.sistemavet.modulo

import kotlinx.serialization.Serializable

@Serializable
data class MascotaDto(
    val id: Int,
    val propietarioId: Int,
    val nombre: String,
    val especie: String,
    val raza: String,
    val sexo: String,
    val fechaNacimiento: String,
    val pesoAproximado: Double,
)

@Serializable
data class CitaDto(
    val id: Int,
    val mascotaId: Int,
    val mascota: String,
    val veterinarioId: Int,
    val veterinario: String,
    val fecha: String,
    val hora: String,
    val motivo: String,
    val estado: String,
    val estadoVista: String,
)

@Serializable
data class AtencionDto(
    val id: Int,
    val citaId: Int,
    val mascotaId: Int,
    val mascota: String,
    val veterinarioId: Int,
    val veterinario: String,
    val fechaAtencion: String,
    val hora: String,
    val motivo: String,
    val diagnostico: String,
    val tratamiento: String,
    val peso: Double,
    val observaciones: String,
)

@Serializable
data class AlergiaDto(
    val id: Int,
    val mascotaId: Int,
    val nombre: String,
    val observaciones: String,
)

@Serializable
data class VacunaDto(
    val id: Int,
    val mascotaId: Int,
    val nombre: String,
    val fechaAplicacion: String,
    val fechaProximaDosis: String,
)

@Serializable
data class RecetaDto(
    val id: Int,
    val consultaId: Int,
    val medicamento: String,
    val dosis: String,
    val frecuencia: String,
    val duracion: String,
    val instrucciones: String,
)
