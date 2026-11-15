package com.example.sistemavet.modulo

import com.example.sistemavet.Database
import com.example.sistemavet.datos.MascotaRequest
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Date
import java.time.LocalDate

object MascotaRepo {

    // dbo.spRegistrarMascota: crea la mascota y su expediente. Devuelve el id de la mascota.
    fun registrar(r: MascotaRequest, fechaNac: LocalDate): Int =
        Database.connect().use { conn ->
            conn.prepareCall("{call dbo.spRegistrarMascota(?,?,?,?,?,?,?)}").use { cs ->
                cs.setInt(1, r.propietarioId)
                cs.setString(2, r.nombre)
                cs.setString(3, r.especie)
                cs.setString(4, r.raza)
                cs.setString(5, r.sexo)
                cs.setDate(6, Date.valueOf(fechaNac))
                cs.setBigDecimal(7, BigDecimal.valueOf(r.pesoAproximado).setScale(2, RoundingMode.HALF_UP))
                cs.executeQuery().use { rs ->
                    rs.next()
                    rs.getInt("Id")
                }
            }
        }
}

