package com.example.sistemavet.modulo

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException

fun Route.rutasConsultasProp() {
    route("/api/propietario/{id}") {

        get("/mascotas") {
            val id = call.entero("id") ?: return@get
            call.consultar { ConsultasPropRepo.mascotas(id) }
        }

        get("/citas") {
            val id = call.entero("id") ?: return@get
            val mascotaId = call.request.queryParameters["mascotaId"]?.toIntOrNull()
            val estado = call.request.queryParameters["estado"]
            call.consultar { ConsultasPropRepo.citas(id, mascotaId, estado) }
        }

        get("/atenciones") {
            val id = call.entero("id") ?: return@get
            val mascotaId = call.request.queryParameters["mascotaId"]?.toIntOrNull()
            call.consultar { ConsultasPropRepo.atenciones(id, mascotaId) }
        }

        get("/mascotas/{mascotaId}/alergias") {
            val id = call.entero("id") ?: return@get
            val mascotaId = call.entero("mascotaId") ?: return@get
            call.consultar { ConsultasPropRepo.alergias(id, mascotaId) }
        }

        get("/mascotas/{mascotaId}/vacunas") {
            val id = call.entero("id") ?: return@get
            val mascotaId = call.entero("mascotaId") ?: return@get
            call.consultar { ConsultasPropRepo.vacunas(id, mascotaId) }
        }

        get("/consultas/{consultaId}/recetas") {
            val id = call.entero("id") ?: return@get
            val consultaId = call.entero("consultaId") ?: return@get
            call.consultar { ConsultasPropRepo.recetas(id, consultaId) }
        }

        post("/citas/{citaId}/cancelar") {
            val id = call.entero("id") ?: return@post
            val citaId = call.entero("citaId") ?: return@post
            try {
                val ok = withContext(Dispatchers.IO) { ConsultasPropRepo.cancelarCita(id, citaId) }
                if (ok) call.respondText("Cita cancelada.")
                else call.respondText(
                    "La cita no existe, no es suya o ya fue atendida.",
                    status = HttpStatusCode.Conflict
                )
            } catch (e: SQLException) {
                println("Error en cancelar cita: ${e.message}")
                call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}

// Corre la consulta fuera del hilo de Ktor y responde la lista; si falla la BD, 500
private suspend inline fun <reified T : Any> ApplicationCall.consultar(crossinline bloque: () -> T) {
    try {
        respond(withContext(Dispatchers.IO) { bloque() })
    } catch (e: SQLException) {
        println("Error en consultas propietario: ${e.message}")
        respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
    }
}

// Lee un parámetro de la ruta como entero; si no lo es, responde 400 y devuelve null
private suspend fun ApplicationCall.entero(nombre: String): Int? {
    val valor = parameters[nombre]?.toIntOrNull()
    if (valor == null) respondText("Parámetro inválido: $nombre", status = HttpStatusCode.BadRequest)
    return valor
}