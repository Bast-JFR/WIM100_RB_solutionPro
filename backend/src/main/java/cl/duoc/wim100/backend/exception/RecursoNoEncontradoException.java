package cl.duoc.wim100.backend.exception;

// se lanza cuando se busca un id que no existe -> 404
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
