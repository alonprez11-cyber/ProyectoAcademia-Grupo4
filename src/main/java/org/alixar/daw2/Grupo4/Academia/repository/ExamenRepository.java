package org.alixar.daw2.Grupo4.Academia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.alixar.daw2.Grupo4.Academia.entities.Examen;

public interface ExamenRepository extends JpaRepository<Examen, Long> {

    List<Examen> findByCursoId(Long cursoId);
}
