package cl.duoc.wim100.network

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErroresTest {

    private fun httpError(codigo: Int) = HttpException(Response.error<Any>(codigo, "".toResponseBody()))

    @Test
    fun `cada codigo http tiene su mensaje`() {
        assertEquals("Datos inválidos, revisa el formulario", httpError(400).mensajeAmigable())
        assertEquals("La candidata ya no existe en el servidor", httpError(404).mensajeAmigable())
        assertEquals("Ya existe una candidata con ese correo", httpError(409).mensajeAmigable())
        assertEquals("Error del servidor (500)", httpError(500).mensajeAmigable())
    }

    @Test
    fun `sin conexion avisa que el backend no responde`() {
        val mensaje = IOException("timeout").mensajeAmigable()
        assertEquals("No se pudo conectar con el servidor, revisa que el backend esté corriendo", mensaje)
    }

    @Test
    fun `otro error muestra su mensaje`() {
        assertEquals("algo raro", IllegalStateException("algo raro").mensajeAmigable())
    }
}
