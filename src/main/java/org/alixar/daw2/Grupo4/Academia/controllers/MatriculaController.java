package org.alixar.daw2.Grupo4.Academia.controllers;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.alixar.daw2.Grupo4.Academia.entities.Alumno;
import org.alixar.daw2.Grupo4.Academia.entities.Curso;
import org.alixar.daw2.Grupo4.Academia.entities.EstadoMatricula;
import org.alixar.daw2.Grupo4.Academia.entities.Matricula;
import org.alixar.daw2.Grupo4.Academia.repository.AlumnoRepository;
import org.alixar.daw2.Grupo4.Academia.repository.CursoRepository;
import org.alixar.daw2.Grupo4.Academia.repository.MatriculaRepository;
import org.alixar.daw2.Grupo4.Academia.repository.PagoRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatriculaRepository matriculas;
    private final AlumnoRepository alumnos;
    private final CursoRepository cursos;
    private final PagoRepository pagos;

    public MatriculaController(MatriculaRepository matriculas, AlumnoRepository alumnos,
                               CursoRepository cursos, PagoRepository pagos) {
        this.matriculas = matriculas;
        this.alumnos = alumnos;
        this.cursos = cursos;
        this.pagos = pagos;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("matriculas", matriculas.findAll(Sort.by("fechaMatricula").descending()));
        return "matriculas/Matricula";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Matricula matricula = new Matricula();
        matricula.setFechaMatricula(LocalDate.now());
        matricula.setEstado(EstadoMatricula.PENDIENTE);
        model.addAttribute("matricula", matricula);
        cargarCombos(model);
        return "matriculas/matricula-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Matricula> matricula = matriculas.findById(id);
        if (matricula.isEmpty()) {
            flash.addFlashAttribute("error", "La matrícula no existe.");
            return "redirect:/matriculas";
        }
        model.addAttribute("matricula", matricula.get());
        cargarCombos(model);
        return "matriculas/matricula-form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("matricula") Matricula matricula, BindingResult result,
                          Model model, RedirectAttributes flash) {

        Long alumnoId = matricula.getAlumno() == null ? null : matricula.getAlumno().getId();
        Long cursoId = matricula.getCurso() == null ? null : matricula.getCurso().getId();

        Alumno alumno = alumnoId == null ? null : alumnos.findById(alumnoId).orElse(null);
        Curso curso = cursoId == null ? null : cursos.findById(cursoId).orElse(null);

        if (alumno == null) {
            result.addError(new FieldError("matricula", "alumno.id", "Selecciona un alumno"));
        }
        if (curso == null) {
            result.addError(new FieldError("matricula", "curso.id", "Selecciona un curso"));
        }
        if (alumno != null && curso != null) {
            matriculas.findByAlumnoIdAndCursoId(alumnoId, cursoId)
                    .filter(otra -> !Objects.equals(otra.getId(), matricula.getId()))
                    .ifPresent(otra -> result.addError(new FieldError("matricula", "curso.id",
                            "Este alumno ya está matriculado en ese curso")));
        }
        if (!result.hasFieldErrors("codigo")) {
            matriculas.findByCodigo(matricula.getCodigo())
                    .filter(otra -> !Objects.equals(otra.getId(), matricula.getId()))
                    .ifPresent(otra -> result.rejectValue("codigo", "duplicado", "Ya existe una matrícula con ese código"));
        }

        if (result.hasErrors()) {
            cargarCombos(model);
            return "matriculas/matricula-form";
        }

        // Sustituimos los campos vacios del formulario por las entidades reales de la base de datos.
        matricula.setAlumno(alumno);
        matricula.setCurso(curso);

        boolean esNueva = matricula.getId() == null;
        matriculas.save(matricula);
        flash.addFlashAttribute("exito", esNueva ? "Matrícula creada correctamente." : "Matrícula actualizada correctamente.");
        return "redirect:/matriculas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        if (pagos.existsByMatriculaId(id)) {
            flash.addFlashAttribute("error", "No se puede eliminar: la matrícula tiene pagos. Elimínalos primero.");
            return "redirect:/matriculas";
        }
        try {
            matriculas.deleteById(id);
            flash.addFlashAttribute("exito", "Matrícula eliminada.");
        } catch (DataIntegrityViolationException e) {
            // Por ejemplo, si ya tiene un certificado (entidad de Persona 3).
            flash.addFlashAttribute("error", "No se puede eliminar: otros registros dependen de esta matrícula.");
        }
        return "redirect:/matriculas";
    }

    /** Alumnos, cursos y estados ya existentes, para los desplegables del formulario. */
    private void cargarCombos(Model model) {
        model.addAttribute("alumnos", alumnos.findAll(Sort.by("apellidos", "nombre")));
        model.addAttribute("cursos", cursos.findAll(Sort.by("nombre")));
        model.addAttribute("estados", EstadoMatricula.values());
    }
}

