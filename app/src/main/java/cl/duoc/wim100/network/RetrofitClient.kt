package cl.duoc.wim100.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // microservicio desplegado en render (base de datos en supabase), funciona desde cualquier red
    // para probar el backend en local: emulador http://10.0.2.2:8080/api/ o celular http://<ip-del-pc>:8080/api/
    // la url siempre tiene que terminar en /
    const val BACKEND_URL = "https://wim100-rb-solutionpro-2.onrender.com/api/"

    // api externa
    const val RANDOM_USER_URL = "https://randomuser.me/"

    // muestra en el logcat cada peticion y respuesta (filtrar por okhttp)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
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
