package cl.duoc.wim100.backend.controller;

import cl.duoc.wim100.backend.dto.CandidataRequest;
import cl.duoc.wim100.backend.model.Candidata;
import cl.duoc.wim100.backend.service.CandidataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// endpoints rest de candidatas
@RestController
@RequestMapping("/api/candidatas")
public class CandidataController {

    private final CandidataService service;

    public CandidataController(CandidataService service) {
        this.service = service;
    }

    // GET /api/candidatas
    @GetMapping
    public List<Candidata> listar() {
        return service.listar();
    }

    // GET /api/candidatas/{id}
    @GetMapping("/{id}")
    public Candidata obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // POST /api/candidatas -> 201
    @PostMapping
    public ResponseEntity<Candidata> crear(@Valid @RequestBody CandidataRequest req) {
        Candidata creada = service.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // PUT /api/candidatas/{id}
    @PutMapping("/{id}")
    public Candidata actualizar(@PathVariable Long id, @Valid @RequestBody CandidataRequest req) {
        return service.actualizar(id, req);
    }

    // DELETE /api/candidatas/{id} -> 204
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
