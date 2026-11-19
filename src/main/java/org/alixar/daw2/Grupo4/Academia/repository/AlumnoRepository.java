package org.alixar.daw2.Grupo4.Academia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.alixar.daw2.Grupo4.Academia.entities.Alumno;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    Optional<Alumno> findByDni(String dni);

    Optional<Alumno> findByEmail(String email);
}
