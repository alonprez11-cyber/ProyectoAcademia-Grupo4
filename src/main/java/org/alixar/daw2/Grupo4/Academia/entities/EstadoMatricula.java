package org.alixar.daw2.Grupo4.Academia.entities;

public enum EstadoMatricula {
    PENDIENTE("Pendiente"), ACTIVA("Activa"), FINALIZADA("Finalizada"), CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoMatricula(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto para mostrar en listados y desplegables. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
