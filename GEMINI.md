# Project Guidelines & Memory

## Arquitectura del Proyecto (Clean Architecture)
Este proyecto se desarrollará estrictamente bajo los principios de **Clean Architecture**. Todo el código generado debe respetar la separación de responsabilidades y la regla de dependencia:

1. **Capa de Dominio (`domain`):**
   - Núcleo del negocio, completamente independiente de frameworks, UI o librerías de plataforma.
   - Contiene:
     - **Modelos de dominio:** Entidades puras de Kotlin.
     - **Casos de Uso (`UseCases` / `Interactors`):** Encapsulan una sola acción de lógica de negocio.
     - **Interfaces de repositorios:** Contratos que la capa de datos debe implementar.
2. **Capa de Datos (`data`):**
   - Implementa los contratos definidos en la capa de dominio.
   - Contiene:
     - **Implementaciones de repositorios** (`*RepositoryImpl`).
     - **Fuentes de datos (`DataSources`):** Remotas (API/RSS parsers/HTTP) y locales (caché, base de datos, almacenamiento).
     - **Modelos de datos (DTOs):** Mappers para convertir entre DTOs y modelos de dominio.
3. **Capa de Presentación (`presentation` / `ui`):**
   - Componentes visuales en Compose Multiplatform y gestión de estado con ViewModels.
   - Depende de los Casos de Uso de dominio para interactuar con los datos.
   - Flujo unidireccional de datos (UDF) con estados inmutables (`UiState`) y eventos (`UiEvent`).
   - **Gestión de Cadenas de Texto (Strings):** Prohibido dejar textos en crudo (*hardcoded strings*) en los componentes visuales de la UI. Todo texto debe extraerse a un archivo separado (recursos de strings o archivo dedicado de cadenas).
4. **Regla de Dependencia:**
   - `presentation` -> `domain` <- `data`.
   - La capa de dominio es pura y nunca debe depender de `data`, `presentation` ni de APIs específicas de una plataforma o framework externo.

---

## Reglas para Commits
Cuando el usuario solicite realizar un commit o preparar commits:
1. **Paso previo obligatorio (Ktlint, Detekt y Strings):**
   - Antes de ejecutar la skill `commit` o registrar cualquier cambio en Git, revisar y corregir todos los problemas señalados por **ktlint** y **detekt**.
   - Especial atención a la regla de Detekt en `detekt.yml`: **Toda función con 2 o más argumentos debe usar parámetros nombrados en su llamada.**
   - Asegurar que el formato cumpla con las reglas de indentación, wrapping y comas finales definidas en `.editorconfig` para ktlint.
   - **Extracción de textos en crudo:** Verificar que ningún texto implementado en la UI permanezca en crudo (*hardcoded*); todos deben extraerse a un archivo separado antes de proceder con el commit.
2. **Consulta previa sobre Tests:**
   - En caso de que el código creado o modificado no tenga pruebas asociadas, **preguntar explícitamente al programador si desea que se implementen tests** antes de proceder con el commit.
   - Si el programador desea tests, crearlos y verificar que pasen antes de continuar.
3. **Ejecución del commit:**
   - Una vez que el código esté limpio, sin advertencias ni errores de linter y con la decisión sobre los tests resuelta, proceder con la skill `commit`:
     - Commits atómicos por responsabilidad lógica.
     - Mensajes exclusivamente en inglés y formato Conventional Commits (`feat`, `fix`, `refactor`, `chore`, etc.).
     - Scope automático para archivos dentro de `apps/` si aplica.
     - Sin trailers ni atribuciones (`Co-authored-by`, etc.).

---

## Supabase & Seguridad (Memoria de Proyecto)
- **Estado Actual (Desarrollo):** Las tablas `public.sources` y `public.feeds` tienen `GRANT ALL` para `anon, authenticated` y políticas RLS abiertas para `anon` (`USING (true)`). Esto es temporal y válido **únicamente durante la fase de desarrollo** para facilitar la integración inicial de PostgREST sin autenticación.
- **Pendiente para Producción (Seguridad):** En el futuro, cuando se implemente Supabase Auth (o antes de pasar a producción), se debe migrar la seguridad:
  1. Restringir los permisos del rol `anon` (por ejemplo solo lectura o ningún acceso según aplique).
  2. Agregar `user_id UUID REFERENCES auth.users(id)` a las tablas.
  3. Configurar políticas RLS estrictas vinculadas a `auth.uid() = user_id` para aislar los datos por usuario.
