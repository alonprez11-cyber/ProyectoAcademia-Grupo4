package org.alixar.daw2.Grupo4.Academia.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TEMPORAL: solo para que Matrícula compile y se pueda probar.
 * Persona 2 aporta el Curso definitivo; al unir ramas, quedaos con el suyo.
 * Matrícula solo necesita de él: constructor vacío, getId() y getNombre().
 */
@Entity
@Table(name = "curso")
@Getter @Setter @NoArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del curso es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;
}
