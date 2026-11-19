package org.alixar.daw2.Grupo4.Academia.entities;

public enum EstadoPago {
    PENDIENTE("Pendiente"), COMPLETADO("Completado"), DEVUELTO("Devuelto");

    private final String etiqueta;

    EstadoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto para mostrar en listados y desplegables. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
