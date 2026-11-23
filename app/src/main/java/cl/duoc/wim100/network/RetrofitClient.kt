package cl.duoc.wim100.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ip del pc en la red local (ipconfig), asi el celular fisico llega al backend
    // el celular y el pc tienen que estar en la misma red
    // para el emulador tambien sirve http://10.0.2.2:8080/api/ (es el localhost del pc)
    // la url siempre tiene que terminar en /
    const val BACKEND_URL = "http://192.168.1.139:8080/api/"

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
