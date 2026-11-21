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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "examenes")
@Getter @Setter @NoArgsConstructor
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 120, message = "Máximo 120 caracteres")
    @Column(nullable = false, length = 120)
    private String titulo;

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La nota máxima es obligatoria")
    @DecimalMin(value = "0.01", message = "La nota máxima debe ser mayor que 0")
    @DecimalMax(value = "10.00", message = "La nota máxima no puede superar 10")
    @Digits(integer = 2, fraction = 2, message = "Máximo 2 decimales")
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal notaMaxima;

    /** Peso del examen en la nota final del curso (1-100). */
    @NotNull(message = "El porcentaje es obligatorio")
    @Min(value = 1, message = "El porcentaje mínimo es 1")
    @Max(value = 100, message = "El porcentaje máximo es 100")
    @Column(nullable = false)
    private Integer porcentaje;

    // Se valida en ExamenController (el formulario envía curso.id).
    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    /** "Examen final · Inglés B2" para desplegables y listados. */
    public String getDescripcion() {
        String c = curso != null && curso.getNombre() != null ? curso.getNombre() : "?";
        return titulo + " · " + c;
    }
}
