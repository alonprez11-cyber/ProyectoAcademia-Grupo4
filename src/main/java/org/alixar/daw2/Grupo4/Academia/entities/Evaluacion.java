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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Nota de un alumno en un examen. Un alumno solo tiene una nota por examen. */
@Entity
@Table(name = "evaluaciones",
       uniqueConstraints = @UniqueConstraint(columnNames = { "alumno_id", "examen_id" }))
@Getter @Setter @NoArgsConstructor
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha de la evaluación no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate fecha;

    // Que no supere la nota máxima del examen se valida en EvaluacionController.
    @NotNull(message = "La nota es obligatoria")
    @DecimalMin(value = "0.00", message = "La nota no puede ser negativa")
    @Digits(integer = 2, fraction = 2, message = "Máximo 2 decimales")
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal nota;

    /** Opcional. */
    @Size(max = 500, message = "Máximo 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    // Alumno y examen se validan en EvaluacionController (el formulario envía alumno.id / examen.id).
    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "examen_id", nullable = false)
    private Examen examen;
}
