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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.alixar.daw2.Grupo4.Academia.entities.Certificado;
import org.alixar.daw2.Grupo4.Academia.entities.EstadoMatricula;
import org.alixar.daw2.Grupo4.Academia.entities.Matricula;
import org.alixar.daw2.Grupo4.Academia.entities.TipoCertificado;
import org.alixar.daw2.Grupo4.Academia.repository.CertificadoRepository;
import org.alixar.daw2.Grupo4.Academia.repository.MatriculaRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/certificados")
public class CertificadoController {

    private final CertificadoRepository certificados;
    private final MatriculaRepository matriculas;

    public CertificadoController(CertificadoRepository certificados, MatriculaRepository matriculas) {
        this.certificados = certificados;
        this.matriculas = matriculas;
    }

    /** Listado + matrículas que aún no tienen certificado (con botón para generarlo). */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("certificados", certificados.findAll(Sort.by("fechaEmision").descending()));
        model.addAttribute("pendientes", certificados.matriculasSinCertificado(EstadoMatricula.CANCELADA));
        return "certificados/lista";
    }

    /**
     * Alta. Si llega ?matriculaId=..., el formulario sale ya relleno ("generar certificado"):
     * código sugerido, fecha de hoy y esa matrícula seleccionada.
     */
    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(name = "matriculaId", required = false) Long matriculaId, Model model) {
        Certificado certificado = new Certificado();
        certificado.setCodigo(siguienteCodigo());
        certificado.setFechaEmision(LocalDate.now());
        certificado.setTipo(TipoCertificado.APROVECHAMIENTO);
        if (matriculaId != null) {
            matriculas.findById(matriculaId).ifPresent(certificado::setMatricula);
        }
        model.addAttribute("certificado", certificado);
        cargarCombos(model);
        return "certificados/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Certificado> certificado = certificados.findById(id);
        if (certificado.isEmpty()) {
            flash.addFlashAttribute("error", "El certificado no existe.");
            return "redirect:/certificados";
        }
        model.addAttribute("certificado", certificado.get());
        cargarCombos(model);
        return "certificados/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("certificado") Certificado certificado, BindingResult result,
                          Model model, RedirectAttributes flash) {

        Long matriculaId = certificado.getMatricula() == null ? null : certificado.getMatricula().getId();
        Matricula matricula = matriculaId == null ? null : matriculas.findById(matriculaId).orElse(null);

        if (matricula == null) {
            result.addError(new FieldError("certificado", "matricula.id", "Selecciona una matrícula"));
        } else {
            if (matricula.getEstado() == EstadoMatricula.CANCELADA) {
                result.addError(new FieldError("certificado", "matricula.id",
                        "No se puede certificar una matrícula cancelada"));
            }
            // @OneToOne: una matrícula solo puede tener un certificado.
            certificados.findByMatriculaId(matriculaId)
                        .filter(otro -> !Objects.equals(otro.getId(), certificado.getId()))
                        .ifPresent(otro -> result.addError(new FieldError("certificado", "matricula.id",
                                "Esta matrícula ya tiene el certificado " + otro.getCodigo())));
        }

        if (!result.hasFieldErrors("codigo")) {
            certificados.findByCodigo(certificado.getCodigo())
                        .filter(otro -> !Objects.equals(otro.getId(), certificado.getId()))
                        .ifPresent(otro -> result.rejectValue("codigo", "duplicado", "Ya existe un certificado con ese código"));
        }

        if (result.hasErrors()) {
            cargarCombos(model);
            return "certificados/form";
        }

        certificado.setMatricula(matricula);

        boolean esNuevo = certificado.getId() == null;
        certificados.save(certificado);
        flash.addFlashAttribute("exito", esNuevo ? "Certificado generado correctamente." : "Certificado actualizado correctamente.");
        return "redirect:/certificados";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            certificados.deleteById(id);
            flash.addFlashAttribute("exito", "Certificado eliminado.");
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("error", "No se puede eliminar este certificado.");
        }
        return "redirect:/certificados";
    }

    private void cargarCombos(Model model) {
        model.addAttribute("matriculas", matriculas.findAll(Sort.by("codigo")));
        model.addAttribute("tipos", TipoCertificado.values());
    }

    /** CER-00001, CER-00002... a partir del último id (el usuario puede cambiarlo). */
    private String siguienteCodigo() {
        long siguiente = certificados.findTopByOrderByIdDesc().map(c -> c.getId() + 1).orElse(1L);
        return String.format("CER-%05d", siguiente);
    }
}
