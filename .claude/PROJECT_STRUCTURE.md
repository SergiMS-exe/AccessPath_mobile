# AccessPath — Estructura y Propósito del Proyecto

> **Nota para Claude:** Este documento describe la arquitectura del proyecto AccessPath Mobile.
> Úsalo como referencia cuando el usuario pida modificar, añadir o entender cualquier parte del código.

---

## ¿Qué es AccessPath?

AccessPath es una aplicación móvil de **mapas de accesibilidad**. Su objetivo es ayudar a las personas a descubrir qué tan accesibles son los lugares públicos (restaurantes, museos, hospitales, parques, etc.) antes de visitarlos.

Cada lugar tiene una puntuación en tres dimensiones:
- **Física** → rampas, ascensores, puertas anchas, aparcamiento adaptado
- **Sensorial** → señales en Braille, bucles de inducción, guías de audio, pavimento táctil
- **Cognitiva** → lectura fácil, pictogramas, espacios tranquilos, señalización clara

La app muestra los lugares en un mapa con marcadores de colores según su nivel de accesibilidad general (verde = muy fácil, rojo = difícil).

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 2.1.0 |
| UI compartida | Compose Multiplatform 1.7.3 |
| Plataformas | Android (API 24+) e iOS |
| Mapas Android | Google Maps Compose 6.4.0 |
| Mapas iOS | MapKit (nativo via UIKitView) |
| Localización Android | Google Play Services Fused Location |
| Localización iOS | Core Location (CLLocationManager) |
| Asincronía | Kotlin Coroutines 1.9.0 |
| Gestión de dependencias | Gradle Version Catalog (`libs.versions.toml`) |

---

## Estructura de carpetas

```
AccessPath_mobile/
│
├── composeApp/                        # Módulo principal multiplataforma
│   └── src/
│       ├── commonMain/                # Código compartido entre Android e iOS
│       │   └── kotlin/org/s3m4su/accesspath/
│       │       ├── App.kt             # Composable raíz (tema + pantalla inicial)
│       │       ├── Platform.kt        # Interface expect para info de plataforma
│       │       ├── data/
│       │       │   └── Place.kt       # Modelos de dominio + datos mock
│       │       ├── location/
│       │       │   ├── Location.kt    # Data class con lat/lon
│       │       │   └── LocationService.kt  # expect class del servicio de ubicación
│       │       ├── map/
│       │       │   └── MapView.kt     # expect composable del mapa
│       │       └── ui/
│       │           ├── LandingScreen.kt         # Pantalla principal (mapa + drawer + sheet)
│       │           ├── components/
│       │           │   ├── DrawerMenu.kt         # Menú lateral con perfil de usuario
│       │           │   ├── LandingComponents.kt  # Barra de búsqueda, bottom sheet, chips
│       │           │   └── Previews.kt           # Previews de Compose para Android Studio
│       │           └── theme/
│       │               └── Theme.kt              # Sistema de colores y tipografía
│       │
│       ├── androidMain/               # Implementaciones específicas de Android
│       │   └── kotlin/org/s3m4su/accesspath/
│       │       ├── MainActivity.kt    # Activity de Android (pide permisos, lanza Compose)
│       │       ├── Platform.android.kt
│       │       ├── location/
│       │       │   └── LocationService.android.kt  # actual: usa Fused Location Provider
│       │       ├── map/
│       │       │   └── MapView.android.kt           # actual: Google Maps Compose
│       │       └── ui/components/
│       │           └── Previews.kt
│       │
│       └── iosMain/                   # Implementaciones específicas de iOS
│           └── kotlin/org/s3m4su/accesspath/
│               ├── MainViewController.kt    # Punto de entrada Compose → iOS
│               ├── Platform.ios.kt
│               ├── location/
│               │   └── LocationService.ios.kt  # actual: usa CLLocationManager
│               └── map/
│                   └── MapView.ios.kt           # actual: MapKit via UIKitView
│
├── iosApp/                            # Wrapper nativo Swift para iOS
│   ├── iosApp/
│   │   ├── iOSApp.swift               # Punto de entrada SwiftUI
│   │   └── ContentView.swift          # Embebe la UI de Compose via MainViewController
│   └── Configuration/
│       └── Config.xcconfig            # Team ID, bundle ID, versión
│
├── gradle/
│   └── libs.versions.toml             # Catálogo central de versiones de dependencias
├── composeApp/build.gradle.kts        # Config de build: source sets, dependencias, API keys
├── build.gradle.kts                   # Config raíz de Gradle
├── settings.gradle.kts                # Módulos incluidos en el proyecto
├── local.properties                   # API keys locales (NO subir a git)
└── CLAUDE.md                          # Instrucciones para Claude Code
```

---

## Patrón clave: `expect` / `actual`

El proyecto usa el mecanismo de Kotlin Multiplatform para abstraer las diferencias entre plataformas:

```
commonMain/   →  expect class LocationService { ... }     ← define la interfaz
androidMain/  →  actual class LocationService { ... }     ← implementación Android
iosMain/      →  actual class LocationService { ... }     ← implementación iOS
```

Esto aplica a tres componentes:
1. **LocationService** — cómo obtener la posición GPS del usuario
2. **MapView** — cómo renderizar el mapa (Google Maps vs MapKit)
3. **Platform** — información sobre la plataforma actual (nombre y versión)

---

## Modelos de dominio (`data/Place.kt`)

### `Place` — lugar del mapa
```kotlin
data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Float,                                    // 0.0–5.0 promedio general
    val category: PlaceCategory,
    val imageUrl: String? = null,
    val description: String? = null,
    val physicalAccessibility: AccessibilityScore? = null,
    val sensoryAccessibility: AccessibilityScore? = null,
    val cognitiveAccessibility: AccessibilityScore? = null,
    val features: List<AccessibilityFeature> = emptyList()
)
```

### `AccessibilityScore`
```kotlin
data class AccessibilityScore(val score: Double, val level: AccessibilityLevel)
// AccessibilityScore.fromScore(score) deriva el level.
// Niveles: VERY_EASY (>=4.0), EASY (>=3.0), MODERATE (>=2.0), DIFFICULT (<2.0)
```
`Place.averageAccessibility` es una propiedad calculada: promedia las dimensiones
fisica/sensorial/cognitiva que no sean null y devuelve un `AccessibilityScore?`.

### `PlaceCategory` — tipos de lugares
`RESTAURANT, CAFE, HOTEL, MUSEUM, THEATER, LIBRARY, SHOP, MALL, HOSPITAL, PHARMACY, PARK, TRANSPORT, OTHER`

### `AccessibilityFeature` — características concretas
`WHEELCHAIR_ACCESS, RAMP, ELEVATOR, WIDE_DOORS, ACCESSIBLE_BATHROOM, PARKING, HANDRAILS,
 BRAILLE_SIGNAGE, HEARING_LOOP, SIGN_LANGUAGE, TACTILE_PAVING, AUDIO_GUIDES,
 EASY_READING, PICTOGRAMS, QUIET_SPACE, CLEAR_SIGNAGE`

### `MockPlaces` — datos de prueba
Objeto singleton con 7 lugares ficticios en Madrid (lat ~40.41, lon ~-3.70).
**No hay backend real todavía** — todo el contenido es local y estático.

---

## Pantallas e interfaces de usuario

### `LandingScreen` — pantalla principal
- Mapa a pantalla completa centrado en la ubicación del usuario (o Madrid como fallback)
- 7 marcadores con colores según nivel de accesibilidad
- Controles de mapa (zoom + / zoom −, centrar en usuario) a la derecha
- `TopSearchBar` con icono de menú (abre drawer) e icono de búsqueda
- `ModalNavigationDrawer` con `DrawerMenu`
- `PlaceBottomSheet` que aparece al tocar un marcador (o automáticamente al arrancar en demo)

### `DrawerMenu` — menú lateral
- Perfil de usuario (nombre, badge "GUÍA LOCAL", nivel colaborador, nº reseñas)
- Items: Inicio, Lugares guardados, Mis reseñas, Configuración
- Toggle de modo oscuro
- Botón de cierre de sesión

### `PlaceBottomSheet` — ficha de lugar
- Nombre, categoría (con icono), dirección
- Tres chips de accesibilidad (física, sensorial, cognitiva) con nivel y color
- Puntuación con estrella
- Botones: "Detalles" y "Ir ahora" (sin implementar aún)

### `Theme.kt` — sistema de diseño
- Clase `AccessPathColors` con colores light y dark
- Colores de accesibilidad compatibles con WCAG AA
- `CompositionLocal` para acceder al tema en cualquier punto del árbol Compose

---

## Estado de implementación

### ✅ Implementado
- Arquitectura KMP completa (Android + iOS)
- Sistema de permisos de ubicación (Android y iOS)
- Mapa funcional en ambas plataformas con marcadores
- Modelos de dominio de accesibilidad
- Componentes de UI: drawer, barra de búsqueda, bottom sheet, controles de mapa
- Modo oscuro
- Sistema de temas con colores accesibles
- 7 lugares mock en Madrid

### ⏳ Pendiente / TODO
- **Backend real** — actualmente todo es mock local
- Navegación a pantalla de detalles de lugar
- Funcionalidad de búsqueda (actualmente solo log en consola)
- Filtrado por categoría o nivel de accesibilidad
- Pantalla de lugares guardados
- Pantalla de reseñas del usuario
- Pantalla de configuración
- Envío y lectura de reseñas
- Persistencia de datos local
- Autenticación de usuario (logout es un TODO)

---

## Configuración y claves API

### Clave de Google Maps
La clave va en `local.properties` (no se sube a git):
```properties
MAPS_API_KEY=AIzaSy...
```
Se inyecta en el `AndroidManifest.xml` vía el bloque `buildConfigField` en `composeApp/build.gradle.kts`.

### iOS
El `TEAM_ID` se configura en `iosApp/Configuration/Config.xcconfig`.
Para compilar en iOS se necesita abrir `iosApp/iosApp.xcodeproj` en Xcode.

---

## Comandos útiles

```bash
# Compilar Android (debug)
.\gradlew.bat :composeApp:assembleDebug   # Windows
./gradlew :composeApp:assembleDebug       # macOS/Linux

# Ejecutar tests
./gradlew test
./gradlew :composeApp:testDebugUnitTest

# Limpiar build
./gradlew clean
```

---

## Arquitectura de carpetas por responsabilidad

| Responsabilidad | Archivo(s) |
|-----------------|-----------|
| Punto de entrada Android | `androidMain/.../MainActivity.kt` |
| Punto de entrada iOS | `iosApp/ContentView.swift` + `iosMain/.../MainViewController.kt` |
| Navegación y pantalla raíz | `commonMain/.../App.kt` |
| Pantalla principal | `commonMain/.../ui/LandingScreen.kt` |
| Menú lateral | `commonMain/.../ui/components/DrawerMenu.kt` |
| Componentes de UI | `commonMain/.../ui/components/LandingComponents.kt` |
| Sistema de colores y tema | `commonMain/.../ui/theme/Theme.kt` |
| Modelos y datos | `commonMain/.../data/Place.kt` |
| Servicio de ubicación | `location/LocationService.kt` + implementaciones por plataforma |
| Vista de mapa | `map/MapView.kt` + implementaciones por plataforma |
| Versiones de dependencias | `gradle/libs.versions.toml` |
| Config de build | `composeApp/build.gradle.kts` |
