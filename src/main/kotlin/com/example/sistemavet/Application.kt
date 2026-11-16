package com.example.sistemavet

import com.example.sistemavet.modulo.ConsultasPropRepo
import com.example.sistemavet.modulo.notificacionRoutes
import com.example.sistemavet.modulo.usuarioRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import com.example.sistemavet.modulo.citaRoutes
import com.example.sistemavet.modulo.mascotaRoutes
import com.example.sistemavet.modulo.rutasConsultasProp
import com.example.sistemavet.modulo.citasVetRoutes
import com.example.sistemavet.modulo.expedienteVetRoutes




fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(
        Netty,
        port = port,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Como la app manda cosas de mas se ignorarn
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    routing {
        get("/") {
            call.respondText("API SistemaVet funcionando")
        }

        // Prueba de conexión a Azure SQL: abre http://localhost:8080/health
        get("/health") {
            try {
                val tablas = withContext(Dispatchers.IO) {
                    Database.connect().use { conn ->
                        conn.createStatement().use { st ->
                            val rs = st.executeQuery("SELECT COUNT(*) FROM sys.tables")
                            rs.next()
                            rs.getInt(1)
                        }
                    }
                }
                call.respondText("Conectado a Azure SQL. Tablas encontradas: $tablas")
            } catch (e: Exception) {
                call.respondText(
                    "Error de conexion: ${e.message}",
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        // Login, activacion de cuentas
        usuarioRoutes()
        // notificaciones las obtiene y si las leyo
        notificacionRoutes()
        // Citas de propietario
        citaRoutes()
        // Registro de mascotas
        mascotaRoutes()
        // Consultas de propietario
       rutasConsultasProp()
        // Citas veterinario
        citasVetRoutes()
        // Expedientes veterinario
        expedienteVetRoutes()

    }
}
