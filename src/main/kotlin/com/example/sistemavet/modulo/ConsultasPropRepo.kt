package com.example.sistemavetapi // ← ajusten al package real de la API

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

// Acceso a datos de las consultas del propietario (solo lectura).
// Todo pasa por los procedimientos de ScriptBD/SPConsultasProp.sql, y cada uno
// recibe el @PropietarioId para que nunca devuelva datos de otro propietario.
//
class ConsultasPropRepo(
    private val conexion: () -> Connection,
) {
    fun mascotas(propietarioId: Int): List<MascotaDto> =
        ejecutar("{call dbo.usp_Prop_ListarMascotas(?)}", { it.setInt(1, propietarioId) }) { rs ->
            MascotaDto(
                id = rs.getInt("Id"),
                propietarioId = rs.getInt("PropietarioId"),
                nombre = rs.texto("Nombre"),
                especie = rs.texto("Especie"),
                raza = rs.texto("Raza"),
                sexo = rs.texto("Sexo"),
                fechaNacimiento = rs.texto("FechaNacimiento"),
                pesoAproximado = rs.getDouble("PesoAproximado"),
            )
        }

    fun citas(
        propietarioId: Int,
        mascotaId: Int?,
        estadoVista: String?,
    ): List<CitaDto> =
        ejecutar("{call dbo.usp_Prop_ListarCitas(?, ?, ?)}", {
            it.setInt(1, propietarioId)
            it.setIntONull(2, mascotaId)
            it.setStringONull(3, estadoVista)
        }) { rs ->
            CitaDto(
                id = rs.getInt("Id"),
                mascotaId = rs.getInt("MascotaId"),
                mascota = rs.texto("Mascota"),
                veterinarioId = rs.getInt("VeterinarioId"),
                veterinario = rs.texto("Veterinario"),
                fecha = rs.texto("Fecha"),
                hora = rs.texto("Hora"),
                motivo = rs.texto("Motivo"),
                estado = rs.texto("Estado"),
                estadoVista = rs.texto("EstadoVista"),
            )
        }

    fun atenciones(
        propietarioId: Int,
        mascotaId: Int?,
    ): List<AtencionDto> =
        ejecutar("{call dbo.usp_Prop_ListarAtenciones(?, ?)}", {
            it.setInt(1, propietarioId)
            it.setIntONull(2, mascotaId)
        }) { rs ->
            AtencionDto(
                id = rs.getInt("Id"),
                citaId = rs.getInt("CitaId"),
                mascotaId = rs.getInt("MascotaId"),
                mascota = rs.texto("Mascota"),
                veterinarioId = rs.getInt("VeterinarioId"),
                veterinario = rs.texto("Veterinario"),
                fechaAtencion = rs.texto("FechaAtencion"),
                hora = rs.texto("Hora"),
                motivo = rs.texto("Motivo"),
                diagnostico = rs.texto("Diagnostico"),
                tratamiento = rs.texto("Tratamiento"),
                peso = rs.getDouble("Peso"),
                observaciones = rs.texto("Observaciones"),
            )
        }

    fun alergias(
        propietarioId: Int,
        mascotaId: Int,
    ): List<AlergiaDto> =
        ejecutar("{call dbo.usp_Prop_ListarAlergias(?, ?)}", {
            it.setInt(1, propietarioId)
            it.setInt(2, mascotaId)
        }) { rs ->
            AlergiaDto(
                id = rs.getInt("Id"),
                mascotaId = rs.getInt("MascotaId"),
                nombre = rs.texto("Nombre"),
                observaciones = rs.texto("Observaciones"),
            )
        }

    fun vacunas(
        propietarioId: Int,
        mascotaId: Int,
    ): List<VacunaDto> =
        ejecutar("{call dbo.usp_Prop_ListarVacunas(?, ?)}", {
            it.setInt(1, propietarioId)
            it.setInt(2, mascotaId)
        }) { rs ->
            VacunaDto(
                id = rs.getInt("Id"),
                mascotaId = rs.getInt("MascotaId"),
                nombre = rs.texto("Nombre"),
                fechaAplicacion = rs.texto("FechaAplicacion"),
                fechaProximaDosis = rs.texto("FechaProximaDosis"),
            )
        }

    fun recetas(
        propietarioId: Int,
        consultaId: Int,
    ): List<RecetaDto> =
        ejecutar("{call dbo.usp_Prop_ListarRecetas(?, ?)}", {
            it.setInt(1, propietarioId)
            it.setInt(2, consultaId)
        }) { rs ->
            RecetaDto(
                id = rs.getInt("Id"),
                consultaId = rs.getInt("ConsultaId"),
                medicamento = rs.texto("Medicamento"),
                dosis = rs.texto("Dosis"),
                frecuencia = rs.texto("Frecuencia"),
                duracion = rs.texto("Duracion"),
                instrucciones = rs.texto("Instrucciones"),
            )
        }

    // Ejecuta un procedimiento y convierte cada fila con "mapear".
    // el .use cierra la conexión al terminar. Si "conexion()" devuelve una
    // conexión compartida y no una nueva (o una del pool), quitar ese .use.
    private fun <T> ejecutar(
        llamada: String,
        parametros: (CallableStatement) -> Unit,
        mapear: (ResultSet) -> T,
    ): List<T> =
        conexion().use { cn ->
            cn.prepareCall(llamada).use { cs ->
                parametros(cs)
                cs.executeQuery().use { rs ->
                    val filas = mutableListOf<T>()
                    while (rs.next()) filas += mapear(rs)
                    filas
                }
            }
        }

    // Los textos que pueden venir NULL salen como "" para no romper los DTO
    private fun ResultSet.texto(columna: String): String = getString(columna) ?: ""

    private fun CallableStatement.setIntONull(
        indice: Int,
        valor: Int?,
    ) {
        if (valor == null) setNull(indice, Types.INTEGER) else setInt(indice, valor)
    }

    private fun CallableStatement.setStringONull(
        indice: Int,
        valor: String?,
    ) {
        if (valor == null) setNull(indice, Types.NVARCHAR) else setString(indice, valor)
    }
}
