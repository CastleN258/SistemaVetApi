package com.example.sistemavet.modulo

import com.example.sistemavet.datos.RegistroRequest
import java.sql.Timestamp
import com.example.sistemavet.Database

// Usuarios en base de datos
data class BDUsuario(
    val id: Int,
    val nombreCompleto: String,
    val correo: String,
    val contrasenaHash: String,
    val rol: String,
    val cuentaActivada: Boolean
)
object RepositorioUsuarios{

    // Registra el propetario. Error 50001 si ya existe
    fun registrarPropietario(r: RegistroRequest, hash: String, token: String, expira: Timestamp): Int =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spRegistrarPropietario(?,?,?,?,?,?,?)}").use { cs ->
                cs.setString(1, r.nombre)
                cs.setString(2, r.ide)
                cs.setString(3, r.correo)
                cs.setString(4, r.telefono)
                cs.setString(5, hash)
                cs.setString(6, token)
                cs.setTimestamp(7, expira)
                cs.executeQuery().use { rs ->
                    rs.next()
                    rs.getInt("Id")
                }
            }
        }

    // Se obtiene el correo y si no es null
    fun obtenerPorCorreo(correo: String): BDUsuario? =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spObtenerUsuarioPorCorreo(?)}").use { cs ->
                cs.setString(1, correo)
                cs.executeQuery().use { rs ->
                    if (rs.next()) {
                        BDUsuario(
                            id = rs.getInt("Id"),
                            nombreCompleto = rs.getString("NombreCompleto"),
                            correo = rs.getString("Correo"),
                            contrasenaHash = rs.getString("ContrasenaHash"),
                            rol = rs.getString("Rol"),
                            cuentaActivada = rs.getBoolean("CuentaActivada")
                        )
                    } else {
                        null
                    }
                }
            }
        }
    // verdadero si activó la cuenta; falso si el token no sirve o venció.
    fun activarCuenta(token: String): Boolean =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spActivarCuenta(?)}").use { cs ->
                cs.setString(1, token)
                cs.executeQuery().use { rs ->
                    rs.next()
                    rs.getInt("Actualizados") > 0
                }
            }
        }



}