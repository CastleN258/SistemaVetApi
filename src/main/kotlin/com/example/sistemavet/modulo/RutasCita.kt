package com.example.sistemavet.modulo

import com.example.sistemavet.datos.CitaCreada
import com.example.sistemavet.datos.SolicitudCitaRequest
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private val FORMATO_FECHA =
    DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

fun Route.citaRoutes() {

    // lista de mascota
    get("/api/mascotas/propietario/{id}/opciones") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respondText("Id inválido.", status = HttpStatusCode.BadRequest)
            return@get
        }
        try {
            call.respond(withContext(Dispatchers.IO) { CitaRepo.mascotasDe(id) })
        } catch (e: SQLException) {
            println("Error en mascotas/opciones: ${e.message}")
            call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
        }
    }

    // lista de veterinarios
    get("/api/veterinarios") {
        try {
            call.respond(withContext(Dispatchers.IO) { CitaRepo.veterinarios() })
        } catch (e: SQLException) {
            println("Error en veterinarios: ${e.message}")
            call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
        }
    }

    route("/api/citas") {

        // obtener horarios
        get("/horarios") {
            val vetId = call.request.queryParameters["veterinarioId"]?.toIntOrNull()
            val fecha = try {
                LocalDate.parse(call.request.queryParameters["fecha"] ?: "", FORMATO_FECHA)
            } catch (e: Exception) {
                null
            }
            if (vetId == null || fecha == null) {
                call.respondText("Veterinario o fecha inválidos.", status = HttpStatusCode.BadRequest)
                return@get
            }
            try {
                call.respond(withContext(Dispatchers.IO) { CitaRepo.horarios(vetId, fecha) })
            } catch (e: SQLException) {
                println("Error en citas/horarios: ${e.message}")
                call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
            }
        }

        // POST llamar a solicitar
        post("/solicitar") {
            val r = call.receive<SolicitudCitaRequest>()
            val fecha = try { LocalDate.parse(r.fecha, FORMATO_FECHA) } catch (e: Exception) { null }
            val hora = try { LocalTime.parse(r.hora) } catch (e: Exception) { null }

            if (fecha == null || hora == null || r.motivo.isBlank()) {
                call.respondText("Datos de la cita inválidos.", status = HttpStatusCode.BadRequest)
                return@post
            }
            try {
                val id = withContext(Dispatchers.IO) { CitaRepo.solicitar(r, fecha, hora) }
                call.respond(HttpStatusCode.Created, CitaCreada(id))
            } catch (e: SQLException) {
                // Los códigos 5001x vienen de los THROW de spSolicitarCita
                val estado = when (e.errorCode) {
                    50010, 50011 -> HttpStatusCode.BadRequest
                    50012 -> HttpStatusCode.Conflict
                    50013 -> HttpStatusCode.Forbidden
                    else -> HttpStatusCode.InternalServerError
                }
                if (estado == HttpStatusCode.InternalServerError) {
                    println("Error en citas/solicitar: ${e.message}")
                    call.respondText("Error del servidor.", status = estado)
                } else {
                    call.respondText(e.message ?: "No se pudo registrar la cita.", status = estado)
                }
            }
        }
    }
}

