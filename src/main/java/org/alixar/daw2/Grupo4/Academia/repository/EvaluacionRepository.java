package org.alixar.daw2.Grupo4.Academia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.alixar.daw2.Grupo4.Academia.entities.Evaluacion;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    Optional<Evaluacion> findByAlumnoIdAndExamenId(Long alumnoId, Long examenId);

    boolean existsByExamenId(Long examenId);
}
