plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    application
}

group = "com.example.sistemavet"
version = "0.0.1"

application {
    mainClass.set("com.example.sistemavet.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:3.6.1")
    implementation("io.ktor:ktor-server-netty:3.6.1")
    implementation("io.ktor:ktor-server-content-negotiation:3.6.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.6.1")
    implementation("com.microsoft.sqlserver:mssql-jdbc:13.4.0.jre11")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("org.mindrot:jbcrypt:0.4")
}
