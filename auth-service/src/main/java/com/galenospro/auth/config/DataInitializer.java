package com.galenospro.auth.config;

import com.galenospro.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            log.info("Usuarios ya existen, omitiendo seed.");
            return;
        }

        crearSecuencia();

        insertar(1L, "Admin",   "Galenos", "admin@bernales.gob.pe",           "Galenos123!", "Administrador", "ADMIN",         null);
        insertar(2L, "Juan",    "Perez",   "farmaceutico@bernales.gob.pe",    "Galenos123!", "Farmacéutico",  "FARMACEUTICO",  1L);
        insertar(3L, "Maria",   "Lopez",   "jefe@bernales.gob.pe",            "Galenos123!", "Jefe Farmacia", "JEFE_FARMACIA", 1L);
        insertar(4L, "Carlos",  "Quispe",  "almacenero@bernales.gob.pe",      "Galenos123!", "Almacenero",    "ALMACENERO",    null);

        log.info("4 usuarios de prueba creados. Password: Galenos123!");
    }

    private void crearSecuencia() {
        try {
            jdbcTemplate.execute("CREATE SEQUENCE SEQ_USUARIO START WITH 5 INCREMENT BY 1 NOCACHE NOCYCLE");
            log.info("Secuencia SEQ_USUARIO creada.");
        } catch (Exception e) {
            log.info("Secuencia SEQ_USUARIO ya existe.");
        }
    }

    private void insertar(Long id, String nombres, String apellidos, String email,
                          String password, String cargo, String rol, Long farmaciaId) {
        try {
            jdbcTemplate.update(
                "INSERT INTO USUARIO (ID, NOMBRES, APELLIDOS, EMAIL, PASSWORD, CARGO, ROL, FARMACIA_ID, ACTIVO, FECHA_CREACION) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, SYSDATE)",
                id, nombres, apellidos, email,
                passwordEncoder.encode(password),
                cargo, rol, farmaciaId
            );
            log.info("Usuario creado: {} ({})", email, rol);
        } catch (Exception e) {
            log.warn("No se pudo crear usuario {}: {}", email, e.getMessage());
        }
    }
}
