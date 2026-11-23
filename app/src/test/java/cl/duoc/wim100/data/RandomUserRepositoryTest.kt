package cl.duoc.wim100.data

import cl.duoc.wim100.network.RandomName
import cl.duoc.wim100.network.RandomUser
import cl.duoc.wim100.network.RandomUserApi
import cl.duoc.wim100.network.RandomUserResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RandomUserRepositoryTest {

    private val api = mockk<RandomUserApi>()

    @Test
    fun `arma el nombre completo y deja el correo en minuscula`() = runTest {
        coEvery { api.generar() } returns RandomUserResponse(
            listOf(RandomUser(RandomName("Lucia", "Gomez"), "Lucia.Gomez@Example.com"))
        )

        val datos = RandomUserRepository(api).obtenerDatosSugeridos()

        assertEquals("Lucia Gomez", datos.nombre)
        assertEquals("lucia.gomez@example.com", datos.correo)
    }
}
