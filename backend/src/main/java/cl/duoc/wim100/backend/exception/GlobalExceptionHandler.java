package cl.duoc.wim100.backend.exception;

import cl.duoc.wim100.backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// atrapa las excepciones de toda la api y las transforma en el codigo http que corresponde
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
        return armarRespuesta(HttpStatus.NOT_FOUND, e.getMessage(), new HashMap<>());
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> correoDuplicado(CorreoDuplicadoException e) {
        return armarRespuesta(HttpStatus.CONFLICT, e.getMessage(), new HashMap<>());
    }

    // errores de @Valid, devuelve un mensaje por cada campo malo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(campo ->
                errores.put(campo.getField(), campo.getDefaultMessage())
        );
        return armarRespuesta(HttpStatus.BAD_REQUEST, "datos invalidos", errores);
    }

    private ResponseEntity<ErrorResponse> armarRespuesta(HttpStatus status, String mensaje, Map<String, String> errores) {
        ErrorResponse body = new ErrorResponse(status.value(), mensaje, errores);
        return ResponseEntity.status(status).body(body);
    }
}
