package cl.duoc.wim100.backend.dto;

import java.util.Map;

// formato de los errores que devuelve la api
public record ErrorResponse(int status, String mensaje, Map<String, String> errores) {
}
