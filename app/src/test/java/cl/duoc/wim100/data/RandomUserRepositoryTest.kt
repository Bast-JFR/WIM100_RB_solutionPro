package cl.duoc.wim100.data

import cl.duoc.wim100.domain.FormValidators
import cl.duoc.wim100.network.RandomName
import cl.duoc.wim100.network.RandomUser
import cl.duoc.wim100.network.RandomUserApi
import cl.duoc.wim100.network.RandomUserResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun `las candidatas importadas pasan las validaciones de la app`() = runTest {
        coEvery { api.generar(cantidad = 2) } returns RandomUserResponse(
            listOf(
                RandomUser(RandomName("Lucia", "Gomez"), "lucia@example.com"),
                RandomUser(RandomName("Sara", "Ruiz"), "sara@example.com")
            )
        )

        val candidatas = RandomUserRepository(api).obtenerCandidatas(2)

        assertEquals(2, candidatas.size)
        for (c in candidatas) {
            assertTrue(FormValidators.validateRut(c.rut).isValid)
            assertTrue(FormValidators.validateCandidatePhone(c.phone).isValid)
            assertTrue(FormValidators.validateCandidateEmail(c.email).isValid)
        }
    }

    @Test
    fun `el digito verificador se calcula bien`() {
        val repo = RandomUserRepository(api)
        assertEquals("9", repo.calcularDv(18234567))
        assertEquals("K", repo.calcularDv(11111112))
        assertEquals("0", repo.calcularDv(19876543))
    }
}
