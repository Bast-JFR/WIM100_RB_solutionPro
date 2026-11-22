package cl.duoc.wim100.backend.config;

import cl.duoc.wim100.backend.model.Candidata;
import cl.duoc.wim100.backend.repository.CandidataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// se ejecuta al iniciar el backend, si la tabla esta vacia carga datos de prueba (ficticios)
@Component
public class DataLoader implements CommandLineRunner {

    private final CandidataRepository repository;

    public DataLoader(CandidataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(nueva("Camila Rojas", "camila.rojas@mail.com", "18.234.567-9", "+56 9 1111 2222",
                "Desarrolladora Android", "2 años", 82.0, "En evaluación"));
        repository.save(nueva("Valentina Soto", "valentina.soto@mail.com", "19.876.543-0", "+56 9 3333 4444",
                "Analista QA", "3 años", 91.0, "Seleccionada"));
        repository.save(nueva("Fernanda Díaz", "fernanda.diaz@mail.com", "17.654.321-3", "+56 9 5555 6666",
                "Desarrolladora Android", "1 año", 0.0, "Pendiente"));
        System.out.println("datos de prueba cargados");
    }

    private Candidata nueva(String nombre, String correo, String rut, String telefono,
                            String cargo, String experiencia, Double puntaje, String estado) {
        Candidata c = new Candidata();
        c.setNombre(nombre);
        c.setCorreo(correo);
        c.setRut(rut);
        c.setTelefono(telefono);
        c.setCargo(cargo);
        c.setExperiencia(experiencia);
        c.setPuntaje(puntaje);
        c.setEstado(estado);
        c.setObservacion("");
        return c;
    }
}
