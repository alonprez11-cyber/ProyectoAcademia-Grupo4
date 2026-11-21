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
import org.alixar.daw2.Grupo4.Academia.entities.Evaluacion;
import org.alixar.daw2.Grupo4.Academia.entities.Examen;
import org.alixar.daw2.Grupo4.Academia.repository.AlumnoRepository;
import org.alixar.daw2.Grupo4.Academia.repository.EvaluacionRepository;
import org.alixar.daw2.Grupo4.Academia.repository.ExamenRepository;
import org.alixar.daw2.Grupo4.Academia.repository.MatriculaRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/evaluaciones")
public class EvaluacionController {

    private final EvaluacionRepository evaluaciones;
    private final AlumnoRepository alumnos;
    private final ExamenRepository examenes;
    private final MatriculaRepository matriculas;

    public EvaluacionController(EvaluacionRepository evaluaciones, AlumnoRepository alumnos,
                                ExamenRepository examenes, MatriculaRepository matriculas) {
        this.evaluaciones = evaluaciones;
        this.alumnos = alumnos;
        this.examenes = examenes;
        this.matriculas = matriculas;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("evaluaciones", evaluaciones.findAll(Sort.by("fecha").descending()));
        return "evaluaciones/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setFecha(LocalDate.now());
        model.addAttribute("evaluacion", evaluacion);
        cargarCombos(model);
        return "evaluaciones/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Evaluacion> evaluacion = evaluaciones.findById(id);
        if (evaluacion.isEmpty()) {
            flash.addFlashAttribute("error", "La evaluación no existe.");
            return "redirect:/evaluaciones";
        }
        model.addAttribute("evaluacion", evaluacion.get());
        cargarCombos(model);
        return "evaluaciones/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("evaluacion") Evaluacion evaluacion, BindingResult result,
                          Model model, RedirectAttributes flash) {

        Long alumnoId = evaluacion.getAlumno() == null ? null : evaluacion.getAlumno().getId();
        Long examenId = evaluacion.getExamen() == null ? null : evaluacion.getExamen().getId();
        Alumno alumno = alumnoId == null ? null : alumnos.findById(alumnoId).orElse(null);
        Examen examen = examenId == null ? null : examenes.findById(examenId).orElse(null);

        if (alumno == null) {
            result.addError(new FieldError("evaluacion", "alumno.id", "Selecciona un alumno"));
        }
        if (examen == null) {
            result.addError(new FieldError("evaluacion", "examen.id", "Selecciona un examen"));
        }

        if (examen != null) {
            // Nota: entre 0 y la nota máxima del examen.
            if (!result.hasFieldErrors("nota") && evaluacion.getNota().compareTo(examen.getNotaMaxima()) > 0) {
                result.rejectValue("nota", "excede",
                        "La nota no puede superar la nota máxima del examen (" + examen.getNotaMaxima() + ")");
            }
            // Fecha: no anterior a la del examen.
            if (!result.hasFieldErrors("fecha") && evaluacion.getFecha().isBefore(examen.getFecha())) {
                result.rejectValue("fecha", "anterior", "La fecha no puede ser anterior a la del examen");
            }
        }

        if (alumno != null && examen != null) {
            // El alumno debe estar matriculado en el curso del examen.
            Long cursoId = examen.getCurso().getId();
            if (matriculas.findByAlumnoIdAndCursoId(alumnoId, cursoId).isEmpty()) {
                result.addError(new FieldError("evaluacion", "alumno.id",
                        "El alumno no está matriculado en el curso de este examen"));
            }
            // Una sola nota por alumno y examen.
            evaluaciones.findByAlumnoIdAndExamenId(alumnoId, examenId)
                        .filter(otra -> !Objects.equals(otra.getId(), evaluacion.getId()))
                        .ifPresent(otra -> result.addError(new FieldError("evaluacion", "examen.id",
                                "Este alumno ya tiene nota en este examen")));
        }

        if (result.hasErrors()) {
            cargarCombos(model);
            return "evaluaciones/form";
        }

        evaluacion.setAlumno(alumno);
        evaluacion.setExamen(examen);

        boolean esNueva = evaluacion.getId() == null;
        evaluaciones.save(evaluacion);
        flash.addFlashAttribute("exito", esNueva ? "Evaluación registrada correctamente." : "Evaluación actualizada correctamente.");
        return "redirect:/evaluaciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            evaluaciones.deleteById(id);
            flash.addFlashAttribute("exito", "Evaluación eliminada.");
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("error", "No se puede eliminar esta evaluación.");
        }
        return "redirect:/evaluaciones";
    }

    private void cargarCombos(Model model) {
        model.addAttribute("alumnos", alumnos.findAll(Sort.by("apellidos", "nombre")));
        model.addAttribute("examenes", examenes.findAll(Sort.by("fecha").descending()));
    }
}
