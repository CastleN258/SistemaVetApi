package com.example.sistemavet.modulo

import com.example.sistemavet.datos.MascotaCreada
import com.example.sistemavet.datos.MascotaRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private val FORMATO_NAC =
    DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

fun Route.mascotaRoutes() {

    // POST de registrar mascota
    post("/api/mascotas/registrar") {
        val r = call.receive<MascotaRequest>()
        val nombre = r.nombre.trim()
        val especie = r.especie.trim()
        val raza = r.raza.trim()
        val sexo = r.sexo.trim().uppercase()
        val fechaNac = try { LocalDate.parse(r.fechaNacimiento.trim(), FORMATO_NAC) } catch (e: Exception) { null }

        // Los límites coinciden con las columnas de la tabla Mascotas
        val error = when {
            nombre.isEmpty() || especie.isEmpty() || raza.isEmpty() -> "Complete todos los campos."
            nombre.length > 60 || especie.length > 40 || raza.length > 60 ->
                "El nombre, la especie o la raza son demasiado largos."
            sexo != "MACHO" && sexo != "HEMBRA" -> "El sexo debe ser MACHO o HEMBRA."
            fechaNac == null -> "La fecha de nacimiento debe tener formato dd/mm/aaaa."
            r.pesoAproximado <= 0 || r.pesoAproximado >= 1000 -> "El peso debe ser mayor a 0 y menor a 1000 kg."
            else -> null
        }
        if (error != null || fechaNac == null) {
            call.respondText(error ?: "Datos inválidos.", status = HttpStatusCode.BadRequest)
            return@post
        }

        try {
            val id = withContext(Dispatchers.IO) {
                MascotaRepo.registrar(r.copy(nombre = nombre, especie = especie, raza = raza, sexo = sexo), fechaNac)
            }
            call.respond(
                HttpStatusCode.Created,
                MascotaCreada(id, r.propietarioId, nombre, especie, raza, sexo,
                    r.fechaNacimiento.trim(), r.pesoAproximado)
            )
        } catch (e: SQLException) {
            // 50010 = propietario inexistente o sin activar, 50011 = fecha futura (vienen del SP)
            when (e.errorCode) {
                50010 -> call.respondText(e.message ?: "Propietario no válido.", status = HttpStatusCode.Forbidden)
                50011 -> call.respondText(e.message ?: "Fecha inválida.", status = HttpStatusCode.BadRequest)
                else -> {
                    println("Error en /mascotas/registrar: ${e.message}")
                    call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

