package cl.duoc.wim100.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// prueba las validaciones de los formularios (no necesitan mocks, es logica pura)
class FormValidatorsTest {

    @Test
    fun `rut valido con puntos y guion`() {
        assertTrue(FormValidators.validateRut("18.234.567-9").isValid)
    }

    @Test
    fun `rut con digito verificador k`() {
        // 11.111.112-K es valido
        assertTrue(FormValidators.validateRut("11.111.112-K").isValid)
    }

    @Test
    fun `rut con digito verificador malo`() {
        val r = FormValidators.validateRut("18.234.567-1")
        assertFalse(r.isValid)
        assertEquals("El dígito verificador del RUT es incorrecto.", r.errorMessage)
    }

    @Test
    fun `rut vacio o con letras`() {
        assertFalse(FormValidators.validateRut("").isValid)
        assertFalse(FormValidators.validateRut("12.3A5.678-9").isValid)
        assertFalse(FormValidators.validateRut("123").isValid)
    }

    @Test
    fun `correo valido e invalido`() {
        assertTrue(FormValidators.validateCandidateEmail("camila.rojas@mail.com").isValid)
        assertFalse(FormValidators.validateCandidateEmail("camila.rojas").isValid)
        assertFalse(FormValidators.validateCandidateEmail("").isValid)
        assertTrue(FormValidators.validateLoginEmail("usuario@demo.cl").isValid)
        assertFalse(FormValidators.validateLoginEmail("usuario@").isValid)
        assertFalse(FormValidators.validateLoginEmail("  ").isValid)
    }

    @Test
    fun `nombre obligatorio y de al menos 3 letras`() {
        assertFalse(FormValidators.validateCandidateName("").isValid)
        assertFalse(FormValidators.validateCandidateName("An").isValid)
        assertTrue(FormValidators.validateCandidateName("Ana Rojas").isValid)
    }

    @Test
    fun `telefono es opcional pero si viene debe tener 8 digitos`() {
        assertTrue(FormValidators.validateCandidatePhone("").isValid)
        assertFalse(FormValidators.validateCandidatePhone("1234").isValid)
        assertTrue(FormValidators.validateCandidatePhone("+56 9 1111 2222").isValid)
    }

    @Test
    fun `cargo y profesion obligatorios`() {
        assertFalse(FormValidators.validateCandidateRole(" ").isValid)
        assertTrue(FormValidators.validateCandidateRole("Analista QA").isValid)
        assertFalse(FormValidators.validateProfession("").isValid)
        assertFalse(FormValidators.validateProfession("QA").isValid)
        assertTrue(FormValidators.validateProfession("Ingeniera").isValid)
    }
}
