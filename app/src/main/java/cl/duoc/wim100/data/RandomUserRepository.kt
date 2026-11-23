package cl.duoc.wim100.data

import cl.duoc.wim100.model.Candidate
import cl.duoc.wim100.network.RandomUserApi
import kotlin.random.Random

// datos que se sugieren en el formulario
data class DatosSugeridos(val nombre: String, val correo: String)

// repositorio de la api externa, separado del microservicio propio
class RandomUserRepository(private val api: RandomUserApi) {

    private val cargos = listOf("Desarrolladora Android", "Analista QA", "Backend Developer", "Data Analyst", "Diseñadora UX")

    suspend fun obtenerDatosSugeridos(): DatosSugeridos {
        val usuario = api.generar().results.first()
        val nombre = "${usuario.name.first} ${usuario.name.last}"
        return DatosSugeridos(nombre = nombre, correo = usuario.email.lowercase())
    }

    // trae varias personas de randomuser y las arma como candidatas de ejemplo
    // el rut y el telefono se generan aca para que pasen las validaciones de la app
    suspend fun obtenerCandidatas(cantidad: Int): List<Candidate> {
        val usuarios = api.generar(cantidad = cantidad).results
        return usuarios.map { u ->
            Candidate(
                name = "${u.name.first} ${u.name.last}",
                email = u.email.lowercase(),
                rut = generarRut(),
                phone = "+56 9 ${Random.nextInt(1000, 9999)} ${Random.nextInt(1000, 9999)}",
                role = cargos.random(),
                experience = "${Random.nextInt(1, 8)} años"
            )
        }
    }

    // rut ficticio pero valido (con su digito verificador)
    fun generarRut(): String {
        val numero = Random.nextInt(10_000_000, 25_000_000)
        return formatearRut(numero, calcularDv(numero))
    }

    fun calcularDv(numero: Int): String {
        var suma = 0
        var multiplicador = 2
        var n = numero
        while (n > 0) {
            suma += (n % 10) * multiplicador
            n /= 10
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = 11 - (suma % 11)
        return when (resto) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }
    }

    private fun formatearRut(numero: Int, dv: String): String {
        val texto = numero.toString()
        val conPuntos = texto.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$dv"
    }
}
