package cl.duoc.wim100.backend.repository;

import cl.duoc.wim100.backend.model.Candidata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// spring arma las consultas solo con el nombre del metodo
public interface CandidataRepository extends JpaRepository<Candidata, Long> {

    List<Candidata> findAllByOrderByIdDesc();

    boolean existsByCorreoIgnoreCase(String correo);

    // para editar: revisa si otra candidata (distinto id) ya tiene ese correo
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
}
