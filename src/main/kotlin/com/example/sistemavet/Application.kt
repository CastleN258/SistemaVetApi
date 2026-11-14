package com.example.sistemavet

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

fun main() { // A la hora de correr presionen edit y lo corren de ahi con las credenciales de la base de datos.
    // Si no te dicen
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
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

    }
}
