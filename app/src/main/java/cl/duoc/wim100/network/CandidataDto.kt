package cl.duoc.wim100.network

// json de una candidata tal como lo envia y recibe el microservicio
// los nombres tienen que ser iguales a los del backend
data class CandidataDto(
    val id: Long? = null,
    val nombre: String,
    val correo: String,
    val rut: String,
    val telefono: String? = "",
    val cargo: String,
    val experiencia: String? = "",
    val puntaje: Double? = 0.0,
    val estado: String? = "Pendiente",
    val observacion: String? = ""
)
