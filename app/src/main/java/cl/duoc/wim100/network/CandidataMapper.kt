package cl.duoc.wim100.network

import cl.duoc.wim100.model.Candidate

// convierte entre el json del servidor y la entidad de room

// servidor -> room. el cv no viaja al servidor (es un archivo del celular), se conserva el local
fun CandidataDto.toEntity(cvLocal: String = ""): Candidate {
    return Candidate(
        id = (id ?: 0L).toInt(),
        name = nombre,
        email = correo,
        rut = rut,
        phone = telefono ?: "",
        role = cargo,
        experience = experiencia ?: "",
        cvUri = cvLocal,
        score = puntaje ?: 0.0,
        status = estado ?: "Pendiente",
        note = observacion ?: ""
    )
}

// room -> servidor
fun Candidate.toDto(): CandidataDto {
    return CandidataDto(
        id = if (id > 0) id.toLong() else null,
        nombre = name,
        correo = email,
        rut = rut,
        telefono = phone,
        cargo = role,
        experiencia = experience,
        puntaje = score,
        estado = status,
        observacion = note
    )
}
