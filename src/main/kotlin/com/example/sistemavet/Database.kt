package com.example.sistemavet

import java.sql.Connection
import java.sql.DriverManager

// Las credenciales NUNCA van en el código: se leen de variables de entorno.
object Database {
    private val server = System.getenv("DB_SERVER") ?: error("Falta la variable de entorno DB_SERVER")
    private val name = System.getenv("DB_NAME") ?: error("Falta la variable de entorno DB_NAME")
    private val user = System.getenv("DB_USER") ?: error("Falta la variable de entorno DB_USER")
    private val password = System.getenv("DB_PASSWORD") ?: error("Falta la variable de entorno DB_PASSWORD")

    // loginTimeout=90: da tiempo a que la base serverless despierte si estaba pausada
    private val url = "jdbc:sqlserver://$server:1433;" +
            "database=$name;" +
            "encrypt=true;" +
            "trustServerCertificate=false;" +
            "hostNameInCertificate=*.database.windows.net;" +
            "loginTimeout=90"

    fun connect(): Connection = DriverManager.getConnection(url, user, password)
}
