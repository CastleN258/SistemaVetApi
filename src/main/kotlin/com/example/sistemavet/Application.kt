package com.example.sistemavet

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun main() { // A la hora de correr presionen edit y lo corren de ahi con las credenciales de la base de deatos.
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
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
    }
}
