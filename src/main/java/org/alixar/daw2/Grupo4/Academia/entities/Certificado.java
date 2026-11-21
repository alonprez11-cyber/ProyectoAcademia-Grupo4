package org.alixar.daw2.Grupo4.Academia.entities;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Certificado de una matrícula (como mucho uno por matrícula). Este lado lleva la clave foránea. */
@Entity
@Table(name = "certificados")
@Getter @Setter @NoArgsConstructor
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^CER-\\d{5}$", message = "Formato del código: CER- seguido de 5 números (ej. CER-00001)")
    @Column(nullable = false, unique = true, length = 9)
    private String codigo;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @PastOrPresent(message = "La fecha de emisión no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate fechaEmision;

    @NotNull(message = "El tipo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCertificado tipo;

    // unique = true es lo que convierte esto en un @OneToOne real: una matrícula, un certificado.
    // Se valida en CertificadoController (el formulario envía matricula.id).
    @OneToOne(optional = false)
    @JoinColumn(name = "matricula_id", nullable = false, unique = true)
    private Matricula matricula;

    public void setCodigo(String codigo) {
        this.codigo = codigo == null ? null : codigo.trim().toUpperCase();
    }
}
