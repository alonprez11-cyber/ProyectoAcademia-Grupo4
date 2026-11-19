package org.alixar.daw2.Grupo4.Academia.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Guarda y borra imágenes en la carpeta de subidas del servidor.
 */
@Service
public class FileStorageService {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path raiz;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.raiz = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Guarda la imagen en uploads/{subcarpeta}/ con un nombre aleatorio.
     *
     * @return la ruta relativa a guardar en base de datos (p. ej. "profesores/ab12.jpg"),
     *         o null si no se ha subido ningún fichero (campo vacío en el formulario).
     * @throws IllegalArgumentException si el fichero no es una imagen válida
     *         (el controlador puede capturarla y añadirla como error del formulario).
     */
    public String guardarImagen(MultipartFile fichero, String subcarpeta) {
        if (fichero == null || fichero.isEmpty()) {
            return null;
        }

        String extension = extensionDe(fichero.getOriginalFilename());
        String contentType = fichero.getContentType();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)
                || contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("El fichero debe ser una imagen (jpg, png, gif o webp).");
        }

        // El nombre original del usuario nunca se usa: evita colisiones y rutas maliciosas.
        String nombre = UUID.randomUUID() + "." + extension;
        Path carpeta = raiz.resolve(subcarpeta).normalize();
        if (!carpeta.startsWith(raiz)) {
            throw new IllegalArgumentException("Subcarpeta no válida.");
        }

        try (InputStream in = fichero.getInputStream()) {
            Files.createDirectories(carpeta);
            Files.copy(in, carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el fichero en el servidor.", e);
        }
        return subcarpeta + "/" + nombre;
    }

    /** Borra un fichero a partir de la ruta guardada en BD. Si no existe, no hace nada. */
    public void borrar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }
        Path destino = raiz.resolve(rutaRelativa).normalize();
        if (!destino.startsWith(raiz)) {
            return;
        }
        try {
            Files.deleteIfExists(destino);
        } catch (IOException e) {
            // Un fichero que no se puede borrar no debe impedir borrar el registro.
        }
    }

    private String extensionDe(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return "";
        }
        return nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
