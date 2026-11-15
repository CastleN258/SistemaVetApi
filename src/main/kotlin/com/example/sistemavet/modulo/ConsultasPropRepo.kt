package com.example.sistemavet.modulo

import com.example.sistemavet.Database
import com.example.sistemavet.datos.AlergiaDto
import com.example.sistemavet.datos.AtencionDto
import com.example.sistemavet.datos.CitaDto
import com.example.sistemavet.datos.MascotaDto
import com.example.sistemavet.datos.RecetaDto
import com.example.sistemavet.datos.VacunaDto
import java.sql.CallableStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

// Consultas de solo lectura del propietario. Usa los dbo.usp_Prop_* (SPConsultasProp.sql)
object ConsultasPropRepo {

    fun mascotas(propietarioId: Int): List<MascotaDto> =
        ejecutar("{call dbo.usp_Prop_ListarMascotas(?)}", { it.setInt(1, propietarioId) }) { rs ->
            MascotaDto(
                rs.getInt("Id"), rs.getInt("PropietarioId"), rs.texto("Nombre"),
                rs.texto("Especie"), rs.texto("Raza"), rs.texto("Sexo"),
                rs.texto("FechaNacimiento"), rs.getDouble("PesoAproximado")
            )
        }

    fun citas(propietarioId: Int, mascotaId: Int?, estadoVista: String?): List<CitaDto> =
        ejecutar("{call dbo.usp_Prop_ListarCitas(?,?,?)}", {
            it.setInt(1, propietarioId)
            if (mascotaId == null) it.setNull(2, Types.INTEGER) else it.setInt(2, mascotaId)
            if (estadoVista == null) it.setNull(3, Types.NVARCHAR) else it.setString(3, estadoVista)
        }) { rs ->
            CitaDto(
                rs.getInt("Id"), rs.getInt("MascotaId"), rs.texto("Mascota"),
                rs.getInt("VeterinarioId"), rs.texto("Veterinario"), rs.texto("Fecha"),
                rs.texto("Hora"), rs.texto("Motivo"), rs.texto("Estado"), rs.texto("EstadoVista")
            )
        }

    fun atenciones(propietarioId: Int, mascotaId: Int?): List<AtencionDto> =
        ejecutar("{call dbo.usp_Prop_ListarAtenciones(?,?)}", {
            it.setInt(1, propietarioId)
            if (mascotaId == null) it.setNull(2, Types.INTEGER) else it.setInt(2, mascotaId)
        }) { rs ->
            AtencionDto(
                rs.getInt("Id"), rs.getInt("CitaId"), rs.getInt("MascotaId"), rs.texto("Mascota"),
                rs.getInt("VeterinarioId"), rs.texto("Veterinario"), rs.texto("FechaAtencion"),
                rs.texto("Hora"), rs.texto("Motivo"), rs.texto("Diagnostico"),
                rs.texto("Tratamiento"), rs.getDouble("Peso"), rs.texto("Observaciones")
            )
        }

    fun alergias(propietarioId: Int, mascotaId: Int): List<AlergiaDto> =
        ejecutar("{call dbo.usp_Prop_ListarAlergias(?,?)}", {
            it.setInt(1, propietarioId)
            it.setInt(2, mascotaId)
        }) { rs ->
            AlergiaDto(rs.getInt("Id"), rs.getInt("MascotaId"), rs.texto("Nombre"), rs.texto("Observaciones"))
        }

    fun vacunas(propietarioId: Int, mascotaId: Int): List<VacunaDto> =
        ejecutar("{call dbo.usp_Prop_ListarVacunas(?,?)}", {
            it.setInt(1, propietarioId)
            it.setInt(2, mascotaId)
        }) { rs ->
            VacunaDto(
                rs.getInt("Id"), rs.getInt("MascotaId"), rs.texto("Nombre"),
                rs.texto("FechaAplicacion"), rs.texto("FechaProximaDosis")
            )
        }

    fun recetas(propietarioId: Int, consultaId: Int): List<RecetaDto> =
        ejecutar("{call dbo.usp_Prop_ListarRecetas(?,?)}", {
            it.setInt(1, propietarioId)
            it.setInt(2, consultaId)
        }) { rs ->
            RecetaDto(
                rs.getInt("Id"), rs.getInt("ConsultaId"), rs.texto("Medicamento"),
                rs.texto("Dosis"), rs.texto("Frecuencia"), rs.texto("Duracion"),
                rs.texto("Instrucciones")
            )
        }

    // false si la cita no existe, no es suya o ya fue atendida (el SP lanza 50014)
    fun cancelarCita(propietarioId: Int, citaId: Int): Boolean =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spCancelarCitaPropietario(?,?)}").use { cs ->
                cs.setInt(1, citaId)
                cs.setInt(2, propietarioId)
                try {
                    cs.executeUpdate()
                    true
                } catch (e: SQLException) {
                    if (e.errorCode == 50014) false else throw e
                }
            }
        }

    private fun <T> ejecutar(
        llamada: String,
        parametros: (CallableStatement) -> Unit,
        mapear: (ResultSet) -> T
    ): List<T> =
        Database.connect().use { conn ->
            conn.prepareCall(llamada).use { cs ->
                parametros(cs)
                cs.executeQuery().use { rs ->
                    val lista = mutableListOf<T>()
                    while (rs.next()) lista.add(mapear(rs))
                    lista
                }
            }
        }

    // Los textos NULL salen como ""
    private fun ResultSet.texto(columna: String): String = getString(columna) ?: ""
}