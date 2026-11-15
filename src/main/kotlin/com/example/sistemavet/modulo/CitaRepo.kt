package com.example.sistemavet.modulo


import com.example.sistemavet.Database
import com.example.sistemavet.datos.Opcion

import com.example.sistemavet.datos.SolicitudCitaRequest
import java.sql.Date
import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime

object CitaRepo {

    // lista de las masctoas del propietario
    fun mascotasDe(propietarioId: Int): List<Opcion> =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spListarMascotasPorPropietario(?)}").use { cs ->
                cs.setInt(1, propietarioId)
                cs.executeQuery().use { rs ->
                    val lista = mutableListOf<Opcion>()
                    while (rs.next()) lista.add(Opcion(rs.getInt("Id"), rs.getString("Nombre")))
                    lista
                }
            }
        }

    // lista de veterinarios
    fun veterinarios(): List<Opcion> =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spListarVeterinarios}").use { cs ->
                cs.executeQuery().use { rs ->
                    val lista = mutableListOf<Opcion>()
                    while (rs.next()) lista.add(Opcion(rs.getInt("Id"), rs.getString("NombreCompleto")))
                    lista
                }
            }
        }

    fun horarios(veterinarioId: Int, fecha: LocalDate): List<String> =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spListarHorariosDisponibles(?,?)}").use { cs ->
                cs.setInt(1, veterinarioId)
                cs.setDate(2, Date.valueOf(fecha))
                cs.executeQuery().use { rs ->
                    val lista = mutableListOf<String>()
                    while (rs.next()) lista.add(rs.getString("Hora").take(5))   // "08:00:00" -> "08:00"
                    lista
                }
            }
        }

    // Solicitar cita
    fun solicitar(r: SolicitudCitaRequest, fecha: LocalDate, hora: LocalTime): Int =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spSolicitarCita(?,?,?,?,?,?)}").use { cs ->
                cs.setInt(1, r.propietarioId)
                cs.setInt(2, r.mascotaId)
                cs.setInt(3, r.veterinarioId)
                cs.setDate(4, Date.valueOf(fecha))
                cs.setTime(5, Time.valueOf(hora))
                cs.setString(6, r.motivo)
                cs.executeQuery().use { rs ->
                    rs.next()
                    rs.getInt("Id")
                }
            }
        }
}