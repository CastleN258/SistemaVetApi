package com.example.sistemavet.modulo

import com.example.sistemavet.datos.CambioEstadoRequest
import com.example.sistemavet.datos.CompletarCitaRequest
import com.example.sistemavet.datos.IdCreado
import com.example.sistemavet.datos.ReprogramarRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val FORMATO_FECHA_VET = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun Route.citasVetRoutes() {
    route("/api/veterinario/{id}/citas") {

        get {
            val vetId = call.paramInt("id") ?: return@get
            val estado = call.request.queryParameters["estado"]?.uppercase()
            try {
                call.respond(withContext(Dispatchers.IO) { CitaVetRepo.listar(vetId, estado) })
            } catch (e: SQLException) {
                call.errorSql(e)
            }
        }

        // Body: { "estado": "CONFIRMADA" } o { "estado": "CANCELADA" }
        post("/{citaId}/estado") {
            val vetId = call.paramInt("id") ?: return@post
            val citaId = call.paramInt("citaId") ?: return@post
            val body = call.receive<CambioEstadoRequest>()
            val nuevo = body.estado.uppercase()
            if (nuevo != "CONFIRMADA" && nuevo != "CANCELADA") {
                call.respondText("Estado inválido. Use CONFIRMADA o CANCELADA.", status = HttpStatusCode.BadRequest)
                return@post
            }
            try {
                withContext(Dispatchers.IO) { CitaVetRepo.cambiarEstado(citaId, vetId, nuevo) }
                call.respondText("Cita actualizada.")
            } catch (e: SQLException) {
                call.errorSql(e)
            }
        }

        // Body: { "fecha": "25/11/2026", "hora": "09:00" }
        post("/{citaId}/reprogramar") {
            val vetId = call.paramInt("id") ?: return@post
            val citaId = call.paramInt("citaId") ?: return@post
            val body = call.receive<ReprogramarRequest>()
            try {
                val fecha = LocalDate.parse(body.fecha, FORMATO_FECHA_VET)
                val hora = LocalTime.parse(body.hora)
                withContext(Dispatchers.IO) { CitaVetRepo.reprogramar(citaId, vetId, fecha, hora) }
                call.respondText("Cita reprogramada.")
            } catch (e: DateTimeParseException) {
                call.respondText("Fecha u hora inválida (use dd/MM/yyyy y HH:mm).", status = HttpStatusCode.BadRequest)
            } catch (e: SQLException) {
                call.errorSql(e)
            }
        }

        post("/{citaId}/completar") {
            val vetId = call.paramInt("id") ?: return@post
            val citaId = call.paramInt("citaId") ?: return@post
            val b = call.receive<CompletarCitaRequest>()
            if (b.motivo.isBlank() || b.diagnostico.isBlank() || b.tratamiento.isBlank() || b.peso <= 0) {
                call.respondText("Motivo, diagnóstico, tratamiento y peso son obligatorios.", status = HttpStatusCode.BadRequest)
                return@post
            }
            try {
                val fecha = LocalDate.parse(b.fechaAtencion, FORMATO_FECHA_VET)
                val id = withContext(Dispatchers.IO) {
                    CitaVetRepo.completar(citaId, vetId, fecha, b.motivo, b.diagnostico, b.tratamiento, b.peso, b.observaciones)
                }
                call.respond(HttpStatusCode.Created, IdCreado(id))
            } catch (e: DateTimeParseException) {
                call.respondText("Fecha inválida (use dd/MM/yyyy).", status = HttpStatusCode.BadRequest)
            } catch (e: SQLException) {
                call.errorSql(e)
            }
        }
    }
}

// 50010 = no es veterinario; 50014/50020/50030 = no existe; 50011/50012/50015 = regla de negocio
suspend fun ApplicationCall.errorSql(e: SQLException) {
    val (codigo, mensaje) = when (e.errorCode) {
        50010 -> HttpStatusCode.Forbidden to (e.message ?: "No autorizado.")
        50014, 50020, 50030 -> HttpStatusCode.NotFound to (e.message ?: "No encontrado.")
        50011, 50012, 50015 -> HttpStatusCode.Conflict to (e.message ?: "Operación no permitida.")
        else -> {
            println("Error SQL veterinario: ${e.message}")
            HttpStatusCode.InternalServerError to "Error del servidor."
        }
    }
    respondText(mensaje, status = codigo)
}

suspend fun ApplicationCall.paramInt(nombre: String): Int? {
    val valor = parameters[nombre]?.toIntOrNull()
    if (valor == null) respondText("Parámetro inválido: $nombre", status = HttpStatusCode.BadRequest)
    return valor
}

