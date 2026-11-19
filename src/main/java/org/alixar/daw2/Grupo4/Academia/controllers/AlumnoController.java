package org.alixar.daw2.Grupo4.Academia.controllers;

import java.util.Objects;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.alixar.daw2.Grupo4.Academia.entities.Alumno;
import org.alixar.daw2.Grupo4.Academia.repository.AlumnoRepository;
import org.alixar.daw2.Grupo4.Academia.repository.MatriculaRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoRepository alumnos;
    private final MatriculaRepository matriculas;

    public AlumnoController(AlumnoRepository alumnos, MatriculaRepository matriculas) {
        this.alumnos = alumnos;
        this.matriculas = matriculas;
    }

    /** Lista completa de alumnos. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("alumnos", alumnos.findAll(Sort.by("apellidos", "nombre")));
        return "alumnos/Alumno";
    }

    /** Formulario para dar de alta. */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("alumno", new Alumno());
        return "alumnos/alumno-form";
    }

    /** Formulario en modo edicion. */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Alumno> alumno = alumnos.findById(id);
        if (alumno.isEmpty()) {
            flash.addFlashAttribute("error", "El alumno no existe.");
            return "redirect:/alumnos";
        }
        model.addAttribute("alumno", alumno.get());
        return "alumnos/alumno-form";
    }

    /** Guarda el alta o la edición. Si hay errores, vuelve al formulario conservando lo escrito. */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("alumno") Alumno alumno, BindingResult result,
                          RedirectAttributes flash) {

        if (!result.hasFieldErrors("dni")) {
            alumnos.findByDni(alumno.getDni())
                    .filter(otro -> !Objects.equals(otro.getId(), alumno.getId()))
                    .ifPresent(otro -> result.rejectValue("dni", "duplicado", "Ya existe un alumno con ese DNI"));
        }
        if (!result.hasFieldErrors("email")) {
            alumnos.findByEmail(alumno.getEmail())
                    .filter(otro -> !Objects.equals(otro.getId(), alumno.getId()))
                    .ifPresent(otro -> result.rejectValue("email", "duplicado", "Ya existe un alumno con ese email"));
        }

        if (result.hasErrors()) {
            return "alumnos/Alumno";
        }

        boolean esNuevo = alumno.getId() == null;
        alumnos.save(alumno);
        flash.addFlashAttribute("exito", esNuevo ? "Alumno creado correctamente." : "Alumno actualizado correctamente.");
        return "redirect:/alumnos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        if (matriculas.existsByAlumnoId(id)) {
            flash.addFlashAttribute("error", "No se puede eliminar: el alumno tiene matrículas. Elimínalas primero.");
            return "redirect:/alumnos";
        }
        try {
            alumnos.deleteById(id);
            flash.addFlashAttribute("exito", "Alumno eliminado.");
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("error", "No se puede eliminar: otros registros dependen de este alumno.");
        }
        return "redirect:/alumnos";
    }
}

