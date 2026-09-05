# Migración de PostgreSQL a Supabase

El backend ya usa Spring Data JPA y PostgreSQL. La migración consiste en copiar el
esquema y los datos al PostgreSQL administrado por Supabase y cambiar la conexión
del backend; no es necesario reemplazar los repositorios JPA.

## 1. Crear y preparar Supabase

1. Crea un proyecto en [Supabase](https://database.new/).
2. En **Connect**, guarda estas dos conexiones:
   - **Session pooler / URI**, puerto `5432`, para `pg_dump` y `pg_restore`.
   - **Session pooler / JDBC**, puerto `5432`, para Spring Boot.
3. Agrega `sslmode=require` a ambas conexiones.
4. No uses el Transaction pooler (`6543`) con Hibernate.

La contraseña incluida en la URL JDBC debe codificar caracteres reservados como
`&`, `#`, `?` o espacios.

## 2. Migrar el esquema y los datos

Programa una ventana de mantenimiento y detén temporalmente las escrituras en la
base de datos de origen. Ejecuta desde la raíz del backend:

```bash
read -rsp "URL PostgreSQL de origen: " SOURCE_DB_URL && echo
export SOURCE_DB_URL
read -rsp "URL URI de Supabase: " SUPABASE_MIGRATION_DB_URL && echo
export SUPABASE_MIGRATION_DB_URL
./scripts/migrate-postgres-to-supabase.sh
unset SOURCE_DB_URL SUPABASE_MIGRATION_DB_URL
```

El script:

- se niega a sobrescribir un destino con tablas, salvo autorización explícita;
- exporta solamente el esquema `public`, sin propietarios ni privilegios;
- restaura el esquema y los datos en Supabase;
- compara el número de registros de cada tabla;
- conserva el respaldo y los reportes dentro de `.migration/`.

Si el proyecto Supabase no está vacío, revisa primero sus tablas y ejecuta con
`ALLOW_NONEMPTY_TARGET=true` únicamente cuando hayas confirmado que no existen
conflictos.

## 3. Conectar el backend

Configura en el servicio donde está desplegado Spring Boot:

```text
SUPABASE_DB_URL=jdbc:postgresql://aws-0-REGION.pooler.supabase.com:5432/postgres?user=postgres.PROJECT_REF&password=URL_ENCODED_PASSWORD&sslmode=require
JWT_SECRET=...
CORS_ALLOWED_ORIGINS=https://dominio-del-frontend
```

Reinicia el backend. Hibernate usa `ddl-auto=validate`, por lo que comprobará el
esquema sin crear, eliminar ni alterar tablas.

## 4. Verificar antes de habilitar tráfico

1. Confirma que `/auth/health` responde `OK`.
2. Prueba inicio de sesión y registro.
3. Lista y registra pacientes.
4. Crea una evaluación y consulta el historial.
5. Revisa los conteos generados en `.migration/`.

Si falla la validación, vuelve a configurar `SUPABASE_DB_URL` con la URL JDBC de
la base anterior y reinicia el backend.

## 5. Seguridad en Supabase

La aplicación accede directamente por JDBC y no necesita exponer estas tablas
mediante la Data API. En la configuración de API de Supabase, elimina `public`
de los esquemas expuestos si no se consumirá desde el frontend. Si sí se usará
la Data API, habilita RLS y crea políticas antes de entregar las claves públicas.

Nunca guardes URLs con contraseñas, claves JWT ni claves de correo en Git.
