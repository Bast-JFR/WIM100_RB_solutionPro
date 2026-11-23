package cl.duoc.wim100.data

import cl.duoc.wim100.model.Candidate
import cl.duoc.wim100.model.HistoryEntry
import cl.duoc.wim100.network.CandidataApi
import cl.duoc.wim100.network.toDto
import cl.duoc.wim100.network.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

// el microservicio es la fuente oficial de las candidatas
// room guarda una copia local que es la que observan las pantallas
class CandidateRepository(
    private val candidateDao: CandidateDao,
    private val historyDao: HistoryDao,
    private val api: CandidataApi
) {
    val allCandidates: Flow<List<Candidate>> = candidateDao.getAllCandidates()
    val allHistory: Flow<List<HistoryEntry>> = historyDao.getAllHistory()

    // READ: trae la lista del servidor y actualiza la copia local
    suspend fun refrescar() {
        val remotas = api.listar()

        // el cv solo existe en el celular, asi que se guarda antes de reemplazar
        val cvLocales = mutableMapOf<Int, String>()
        for (local in candidateDao.getAllOnce()) {
            cvLocales[local.id] = local.cvUri
        }

        val ids = remotas.mapNotNull { it.id?.toInt() }
        if (ids.isEmpty()) {
            candidateDao.deleteAll()
        } else {
            candidateDao.deleteNotIn(ids)
        }

        val nuevas = remotas.map { dto ->
            val id = dto.id?.toInt() ?: 0
            dto.toEntity(cvLocales[id] ?: "")
        }
        candidateDao.insertCandidates(nuevas)
    }

    // CREATE: el servidor asigna el id y despues se guarda la copia local
    suspend fun insertCandidate(candidate: Candidate): Candidate {
        val creada = api.crear(candidate.toDto())
        val local = creada.toEntity(candidate.cvUri)
        candidateDao.insertCandidate(local)
        return local
    }

    // UPDATE: se usa para editar, evaluar y resolver
    suspend fun updateCandidate(candidate: Candidate): Candidate {
        val actualizada = api.actualizar(candidate.id.toLong(), candidate.toDto())
        val local = actualizada.toEntity(candidate.cvUri)
        candidateDao.insertCandidate(local)
        return local
    }

    // DELETE: si el servidor dice 404 es porque ya no estaba, igual se borra la copia local
    suspend fun deleteCandidate(id: Int) {
        val respuesta = api.eliminar(id.toLong())
        if (!respuesta.isSuccessful && respuesta.code() != 404) {
            throw HttpException(respuesta)
        }
        candidateDao.deleteCandidateById(id)
    }

    suspend fun addHistory(entry: HistoryEntry) {
        historyDao.insertHistory(entry)
    }

    suspend fun getCandidateCount(): Int {
        return candidateDao.getCandidateCount()
    }
}
