package org.alixar.daw2.Grupo4.Academia.config;

import org.alixar.daw2.Grupo4.Academia.entities.Curso;
import org.alixar.daw2.Grupo4.Academia.repository.CursoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TEMPORAL: crea dos cursos de ejemplo si no hay ninguno, para probar matrículas
 * antes de que exista el CRUD de cursos. Borrad esta clase cuando Persona 2 lo haya subido.
 */
@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cursosDeEjemplo(CursoRepository cursos) {
        return args -> {
            if (cursos.count() == 0) {
                for (String nombre : new String[] { "Inglés B2", "Programación Java" }) {
                    Curso c = new Curso();
                    c.setNombre(nombre);
                    cursos.save(c);
                }
            }
        };
    }
}
