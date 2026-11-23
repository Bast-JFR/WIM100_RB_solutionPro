package cl.duoc.wim100.data

import cl.duoc.wim100.network.RandomUserApi

// datos que se sugieren en el formulario
data class DatosSugeridos(val nombre: String, val correo: String)

// repositorio de la api externa, separado del microservicio propio
class RandomUserRepository(private val api: RandomUserApi) {

    suspend fun obtenerDatosSugeridos(): DatosSugeridos {
        val usuario = api.generar().results.first()
        val nombre = "${usuario.name.first} ${usuario.name.last}"
        return DatosSugeridos(nombre = nombre, correo = usuario.email.lowercase())
    }
}
