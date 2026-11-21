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

import org.alixar.daw2.Grupo4.Academia.entities.EstadoPago;
import org.alixar.daw2.Grupo4.Academia.entities.Pago;
import org.alixar.daw2.Grupo4.Academia.entities.Recibo;
import org.alixar.daw2.Grupo4.Academia.repository.PagoRepository;
import org.alixar.daw2.Grupo4.Academia.repository.ReciboRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/recibos")
public class ReciboController {

    private final ReciboRepository recibos;
    private final PagoRepository pagos;

    public ReciboController(ReciboRepository recibos, PagoRepository pagos) {
        this.recibos = recibos;
        this.pagos = pagos;
    }

    /** Listado + pagos completados que aún no tienen recibo (con botón para generarlo). */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("recibos", recibos.findAll(Sort.by("fechaEmision").descending()));
        model.addAttribute("pendientes", recibos.pagosSinRecibo(EstadoPago.COMPLETADO));
        return "recibos/lista";
    }

    /**
     * Alta. Si llega ?pagoId=..., el formulario sale ya relleno ("generar recibo"):
     * número sugerido, fecha de hoy, ese pago, su importe y un concepto.
     */
    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(name = "pagoId", required = false) Long pagoId, Model model) {
        Recibo recibo = new Recibo();
        recibo.setNumero(siguienteNumero());
        recibo.setFechaEmision(LocalDate.now());
        if (pagoId != null) {
            pagos.findById(pagoId).ifPresent(pago -> {
                recibo.setPago(pago);
                recibo.setImporte(pago.getImporte());
                recibo.setConcepto("Pago " + pago.getReferencia() + " de la matrícula " + pago.getMatricula().getCodigo());
            });
        }
        model.addAttribute("recibo", recibo);
        cargarCombos(model);
        return "recibos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Recibo> recibo = recibos.findById(id);
        if (recibo.isEmpty()) {
            flash.addFlashAttribute("error", "El recibo no existe.");
            return "redirect:/recibos";
        }
        model.addAttribute("recibo", recibo.get());
        cargarCombos(model);
        return "recibos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("recibo") Recibo recibo, BindingResult result,
                          Model model, RedirectAttributes flash) {

        Long pagoId = recibo.getPago() == null ? null : recibo.getPago().getId();
        Pago pago = pagoId == null ? null : pagos.findById(pagoId).orElse(null);

        if (pago == null) {
            result.addError(new FieldError("recibo", "pago.id", "Selecciona un pago"));
        } else {
            if (pago.getEstado() != EstadoPago.COMPLETADO) {
                result.addError(new FieldError("recibo", "pago.id", "Solo se puede emitir recibo de pagos completados"));
            }
            // @OneToOne: un pago solo puede tener un recibo.
            recibos.findByPagoId(pagoId)
                   .filter(otro -> !Objects.equals(otro.getId(), recibo.getId()))
                   .ifPresent(otro -> result.addError(new FieldError("recibo", "pago.id",
                           "Este pago ya tiene el recibo " + otro.getNumero())));
            // El recibo justifica el pago: mismo importe.
            if (!result.hasFieldErrors("importe") && recibo.getImporte().compareTo(pago.getImporte()) != 0) {
                result.rejectValue("importe", "distinto",
                        "El importe debe coincidir con el del pago (" + pago.getImporte() + " €)");
            }
        }

        if (!result.hasFieldErrors("numero")) {
            recibos.findByNumero(recibo.getNumero())
                   .filter(otro -> !Objects.equals(otro.getId(), recibo.getId()))
                   .ifPresent(otro -> result.rejectValue("numero", "duplicado", "Ya existe un recibo con ese número"));
        }

        if (result.hasErrors()) {
            cargarCombos(model);
            return "recibos/form";
        }

        recibo.setPago(pago);

        boolean esNuevo = recibo.getId() == null;
        recibos.save(recibo);
        flash.addFlashAttribute("exito", esNuevo ? "Recibo generado correctamente." : "Recibo actualizado correctamente.");
        return "redirect:/recibos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            recibos.deleteById(id);
            flash.addFlashAttribute("exito", "Recibo eliminado.");
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("error", "No se puede eliminar este recibo.");
        }
        return "redirect:/recibos";
    }

    private void cargarCombos(Model model) {
        model.addAttribute("pagos", pagos.findAll(Sort.by("referencia")));
    }

    /** RCB-000001, RCB-000002... a partir del último id (el usuario puede cambiarlo). */
    private String siguienteNumero() {
        long siguiente = recibos.findTopByOrderByIdDesc().map(r -> r.getId() + 1).orElse(1L);
        return String.format("RCB-%06d", siguiente);
    }
}
