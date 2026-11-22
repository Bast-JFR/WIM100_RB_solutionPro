package cl.duoc.wim100.backend.exception;

// se lanza cuando el correo ya esta registrado -> 409
public class CorreoDuplicadoException extends RuntimeException {

    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
