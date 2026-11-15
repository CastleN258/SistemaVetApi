package com.example.sistemavet.modulo

import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter
import com.example.sistemavet.datos.RegistroRequest
import java.sql.Timestamp
import com.example.sistemavet.Database
import com.example.sistemavet.datos.Notificacion

object Notificacionrepo
{
    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun listar(usuarioId: Int): List<Notificacion> =
        Database.connect().use { conn ->
            // 1. Crea los recordatorios y avisos de vacuna que ya toquen
            conn.prepareCall("{call dbo.spGenerarAvisosUsuario(?)}").use { cs ->
                cs.setInt(1, usuarioId)
                cs.execute()
            }
            // 2. Devuelve todas las notificaciones del usuario
            conn.prepareCall("{call dbo.spListarNotificaciones(?)}").use { cs ->
                cs.setInt(1, usuarioId)
                cs.executeQuery().use { rs ->
                    val lista = mutableListOf<Notificacion>()
                    while (rs.next()) {
                        // FechaEnvio está en UTC; se muestra en hora de Costa Rica (UTC-6)
                        val fecha = rs.getTimestamp("FechaEnvio").toLocalDateTime().minusHours(6)
                        lista.add(
                            Notificacion(
                                id = rs.getInt("Id"),
                                usuarioId = usuarioId,
                                tipo = rs.getString("Tipo"),
                                titulo = rs.getString("Titulo"),
                                mensaje = rs.getString("Mensaje"),
                                referenciaId = rs.getInt("ReferenciaId"),
                                fechaEnvio = fecha.format(formato),
                                leida = rs.getBoolean("Leida")
                            )
                        )
                    }
                    lista
                }
            }
        }

    fun marcarLeida(id: Int, usuarioId: Int) {
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spMarcarNotificacionLeida(?,?)}").use { cs ->
                cs.setInt(1, id)
                cs.setInt(2, usuarioId)
                cs.execute()
            }
        }
    }



}