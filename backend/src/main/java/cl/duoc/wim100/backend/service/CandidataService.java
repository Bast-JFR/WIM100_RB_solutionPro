package cl.duoc.wim100.backend.service;

import cl.duoc.wim100.backend.dto.CandidataRequest;
import cl.duoc.wim100.backend.exception.CorreoDuplicadoException;
import cl.duoc.wim100.backend.exception.RecursoNoEncontradoException;
import cl.duoc.wim100.backend.model.Candidata;
import cl.duoc.wim100.backend.repository.CandidataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// aca van las reglas de negocio, el controller solo recibe y responde
@Service
public class CandidataService {

    private final CandidataRepository repository;

    public CandidataService(CandidataRepository repository) {
        this.repository = repository;
    }

    public List<Candidata> listar() {
        return repository.findAllByOrderByIdDesc();
    }

    public Candidata obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("no existe la candidata con id " + id));
    }

    public Candidata crear(CandidataRequest req) {
        // regla: no se registra dos veces el mismo correo
        if (repository.existsByCorreoIgnoreCase(req.correo().trim())) {
            throw new CorreoDuplicadoException("ya existe una candidata con ese correo");
        }
        Candidata c = new Candidata();
        copiarDatos(req, c);
        // al crear siempre parte pendiente y sin puntaje
        c.setPuntaje(0.0);
        c.setEstado("Pendiente");
        c.setObservacion("");
        return repository.save(c);
    }

    public Candidata actualizar(Long id, CandidataRequest req) {
        Candidata c = obtener(id); // si no existe lanza 404
        if (repository.existsByCorreoIgnoreCaseAndIdNot(req.correo().trim(), id)) {
            throw new CorreoDuplicadoException("ya existe una candidata con ese correo");
        }
        copiarDatos(req, c);
        // la evaluacion y la resolucion tambien llegan por aca
        if (req.puntaje() != null) c.setPuntaje(req.puntaje());
        if (req.estado() != null) c.setEstado(req.estado());
        if (req.observacion() != null) c.setObservacion(req.observacion());
        return repository.save(c);
    }

    public void eliminar(Long id) {
        Candidata c = obtener(id);
        repository.delete(c);
    }

    private void copiarDatos(CandidataRequest req, Candidata c) {
        c.setNombre(req.nombre().trim());
        c.setCorreo(req.correo().trim().toLowerCase());
        c.setRut(req.rut().trim());
        c.setTelefono(req.telefono() == null ? "" : req.telefono().trim());
        c.setCargo(req.cargo().trim());
        c.setExperiencia(req.experiencia() == null ? "" : req.experiencia().trim());
    }
}
