package cl.duoc.wim100.network

import retrofit2.HttpException
import java.io.IOException

// traduce los errores tecnicos a un mensaje que entienda la usuaria
fun Throwable.mensajeAmigable(): String {
    return when (this) {
        is HttpException -> when (code()) {
            400 -> "Datos inválidos, revisa el formulario"
            404 -> "La candidata ya no existe en el servidor"
            409 -> "Ya existe una candidata con ese correo"
            else -> "Error del servidor (${code()})"
        }
        is IOException -> "No se pudo conectar con el servidor, revisa que el backend esté corriendo"
        else -> message ?: "Error inesperado"
    }
}
