package cl.duoc.wim100.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.wim100.data.AppDatabase
import cl.duoc.wim100.data.CandidateRepository
import cl.duoc.wim100.data.DatosSugeridos
import cl.duoc.wim100.data.RandomUserRepository
import cl.duoc.wim100.data.SessionPreferences
import cl.duoc.wim100.domain.NotificationHelper
import cl.duoc.wim100.model.Candidate
import cl.duoc.wim100.model.HistoryEntry
import cl.duoc.wim100.model.UserSession
import cl.duoc.wim100.network.RetrofitClient
import cl.duoc.wim100.network.mensajeAmigable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// las dependencias llegan por el constructor, asi en las pruebas se pueden reemplazar por mocks
class SelectionViewModel(
    private val repository: CandidateRepository,
    private val sessionPreferences: SessionPreferences,
    private val notificationHelper: NotificationHelper,
    private val randomUserRepository: RandomUserRepository
) : ViewModel() {

    val candidates: StateFlow<List<Candidate>> = repository.allCandidates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<HistoryEntry>> = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var userSession by mutableStateOf(
        UserSession(
            email = sessionPreferences.getUserEmail(),
            rut = sessionPreferences.getUserRut(),
            profession = sessionPreferences.getUserProfession(),
            role = sessionPreferences.getUserRole(),
            isLoggedIn = sessionPreferences.isLoggedIn()
        )
    )
        private set

    // estado de las llamadas al servidor, lo usan las pantallas
    var cargando by mutableStateOf(false)
        private set

    var mensaje by mutableStateOf<String?>(null)
        private set

    // datos que llegan de la api externa para el formulario
    var datosSugeridos by mutableStateOf<DatosSugeridos?>(null)
        private set

    init {
        // al abrir la app se piden las candidatas al microservicio
        refrescar()
    }

    fun refrescar() {
        viewModelScope.launch {
            cargando = true
            try {
                repository.refrescar()
            } catch (e: Exception) {
                // si el backend esta apagado se sigue mostrando la copia local
                mensaje = e.mensajeAmigable()
            } finally {
                cargando = false
            }
        }
    }

    fun limpiarMensaje() {
        mensaje = null
    }

    // API EXTERNA: pide un nombre y correo de ejemplo a randomuser.me
    // no guarda nada en el servidor, solo completa el formulario
    fun generarDatosSugeridos() {
        viewModelScope.launch {
            try {
                datosSugeridos = randomUserRepository.obtenerDatosSugeridos()
            } catch (e: Exception) {
                mensaje = "API externa no disponible: ${e.mensajeAmigable()}"
            }
        }
    }

    fun limpiarDatosSugeridos() {
        datosSugeridos = null
    }

    fun login(email: String, role: String = "Evaluadora") {
        try {
            val rut = sessionPreferences.getUserRut()
            val profession = sessionPreferences.getUserProfession()
            sessionPreferences.saveSession(email, rut, profession, role)
            userSession = UserSession(email = email, rut = rut, profession = profession, role = role, isLoggedIn = true)
            log("Sesión iniciada por $email ($role)")
            notificationHelper.showNotification(
                title = "Bienvenido a WIM100",
                message = "Sesión iniciada como $email"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registerUser(email: String, rut: String, profession: String, role: String = "Evaluadora") {
        try {
            sessionPreferences.saveSession(email = email, rut = rut, profession = profession, role = role)
            userSession = UserSession(email = email, rut = rut, profession = profession, role = role, isLoggedIn = true)
            log("Nuevo usuario registrado: $email | RUT: $rut | Profesión: $profession")
            notificationHelper.showNotification(
                title = "Registro Exitoso",
                message = "Bienvenido a WIM100, $email"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logout() {
        try {
            sessionPreferences.clearSession()
            userSession = UserSession()
            log("Sesión cerrada")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setRole(role: String) {
        try {
            sessionPreferences.saveSession(userSession.email, userSession.rut, userSession.profession, role)
            userSession = userSession.copy(role = role)
            log("Cambio de rol a $role")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // CREATE
    fun addCandidate(name: String, email: String, rut: String, phone: String, role: String, experience: String, cv: String) {
        viewModelScope.launch {
            try {
                val candidate = Candidate(
                    name = name.trim(),
                    email = email.trim(),
                    rut = rut.trim(),
                    phone = phone.trim(),
                    role = role.trim(),
                    experience = experience.trim(),
                    cvUri = cv
                )
                repository.insertCandidate(candidate)
                mensaje = "Candidata registrada"
                log("Se registró a ${name.trim()} (RUT: ${rut.trim()})")
                notificationHelper.showNotification(
                    title = "Nueva Candidata Registrada",
                    message = "${name.trim()} postula al cargo $role"
                )
            } catch (e: Exception) {
                mensaje = e.mensajeAmigable()
            }
        }
    }

    // UPDATE: editar los datos de una candidata
    fun updateCandidate(candidate: Candidate) {
        viewModelScope.launch {
            try {
                repository.updateCandidate(candidate)
                mensaje = "Datos actualizados"
                log("Se editaron los datos de ${candidate.name}")
            } catch (e: Exception) {
                mensaje = e.mensajeAmigable()
            }
        }
    }

    fun evaluate(id: Int, technical: Int, communication: Int, experience: Int, note: String) {
        viewModelScope.launch {
            try {
                val total = technical * 0.5 + communication * 0.2 + experience * 0.3
                val candidate = candidates.value.firstOrNull { it.id == id }
                if (candidate != null) {
                    val updated = candidate.copy(
                        score = total,
                        status = "Evaluada",
                        note = note
                    )
                    repository.updateCandidate(updated)
                    log("Evaluación actualizada: ${candidate.name} (${"%.1f".format(total)} pts)")
                    notificationHelper.showNotification(
                        title = "Evaluación Completada",
                        message = "${candidate.name}: ${"%.1f".format(total)} puntos"
                    )
                }
            } catch (e: Exception) {
                mensaje = e.mensajeAmigable()
            }
        }
    }

    fun resolve(id: Int, selected: Boolean) {
        viewModelScope.launch {
            try {
                val candidate = candidates.value.firstOrNull { it.id == id }
                if (candidate != null) {
                    val statusText = if (selected) "Seleccionada" else "No seleccionada"
                    val updated = candidate.copy(status = statusText)
                    repository.updateCandidate(updated)
                    log("Dirección $statusText a ${candidate.name}")
                    notificationHelper.showNotification(
                        title = "Decisión de Selección",
                        message = "${candidate.name} ha sido marcada como: $statusText"
                    )
                }
            } catch (e: Exception) {
                mensaje = e.mensajeAmigable()
            }
        }
    }

    // DELETE
    fun deleteCandidate(id: Int) {
        viewModelScope.launch {
            try {
                val candidate = candidates.value.firstOrNull { it.id == id }
                val name = candidate?.name ?: "Candidata"
                repository.deleteCandidate(id)
                mensaje = "Candidata eliminada"
                log("Se eliminó el registro de $name")
            } catch (e: Exception) {
                mensaje = e.mensajeAmigable()
            }
        }
    }

    private fun log(message: String) {
        viewModelScope.launch {
            try {
                repository.addHistory(HistoryEntry(date = today(), text = message))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private fun today() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // fabrica del viewmodel: aca se crean las dependencias reales
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as Application
                val db = AppDatabase.getInstance(app)
                val repository = CandidateRepository(db.candidateDao(), db.historyDao(), RetrofitClient.candidataApi)
                val randomUserRepository = RandomUserRepository(RetrofitClient.randomUserApi)
                SelectionViewModel(repository, SessionPreferences(app), NotificationHelper(app), randomUserRepository)
            }
        }
    }
}
