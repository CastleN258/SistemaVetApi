package com.example.sistemavetapi // ← ajusten al package real de la API

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// PENDIENTE: por ahora el id del propietario viaja en la ruta porque todavía no hay
// sesión. Cuando exista el login con token, el id debe salir del token y no de la
// URL (RNF-09); si no, cualquiera podría pedir los datos de otro propietario.
fun Route.rutasConsultasProp(repo: ConsultasPropRepo) {
    route("/propietario/{id}") {
        get("/mascotas") {
            val id = call.entero("id") ?: return@get
            call.respond(bd { repo.mascotas(id) })
        }

        get("/citas") {
            val id = call.entero("id") ?: return@get
            val mascotaId = call.request.queryParameters["mascotaId"]?.toIntOrNull()
            val estado = call.request.queryParameters["estado"]
            call.respond(bd { repo.citas(id, mascotaId, estado) })
        }

        get("/atenciones") {
            val id = call.entero("id") ?: return@get
            val mascotaId = call.request.queryParameters["mascotaId"]?.toIntOrNull()
            call.respond(bd { repo.atenciones(id, mascotaId) })
        }

        get("/mascotas/{mascotaId}/alergias") {
            val id = call.entero("id") ?: return@get
            val mascotaId = call.entero("mascotaId") ?: return@get
            call.respond(bd { repo.alergias(id, mascotaId) })
        }

        get("/mascotas/{mascotaId}/vacunas") {
            val id = call.entero("id") ?: return@get
            val mascotaId = call.entero("mascotaId") ?: return@get
            call.respond(bd { repo.vacunas(id, mascotaId) })
        }

        get("/consultas/{consultaId}/recetas") {
            val id = call.entero("id") ?: return@get
            val consultaId = call.entero("consultaId") ?: return@get
            call.respond(bd { repo.recetas(id, consultaId) })
        }
    }
}

// JDBC bloquea el hilo, así que se ejecuta fuera del hilo principal de Ktor
private suspend fun <T> bd(bloque: () -> T): T = withContext(Dispatchers.IO) { bloque() }

// Lee un parámetro de la ruta como entero; si no lo es, responde 400 y devuelve null
private suspend fun ApplicationCall.entero(nombre: String): Int? {
    val valor = parameters[nombre]?.toIntOrNull()
    if (valor == null) {
        respondText("Parámetro inválido: $nombre", status = HttpStatusCode.BadRequest)
    }
    return valor
}
