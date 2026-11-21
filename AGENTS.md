# Project Guidelines & Memory

## Reglas para Commits
Cuando el usuario solicite realizar un commit o preparar commits:
1. **Paso previo obligatorio (Ktlint y Detekt):**
   - Antes de ejecutar la skill `commit` o registrar cualquier cambio en Git, revisar y corregir todos los problemas señalados por **ktlint** y **detekt**.
   - Especial atención a la regla de Detekt en `detekt.yml`: **Toda función con 2 o más argumentos debe usar parámetros nombrados en su llamada.**
   - Asegurar que el formato cumpla con las reglas de indentación, wrapping y comas finales definidas en `.editorconfig` para ktlint.
2. **Consulta previa sobre Tests:**
   - En caso de que el código creado o modificado no tenga pruebas asociadas, **preguntar explícitamente al programador si desea que se implementen tests** antes de proceder con el commit.
   - Si el programador desea tests, crearlos y verificar que pasen antes de continuar.
3. **Ejecución del commit:**
   - Una vez que el código esté limpio, sin advertencias ni errores de linter y con la decisión sobre los tests resuelta, proceder con la skill `commit`:
     - Commits atómicos por responsabilidad lógica.
     - Mensajes exclusivamente en inglés y formato Conventional Commits (`feat`, `fix`, `refactor`, `chore`, etc.).
     - Scope automático para archivos dentro de `apps/` si aplica.
     - Sin trailers ni atribuciones (`Co-authored-by`, etc.).
