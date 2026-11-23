package cl.duoc.wim100.ui.viewmodel

import cl.duoc.wim100.data.CandidateRepository
import cl.duoc.wim100.data.DatosSugeridos
import cl.duoc.wim100.data.RandomUserRepository
import cl.duoc.wim100.data.SessionPreferences
import cl.duoc.wim100.domain.NotificationHelper
import cl.duoc.wim100.model.Candidate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// prueba la logica del viewmodel
// se mockea todo lo externo: repositorio, sesion, notificaciones y api externa
@OptIn(ExperimentalCoroutinesApi::class)
class SelectionViewModelTest {

    private val repository = mockk<CandidateRepository>(relaxed = true)
    private val sessionPreferences = mockk<SessionPreferences>(relaxed = true)
    private val notificationHelper = mockk<NotificationHelper>(relaxed = true)
    private val randomUserRepository = mockk<RandomUserRepository>()

    private val camila = Candidate(
        id = 1, name = "Camila Rojas", email = "camila@mail.com", rut = "18.234.567-9",
        phone = "+56 9 1111 2222", role = "Desarrolladora Android", experience = "2 años"
    )

    @Before
    fun setUp() {
        // viewModelScope usa el hilo principal de android, en las pruebas se reemplaza
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repository.allCandidates } returns flowOf(listOf(camila))
        every { repository.allHistory } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel() =
        SelectionViewModel(repository, sessionPreferences, notificationHelper, randomUserRepository)

    @Test
    fun `al iniciar pide las candidatas al microservicio`() {
        crearViewModel()
        coVerify { repository.refrescar() }
    }

    @Test
    fun `si el backend esta apagado muestra el mensaje de error`() {
        coEvery { repository.refrescar() } throws IOException("sin conexion")

        val vm = crearViewModel()

        assertEquals("Sin conexión con el servidor, trabajando en modo local", vm.mensaje)
        assertTrue(vm.modoLocal)
        assertFalse(vm.cargando)
    }

    @Test
    fun `sin conexion y sin datos se rellena con 3 candidatas de ejemplo`() {
        coEvery { repository.refrescar() } throws IOException("sin conexion")
        coEvery { repository.getCandidateCount() } returns 0

        crearViewModel()

        coVerify(exactly = 3) { repository.insertCandidate(any()) }
    }

    @Test
    fun `sin conexion pero con datos no se rellena`() {
        coEvery { repository.refrescar() } throws IOException("sin conexion")
        coEvery { repository.getCandidateCount() } returns 5

        crearViewModel()

        coVerify(exactly = 0) { repository.insertCandidate(any()) }
    }

    @Test
    fun `importar desde la api externa agrega las candidatas`() {
        val nuevas = listOf(camila.copy(id = 0, name = "Lucia Gomez"), camila.copy(id = 0, name = "Sara Ruiz"))
        coEvery { randomUserRepository.obtenerCandidatas(2) } returns nuevas
        val vm = crearViewModel()

        vm.importarDesdeApiExterna(2)

        coVerify { repository.insertCandidate(match { it.name == "Lucia Gomez" }) }
        coVerify { repository.insertCandidate(match { it.name == "Sara Ruiz" }) }
        assertEquals("Se importaron 2 candidatas desde la API externa", vm.mensaje)
    }

    @Test
    fun `si falla la importacion se avisa`() {
        coEvery { randomUserRepository.obtenerCandidatas(any()) } throws IOException("sin internet")
        val vm = crearViewModel()

        vm.importarDesdeApiExterna()

        assertTrue(vm.mensaje!!.startsWith("API externa no disponible"))
    }

    @Test
    fun `guardar sin conexion avisa que quedo en el celular`() {
        every { repository.modoLocal } returns true
        val vm = crearViewModel()

        vm.addCandidate("Camila Rojas", "camila@mail.com", "18.234.567-9", "", "QA", "", "")

        assertEquals("Candidata guardada en el celular (sin conexión)", vm.mensaje)
    }

    @Test
    fun `addCandidate envia la candidata sin espacios sobrantes`() {
        coEvery { repository.insertCandidate(any()) } returns camila
        val vm = crearViewModel()

        vm.addCandidate("  Camila Rojas ", "camila@mail.com ", "18.234.567-9", "", "Desarrolladora Android", "2 años", "")

        coVerify { repository.insertCandidate(match { it.name == "Camila Rojas" && it.email == "camila@mail.com" }) }
        assertEquals("Candidata registrada", vm.mensaje)
    }

    @Test
    fun `addCandidate con correo repetido muestra el error 409`() {
        coEvery { repository.insertCandidate(any()) } throws HttpException(Response.error<Any>(409, "".toResponseBody()))
        val vm = crearViewModel()

        vm.addCandidate("Camila Rojas", "camila@mail.com", "18.234.567-9", "", "QA", "", "")

        assertEquals("Ya existe una candidata con ese correo", vm.mensaje)
    }

    @Test
    fun `evaluate calcula el puntaje ponderado y deja la candidata evaluada`() = runTest {
        val vm = crearViewModel()
        // alguien tiene que estar observando la lista para que tenga datos
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.candidates.collect {} }

        vm.evaluate(1, technical = 80, communication = 70, experience = 90, note = "buena entrevista")

        // 80*0.5 + 70*0.2 + 90*0.3 = 81
        coVerify {
            repository.updateCandidate(match { it.score == 81.0 && it.status == "Evaluada" && it.note == "buena entrevista" })
        }
    }

    @Test
    fun `resolve marca la candidata como seleccionada o no seleccionada`() = runTest {
        val vm = crearViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.candidates.collect {} }

        vm.resolve(1, selected = true)
        vm.resolve(1, selected = false)

        coVerify { repository.updateCandidate(match { it.status == "Seleccionada" }) }
        coVerify { repository.updateCandidate(match { it.status == "No seleccionada" }) }
    }

    @Test
    fun `updateCandidate guarda los cambios`() {
        coEvery { repository.updateCandidate(any()) } returns camila
        val vm = crearViewModel()

        vm.updateCandidate(camila.copy(role = "Full Stack"))

        coVerify { repository.updateCandidate(match { it.role == "Full Stack" }) }
        assertEquals("Datos actualizados", vm.mensaje)
    }

    @Test
    fun `deleteCandidate elimina por id`() {
        val vm = crearViewModel()

        vm.deleteCandidate(1)

        coVerify { repository.deleteCandidate(1) }
        assertEquals("Candidata eliminada", vm.mensaje)
    }

    @Test
    fun `login guarda la sesion y logout la borra`() {
        every { sessionPreferences.getUserRut() } returns "12.345.678-5"
        every { sessionPreferences.getUserProfession() } returns "Lider de seleccion"
        val vm = crearViewModel()

        vm.login("usuario@demo.cl")
        assertTrue(vm.userSession.isLoggedIn)
        assertEquals("usuario@demo.cl", vm.userSession.email)
        verify { sessionPreferences.saveSession("usuario@demo.cl", "12.345.678-5", "Lider de seleccion", "Evaluadora") }

        vm.logout()
        assertFalse(vm.userSession.isLoggedIn)
        verify { sessionPreferences.clearSession() }
    }

    @Test
    fun `setRole cambia el rol de la sesion`() {
        val vm = crearViewModel()

        vm.setRole("Dirección")

        assertEquals("Dirección", vm.userSession.role)
    }

    @Test
    fun `la api externa completa los datos sugeridos`() {
        coEvery { randomUserRepository.obtenerDatosSugeridos() } returns DatosSugeridos("Lucia Gomez", "lucia.gomez@example.com")
        val vm = crearViewModel()

        vm.generarDatosSugeridos()
        assertEquals("Lucia Gomez", vm.datosSugeridos?.nombre)

        vm.limpiarDatosSugeridos()
        assertNull(vm.datosSugeridos)
    }

    @Test
    fun `si la api externa falla se avisa sin romper la app`() {
        coEvery { randomUserRepository.obtenerDatosSugeridos() } throws IOException("sin internet")
        val vm = crearViewModel()

        vm.generarDatosSugeridos()

        assertTrue(vm.mensaje!!.startsWith("API externa no disponible"))
        vm.limpiarMensaje()
        assertNull(vm.mensaje)
    }
}
