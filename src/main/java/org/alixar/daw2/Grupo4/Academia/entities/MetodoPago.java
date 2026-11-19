package org.alixar.daw2.Grupo4.Academia.entities;

public enum MetodoPago {
    EFECTIVO("Efectivo"), TARJETA("Tarjeta"), TRANSFERENCIA("Transferencia"), BIZUM("Bizum");

    private final String etiqueta;

    MetodoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto para mostrar en listados y desplegables. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
