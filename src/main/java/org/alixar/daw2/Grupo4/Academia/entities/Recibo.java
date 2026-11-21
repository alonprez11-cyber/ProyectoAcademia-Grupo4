package org.alixar.daw2.Grupo4.Academia.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Recibo de un pago (como mucho uno por pago). Este lado lleva la clave foránea. */
@Entity
@Table(name = "recibos")
@Getter @Setter @NoArgsConstructor
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número es obligatorio")
    @Pattern(regexp = "^RCB-\\d{6}$", message = "Formato del número: RCB- seguido de 6 números (ej. RCB-000001)")
    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @PastOrPresent(message = "La fecha de emisión no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate fechaEmision;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe debe ser mayor que 0")
    @Digits(integer = 8, fraction = 2, message = "Máximo 8 enteros y 2 decimales")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @NotBlank(message = "El concepto es obligatorio")
    @Size(max = 200, message = "Máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String concepto;

    // Se valida en ReciboController (el formulario envía pago.id).
    @OneToOne(optional = false)
    @JoinColumn(name = "pago_id", nullable = false, unique = true)
    private Pago pago;

    public void setNumero(String numero) {
        this.numero = numero == null ? null : numero.trim().toUpperCase();
    }
}
