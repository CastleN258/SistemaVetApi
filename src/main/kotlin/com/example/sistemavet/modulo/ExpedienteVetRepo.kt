package com.example.sistemavet.modulo

import com.example.sistemavet.Database
import com.example.sistemavet.datos.AlergiaDto
import com.example.sistemavet.datos.AlergiaRequest
import com.example.sistemavet.datos.AtencionDto
import com.example.sistemavet.datos.ConsultaVetDto
import com.example.sistemavet.datos.MascotaVetDto
import com.example.sistemavet.datos.RecetaDto
import com.example.sistemavet.datos.RecetaRequest
import com.example.sistemavet.datos.VacunaDto
import com.example.sistemavet.datos.VacunaRequest
import java.sql.CallableStatement
import java.sql.Date
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDate

// Lecturas por usp_Vet_* y escrituras por spRegistrar* (ya validan que sea veterinario)
object ExpedienteVetRepo {

    fun mascotas(vetId: Int): List<MascotaVetDto> =
        consultar("{call dbo.usp_Vet_ListarMascotas(?)}", { it.setInt(1, vetId) }) { rs ->
            MascotaVetDto(
                rs.getInt("Id"), rs.getInt("PropietarioId"), rs.texto("Propietario"),
                rs.texto("Nombre"), rs.texto("Especie"), rs.texto("Raza"), rs.texto("Sexo"),
                rs.texto("FechaNacimiento"), rs.getDouble("PesoAproximado")
            )
        }

    fun atenciones(vetId: Int, mascotaId: Int?): List<AtencionDto> =
        consultar("{call dbo.usp_Vet_ListarAtenciones(?,?)}", {
            it.setInt(1, vetId)
            if (mascotaId == null) it.setNull(2, Types.INTEGER) else it.setInt(2, mascotaId)
        }) { rs ->
            AtencionDto(
                rs.getInt("Id"), rs.getInt("CitaId"), rs.getInt("MascotaId"), rs.texto("Mascota"),
                rs.getInt("VeterinarioId"), rs.texto("Veterinario"), rs.texto("FechaAtencion"),
                rs.texto("Hora"), rs.texto("Motivo"), rs.texto("Diagnostico"),
                rs.texto("Tratamiento"), rs.getDouble("Peso"), rs.texto("Observaciones")
            )
        }

    fun vacunas(vetId: Int, mascotaId: Int): List<VacunaDto> =
        consultar("{call dbo.usp_Vet_ListarVacunas(?,?)}", {
            it.setInt(1, vetId)
            it.setInt(2, mascotaId)
        }) { rs ->
            VacunaDto(
                rs.getInt("Id"), rs.getInt("MascotaId"), rs.texto("Nombre"),
                rs.texto("FechaAplicacion"), rs.texto("FechaProximaDosis")
            )
        }

    fun alergias(vetId: Int, mascotaId: Int): List<AlergiaDto> =
        consultar("{call dbo.usp_Vet_ListarAlergias(?,?)}", {
            it.setInt(1, vetId)
            it.setInt(2, mascotaId)
        }) { rs ->
            AlergiaDto(rs.getInt("Id"), rs.getInt("MascotaId"), rs.texto("Nombre"), rs.texto("Observaciones"))
        }

    fun recetas(vetId: Int, mascotaId: Int): List<RecetaDto> =
        consultar("{call dbo.usp_Vet_ListarRecetas(?,?)}", {
            it.setInt(1, vetId)
            it.setInt(2, mascotaId)
        }) { rs ->
            RecetaDto(
                rs.getInt("Id"), rs.getInt("ConsultaId"), rs.texto("Medicamento"),
                rs.texto("Dosis"), rs.texto("Frecuencia"), rs.texto("Duracion"),
                rs.texto("Instrucciones")
            )
        }

    fun consultas(vetId: Int): List<ConsultaVetDto> =
        consultar("{call dbo.usp_Vet_ListarConsultas(?)}", { it.setInt(1, vetId) }) { rs ->
            ConsultaVetDto(
                rs.getInt("Id"), rs.getInt("MascotaId"), rs.texto("Mascota"),
                rs.texto("Propietario"), rs.texto("FechaAtencion")
            )
        }

    fun registrarVacuna(vetId: Int, r: VacunaRequest, aplicacion: LocalDate, proxima: LocalDate): Int =
        insertar("{call dbo.spRegistrarVacuna(?,?,?,?,?)}") { cs ->
            cs.setInt(1, vetId)
            cs.setInt(2, r.mascotaId)
            cs.setString(3, r.nombre.trim())
            cs.setDate(4, Date.valueOf(aplicacion))
            cs.setDate(5, Date.valueOf(proxima))
        }

    fun registrarAlergia(vetId: Int, r: AlergiaRequest): Int =
        insertar("{call dbo.spRegistrarAlergia(?,?,?,?,?)}") { cs ->
            cs.setInt(1, vetId)
            cs.setInt(2, r.mascotaId)
            cs.setString(3, r.nombre.trim())
            if (r.observaciones.isNullOrBlank()) cs.setNull(4, Types.NVARCHAR) else cs.setString(4, r.observaciones)
            if (r.consultaId == null) cs.setNull(5, Types.INTEGER) else cs.setInt(5, r.consultaId)
        }

    fun registrarReceta(vetId: Int, r: RecetaRequest): Int =
        insertar("{call dbo.spRegistrarReceta(?,?,?,?,?,?,?)}") { cs ->
            cs.setInt(1, vetId)
            cs.setInt(2, r.consultaId)
            cs.setString(3, r.medicamento.trim())
            cs.setString(4, r.dosis.trim())
            cs.setString(5, r.frecuencia.trim())
            cs.setString(6, r.duracion.trim())
            if (r.instrucciones.isNullOrBlank()) cs.setNull(7, Types.NVARCHAR) else cs.setString(7, r.instrucciones)
        }

    // Ejecuta un SP de lectura y convierte cada fila
    private fun <T> consultar(
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

    // Ejecuta un SP de registro que devuelve "SELECT Id" y entrega ese id
    private fun insertar(llamada: String, parametros: (CallableStatement) -> Unit): Int =
        Database.connect().use { conn ->
            conn.prepareCall(llamada).use { cs ->
                parametros(cs)
                cs.executeQuery().use { rs ->
                    rs.next()
                    rs.getInt("Id")
                }
            }
        }

    private fun ResultSet.texto(columna: String): String = getString(columna) ?: ""
}

