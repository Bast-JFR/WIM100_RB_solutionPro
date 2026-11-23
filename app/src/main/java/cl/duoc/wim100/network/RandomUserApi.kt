package cl.duoc.wim100.network

import retrofit2.http.GET
import retrofit2.http.Query

// api externa publica (randomuser.me), no necesita key
// se usa para sugerir datos en el formulario y para importar candidatas de ejemplo
interface RandomUserApi {

    @GET("api/")
    suspend fun generar(
        @Query("gender") genero: String = "female",
        @Query("nat") nacionalidad: String = "es",
        @Query("inc") campos: String = "name,email",
        @Query("results") cantidad: Int = 1
    ): RandomUserResponse
}

// clases para leer el json que responde randomuser
data class RandomUserResponse(val results: List<RandomUser>)

data class RandomUser(val name: RandomName, val email: String)

data class RandomName(val first: String, val last: String)
