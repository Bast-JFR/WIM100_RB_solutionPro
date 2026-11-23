package cl.duoc.wim100.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 es el localhost del pc visto desde el emulador
    // si se usa un celular fisico hay que poner la ip del pc (ipconfig), ej: http://192.168.1.50:8080/api/
    // la url siempre tiene que terminar en /
    const val BACKEND_URL = "http://10.0.2.2:8080/api/"

    // api externa
    const val RANDOM_USER_URL = "https://randomuser.me/"

    // muestra en el logcat cada peticion y respuesta (filtrar por okhttp)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun crearRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val candidataApi: CandidataApi by lazy {
        crearRetrofit(BACKEND_URL).create(CandidataApi::class.java)
    }

    // otra instancia con otra url base, asi no se mezcla con el microservicio
    val randomUserApi: RandomUserApi by lazy {
        crearRetrofit(RANDOM_USER_URL).create(RandomUserApi::class.java)
    }
}
