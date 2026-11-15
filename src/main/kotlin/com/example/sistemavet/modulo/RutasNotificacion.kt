package com.example.sistemavet.modulo

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException

fun Route.notificacionRoutes() {
    route("/api/notificaciones") {

        // obtenemos las notificaciones
        get("/{usuarioId}") {
            val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
            if (usuarioId == null) {
                call.respondText("Id inválido.", status = HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val lista = withContext(Dispatchers.IO) { Notificacionrepo.listar(usuarioId) }
                call.respond(lista)
            } catch (e: SQLException) {
                println("Error en GET notificaciones: ${e.message}")
                call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
            }
        }

        // Si se leyo la notificacion
        post("/{id}/leida") {
            val id = call.parameters["id"]?.toIntOrNull()
            val usuarioId = call.request.queryParameters["usuarioId"]?.toIntOrNull()
            if (id == null || usuarioId == null) {
                call.respondText("Datos inválidos.", status = HttpStatusCode.BadRequest)
                return@post
            }
            try {
                withContext(Dispatchers.IO) { Notificacionrepo.marcarLeida(id, usuarioId) }
                call.respondText("OK")
            } catch (e: SQLException) {
                println("Error en POST leida: ${e.message}")
                call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}