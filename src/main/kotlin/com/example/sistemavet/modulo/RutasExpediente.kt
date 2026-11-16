package com.example.sistemavet.modulo

import com.example.sistemavet.datos.AlergiaRequest
import com.example.sistemavet.datos.IdCreado
import com.example.sistemavet.datos.RecetaRequest
import com.example.sistemavet.datos.VacunaRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val FORMATO_FECHA_EXP = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun Route.expedienteVetRoutes() {
    route("/api/veterinario/{id}") {

        get("/mascotas") {
            val vetId = call.paramInt("id") ?: return@get
            try {
                call.respond(withContext(Dispatchers.IO) { ExpedienteVetRepo.mascotas(vetId) })
            } catch (e: SQLException) { call.errorSql(e) }
        }

        // Atenciones de una mascota (?mascotaId=) o de todas
        get("/atenciones") {
            val vetId = call.paramInt("id") ?: return@get
            val mascotaId = call.request.queryParameters["mascotaId"]?.toIntOrNull()
            try {
                call.respond(withContext(Dispatchers.IO) { ExpedienteVetRepo.atenciones(vetId, mascotaId) })
            } catch (e: SQLException) { call.errorSql(e) }
        }

        get("/mascotas/{mascotaId}/vacunas") {
            val vetId = call.paramInt("id") ?: return@get
            val mascotaId = call.paramInt("mascotaId") ?: return@get
            try {
                call.respond(withContext(Dispatchers.IO) { ExpedienteVetRepo.vacunas(vetId, mascotaId) })
            } catch (e: SQLException) { call.errorSql(e) }
        }

        get("/mascotas/{mascotaId}/alergias") {
            val vetId = call.paramInt("id") ?: return@get
            val mascotaId = call.paramInt("mascotaId") ?: return@get
            try {
                call.respond(withContext(Dispatchers.IO) { ExpedienteVetRepo.alergias(vetId, mascotaId) })
            } catch (e: SQLException) { call.errorSql(e) }
        }

        get("/mascotas/{mascotaId}/recetas") {
            val vetId = call.paramInt("id") ?: return@get
            val mascotaId = call.paramInt("mascotaId") ?: return@get
            try {
                call.respond(withContext(Dispatchers.IO) { ExpedienteVetRepo.recetas(vetId, mascotaId) })
            } catch (e: SQLException) { call.errorSql(e) }
        }

        // Consultas de este veterinario (para elegir sobre cuál emitir una receta)
        get("/consultas") {
            val vetId = call.paramInt("id") ?: return@get
            try {
                call.respond(withContext(Dispatchers.IO) { ExpedienteVetRepo.consultas(vetId) })
            } catch (e: SQLException) { call.errorSql(e) }
        }

        // Body: { mascotaId, nombre, fechaAplicacion, fechaProximaDosis } con fechas dd/MM/yyyy
        post("/vacunas") {
            val vetId = call.paramInt("id") ?: return@post
            val b = call.receive<VacunaRequest>()
            if (b.nombre.isBlank()) {
                call.respondText("El nombre de la vacuna es obligatorio.", status = HttpStatusCode.BadRequest)
                return@post
            }
            try {
                val aplicacion = LocalDate.parse(b.fechaAplicacion, FORMATO_FECHA_EXP)
                val proxima = LocalDate.parse(b.fechaProximaDosis, FORMATO_FECHA_EXP)
                val id = withContext(Dispatchers.IO) { ExpedienteVetRepo.registrarVacuna(vetId, b, aplicacion, proxima) }
                call.respond(HttpStatusCode.Created, IdCreado(id))
            } catch (e: DateTimeParseException) {
                call.respondText("Fecha inválida (use dd/MM/yyyy).", status = HttpStatusCode.BadRequest)
            } catch (e: SQLException) { call.errorSql(e) }
        }

        // Body: { mascotaId, nombre, observaciones?, consultaId? }
        post("/alergias") {
            val vetId = call.paramInt("id") ?: return@post
            val b = call.receive<AlergiaRequest>()
            if (b.nombre.isBlank()) {
                call.respondText("El nombre de la alergia es obligatorio.", status = HttpStatusCode.BadRequest)
                return@post
            }
            try {
                val id = withContext(Dispatchers.IO) { ExpedienteVetRepo.registrarAlergia(vetId, b) }
                call.respond(HttpStatusCode.Created, IdCreado(id))
            } catch (e: SQLException) { call.errorSql(e) }
        }

        // Body: { consultaId, medicamento, dosis, frecuencia, duracion, instrucciones? }
        post("/recetas") {
            val vetId = call.paramInt("id") ?: return@post
            val b = call.receive<RecetaRequest>()
            if (b.medicamento.isBlank() || b.dosis.isBlank() || b.frecuencia.isBlank() || b.duracion.isBlank()) {
                call.respondText("Medicamento, dosis, frecuencia y duración son obligatorios.", status = HttpStatusCode.BadRequest)
                return@post
            }
            try {
                val id = withContext(Dispatchers.IO) { ExpedienteVetRepo.registrarReceta(vetId, b) }
                call.respond(HttpStatusCode.Created, IdCreado(id))
            } catch (e: SQLException) { call.errorSql(e) }
        }
    }
}