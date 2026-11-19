package org.alixar.daw2.Grupo4.Academia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.alixar.daw2.Grupo4.Academia.entities.Matricula;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    Optional<Matricula> findByCodigo(String codigo);

    Optional<Matricula> findByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);

    boolean existsByAlumnoId(Long alumnoId);

    boolean existsByCursoId(Long cursoId);
}
