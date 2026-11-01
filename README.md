# Backend Clínica Petsalud – Seguridad con Spring Security (PBY2202, Semana 1)

Autenticación **usuario (email) + contraseña** con HTTP Basic, contraseñas con **BCrypt**,
usuarios cargados desde MySQL (JPA/JDBC) y autorización por roles `CLIENTE`, `VETERINARIO`, `ADMIN`.

## Ejecutar
```bash
export DB_USERNAME=root DB_PASSWORD=tu_clave          # credenciales MySQL
export PETSALUD_ADMIN_EMAIL=admin@petsalud.cl         # opcional: crea el 1er ADMIN
export PETSALUD_ADMIN_PASSWORD='Admin12345'
./mvnw spring-boot:run
./mvnw test        # pruebas de seguridad con H2 en memoria
```

## Probar con curl
```bash
# Registro público (siempre crea CLIENTE)
curl -X POST localhost:8080/api/usuarios/registro -H 'Content-Type: application/json' \
  -d '{"email":"ana@correo.cl","password":"Segura123","nombre":"Ana","apellido":"Pérez"}'
# Perfil autenticado
curl -u ana@correo.cl:Segura123 localhost:8080/api/auth/me
# ADMIN crea un veterinario
curl -u admin@petsalud.cl:Admin12345 -X POST 'localhost:8080/api/admin/usuarios?rol=VETERINARIO' \
  -H 'Content-Type: application/json' \
  -d '{"email":"vet@petsalud.cl","password":"Vet123456","nombre":"Luis","apellido":"Mora"}'
```

## Matriz de acceso
| Recurso | CLIENTE | VETERINARIO | ADMIN |
|---|---|---|---|
| Registro `POST /api/usuarios/registro` | público | público | público |
| `GET/PUT /api/usuarios/{id}` | solo el propio | – | todos |
| `GET /api/usuarios`, `DELETE /api/usuarios/**`, `/api/admin/**` | ✗ | ✗ | ✓ |
| Productos (lectura) | ✓ | ✓ | ✓ |
| Productos (crear/editar/borrar), stock bajo, listar ventas, recompensas (crear/borrar) | ✗ | ✗ | ✓ |
| Mascotas / expedientes / citas por mascota | solo las suyas | todas | todas |
| Cambiar estado de cita, citas por veterinario/fechas, escribir expedientes | ✗ | ✓ | ✓ |
| Enviar notificaciones | ✗ | ✓ | ✓ |
