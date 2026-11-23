package cl.duoc.wim100.data

import cl.duoc.wim100.model.Candidate
import cl.duoc.wim100.model.HistoryEntry
import cl.duoc.wim100.network.CandidataApi
import cl.duoc.wim100.network.CandidataDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

// se prueba la logica del repositorio
// el dao (room) y la api (retrofit) se reemplazan por mocks, asi no se necesita emulador ni servidor
class CandidateRepositoryTest {

    private val candidateDao = mockk<CandidateDao>(relaxed = true)
    private val historyDao = mockk<HistoryDao>(relaxed = true)
    private val api = mockk<CandidataApi>()
    private lateinit var repository: CandidateRepository

    private fun dto(id: Long, nombre: String = "Ana Rojas") = CandidataDto(
        id = id,
        nombre = nombre,
        correo = "ana@mail.com",
        rut = "18.234.567-9",
        telefono = "+56 9 1111 2222",
        cargo = "Analista QA",
        experiencia = "2 años",
        puntaje = 0.0,
        estado = "Pendiente",
        observacion = ""
    )

    private fun candidata(id: Int = 0, cv: String = "") = Candidate(
        id = id,
        name = "Ana Rojas",
        email = "ana@mail.com",
        rut = "18.234.567-9",
        phone = "+56 9 1111 2222",
        role = "Analista QA",
        experience = "2 años",
        cvUri = cv
    )

    @Before
    fun setUp() {
        every { candidateDao.getAllCandidates() } returns flowOf(emptyList())
        every { historyDao.getAllHistory() } returns flowOf(emptyList())
        repository = CandidateRepository(candidateDao, historyDao, api)
    }

    @Test
    fun `refrescar guarda en room lo que llega del servidor y conserva el cv local`() = runTest {
        coEvery { api.listar() } returns listOf(dto(1), dto(2, "Sofia Lagos"))
        coEvery { candidateDao.getAllOnce() } returns listOf(candidata(id = 1, cv = "content://cv1"))

        repository.refrescar()

        coVerify { candidateDao.deleteNotIn(listOf(1, 2)) }
        coVerify {
            candidateDao.insertCandidates(match { lista ->
                lista.size == 2 && lista[0].cvUri == "content://cv1" && lista[1].name == "Sofia Lagos"
            })
        }
    }

    @Test
    fun `refrescar con el servidor vacio borra toda la copia local`() = runTest {
        coEvery { api.listar() } returns emptyList()
        coEvery { candidateDao.getAllOnce() } returns emptyList()

        repository.refrescar()

        coVerify { candidateDao.deleteAll() }
    }

    @Test
    fun `insertCandidate guarda la copia local con el id que entrega el servidor`() = runTest {
        coEvery { api.crear(any()) } returns dto(10)

        val creada = repository.insertCandidate(candidata(cv = "content://cv"))

        assertEquals(10, creada.id)
        coVerify { candidateDao.insertCandidate(match { it.id == 10 && it.cvUri == "content://cv" }) }
    }

    @Test
    fun `updateCandidate envia el put con el id correcto`() = runTest {
        coEvery { api.actualizar(5L, any()) } returns dto(5).copy(cargo = "Full Stack")

        val actualizada = repository.updateCandidate(candidata(id = 5).copy(role = "Full Stack"))

        assertEquals("Full Stack", actualizada.role)
        coVerify { api.actualizar(5L, match { it.cargo == "Full Stack" }) }
        coVerify { candidateDao.insertCandidate(any()) }
    }

    @Test
    fun `deleteCandidate borra la copia local cuando el servidor responde 204`() = runTest {
        coEvery { api.eliminar(3L) } returns Response.success(204, Unit)

        repository.deleteCandidate(3)

        coVerify { candidateDao.deleteCandidateById(3) }
    }

    @Test
    fun `deleteCandidate igual borra la copia local si el servidor responde 404`() = runTest {
        coEvery { api.eliminar(3L) } returns Response.error(404, "".toResponseBody())

        repository.deleteCandidate(3)

        coVerify { candidateDao.deleteCandidateById(3) }
    }

    @Test(expected = HttpException::class)
    fun `deleteCandidate lanza error si el servidor responde 500`() = runTest {
        coEvery { api.eliminar(3L) } returns Response.error(500, "".toResponseBody())

        repository.deleteCandidate(3)
    }

    @Test
    fun `allCandidates entrega lo que tiene room`() = runTest {
        every { candidateDao.getAllCandidates() } returns flowOf(listOf(candidata(id = 1)))
        val repo = CandidateRepository(candidateDao, historyDao, api)

        val lista = repo.allCandidates.first()

        assertEquals(1, lista.size)
    }

    @Test
    fun `addHistory y getCandidateCount usan los dao`() = runTest {
        coEvery { candidateDao.getCandidateCount() } returns 3
        val entrada = HistoryEntry(date = "22/11/2026", text = "prueba")

        repository.addHistory(entrada)

        coVerify { historyDao.insertHistory(entrada) }
        assertEquals(3, repository.getCandidateCount())
    }
}
