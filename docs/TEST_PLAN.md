# Plan de pruebas — AccessPath Mobile

Fecha: 2026-09-27
Alcance: `comApp` (Kotlin Multiplatform + Compose Multiplatform 1.7.3).
Total: **198 tests en commonTest + 2 tests instrumentados Android**.

## Resumen ejecutivo

| Capa | Archivo(s) | # tests |
|------|------------|--------:|
| Test fixtures (fakes, builders, helpers) | `test/FakeSettings.kt`, `test/Fixtures.kt`, `test/TestHttpClient.kt`, `test/TestResets.kt` | — |
| ViewModels | `AuthViewModelTest`, `LandingViewModelTest`, `PlaceDetailViewModelTest`, `ContributeViewModelTest`, `ProfileViewModelTest` | ~75 |
| Repositorio de auth | `AuthRepositoryTest` | 11 |
| APIs (cliente HTTP + endpoints) | `ApiClientTest`, `AuthApiTest`, `PlaceApiTest`, `ContributionApiTest`, `SubmissionApiTest`, `ProfileApiTest`, `FormApiTest` | ~45 |
| Mappers DTO → dominio | `DtoMappersTest` | 18 |
| Serialización JSON | `SerializationTest` | 13 |
| Lógica de dominio | `AccessibilityStateTest`, `PlaceFilterTest` | 17 |
| Accesibilidad WCAG | `WcagContrastTest` | 8 |
| Accesibilidad Compose (semántica, touch targets) | `ComposeAccessibilityTest`, `AccessibilityStateChipTest`, `DrawerMenuAccessibilityTest`, `MapControlButtonsTest`, `SearchBarAccessibilityTest`, `AuthScreenAccessibilityTest`, `PlaceDetailScreenAccessibilityTest`, `ContributeScreenAccessibilityTest` | ~30 |
| Instrumentados Android (e2e) | `androidTest/SmokeTest.kt`, `androidTest/NetworkSecurityConfigTest.kt` | 2 |

## Configuración añadida

### `gradle/libs.versions.toml`

- `turbine 1.1.0` para tests de Flows (`app.cash.turbine:turbine`).
- `kotlinx-coroutines-test 1.9.0` (`runTest`, `UnconfinedTestDispatcher`).
- `ktor-client-mock 3.1.3` (`MockEngine` para simular backend).
- `compose-ui-test 1.7.3` (`createComposeRule` para tests de UI multiplatform).
- `compose-ui-test-junit4-android 1.10.1` (tests instrumentados Android).

### `composeApp/build.gradle.kts`

- `commonTest.dependencies`: `kotlin-test`, `kotlinx-coroutines-test`, `turbine`, `ktor-client-mock`, `compose-ui-test`.
- `android.sourceSets["androidTest"]`: `compose-ui-test-junit4-android`, `androidx-test-ext-junit`, `androidx-espresso-core`, `kotlinx-coroutines-test`.

## Cambios en código de producción (seams de test, retrocompatibles)

Para poder testear ViewModels y APIs sin red real, añadí dos tipos de seams:

1. **Parámetro `client: HttpClient = httpClient`** en cada API (`AuthApi`, `PlaceApi`, `ContributionApi`, `SubmissionApi`, `ProfileApi`, `FormApi`). El default sigue siendo el cliente compartido global; los tests inyectan un `HttpClient` con `MockEngine`.
2. **`@Volatile internal var ...Override`** en los singletons (`AuthRepository`, `PlaceApi`, `ContributionApi`, `SubmissionApi`, `ProfileApi`) y `internal fun reset(...)` en `AuthRepository`. Solo accesibles desde `commonTest` (mismo módulo). Producción no los toca.

Refactor mínimo: ningún archivo fuera de `data/api/` y `data/auth/` cambia.

## Pirámide de tests

```
                  ▲ e2e Android (androidTest)
                ▲▲▲ Compose UI test (commonTest, semántica + touch targets)
              ▲▲▲▲▲ Tests de lógica (commonTest, ViewModels)
            ▲▲▲▲▲▲▲ Tests de I/O (commonTest, MockEngine + Auth plugin)
          ▲▲▲▲▲▲▲▲▲ Tests puros (commonTest, modelos + lógica pura)
```

## Cobertura por módulo

### `data/auth/` — AuthRepository
- ✅ Persistencia con `FakeSettings` (`putString`, `getStringOrNull`, `remove`).
- ✅ Restore desde settings con JSON corrupto (cae a Unauthenticated).
- ✅ Login / register / logout / updateTokens con hooks `internal`.
- ✅ `refreshToken` permanece en disco tras logout (sticky, para próxima sesión).

### `data/api/` — Cliente HTTP
- ✅ Cada request lleva `X-Request-Id`.
- ✅ Bearer token se adjunta si está cargado.
- ✅ 401 dispara `refreshTokens` y reintenta.
- ✅ Refresh exitoso actualiza tokens vía `AuthRepository.updateTokens`.
- ✅ Refresh fallido llama `AuthRepository.logout()`.
- ✅ Validador global convierte no-2xx en `ApiException`.
- ✅ `ApiException.isRetryable` para 5xx y 429.
- ✅ Mensaje del envelope `{"error": "..."}` se extrae del body.
- ✅ Fallback a `"HTTP $status"` cuando no hay envelope.

### `data/api/` — Endpoints individuales
- ✅ `AuthApi.login` / `register`: success, body JSON correcto, 401/409 propagan mensaje, 500 retryable.
- ✅ `PlaceApi.search` / `mapPins` / `detail` / `importFromGoogle`: query params, snake_case en body, mapeo a dominio.
- ✅ `ContributionApi.nextQuestion` / `save` / `delete`: snake_case, soporta `criterion: null`.
- ✅ `SubmissionApi.save`: PUT con place_id y comment.
- ✅ `ProfileApi.get` / `set` / `delete`: PUT con needs+consent, DELETE limpio.
- ✅ `FormApi.dimensions`: GET devuelve catálogo.

### `data/api/` — DTO → dominio (mappers)
- ✅ 18 tests cubriendo cada `toDomain()` (Confidence, CriterionScore, DimensionScore, PlaceAccessibility, PlaceDto, PlaceMapItemDto, PlaceDetailDto, SubmissionDto, PhotoDto, ProfileDto, CriterionDto, NextQuestionDto, DimensionDto, ContributionResultDto).
- ✅ Defaults: address null → "", category → OTHER, username null → "Anonimo".

### `data/accessibility/` — Modelos de dominio
- ✅ `AccessibilityState.fromApi` mapea green/yellow/red/no_data y tolera desconocidos (→ NO_DATA).
- ✅ `FunctionalNeed.fromKey` roundtrip de cada entrada, retorna null para clave desconocida.
- ✅ Labels únicos y descriptivos por estado (cumple WCAG 1.4.1: no se depende solo del color).

### `ui/auth/` — AuthViewModel
- ✅ Estado inicial (LOGIN tab, campos vacíos, sin loading, sin error).
- ✅ `switchTab` cambia tab.
- ✅ Setters de campos actualizan estado.
- ✅ Submit login con success emite `Authenticated` event, persiste tokens.
- ✅ Submit login con failure guarda mensaje de error y detiene loading.
- ✅ Submit register rechaza contraseñas no coincidentes SIN llamar al backend.
- ✅ Submit register con success emite `Authenticated`.
- ✅ Submit register con failure guarda error.
- ✅ Mensaje por defecto `"Error desconocido"` cuando `Throwable.message` es null.
- ✅ Email se trimea antes de enviar.

### `ui/` — LandingViewModel
- ✅ Estado inicial vacío.
- ✅ `onCameraIdle` actualiza `mapBounds`.
- ✅ `loadArea` carga places y guarda `loadedBounds`.
- ✅ `loadArea` no repite llamada con mismos bounds.
- ✅ `loadArea` guarda mensaje de error en failure.
- ✅ `bootstrap` centra en `selectedPlace` y skipea location.
- ✅ `bootstrap` pide location si no hay place seleccionado.
- ✅ `refreshLocation` usa fallback (Madrid centro) cuando no hay permiso.
- ✅ `onMyLocationClick` actualiza center + zoom 16, no-op si no hay location.
- ✅ Zoom clamp 5..20.
- ✅ Filter setters (query, categories, states, clear).
- ✅ `onSearchActiveChange` togglea estado.
- ✅ `onMenuItemSelected` actualiza selección.
- ✅ `onAuthStateChanged` actualiza username.
- ✅ `onPlaceClicked` centra mapa.
- ✅ `onPlaceSelectedFromSearch` limpia query.
- ✅ `onPlaceAdded` añade a lista y limpia query.
- ✅ `refreshLocation` actualiza `locationDisabled` cuando el servicio lo reporta.

### `ui/place/` — PlaceDetailViewModel
- ✅ Estado inicial con loading=true.
- ✅ Load success: detail poblado, loading=false.
- ✅ Load failure: error genérico, loading=false.
- ✅ Profile failure no impide cargar detail.
- ✅ Profile success populando `userNeeds`.
- ✅ Segunda llamada sobrescribe estado previo.

### `ui/place/` — ContributeViewModel
- ✅ Init carga `nextQuestion`.
- ✅ Init failure guarda error genérico.
- ✅ `answer` envía contribución y guarda `lastResult`.
- ✅ `answer` sin pregunta es no-op.
- ✅ `answer` failure guarda error y no incrementa count.
- ✅ `undo` borra última contribución y recarga pregunta.
- ✅ `undo` sin `lastResult` es no-op.
- ✅ `saveComment` rechaza string vacío/whitespace.
- ✅ `saveComment` success pone `commentSaved=true`.
- ✅ `saveComment` failure guarda error.
- ✅ `loadNextQuestion` limpia error antes de recargar.
- ✅ `criterion: null` representa fin del cuestionario.

### `ui/profile/` — ProfileViewModel
- ✅ Init carga profile del backend.
- ✅ Init failure cae a profile vacío.
- ✅ `toggleNeed` añade/quita de `selectedNeeds`.
- ✅ `toggleNeed` limpia flag `saved`.
- ✅ `save` envía `needs` con `consent=true`.
- ✅ `save` failure guarda error.
- ✅ `delete` limpia profile y consent.
- ✅ `delete` failure guarda error.

### `ui/landing/` — PlaceFilter
- ✅ Filter vacío acepta todo.
- ✅ Query case-insensitive por nombre y dirección.
- ✅ Query se trimea.
- ✅ Categorías single y multi.
- ✅ Estados single y multi.
- ✅ Combinación query + + .
- ✅ `applyFilter` preserva orden.
- ✅ `applyFilter` retorna vacío si nada matchea.
- ✅ `activeCount` cuenta filtros pero no la query.

### `ui/theme/` — WCAG contraste
- ✅ Texto principal sobre todas las superficies en light y dark mode (≥ AA 4.5:1).
- ✅ Texto secundario sobre todas las superficies en light y dark mode.
- ✅ Texto terciario documenta el **fallo conocido** sobre `surfaceVariant` (test de regresión).
- ✅ Paleta de accesibilidad: cada estado del semáforo cumple AA sobre su fondo en light y dark.
- ✅ Helper `ratio()` validado contra pares conocidos (#000 vs #FFF = 21:1).

### `ui/components/` — Componentes accesibles
- ✅ `AccessibilityStateChip`: cada uno de los 4 estados renderiza su label textual.
- ✅ `MapControlButtons`: 4 botones con `contentDescription` y `Role.Button`, touch target ≥ 48 dp, callback invocable.
- ✅ `SearchBar`: iconos Menu y Filtros con contentDescription, placeholder visible cuando query está vacío, botón Limpiar aparece al escribir.
- ✅ `DrawerMenu`: items de menú con labels visibles, switch de modo oscuro presente, botón logout etiquetado.

### Pantallas completas (smoke UI)
- ✅ `AuthScreen`: branding y tabs visibles.
- ✅ `AuthScreen` login form: email y password visibles.
- ✅ `PlaceDetailScreen`: título, dirección, semáforo accesible, mensaje de submissions vacías, botón "Quiero valorar".
- ✅ `ContributeScreen`: título del lugar, prompt de pregunta, opciones, botón "Salir" sin culpa, mensaje de fin.

### Compose semántica general
- ✅ `Icon` con `contentDescription` es localizable en el árbol semántico.
- ✅ `Text` es localizable.
- ✅ `IconButton` cumple touch target ≥ 48 dp (WCAG 2.5.5 AAA / Material guidelines).
- ✅ `contentDescription` se expone vía `SemanticsProperties.ContentDescription`.
- ✅ Iconos decorativos tienen `contentDescription = null` (no se anuncian).
- ✅ Dark theme renderiza sin crash.
- ✅ Material3 `Switch` expone `Role.Switch` automáticamente.

### Instrumentados Android (e2e)
- ✅ `SmokeTest`: arranca `MainActivity` con permisos de ubicación concedidos y verifica que "AccessPath" + "Iniciar sesion" se muestran (estado inicial sin sesión).
- ✅ `NetworkSecurityConfigTest`: verifica que el recurso `network_security_config.xml` existe y está bien referenciado.

## Cómo correr los tests

```bash
# Unit + integration + a11y UI (todos los comunes, sin necesidad de emulador)
./gradlew :composeApp:testDebugUnitTest

# Solo JVM commonTest
./gradlew :composeApp:jvmTest

# Instrumentados (requiere emulador o dispositivo)
./gradlew :composeApp:connectedAndroidTest
```

## Notas de diseño

1. **Mocks sobre el `httpClient` global**: se eligió el patrón de `@Volatile internal var ...Override` en los `object` de API para no romper la decisión arquitectónica del repo de "un solo `httpClient` compartido". Los tests de ViewModel usan los overrides (rápidos, sin red); los tests de la API usan `MockEngine` inyectado vía el parámetro `client` (validan el contrato HTTP real).

2. **`AuthRepository` ahora acepta `Settings` opcional**: en producción usa `Settings()` (real, cifrado por el OS en Android), en tests usa `FakeSettings` (in-memory).

3. **`testHttpClient` reescribe el validador global**: replica `ApiException`-mapping para que los tests que esperan ApiException no tengan que duplicar lógica.

4. **No se mockea `Greeting`** (trivial) ni `Platform.kt` (trivial). Solo lo que tiene lógica real está cubierto.

5. **Tests UI usan `createComposeRule` en commonTest**: aprovecha `org.jetbrains.compose.ui:ui-test`. Esto evita duplicar tests entre `commonTest` y `androidTest`.

6. **Tests instrumentados son mínimos** (smoke + config): añadir e2e completos con MockWebServer real está fuera del alcance actual. Quedan como siguiente paso.

## Próximos pasos sugeridos

1. **Tests e2e con MockWebServer**: backend real simulado en `localhost`, smoke flow completo login → mapa → detalle → valorar.
2. **Tests de iOS**: equivalente a `androidTest` pero usando `XCTest`. Compose UI test cubre la lógica compartida.
3. **Coverage report**: añadir `kotlinx-kover` o similar para medir % cubierto. Hoy hay ~198 tests pero sin % de líneas.
4. **Tests de propiedades**: añadir `kotest-property` para fuzzing de `PlaceFilter`, validadores y mappers.
5. **Tests de regresión para los fallos de a11y documentados**: tras implementar las correcciones del `docs/ACCESSIBILITY_AUDIT.md`, añadir tests que verifiquen el nuevo contraste.