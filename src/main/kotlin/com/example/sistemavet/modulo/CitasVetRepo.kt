package com.example.sistemavet.modulo

import com.example.sistemavet.Database
import com.example.sistemavet.datos.CitaVetDto
import java.sql.Date
import java.sql.Types
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object CitaVetRepo {
    private val FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val HORA = DateTimeFormatter.ofPattern("HH:mm")

    fun listar(veterinarioId: Int, estado: String?): List<CitaVetDto> =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spListarCitasVeterinario(?,?)}").use { cs ->
                cs.setInt(1, veterinarioId)
                if (estado == null) cs.setNull(2, Types.NVARCHAR) else cs.setString(2, estado)
                cs.executeQuery().use { rs ->
                    val lista = mutableListOf<CitaVetDto>()
                    while (rs.next()) {
                        lista.add(
                            CitaVetDto(
                                id = rs.getInt("Id"),
                                mascotaId = rs.getInt("MascotaId"),
                                mascota = rs.getString("MascotaNombre") ?: "",
                                propietarioId = rs.getInt("PropietarioId"),
                                propietario = rs.getString("PropietarioNombre") ?: "",
                                fecha = LocalDate.parse(rs.getString("Fecha")).format(FECHA),
                                hora = LocalTime.parse(rs.getString("Hora")).format(HORA),
                                motivo = rs.getString("Motivo") ?: "",
                                estado = rs.getString("Estado") ?: ""
                            )
                        )
                    }
                    lista
                }
            }
        }

    // Confirmar o cancelar. El SP deja la notificación guardada para el propietario.
    fun cambiarEstado(citaId: Int, veterinarioId: Int, nuevoEstado: String) {
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spCambiarEstadoCita(?,?,?)}").use { cs ->
                cs.setInt(1, citaId)
                cs.setInt(2, veterinarioId)
                cs.setString(3, nuevoEstado)
                cs.executeQuery().use { }
            }
        }
    }

    fun reprogramar(citaId: Int, veterinarioId: Int, fecha: LocalDate, hora: LocalTime) {
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spReprogramarCita(?,?,?,?)}").use { cs ->
                cs.setInt(1, citaId)
                cs.setInt(2, veterinarioId)
                cs.setDate(3, Date.valueOf(fecha))
                cs.setTime(4, java.sql.Time.valueOf(hora))
                cs.executeQuery().use { }
            }
        }
    }

    // Completa la cita y registra la consulta; devuelve el id de la consulta
    fun completar(
        citaId: Int, veterinarioId: Int, fechaAtencion: LocalDate, motivo: String,
        diagnostico: String, tratamiento: String, peso: Double, observaciones: String?
    ): Int =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spCompletarCita(?,?,?,?,?,?,?,?)}").use { cs ->
                cs.setInt(1, citaId)
                cs.setInt(2, veterinarioId)
                cs.setDate(3, Date.valueOf(fechaAtencion))
                cs.setString(4, motivo)
                cs.setString(5, diagnostico)
                cs.setString(6, tratamiento)
                cs.setBigDecimal(7, java.math.BigDecimal.valueOf(peso))
                if (observaciones.isNullOrBlank()) cs.setNull(8, Types.NVARCHAR) else cs.setString(8, observaciones)
                cs.executeQuery().use { rs ->
                    rs.next()
                    rs.getInt("Id")
                }
            }
        }
}

