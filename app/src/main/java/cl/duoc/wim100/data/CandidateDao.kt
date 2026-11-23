package cl.duoc.wim100.data

import androidx.room.*
import cl.duoc.wim100.model.Candidate
import kotlinx.coroutines.flow.Flow

@Dao
interface CandidateDao {
    @Query("SELECT * FROM candidates ORDER BY id DESC")
    fun getAllCandidates(): Flow<List<Candidate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: Candidate)

    // guarda de una la lista que llega del servidor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidates(candidates: List<Candidate>)

    @Update
    suspend fun updateCandidate(candidate: Candidate)

    @Query("DELETE FROM candidates WHERE id = :id")
    suspend fun deleteCandidateById(id: Int)

    @Query("SELECT COUNT(*) FROM candidates")
    suspend fun getCandidateCount(): Int

    // lista normal (sin flow) para usarla dentro del repositorio
    @Query("SELECT * FROM candidates")
    suspend fun getAllOnce(): List<Candidate>

    // borra las copias locales que ya no existen en el servidor
    // las de id negativo no se tocan porque son las creadas sin conexion (pendientes)
    @Query("DELETE FROM candidates WHERE id > 0 AND id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<Int>)

    @Query("DELETE FROM candidates WHERE id > 0")
    suspend fun deleteAllRemote()

    @Query("SELECT MIN(id) FROM candidates")
    suspend fun getMinId(): Int?
}
