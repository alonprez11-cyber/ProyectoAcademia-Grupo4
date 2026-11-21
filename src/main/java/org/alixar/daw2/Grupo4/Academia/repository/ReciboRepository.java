package org.alixar.daw2.Grupo4.Academia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.alixar.daw2.Grupo4.Academia.entities.EstadoPago;
import org.alixar.daw2.Grupo4.Academia.entities.Pago;
import org.alixar.daw2.Grupo4.Academia.entities.Recibo;

public interface ReciboRepository extends JpaRepository<Recibo, Long> {

    Optional<Recibo> findByNumero(String numero);

    Optional<Recibo> findByPagoId(Long pagoId);

    Optional<Recibo> findTopByOrderByIdDesc();

    /** Pagos en el estado indicado que todavía no tienen recibo (para ofrecer "Generar recibo"). */
    @Query("select p from Pago p where p.estado = :estado "
         + "and p.id not in (select r.pago.id from Recibo r) order by p.fecha desc")
    List<Pago> pagosSinRecibo(@Param("estado") EstadoPago estado);
}
