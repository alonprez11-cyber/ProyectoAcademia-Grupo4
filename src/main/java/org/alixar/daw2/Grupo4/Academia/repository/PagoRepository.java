package org.alixar.daw2.Grupo4.Academia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.alixar.daw2.Grupo4.Academia.entities.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByReferencia(String referencia);

    boolean existsByMatriculaId(Long matriculaId);
}
