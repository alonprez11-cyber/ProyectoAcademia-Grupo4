CREATE DATABASE IF NOT EXISTS academia;
USE academia;

-- ─────────────────────────────────────────────
-- AULA
-- ─────────────────────────────────────────────
CREATE TABLE aula (
                      id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
                      codigo      VARCHAR(20)  NOT NULL UNIQUE,
                      nombre      VARCHAR(100) NOT NULL,
                      capacidad   INT          NOT NULL,
                      ubicacion   VARCHAR(200),
                      tiene_proyector BOOLEAN  NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- ALUMNO
-- ─────────────────────────────────────────────
CREATE TABLE alumno (
                        id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
                        dni             VARCHAR(9)   NOT NULL UNIQUE,
                        nombre          VARCHAR(100) NOT NULL,
                        apellidos       VARCHAR(200) NOT NULL,
                        email           VARCHAR(200) NOT NULL UNIQUE,
                        telefono        VARCHAR(20),
                        fecha_nacimiento DATE        NOT NULL,
                        activo          BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- PROFESOR
-- ─────────────────────────────────────────────
CREATE TABLE profesor (
                          id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
                          dni         VARCHAR(9)   NOT NULL UNIQUE,
                          nombre      VARCHAR(100) NOT NULL,
                          apellidos   VARCHAR(200) NOT NULL,
                          email       VARCHAR(200) NOT NULL UNIQUE,
                          especialidad VARCHAR(150),
                          activo      BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- CURSO (FK → aula)
-- ─────────────────────────────────────────────
CREATE TABLE curso (
                       id          BIGINT         AUTO_INCREMENT PRIMARY KEY,
                       codigo      VARCHAR(20)    NOT NULL UNIQUE,
                       nombre      VARCHAR(150)   NOT NULL,
                       descripcion TEXT,
                       horas       INT            NOT NULL,
                       precio      DECIMAL(10,2)  NOT NULL,
                       fecha_inicio DATE          NOT NULL,
                       fecha_fin   DATE           NOT NULL,
                       activo      BOOLEAN        NOT NULL DEFAULT TRUE,
                       aula_id     BIGINT,
                       CONSTRAINT fk_curso_aula FOREIGN KEY (aula_id) REFERENCES aula(id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- PROFESOR_CURSO (ManyToMany)
-- ─────────────────────────────────────────────
CREATE TABLE profesor_curso (
                                profesor_id BIGINT NOT NULL,
                                curso_id    BIGINT NOT NULL,
                                PRIMARY KEY (profesor_id, curso_id),
                                CONSTRAINT fk_pc_profesor FOREIGN KEY (profesor_id) REFERENCES profesor(id),
                                CONSTRAINT fk_pc_curso    FOREIGN KEY (curso_id)    REFERENCES curso(id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- MATRÍCULA (FK → alumno, curso)
-- ─────────────────────────────────────────────
CREATE TABLE matricula (
                           id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
                           codigo         VARCHAR(20)  NOT NULL UNIQUE,
                           fecha_matricula DATE        NOT NULL,
                           estado         VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
                           alumno_id      BIGINT       NOT NULL,
                           curso_id       BIGINT       NOT NULL,
                           CONSTRAINT fk_matricula_alumno FOREIGN KEY (alumno_id) REFERENCES alumno(id),
                           CONSTRAINT fk_matricula_curso  FOREIGN KEY (curso_id)  REFERENCES curso(id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- PAGO (FK → matricula)
-- ─────────────────────────────────────────────
CREATE TABLE pago (
                      id          BIGINT         AUTO_INCREMENT PRIMARY KEY,
                      referencia  VARCHAR(50)    NOT NULL UNIQUE,
                      fecha       DATE           NOT NULL,
                      importe     DECIMAL(10,2)  NOT NULL,
                      metodo      VARCHAR(30)    NOT NULL,
                      estado      VARCHAR(30)    NOT NULL DEFAULT 'PENDIENTE',
                      matricula_id BIGINT        NOT NULL,
                      CONSTRAINT fk_pago_matricula FOREIGN KEY (matricula_id) REFERENCES matricula(id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- EXAMEN (FK → curso)
-- ─────────────────────────────────────────────
CREATE TABLE examen (
                        id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
                        titulo      VARCHAR(200) NOT NULL,
                        fecha       DATE        NOT NULL,
                        nota_maxima DECIMAL(5,2) NOT NULL DEFAULT 10.00,
                        porcentaje  DECIMAL(5,2) NOT NULL,
                        curso_id    BIGINT      NOT NULL,
                        CONSTRAINT fk_examen_curso FOREIGN KEY (curso_id) REFERENCES curso(id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- EVALUACIÓN (FK → alumno, examen)
-- ─────────────────────────────────────────────
CREATE TABLE evaluacion (
                            id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
                            fecha       DATE         NOT NULL,
                            nota        DECIMAL(5,2) NOT NULL,
                            observaciones TEXT,
                            alumno_id   BIGINT       NOT NULL,
                            examen_id   BIGINT       NOT NULL,
                            CONSTRAINT fk_eval_alumno FOREIGN KEY (alumno_id) REFERENCES alumno(id),
                            CONSTRAINT fk_eval_examen FOREIGN KEY (examen_id) REFERENCES examen(id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- CERTIFICADO (OneToOne → matricula)
-- ─────────────────────────────────────────────
CREATE TABLE certificado (
                             id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
                             codigo        VARCHAR(50)  NOT NULL UNIQUE,
                             fecha_emision DATE         NOT NULL,
                             tipo          VARCHAR(50)  NOT NULL,
                             matricula_id  BIGINT       NOT NULL UNIQUE,
                             CONSTRAINT fk_cert_matricula FOREIGN KEY (matricula_id) REFERENCES matricula(id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
-- RECIBO (OneToOne → pago)
-- ─────────────────────────────────────────────
CREATE TABLE recibo (
                        id            BIGINT         AUTO_INCREMENT PRIMARY KEY,
                        numero        VARCHAR(50)    NOT NULL UNIQUE,
                        fecha_emision DATE           NOT NULL,
                        importe       DECIMAL(10,2)  NOT NULL,
                        concepto      VARCHAR(200)   NOT NULL,
                        pago_id       BIGINT         NOT NULL UNIQUE,
                        CONSTRAINT fk_recibo_pago FOREIGN KEY (pago_id) REFERENCES pago(id)
) ENGINE=InnoDB;