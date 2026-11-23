package cl.duoc.wim100.data

import cl.duoc.wim100.model.Candidate
import cl.duoc.wim100.model.HistoryEntry
import cl.duoc.wim100.network.CandidataApi
import cl.duoc.wim100.network.toDto
import cl.duoc.wim100.network.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

// el microservicio es la fuente oficial de las candidatas
// room guarda una copia local que es la que observan las pantallas
// si el servidor no responde la app sigue funcionando en modo local (sin conexion)
// las candidatas creadas sin conexion quedan con id negativo hasta que se suben al servidor
class CandidateRepository(
    private val candidateDao: CandidateDao,
    private val historyDao: HistoryDao,
    private val api: CandidataApi
) {
    val allCandidates: Flow<List<Candidate>> = candidateDao.getAllCandidates()
    val allHistory: Flow<List<HistoryEntry>> = historyDao.getAllHistory()

    // true cuando el ultimo intento de hablar con el servidor fallo por falta de conexion
    var modoLocal = false
        private set

    // READ: primero sube lo pendiente, despues trae la lista del servidor y actualiza la copia local
    suspend fun refrescar() {
        try {
            subirPendientes()
            val remotas = api.listar()
            modoLocal = false

            // el cv solo existe en el celular, asi que se guarda antes de reemplazar
            val cvLocales = mutableMapOf<Int, String>()
            for (local in candidateDao.getAllOnce()) {
                cvLocales[local.id] = local.cvUri
            }

            val ids = remotas.mapNotNull { it.id?.toInt() }
            if (ids.isEmpty()) {
                candidateDao.deleteAllRemote()
            } else {
                candidateDao.deleteNotIn(ids)
            }

            val nuevas = remotas.map { dto ->
                val id = dto.id?.toInt() ?: 0
                dto.toEntity(cvLocales[id] ?: "")
            }
            candidateDao.insertCandidates(nuevas)
        } catch (e: IOException) {
            modoLocal = true
            throw e
        }
    }

    // sube al servidor las candidatas que se crearon sin conexion
    private suspend fun subirPendientes() {
        val pendientes = candidateDao.getAllOnce().filter { it.id < 0 }
        for (local in pendientes) {
            try {
                val creada = api.crear(local.toDto())
                candidateDao.deleteCandidateById(local.id)
                candidateDao.insertCandidate(creada.toEntity(local.cvUri))
            } catch (e: HttpException) {
                // 409 = el correo ya existe en el servidor, la copia local sobra
                if (e.code() == 409) {
                    candidateDao.deleteCandidateById(local.id)
                }
            }
        }
    }

    // CREATE: el servidor asigna el id; si no hay conexion se guarda solo en el celular
    suspend fun insertCandidate(candidate: Candidate): Candidate {
        // si ya se sabe que no hay servidor no se espera el timeout, se guarda directo
        if (modoLocal) return guardarLocal(candidate)
        return try {
            val creada = api.crear(candidate.toDto())
            modoLocal = false
            val local = creada.toEntity(candidate.cvUri)
            candidateDao.insertCandidate(local)
            local
        } catch (e: IOException) {
            modoLocal = true
            guardarLocal(candidate)
        }
    }

    private suspend fun guardarLocal(candidate: Candidate): Candidate {
        val local = candidate.copy(id = siguienteIdLocal())
        candidateDao.insertCandidate(local)
        return local
    }

    // UPDATE: se usa para editar, evaluar y resolver
    suspend fun updateCandidate(candidate: Candidate): Candidate {
        // si todavia no esta en el servidor solo se actualiza la copia local
        if (candidate.id < 0 || modoLocal) {
            candidateDao.insertCandidate(candidate)
            return candidate
        }
        return try {
            val actualizada = api.actualizar(candidate.id.toLong(), candidate.toDto())
            modoLocal = false
            val local = actualizada.toEntity(candidate.cvUri)
            candidateDao.insertCandidate(local)
            local
        } catch (e: IOException) {
            modoLocal = true
            candidateDao.insertCandidate(candidate)
            candidate
        }
    }

    // DELETE: si el servidor dice 404 es porque ya no estaba, igual se borra la copia local
    suspend fun deleteCandidate(id: Int) {
        if (id < 0 || modoLocal) {
            candidateDao.deleteCandidateById(id)
            return
        }
        val respuesta = try {
            api.eliminar(id.toLong())
        } catch (e: IOException) {
            modoLocal = true
            candidateDao.deleteCandidateById(id)
            return
        }
        modoLocal = false
        if (!respuesta.isSuccessful && respuesta.code() != 404) {
            throw HttpException(respuesta)
        }
        candidateDao.deleteCandidateById(id)
    }

    // ids negativos para las candidatas creadas sin conexion (no chocan con los del servidor)
    private suspend fun siguienteIdLocal(): Int {
        val minimo = candidateDao.getMinId() ?: 0
        return if (minimo < 0) minimo - 1 else -1
    }

    suspend fun addHistory(entry: HistoryEntry) {
        historyDao.insertHistory(entry)
    }

    suspend fun getCandidateCount(): Int {
        return candidateDao.getCandidateCount()
    }
}
