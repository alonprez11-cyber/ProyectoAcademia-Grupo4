package org.alixar.daw2.Grupo4.Academia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.alixar.daw2.Grupo4.Academia.entities.Certificado;
import org.alixar.daw2.Grupo4.Academia.entities.EstadoMatricula;
import org.alixar.daw2.Grupo4.Academia.entities.Matricula;

public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

    Optional<Certificado> findByCodigo(String codigo);

    Optional<Certificado> findByMatriculaId(Long matriculaId);

    Optional<Certificado> findTopByOrderByIdDesc();

    /** Matrículas que todavía no tienen certificado (para ofrecer "Generar certificado"). */
    @Query("select m from Matricula m where m.estado <> :excluido "
         + "and m.id not in (select c.matricula.id from Certificado c) order by m.codigo")
    List<Matricula> matriculasSinCertificado(@Param("excluido") EstadoMatricula excluido);
}
