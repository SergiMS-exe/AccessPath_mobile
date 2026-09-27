# Resumen ejecutivo — Auditoría accesibilidad + Plan de tests

Fecha: 2026-09-27 · Repo: `AccessPath_mobile` (KMP + Compose Multiplatform 1.7.3)

## 1. Auditoría de accesibilidad

Análisis estático del código + cálculo numérico de contraste WCAG sobre la paleta declarada en `Theme.kt`.

**Hallazgos**: 14 fallos de contraste en light mode, 12 en dark mode; 8 zonas con textos alternativos débiles; 5 problemas de teclado/foco; 7 de roles semánticos; 4 estados dinámicos sin `liveRegion`; 3 touch targets pequeños; 2 problemas de movimiento/tamaño de fuente.

**Críticos (bloquean lanzamiento)**:
- `textTertiary #9E9E9E` (light) → 2.46–2.68:1 sobre superficies. Prácticamente ilegible para placeholders, fechas, consentimientos.
- `BasicTextField` con placeholder como label en `AuthField` y `SearchBar` → al escribir, el label desaparece y TalkBack/VoiceOver deja de anunciar el nombre del campo.
- Pildoras negras en lightbox (`PlaceDetailComponents.kt`) → 1.10–1.23:1 en dark, invisibles.
- Sin `stateDescription` en chips/filas seleccionables (FilterChip, ProfileScreen, DrawerMenuItemRow) → usuarios ciegos no saben qué está activo.
- Marcadores del mapa sin info del estado de accesibilidad (`snippet = place.address`, no incluye `overallState.label`).

**Plan en 4 sprints** documentado en `docs/ACCESSIBILITY_AUDIT.md`, incluyendo valores hex concretos para corregir el contraste y snippets de Compose (`OutlinedTextField` con `label`, `Modifier.semantics { stateDescription = ... }`, `liveRegion` para snackbars).

---

## 2. Plan de tests ejecutado

### Configuración

`gradle/libs.versions.toml` + `composeApp/build.gradle.kts`:

- `turbine 1.1.0`
- `kotlinx-coroutines-test 1.9.0`
- `ktor-client-mock 3.1.3`
- `compose-ui-test 1.7.3` (commonTest)
- `compose-ui-test-junit4-android 1.10.1` (androidTest)

### Refactor mínimo retrocompatible

Para poder testear sin red y sin tocar la decisión "un solo `httpClient` compartido":

- Parámetro `client: HttpClient = httpClient` añadido a las 6 APIs (`AuthApi`, `PlaceApi`, `ContributionApi`, `SubmissionApi`, `ProfileApi`, `FormApi`).
- `@Volatile internal var ...Override` en `AuthRepository`, `PlaceApi`, `ContributionApi`, `SubmissionApi`, `ProfileApi`.
- `internal fun reset(...)` en `AuthRepository` para tests.
- `AuthRepository` ahora acepta `Settings` inyectable.

### Test fixtures

- `FakeSettings` — in-memory, sin disco.
- `Fixtures` — builders de `Place`, `AuthUser`, `Dimension`, `Criterion`, `PlaceDetail`, etc.
- `TestHttpClient` — helper con `MockEngine` + Auth plugin + validador global que replica la lógica de producción.
- `TestResets.all()` — limpia hooks entre tests.

### Tests creados: 200 en total

| Capa | # tests | Qué cubre |
|------|--------:|-----------|
| ViewModels | ~75 | Auth, Landing, PlaceDetail, Contribute, Profile: estados, eventos, errores, trim, validaciones locales, undo, refresh |
| AuthRepository | 11 | persistencia, restore con JSON corrupto, refresh sticky, logout |
| Cliente HTTP | ~20 | Auth plugin, refresh + retry 401, `ApiException`, `X-Request-Id`, `isRetryable` |
| APIs (con `MockEngine`) | ~25 | cada endpoint, success/error, snake_case en body, query params |
| Mappers DTO | 18 | cada `toDomain()`, defaults (anonimo, OTHER, address "") |
| Serialización JSON | 13 | roundtrip + snake_case + `ignoreUnknownKeys` |
| Lógica de dominio | 17 | `PlaceFilter` (query/categorías/estados), `AccessibilityState.fromApi`, `FunctionalNeed.fromKey` |
| WCAG contraste | 8 | numérico sobre Theme.kt: textPrimary/Secondary/Tertiary sobre las 3 superficies × 2 modos |
| Semántica Compose | ~30 | `contentDescription`, `Role`, touch target ≥48 dp, iconos decorativos null |
| Pantallas (smoke UI) | ~10 | AuthScreen, PlaceDetailScreen, ContributeScreen con `createComposeRule` |
| Instrumentados Android | 2 | `SmokeTest` arranca `MainActivity` + `NetworkSecurityConfigTest` |

### Cancelado

- Tests del stack de `App.kt`: depende de `MapView` nativo, complicado de mockear. Se cubre indirectamente con tests de VM + smoke instrumentado.

### Cómo correr

```bash
./gradlew :composeApp:testDebugUnitTest        # commonTest (sin emulador)
./gradlew :composeApp:connectedAndroidTest     # androidTest (necesita emulador)
```

No se ejecutaron los tests (`assembleDebug` está bloqueado por convención del repo y porque necesita `local.properties` con `API_BASE_URL`).

### Limitaciones

- Sin reporte de cobertura % (no se añadió `kover`).
- Sin e2e completo con MockWebServer — listado como siguiente paso en `docs/TEST_PLAN.md`.
- Sin tests para iOS XCTest (la lógica compartida se cubre con commonTest).

---

## Entregables

- `docs/ACCESSIBILITY_AUDIT.md` — auditoría WCAG completa con hallazgos, severidad, valores hex de corrección y plan priorizado en 4 sprints.
- `docs/TEST_PLAN.md` — plan de tests detallado con desglose por archivo, comandos para correrlos y notas de diseño.

## Archivos creados (32)

**commonTest** (30):
```
test/FakeSettings.kt
test/Fixtures.kt
test/TestHttpClient.kt
test/TestResets.kt
data/accessibility/AccessibilityStateTest.kt
data/api/ApiClientTest.kt
data/api/AuthApiTest.kt
data/api/ContributionApiTest.kt
data/api/DtoMappersTest.kt
data/api/FormApiTest.kt
data/api/PlaceApiTest.kt
data/api/ProfileApiTest.kt
data/api/SerializationTest.kt
data/api/SubmissionApiTest.kt
data/auth/AuthRepositoryTest.kt
ui/auth/AuthScreenAccessibilityTest.kt
ui/auth/AuthViewModelTest.kt
ui/components/AccessibilityStateChipTest.kt
ui/components/DrawerMenuAccessibilityTest.kt
ui/components/MapControlButtonsTest.kt
ui/components/SearchBarAccessibilityTest.kt
ui/landing/PlaceFilterTest.kt
ui/LandingViewModelTest.kt
ui/place/ContributeScreenAccessibilityTest.kt
ui/place/ContributeViewModelTest.kt
ui/place/PlaceDetailScreenAccessibilityTest.kt
ui/place/PlaceDetailViewModelTest.kt
ui/profile/ProfileViewModelTest.kt
ui/theme/ComposeAccessibilityTest.kt
ui/theme/WcagContrastTest.kt
```

**androidTest** (2):
```
SmokeTest.kt
NetworkSecurityConfigTest.kt
```