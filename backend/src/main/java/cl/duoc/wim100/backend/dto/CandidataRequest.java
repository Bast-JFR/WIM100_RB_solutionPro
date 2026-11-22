package cl.duoc.wim100.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// json que recibe la api al crear o editar una candidata
// si algo no cumple las validaciones se responde 400 antes de llegar al service
public record CandidataRequest(

        @NotBlank(message = "el nombre es obligatorio")
        @Size(min = 3, max = 80, message = "el nombre debe tener entre 3 y 80 caracteres")
        String nombre,

        @NotBlank(message = "el correo es obligatorio")
        @Email(message = "formato de correo invalido")
        String correo,

        @NotBlank(message = "el rut es obligatorio")
        @Size(max = 12, message = "rut demasiado largo")
        String rut,

        @Size(max = 20, message = "telefono demasiado largo")
        String telefono,

        @NotBlank(message = "el cargo es obligatorio")
        String cargo,

        @Size(max = 40)
        String experiencia,

        @Min(value = 0, message = "el puntaje no puede ser negativo")
        @Max(value = 100, message = "el puntaje maximo es 100")
        Double puntaje,

        @Pattern(regexp = "Pendiente|En evaluación|Evaluada|Seleccionada|No seleccionada",
                message = "estado no valido")
        String estado,

        @Size(max = 300)
        String observacion
) {
}
