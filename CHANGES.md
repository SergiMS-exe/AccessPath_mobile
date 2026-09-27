# Resumen de cambios — AccessPath Mobile

## Contexto

La app móvil (Kotlin Multiplatform + Compose Multiplatform) ya tenía una arquitectura limpia: `commonMain` con cliente Ktor compartido, Auth plugin con refresh transparente, `AuthRepository` con persistencia via `multiplatform-settings`, navegación stack manual, y `BuildKonfig.API_BASE_URL` como única fuente del base URL.

Los cambios en este pase son: (a) la skill de comportamiento y (b) el CI workflow para validar builds en cada PR. No se han tocado piezas de runtime (handlers, view models, etc.) en esta tanda.

---

## Fase 1 — Skill de comportamiento

### `.opencode/skill/accesspath-feature/SKILL.md` (nuevo)
Skill versionada en el repo (no en `~/.config/opencode/`), para que viaje con el clone. Estructura paralela a la skill del backend:

- **Stack y arquitectura**: árbol de `commonMain` / `androidMain` / `iosMain` con qué vive en cada uno.
- **Convenciones del `.claude/CLAUDE.md`**:
  - Sin acentos en código, comentarios, identificadores, ni logs.
  - No ejecutar Gradle builds salvo que se pida.
  - KMP: preferir `commonMain` siempre.
  - Stack manual sin librería de navegación externa.
  - Un solo `httpClient` compartido con Auth plugin.
  - Tokens persistidos con `multiplatform-settings`.
  - Refresh transparente vía Ktor `Auth`.
- **Patrones canónicos con snippets**:
  - `object RecursoApi { suspend fun ... }` con `httpClient.get("$API_BASE_URL/api/v1/...").body()`.
  - `ViewModel` con `MutableStateFlow<UiState>` y `sealed interface UiState { Loading; Ready(...); Error(...) }`.
  - `Screen` Composable que observa `state.collectAsState()` y maneja cada estado.
  - `@Serializable data class Recurso(...)` para DTOs.
  - Test en `commonTest` con `runTest` + `assertEquals`.
- **Checklist de seguridad aplicado al cliente**:
  - Tokens nunca en logs (Ktor `Logging` plugin solo en INFO, nunca BODY).
  - `multiplatform-settings` sin encryption (OK para Android 7+).
  - HTTPS obligatorio: `network_security_config.xml` con `cleartextTrafficPermitted="false"` en release.
  - Sin hardcodear URLs/keys.
  - Inputs del usuario validados antes de enviar.
  - Crash reporting (cuando se añada) redacta `Authorization`.
- **Checklist de calidad pre-PR**:
  - `commonMain` maximizado.
  - Composables pequeños en `ui/components/`.
  - ViewModels con `viewModelScope.launch` + `try/catch ApiException`.
  - Estados con `sealed interface UiState`.
  - Modelos `@Serializable` en `data/<recurso>/`, no en `ui/`.
  - Un solo `httpClient`.
  - `./gradlew ktlintCheck`, `test`, `assembleRelease` pasan.
- **Decisiones arquitectónicas explícitas** (no desafiar sin propuesta formal):
  - Stack manual sin librería externa.
  - Un solo `httpClient` compartido.
  - `multiplatform-settings` (no MMKV, no EncryptedSharedPreferences).
  - Google Maps solo Android.
  - `BuildKonfig.API_BASE_URL` única fuente.
  - Min Android SDK 24.
- **"Lo que NO hacer"** (10 puntos): sin acentos, no ejecutar Gradle builds sin pedir, no `HttpClient` por ViewModel, no hardcodear URLs, no `Log.d`/`println`, no loguear bodies con tokens, no `interface{}`, no añadir deps sin discutir, no `mutableStateOf` para listas grandes, no saltarse `try/catch` en `suspend`.
- **Mapa rápido de archivos clave**.

---

## Fase 9 — CI workflow

### `.github/workflows/ci.yml` (nuevo)
Ejecuta en `push` a `main` y en cada PR:

1. **Checkout** (`actions/checkout@v4`).
2. **Setup JDK 17** (`actions/setup-java@v4`, distribución Temurin).
3. **Setup Gradle** (`gradle/actions/setup-gradle@v4`) — configura wrapper.
4. **Cache Gradle** (`actions/cache@v4`) — keyed por `hashFiles('**/*.gradle*', '**/libs.versions.toml')` para que el cache se invalide cuando cambian versiones o scripts.
5. **Compile Android target** — `./gradlew :composeApp:compileKotlinAndroid --no-daemon`.
6. **Compile iOS targets** — `./gradlew :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64 --no-daemon || true` (en Linux fallan sin error porque necesitan macOS; documentado en el `|| true`).
7. **Run tests** — `./gradlew :composeApp:test --no-daemon` (cuando haya tests).
8. **Verify BuildKonfig can resolve** — escribe `API_BASE_URL=https://example.com` y `MAPS_API_KEY=` en `local.properties` (que es gitignored, así que no contamina el repo), y luego `./gradlew :composeApp:generateBuildKonfig`. Esto verifica que el plugin buildkonfig no rompe la build con un placeholder.

**Decisión clave**: el CI genera un placeholder de `API_BASE_URL` localmente para que `generateBuildKonfig` corra sin secretos reales. Los secretos reales nunca entran al CI ni a los artefactos.

---

## Estado actual del repo

```
D:\Git\AccessPath_mobile\
├── .github/
│   └── workflows/
│       └── ci.yml                   (nuevo)
└── .opencode/
    └── skill/
        └── accesspath-feature/
            └── SKILL.md             (nuevo)
```

No se han modificado ficheros de runtime (`composeApp/src/...`) en este pase. La skill y el CI son piezas de soporte que endurezcan los siguientes cambios.

---

## Verificación pendiente en la máquina del usuario

Como el usuario "No ejecutar Gradle builds" es una convención explícita del repo (`.claude/CLAUDE.md:9`), el CI no se prueba en local. Para verificar end-to-end:

```bash
cd D:\Git\AccessPath_mobile
# 1. Push a GitHub la rama con estos cambios.
# 2. Crear PR.
# 3. El workflow corre y debe quedar en verde.
```

Si quieres probar localmente sin pushear:

```bash
echo "API_BASE_URL=https://example.com" >> local.properties
echo "MAPS_API_KEY=" >> local.properties
./gradlew :composeApp:generateBuildKonfig :composeApp:compileKotlinAndroid --no-daemon
```

Si el segundo comando termina sin error, la build es válida y el CI pasará.

---

## Pendientes (no hechos)

1. **Tests reales para la app**: el repo tiene `composeApp/src/commonTest/kotlin/.../ComposeAppCommonTest.kt` pero es un placeholder. No se han escrito tests de `ApiClient`, `AuthRepository`, ni ViewModels. Esto debería ser un siguiente pase.
2. **ktlint / detekt**: el CI no los ejecuta todavía porque el repo no tiene `.editorconfig` ni `detekt.yml` configurados. Recomendable añadir en un siguiente pase, junto con los tests reales.
3. **Place API key real**: `local.properties` en CI tiene `MAPS_API_KEY=` vacío. Esto significa que el build pasa pero Google Maps no funcionará en runtime si la APK release se llega a instalar. Cuando el CI se ejecute en un fork sin secrets, el mapa no funcionará. Es el trade-off correcto (no exponer secretos), pero conviene documentarlo.
4. **Migración a minSdk 26+ (Android 8.0)**: actualmente minSdk 24 (Android 7.0). El plan era subir para mejor TLS 1.3 por defecto, pero no se ha hecho.
5. **Sentry / Crashlytics**: si se añade, hay que redactar `Authorization` antes de enviar payloads. Documentado en la skill.

---

## Relación con el backend

La skill del mobile **referencia explícitamente** que `API_BASE_URL` sale de `BuildKonfig.API_BASE_URL`, que a su vez viene del `API_BASE_URL=...` de `local.properties` (gitignored). El backend en su `CLAUDE.md` no expone su base URL directamente; ambos repos cooperan vía:

- backend sirve HTTPS en `https://accesspath.sermate.dev/api/v1/...`.
- mobile lee `https://accesspath.sermate.dev` de `local.properties` y le concatena `/api/v1/...` en cada `httpClient.get(...)`.

El cambio reciente que motivó esta tanda (apuntar el backend a `accesspath.sermate.dev` en vez de `accesspath.api.sermate.dev`, y endurecer `local.properties` con fail-build si falta `API_BASE_URL`) está documentado en ambas skills.
