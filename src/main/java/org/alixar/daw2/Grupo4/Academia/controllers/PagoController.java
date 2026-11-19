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

import org.alixar.daw2.Grupo4.Academia.entities.EstadoPago;
import org.alixar.daw2.Grupo4.Academia.entities.Matricula;
import org.alixar.daw2.Grupo4.Academia.entities.MetodoPago;
import org.alixar.daw2.Grupo4.Academia.entities.Pago;
import org.alixar.daw2.Grupo4.Academia.repository.MatriculaRepository;
import org.alixar.daw2.Grupo4.Academia.repository.PagoRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoRepository pagos;
    private final MatriculaRepository matriculas;

    public PagoController(PagoRepository pagos, MatriculaRepository matriculas) {
        this.pagos = pagos;
        this.matriculas = matriculas;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pagos", pagos.findAll(Sort.by("fecha").descending()));
        return "pagos/Pagos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Pago pago = new Pago();
        pago.setFecha(LocalDate.now());
        pago.setEstado(EstadoPago.PENDIENTE);
        model.addAttribute("pago", pago);
        cargarCombos(model);
        return "pagos/pagos-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Pago> pago = pagos.findById(id);
        if (pago.isEmpty()) {
            flash.addFlashAttribute("error", "El pago no existe.");
            return "redirect:/pagos";
        }
        model.addAttribute("pago", pago.get());
        cargarCombos(model);
        return "pagos/pagos-form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("pago") Pago pago, BindingResult result,
                          Model model, RedirectAttributes flash) {

        Long matriculaId = pago.getMatricula() == null ? null : pago.getMatricula().getId();
        Matricula matricula = matriculaId == null ? null : matriculas.findById(matriculaId).orElse(null);
        if (matricula == null) {
            result.addError(new FieldError("pago", "matricula.id", "Selecciona una matrícula"));
        }

        if (!result.hasFieldErrors("referencia")) {
            pagos.findByReferencia(pago.getReferencia())
                    .filter(otro -> !Objects.equals(otro.getId(), pago.getId()))
                    .ifPresent(otro -> result.rejectValue("referencia", "duplicado", "Ya existe un pago con esa referencia"));
        }

        if (result.hasErrors()) {
            cargarCombos(model);
            return "pagos/pagos-form";
        }

        pago.setMatricula(matricula);

        boolean esNuevo = pago.getId() == null;
        pagos.save(pago);
        flash.addFlashAttribute("exito", esNuevo ? "Pago registrado correctamente." : "Pago actualizado correctamente.");
        return "redirect:/pagos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            pagos.deleteById(id);
            flash.addFlashAttribute("exito", "Pago eliminado.");
        } catch (DataIntegrityViolationException e) {
            // Por ejemplo, si ya tiene un recibo asociado (entidad de Persona 3).
            flash.addFlashAttribute("error", "No se puede eliminar: otros registros dependen de este pago (p. ej. su recibo).");
        }
        return "redirect:/pagos";
    }

    private void cargarCombos(Model model) {
        model.addAttribute("matriculas", matriculas.findAll(Sort.by("codigo")));
        model.addAttribute("metodos", MetodoPago.values());
        model.addAttribute("estados", EstadoPago.values());
    }
}

