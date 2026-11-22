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
- **Estado Actual:** Las tablas `public.sources` y `public.feeds` tienen Row Level Security (RLS) habilitado y permisos restringidos exclusivamente al rol `authenticated`. El rol `anon` tiene permisos revocados.
  1. `public.sources`: Contiene `user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE DEFAULT auth.uid()`. Política RLS vinculada a `auth.uid() = user_id`.
  2. `public.feeds`: No duplica `user_id`; su política RLS protege los datos mediante subconsulta `EXISTS (SELECT 1 FROM public.sources WHERE sources.id = feeds.source_id AND sources.user_id = auth.uid())`.
  3. Rendimiento: Creados índices en `sources(user_id)` y `feeds(source_id)`.

---

## Sincronización Offline-First (Memoria de Proyecto)
- **Estado Actual:** Sincronización remota hacia local (`syncSources()` / `SyncSourcesUseCase` y `syncFeeds()` / `SyncFeedsUseCase`) implementada para descargar fuentes y feeds desde Supabase a Room al inicializar `FeedsViewModel`, preservando Room como la Única Fuente de Verdad (SSOT).
- **Pendiente para el Futuro:** Implementar sincronización periódica en segundo plano (WorkManager) o mediante interacción de usuario (pull-to-refresh en `FeedsScreen`).

---

## Supabase Auth (Memoria de Proyecto)
- **Autenticación Opcional (Modo Invitado vs Registrado):** El inicio de sesión / registro con Supabase Auth será completamente opcional.
  - **Modo Invitado / Sin Cuenta:** El usuario puede saltarse el login y utilizar la app de forma 100% local, usando Room como almacenamiento exclusivo y sin interactuar con Supabase.
  - **Modo Registrado:** Permite respaldo y sincronización remota de fuentes y feeds en la nube.
  - **Migración de Datos:** Si un usuario invitado decide registrarse posteriormente, se contemplará migrar y subir sus fuentes locales existentes en Room a su nueva cuenta en Supabase.
- **Pendiente para el Futuro:** Implementar los flujos de autenticación (inicio de sesión, registro y gestión de sesión persistente), así como la vinculación de la sesión con el aislamiento de datos por usuario en Supabase (`user_id = auth.uid()`).

---

## UI Adaptativa / Modo Tablet (Memoria de Proyecto)
- **Pendiente para el Futuro:** Implementar diseño adaptativo para tablets y pantallas grandes en Compose Multiplatform (layout de dos paneles / *Two-Pane* / *List-Detail*). Permitirá aprovechar pantallas de mayor tamaño mostrando simultáneamente la lista de fuentes/artículos y el detalle o visor del feed.

---

## Splash Screen y Navegación Inicial (Memoria de Proyecto)
- **Motivo y Necesidad:** Al arrancar la aplicación, el estado de sesión de Supabase Auth se inicializa de forma asíncrona desde el almacenamiento (`AuthState.Initializing`), y paralelamente se debe evaluar si el usuario ya eligió previamente utilizar la app en Modo Invitado (`isGuestMode`). Si se renderizara la interfaz inmediatamente sin aguardar dicha inicialización, ocurriría un parpadeo visual (*flicker*), mostrando fugazmente la pantalla de autenticación antes de redirigir a `FeedsScreen`.
- **Pendiente para el Futuro:** Implementar una pantalla o estado de Splash Screen que retenga el arranque hasta que se resuelva la inicialización de `AuthState` y la preferencia de Modo Invitado, redirigiendo de forma suave y definitiva a `FeedsScreen` (si está autenticado o en modo invitado) o a `AuthScreen` (si es un usuario nuevo por primera vez).

---

## Autenticación con Google (Memoria de Proyecto)
- **Estado Actual:** El botón para iniciar sesión con Google en `AuthScreen` se encuentra temporalmente comentado para enfocar y estabilizar primero el flujo de registro por correo/contraseña y modo invitado.
- **Pendiente para el Futuro:** Implementar la integración de Google Sign-In con Supabase Auth (flujo OAuth / Credential Manager según la plataforma) y rehabilitar el botón en `AuthScreen`.



