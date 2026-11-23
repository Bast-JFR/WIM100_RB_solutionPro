package cl.duoc.wim100.network

import cl.duoc.wim100.model.Candidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CandidataMapperTest {

    @Test
    fun `toEntity pone valores por defecto cuando el servidor manda nulos`() {
        val dto = CandidataDto(
            id = 7, nombre = "Ana", correo = "ana@mail.com", rut = "1-9", telefono = null,
            cargo = "QA", experiencia = null, puntaje = null, estado = null, observacion = null
        )

        val entidad = dto.toEntity("content://cv")

        assertEquals(7, entidad.id)
        assertEquals("", entidad.phone)
        assertEquals(0.0, entidad.score, 0.0)
        assertEquals("Pendiente", entidad.status)
        assertEquals("content://cv", entidad.cvUri)
    }

    @Test
    fun `toDto no manda id cuando la candidata es nueva`() {
        val nueva = Candidate(name = "Ana", email = "ana@mail.com", phone = "", role = "QA", experience = "")

        val dto = nueva.toDto()

        assertNull(dto.id)
        assertEquals("Ana", dto.nombre)
        assertEquals("QA", dto.cargo)
    }

    @Test
    fun `toDto manda el id cuando la candidata ya existe`() {
        val existente = Candidate(id = 4, name = "Ana", email = "ana@mail.com", phone = "", role = "QA", experience = "")

        assertEquals(4L, existente.toDto().id)
    }
}
