package com.example.sistemavet.modulo

import com.example.sistemavet.datos.RegistroLogin
import com.example.sistemavet.datos.RegistroRequest
import com.example.sistemavet.datos.UsuarioSesion
import io.ktor.http.ContentType
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
import org.mindrot.jbcrypt.BCrypt
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID


// Validaciones para el correo y contraseña
private val REGEX_CONTRASENA = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$")
private val REGEX_CORREO = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")


fun Route.usuarioRoutes() {
    route("/api/usuarios") {

        // POST /api/usuarios/registrar
        post("/registrar") {
            val r = call.receive<RegistroRequest>()

            if (r.nombre.isBlank() || r.ide.isBlank() || r.telefono.isBlank()) {
                call.respondText("Complete todos los campos.", status = HttpStatusCode.BadRequest)
                return@post
            }
            if (!REGEX_CORREO.matches(r.correo.trim())) {
                call.respondText("El correo no tiene un formato válido.", status = HttpStatusCode.BadRequest)
                return@post
            }
            if (!REGEX_CONTRASENA.matches(r.contrasena)) {
                call.respondText(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un símbolo.",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            val token = UUID.randomUUID().toString().replace("-", "")
            // El token se guarda en UTC y vence en 24 horas
            val expira = Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusHours(24))

            try {
                val id = withContext(Dispatchers.IO) {
                    val hash = BCrypt.hashpw(r.contrasena, BCrypt.gensalt(10))
                    RepositorioUsuarios.registrarPropietario(
                        r.copy(correo = r.correo.trim()), hash, token, expira
                    )
                }

                // Se imprime y activa a mano
                println("[ACTIVACION] ${r.correo.trim()} -> http://localhost:8080/api/usuarios/activar?token=$token")

                call.respond(
                    HttpStatusCode.Created,
                    UsuarioSesion(id, r.nombre, r.correo.trim(), "PROPIETARIO", false, null)
                )
            } catch (e: SQLException) {
                when (e.errorCode) {
                    50001, 50002 -> call.respondText(
                        e.message ?: "El dato ya está registrado.",
                        status = HttpStatusCode.Conflict
                    )
                    else -> {
                        println("Error en /registrar: ${e.message}")
                        call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
                    }
                }
            }
        }

        // POST /api/usuarios/login
        post("/login") {
            val r = call.receive<RegistroLogin>()
            val mensajeGenerico = "Correo o contraseña incorrectos."   // el mismo para correo malo y contraseña mala

            try {
                val usuario = withContext(Dispatchers.IO) { RepositorioUsuarios.obtenerPorCorreo(r.correo.trim()) }

                val contrasenaOk = usuario != null && withContext(Dispatchers.IO) {
                    try {
                        BCrypt.checkpw(r.contrasena, usuario.contrasenaHash)
                    } catch (e: IllegalArgumentException) {
                        false
                    }
                }

                if (usuario == null || !contrasenaOk) {
                    call.respondText(mensajeGenerico, status = HttpStatusCode.Unauthorized)
                    return@post
                }
                if (!usuario.cuentaActivada) {
                    call.respondText(
                        "Debe activar su cuenta con el enlace enviado a su correo.",
                        status = HttpStatusCode.Forbidden
                    )
                    return@post
                }

                call.respond(
                    UsuarioSesion(
                        usuario.id, usuario.nombreCompleto, usuario.correo,
                        usuario.rol, usuario.cuentaActivada, null   // token JWT: pendiente
                    )
                )
            } catch (e: SQLException) {
                println("Error en /login: ${e.message}")
                call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
            }
        }

        // GET /api/usuarios/activar?token=...   (el enlace del correo de verificación)
        get("/activar") {
            val token = call.request.queryParameters["token"]
            if (token.isNullOrBlank()) {
                call.respondText("Falta el token.", status = HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val activada = withContext(Dispatchers.IO) { RepositorioUsuarios.activarCuenta(token) }
                if (activada) {
                    call.respondText(
                        "<h2>Cuenta activada</h2><p>Ya puede iniciar sesión en la aplicación.</p>",
                        ContentType.Text.Html
                    )
                } else {
                    call.respondText(
                        "<h2>Enlace inválido o vencido</h2>",
                        ContentType.Text.Html, HttpStatusCode.BadRequest
                    )
                }
            } catch (e: SQLException) {
                println("Error en /activar: ${e.message}")
                call.respondText("Error del servidor.", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}