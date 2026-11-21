package org.alixar.daw2.Grupo4.Academia.entities;

public enum TipoCertificado {
    ASISTENCIA("Asistencia"),
    APROVECHAMIENTO("Aprovechamiento");

    private final String etiqueta;

    TipoCertificado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
