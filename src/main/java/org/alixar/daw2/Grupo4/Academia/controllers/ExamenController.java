package org.alixar.daw2.Grupo4.Academia.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import org.alixar.daw2.Grupo4.Academia.entities.Curso;
import org.alixar.daw2.Grupo4.Academia.entities.Examen;
import org.alixar.daw2.Grupo4.Academia.repository.CursoRepository;
import org.alixar.daw2.Grupo4.Academia.repository.EvaluacionRepository;
import org.alixar.daw2.Grupo4.Academia.repository.ExamenRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/examenes")
public class ExamenController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ExamenRepository examenes;
    private final CursoRepository cursos;
    private final EvaluacionRepository evaluaciones;

    public ExamenController(ExamenRepository examenes, CursoRepository cursos, EvaluacionRepository evaluaciones) {
        this.examenes = examenes;
        this.cursos = cursos;
        this.evaluaciones = evaluaciones;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("examenes", examenes.findAll(Sort.by("fecha").descending()));
        return "examenes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Examen examen = new Examen();
        examen.setNotaMaxima(new java.math.BigDecimal("10.00"));
        model.addAttribute("examen", examen);
        cargarCombos(model);
        return "examenes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Examen> examen = examenes.findById(id);
        if (examen.isEmpty()) {
            flash.addFlashAttribute("error", "El examen no existe.");
            return "redirect:/examenes";
        }
        model.addAttribute("examen", examen.get());
        cargarCombos(model);
        return "examenes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("examen") Examen examen, BindingResult result,
                          Model model, RedirectAttributes flash) {

        Long cursoId = examen.getCurso() == null ? null : examen.getCurso().getId();
        Curso curso = cursoId == null ? null : cursos.findById(cursoId).orElse(null);
        if (curso == null) {
            result.addError(new FieldError("examen", "curso.id", "Selecciona un curso"));
        } else {
            // Fecha: debe caer dentro de las fechas del curso.
            if (!result.hasFieldErrors("fecha")) {
                LocalDate fecha = examen.getFecha();
                if (fecha.isBefore(curso.getFechaInicio()) || fecha.isAfter(curso.getFechaFin())) {
                    result.rejectValue("fecha", "fueraDeCurso",
                            "La fecha debe estar entre el inicio (" + curso.getFechaInicio().format(FORMATO)
                            + ") y el fin (" + curso.getFechaFin().format(FORMATO) + ") del curso");
                }
            }
            // Porcentaje: los exámenes de un curso no pueden sumar más de 100 %.
            if (!result.hasFieldErrors("porcentaje")) {
                int otros = examenes.findByCursoId(cursoId).stream()
                        .filter(e -> !Objects.equals(e.getId(), examen.getId()))
                        .mapToInt(Examen::getPorcentaje)
                        .sum();
                if (otros + examen.getPorcentaje() > 100) {
                    result.rejectValue("porcentaje", "exceso",
                            "Los exámenes de este curso sumarían " + (otros + examen.getPorcentaje())
                            + " % (máximo 100 %). Ya hay " + otros + " % asignado.");
                }
            }
        }

        if (result.hasErrors()) {
            cargarCombos(model);
            return "examenes/form";
        }

        examen.setCurso(curso);

        boolean esNuevo = examen.getId() == null;
        examenes.save(examen);
        flash.addFlashAttribute("exito", esNuevo ? "Examen creado correctamente." : "Examen actualizado correctamente.");
        return "redirect:/examenes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        if (evaluaciones.existsByExamenId(id)) {
            flash.addFlashAttribute("error", "No se puede eliminar: el examen ya tiene evaluaciones.");
            return "redirect:/examenes";
        }
        try {
            examenes.deleteById(id);
            flash.addFlashAttribute("exito", "Examen eliminado.");
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("error", "No se puede eliminar: otros registros dependen de este examen.");
        }
        return "redirect:/examenes";
    }

    private void cargarCombos(Model model) {
        model.addAttribute("cursos", cursos.findAll(Sort.by("nombre")));
    }
}
