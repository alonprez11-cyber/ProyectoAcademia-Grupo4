package org.alixar.daw2.Grupo4.Academia.entities;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "matricula", uniqueConstraints = @UniqueConstraint(columnNames = { "alumno_id", "curso_id" }))
@Getter @Setter @NoArgsConstructor
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^MAT-\\d{5}$", message = "Formato del código: MAT- seguido de 5 números (ej. MAT-00001)")
    @Column(nullable = false, unique = true, length = 9)
    private String codigo;

    //  El alumno y curso son obligatorios y se comprueba en MatriculaController (el formulario envía alumno.id / curso.id y un "" crea un objeto vacío que @NotNull no detectaría).
    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @NotNull(message = "La fecha de matrícula es obligatoria")
    @PastOrPresent(message = "La fecha de matrícula no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate fechaMatricula;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMatricula estado;

    @OneToMany(mappedBy = "matricula")
    private List<Pago> pagos = new ArrayList<>();

    public void setCodigo(String codigo) {
        this.codigo = codigo == null ? null : codigo.trim().toUpperCase();
    }

    /** To string basico para desplegables y listados: "MAT-00001 · García López, Ana · Inglés B2". */
    public String getDescripcion() {
        String a = alumno != null && alumno.getApellidos() != null ? alumno.getNombreCompleto() : "?";
        String c = curso != null && curso.getNombre() != null ? curso.getNombre() : "?";
        return codigo + " · " + a + " · " + c;
    }
}
