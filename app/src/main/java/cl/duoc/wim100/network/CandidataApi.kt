package cl.duoc.wim100.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// endpoints del microservicio propio (spring boot)
// cada funcion es una peticion http, retrofit arma la implementacion
interface CandidataApi {

    @GET("candidatas")
    suspend fun listar(): List<CandidataDto>

    @GET("candidatas/{id}")
    suspend fun obtener(@Path("id") id: Long): CandidataDto

    @POST("candidatas")
    suspend fun crear(@Body candidata: CandidataDto): CandidataDto

    @PUT("candidatas/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body candidata: CandidataDto): CandidataDto

    @DELETE("candidatas/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
