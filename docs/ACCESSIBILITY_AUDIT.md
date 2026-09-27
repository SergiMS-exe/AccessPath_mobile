# Auditoría de accesibilidad — AccessPath Mobile

Fecha: 2026-09-27
Alcance: `composeApp/src/commonMain/...` (Kotlin Multiplatform + Compose Multiplatform 1.7.3)
Referencia: WCAG 2.1 nivel AA. Mobile: WCAG2Mobile / Mobile Web Best Practices.

> No se ejecutó la app (restricción del repo). El análisis es estático sobre
> el código + cálculo numérico de contraste WCAG sobre la paleta declarada.

## Resumen ejecutivo

| Categoría                  | Hallazgos | Críticos | Graves | Medios |
|----------------------------|----------:|---------:|-------:|-------:|
| Contraste WCAG AA          | 14 + 12  | 8        | 10     | 8      |
| Textos alternativos        | 8         | 2        | 4      | 2      |
| Navegación por teclado     | 5         | 1        | 2      | 2      |
| Roles y semántica          | 7         | 2        | 3      | 2      |
| Estados dinámicos          | 4         | 2        | 2      | 0      |
| Touch targets              | 3         | 0        | 2      | 1      |
| Movimiento y tamaño fuente | 2         | 0        | 1      | 1      |

**Veredicto:** la base está bien encaminada — el semáforo usa siempre texto + color (cumple 1.4.1), el opt-in de perfil funcional respeta privacidad, los chips tienen labelVisible, los botones Material3 ya exponen `Role.Button` y focus nativo. Los fallos son sobre todo **contraste del texto terciario y placeholders**, **falta de `stateDescription` para elementos seleccionables**, y **BasicTextField sin label accesible persistente**.

---

## 1. Contraste WCAG (paleta declarada en `Theme.kt`)

Umbrales WCAG 2.1 AA: **texto normal 4.5:1**, **texto grande 3:1** (≥ 18pt o 14pt bold), **componentes UI 3:1**.

### 1.1 Light theme — fallos

| Ratio | fg / bg | Dónde | Severidad |
|------:|---------|-------|-----------|
| **2.46:1** | `textTertiary #9E9E9E` / `surfaceVariant #F5F5F5` | `AuthScreen.kt:355` placeholder del campo (NO label real) | **Crítico** |
| 2.57:1 | `textTertiary` / `background #FAFAFA` | `ProfileScreen.kt:211` consentimiento, `ContributeScreen.kt:104` contador, `:333` "Salir…" | **Crítico** |
| 2.68:1 | `textTertiary` / `surface #FFFFFF` | `LandingComponents.kt:202` placeholder SearchBar, `:319` "No se encontraron", `PlaceDetailScreen.kt:613` "Opiniones divididas", `:701` confianza, `:755` fecha submission | **Crítico** |
| 3.46:1 | `moderate #E65100` / `moderateBg #FFF3E0` | chip "Con salvedades" en `LandingComponents.kt:773-790` | **Grave** |
| 3.79:1 | `Color.White` / `moderate #E65100` | chip de semáforo AMARILLO **seleccionado** (`LandingComponents.kt:537`) | **Grave** |
| 3.36:1 | `Color.White` / `black @ 55%` | pildora "N fotos" en `PlaceDetailComponents.kt:150` | **Grave** |
| 3.95:1 | `Color.White` / `black @ 50%` | contador "X/Y" en lightbox `PlaceDetailComponents.kt:238` | **Grave** |
| 1.14:1 | `Color.White` / `black @ 94%` | fondo del lightbox entero — solo en `:238` overlay | **Crítico** (sobre el overlay general) |
| 4.23:1 | `textSecondary #757575` / `surfaceVariant #F5F5F5` | `AuthScreen.kt:129` tab inactivo, `DrawerMenu.kt` no afecta, `ProfileScreen.kt:120` propósito, `PlaceDetailScreen.kt:814` empty state, `ContributeScreen.kt:246/293/378` | **Medio** |
| 4.41:1 | `textSecondary` / `background` | `AuthScreen.kt:104` subtítulo, `ProfileScreen.kt:200` "Perfil guardado" | **Medio** |

### 1.2 Dark theme — fallos

| Ratio | fg / bg | Dónde | Severidad |
|------:|---------|-------|-----------|
| **2.86:1** | `error #CF6679` / `background @ error10%` | mensaje de error `AuthScreen.kt:188` | **Grave** (error debe ser legible) |
| **2.99:1** | `textTertiary #757575` / `surfaceVariant #2D2D2D` | `AuthScreen.kt:355` placeholder, `PlaceDetailScreen.kt:820` "Sé la primera…" | **Crítico** |
| 3.62:1 | `textTertiary` / `surface #1E1E1E` | placeholders y textos terciarios | Medio |
| 4.07:1 | `textTertiary` / `background #121212` | consentimiento, "Salir…", contador respuestas | Medio |
| 1.10–1.23:1 | `Color.White` / `black @ 50–94%` | pildoras negras en dark | **Crítico** (invisible) |

### 1.3 Pasa correctamente (≥ AA)

- `textPrimary` sobre `surface` / `background` / `surfaceVariant`: 12–16:1 ✓
- Botones primarios (`Color.White` sobre `#6200EE` / `#BB86FC`): 7.6–7.9:1 ✓
- Chips de semáforo VERDE/ROJO/NO_DATA (white sobre fondo): 4.8–11:1 ✓
- `error #B00020` sobre `background` light: 7.0:1 ✓
- Banner de ubicación desactivada (`onErrorContainer`/`errorContainer`): 13:1 ✓

### 1.4 Plan de remediación de contraste

1. **`textTertiary #9E9E9E` (light) y `#757575` (dark)**: subir a `#6E6E6E` light y `#A0A0A0` dark. Mínimo 4.5:1 sobre `surface`/`background`/`surfaceVariant`. Aplica a placeholders, fechas, hints de "no resultados" y consentimiento.
2. **Pildoras sobre negro** (`PlaceDetailComponents.kt:128-153`, `:230-242`): cambiar `Color.Black @ 0.5–0.94` por un overlay que respete contraste, p. ej. `Color.Black.copy(alpha = 0.65f)` + sombra para legibilidad, o mejor, mover los metadatos a una barra inferior opaca con texto blanco sobre `surface` semitransparente. **Crítico en dark** donde están al borde de invisibilidad.
3. **Chip amarillo** (`LandingComponents.kt:537`): o bien se sube `moderate` a `#B85400` (4.62:1 sobre `#FFF3E0`) y `#E69400` (4.6:1 sobre `#4A2800` dark), o se cambia la combinación a `Color.Black` como `selectedLabelColor` cuando el contenedor es claro (recomendado para AA en los 4 estados del semáforo).
4. **`textSecondary #757575` (light)** sobre `surfaceVariant` y `background`: subir a `#5F5F5F` (5.4:1 light). Cubre subtítulos, tabs, textos empty.
5. **Mensaje de error en dark**: subir `error` a un valor con más saturación en L (la paleta dark de Material3 usa `#FFB4AB` por defecto). Documentar en `Theme.kt` que `error` debe pasar 4.5:1 sobre `background`.

---

## 2. Textos alternativos (`contentDescription`)

Inventario completo en `grep contentDescription commonMain`. Diagnóstico:

### 2.1 Aceptable
- Iconos de acción con label visible cercano (`LandingComponents.kt:212, 220`, `AddPlaceSheet.kt:150` "Limpiar", `AuthScreen.kt:369` "Mostrar"/"Ocultar", `ProfileScreen.kt:70, 164`, `PlaceDetailScreen.kt:128, 319, 503, 619`).
- Iconos FAB: "Mi ubicación", "Zoom in/out" — bien.

### 2.2 Hallazgos

| # | Archivo:línea | Problema | Severidad |
|---|---|---|---|
| 1 | `LandingComponents.kt:163-169` | Icono `Menu` decorativo usado como botón — `Modifier.size(24.dp).clickable(onMenuClick)` sin `onClickLabel`, sin `Role.Button`. TalkBack dirá "icono, doble toque para activar" sin decir "abrir menú". | **Grave** |
| 2 | `LandingComponents.kt:781-785` | Icono `Icons.AutoMirrored.Filled.Accessible` dentro de `AccessibilityStateChip` con `contentDescription = null` — el texto del chip lo describe, pero al fusionarse con TalkBack podría omitir el icono. Mejor `contentDescription = null` y `Modifier.semantics(mergeDescendants = true)` en el chip, **o** añadir `contentDescription = state.label`. | Medio |
| 3 | `PlaceDetailScreen.kt:386-392` | Botón "Ver más (N) / Ver menos" — el `TextButton` lleva texto, pero el `Icon` `ExpandMore/ExpandLess` no se anuncia con su rol. Solo el texto se lee. | Medio |
| 4 | `DrawerMenu.kt:208-211, 247-250, 289-292, 83-87` | Iconos dentro de filas con label visible — TalkBack los anuncia por separado. Usar `Modifier.semantics(mergeDescendants = true)` en `DrawerMenuItemRow` y `Surface` del DarkMode. | **Grave** |
| 5 | `ProfileScreen.kt:139-170` | `Surface.clickable { toggleNeed(need.key) }` con un `Icon Check` solo cuando seleccionado — no hay `stateDescription` para "seleccionado / no seleccionado". | **Grave** |
| 6 | `LandingComponents.kt:479-492, 526-540` | `FilterChip` sin `stateDescription`. TalkBack dice "Filtro, doble toque para activar" sin indicar el estado. | **Grave** |
| 7 | `PlaceDetailComponents.kt:62-73, 100-110` | `PhotoPlaceholder` decorativo con `contentDescription = null` — bien, pero al usarlo en galería (`PlaceDetailScreen.kt:769-781` `SubmissionCard`), la foto clickable no anuncia nada descriptivo. Cuando se carguen imágenes reales, **deben** llevar descripción (qué muestra la foto: rampa, baño adaptado, etc.). | **Crítico cuando haya backend** |
| 8 | `LandingScreen.kt:188-227` | Banner "La ubicación está desactivada…" — el `Icon LocationOff` tiene `contentDescription = null`. Como el `Text` del banner describe el icono, no es crítico, pero si se internacionaliza o el texto cambia, deja un rol ambiguo. | Bajo |

### 2.3 Plan de remediación de alternativas

1. Cualquier `Modifier.clickable {}` en un Surface/Row que actúa como botón debe tener `onClickLabel = "..."` (texto que describe la acción, no el contenido) — ej. `Modifier.clickable(onClick = ..., onClickLabel = "Abrir menú")`.
2. `FilterChip`, `DrawerMenuItemRow`, `Surface` con toggle en `ProfileScreen`: añadir `Modifier.semantics { stateDescription = if (selected) "Seleccionado" else "No seleccionado" }`.
3. Antes de cargar imágenes reales con Coil/Kamel/etc., definir un modelo con `altText` por foto (rampa, baño, mostrador, etc.). El placeholder actual no tiene esa información.

---

## 3. Etiquetas accesibles de los campos (placeholder ≠ label)

Esto es **crítico para TalkBack/VoiceOver** y a menudo se pasa por alto.

### 3.1 Hallazgos

| Archivo:línea | Problema | Severidad |
|---|---|---|
| `AuthScreen.kt:333-361` | `BasicTextField` con `decorationBox` que renderiza `Text(label)` solo cuando `value.isEmpty()`. Cuando el usuario escribe, **el label desaparece de la composición** y TalkBack deja de anunciar el nombre del campo. | **Crítico** |
| `LandingComponents.kt:175-205` | `BasicTextField` (búsqueda). Mismo patrón: "Buscar lugares accesibles" desaparece al escribir. | **Crítico** |
| `AddPlaceSheet.kt:140-157` | `OutlinedTextField` con `label` — bien, Material3 lo mantiene flotante. ✓ |
| `ContributeScreen.kt:391-397` | `OutlinedTextField` para comentario — `placeholder` solo visible cuando vacío. Al escribir, el placeholder desaparece y TalkBack anuncia un campo vacío sin nombre. | **Grave** |

### 3.2 Plan de remediación

- `AuthField`: cambiar `BasicTextField` + `decorationBox` por `OutlinedTextField(value, onValueChange, label = { Text(label) }, singleLine, leadingIcon = { Icon(icon, null) }, trailingIcon = { visibility }, keyboardOptions, keyboardActions)`. Eso mantiene el label flotante y la semántica nativa (role.TextField con label).
- Mismo cambio para `SearchBar`.
- `CommentStep`: añadir `label = { Text("Comentario") }` al `OutlinedTextField` del comentario.

---

## 4. Roles y semántica

### 4.1 Hallazgos

| # | Archivo:línea | Problema | Severidad |
|---|---|---|---|
| 1 | `AuthScreen.kt:115-136` | Tabs implementados como dos `TextButton` dentro de un `Surface`. No son `Tab`/`TabRow` de Material3, así que TalkBack no anuncia "Pestaña 1 de 2". | **Grave** |
| 2 | `PlaceDetailScreen.kt:280-287` | `Dialog` con `AccessibilityDetailDialog` — Material3 Dialog ya gestiona foco, OK. Pero no se marca `Modifier.semantics { dismissAction = ... }` para que TalkBack pueda cerrar con gesto de swipe-down. | Medio |
| 3 | `LandingComponents.kt:579-605` | `Surface` del bottom sheet es `draggable` vertical — TalkBack no anuncia que es cerrable por arrastre. Añadir un botón "Cerrar" alternativo (X) arriba o usar `BottomSheetScaffold` de Material3. | **Grave** |
| 4 | `DrawerMenu.kt:59` | `ModalNavigationDrawer` — Material3 ya gestiona foco y TalkBack correctamente. ✓ |
| 5 | `LandingScreen.kt:106-129` | Drawer OK, pero `gesturesEnabled = drawerState.isOpen` lo cierra al hacer tap fuera — OK. Sin embargo, no hay indicación visible de "Menú abierto" fuera del drawer; usuarios con baja visión pueden no darse cuenta. | Bajo |
| 6 | `LandingComponents.kt:217-223` | El `IconButton` "Filtros" no indica estado de filtros activos a TalkBack — solo cambia el color. | **Grave** |
| 7 | `PlaceDetailScreen.kt:125-131, ProfileScreen.kt:67-73, PlaceDetailComponents.kt:223-228, LandingComponents.kt:150-160, AddPlaceSheet.kt:108-114` | `IconButton(onClick = onBack/onClose)` con buen `contentDescription`. ✓ |

### 4.2 Plan de remediación de roles

- Sustituir los tabs de `AuthScreen.kt:115-136` por `TabRow` + `Tab(selected, onClick, text = { ... }, selectedContentColor = ..., unselectedContentColor = ...)`.
- En `LandingComponents.kt:217-223`, añadir `Modifier.semantics { stateDescription = if (filter.activeCount > 0) "${filter.activeCount} filtros activos" else "Sin filtros activos" }` al `IconButton` de Filtros.
- En `PlaceDetailScreen.kt`, exponer `dismissAction` en el `Dialog` para que TalkBack/VoiceOver pueda cerrarlo con swipe.
- Reemplazar el bottom sheet custom por `ModalBottomSheet` (Material3) — gestiona foco, cierre con back, y anuncio de "hoja inferior" automáticamente.

---

## 5. Estados dinámicos y `liveRegion`

WCAG 4.1.3 (Status Messages) exige que los cambios importantes se anuncien.

| # | Archivo:línea | Problema | Severidad |
|---|---|---|---|
| 1 | `PlaceDetailScreen.kt:160-164` | `Snackbar "Dirección copiada"` — `SnackbarHost` no se marca como `liveRegion`. TalkBack/VoiceOver no anuncia automáticamente. | **Grave** |
| 2 | `LandingScreen.kt:187-227` | Banner de ubicación desactivada aparece/desaparece sin `liveRegion`. | **Grave** |
| 3 | `ProfileScreen.kt:185-203` | "Perfil guardado" — mensaje informativo sin `liveRegion`. | Medio |
| 4 | `ContributeScreen.kt:357-381, 414` | "Comentario guardado" — sin `liveRegion`. | Medio |

### Plan
Envolver cada Surface/mensaje informativo en `Modifier.semantics { liveRegion = LiveRegionMode.Polite }`. Para errores usar `LiveRegionMode.Assertive`.

---

## 6. Touch targets (mínimo recomendado 48×48 dp; WCAG 2.5.5 nivel AAA, Android mínimo recomendado)

| Archivo:línea | Componente | Tamaño real | Severidad |
|---|---|---|---|
| `LandingComponents.kt:163-169` | Icono `Menu` clicable | 24×24 dp sin padding | **Grave** |
| `AuthScreen.kt:363-374` | `IconButton` mostrar/ocultar contraseña | 20×20 dp explícito (`Modifier.size(20.dp)`) | **Grave** |
| `LandingComponents.kt:254-260, 263-265` | Iconos en pildora "Buscar en esta zona" | 18 dp decorativos — bien (hay texto al lado) | OK |
| `PlaceDetailScreen.kt:317-323, 387-393, 617-622` | Iconos copiar, ExpandMore, dentro de filas con `Modifier.clickable` o `TextButton` — heredan tamaño del contenedor | ≥ 48 dp ✓ | OK |
| `LandingComponents.kt:809-822, 831-851` | FAB MiUbicación, IconButton zoom | 48+ dp por defecto ✓ | OK |

### Plan
- Envolver el `Icon Menu` en un `IconButton` o añadir `Modifier.size(48.dp).clickable(..., onClickLabel = "Abrir menú")`.
- Quitar `Modifier.size(20.dp)` del IconButton de visibilidad de contraseña — Material3 IconButton ya garantiza 48×48.

---

## 7. Navegación por teclado (ChromeOS, Android con teclado físico, focus traversal)

| # | Hallazgo | Severidad |
|---|---|---|
| 1 | No se observa `focusRequester`, `Modifier.focusable()`, ni `focusGroup()` en la app. Compose Material3 da focus automático a `Button`, `IconButton`, `TextField`, pero `Surface.clickable`, `Row.clickable` (AddressCard, DrawerMenuItemRow, ProfileScreen rows, SuggestionRow) **sí son focusables** vía `clickable`, sin embargo: | — |
| 2 | El `AddressCard` (`PlaceDetailScreen.kt:294-325`) tiene `Modifier.clickable {}` — focusable con teclado, pero al activarse con Enter abre el portapapeles y muestra un Snackbar. La confirmación no se anuncia sin `liveRegion`. | Medio |
| 3 | `ModalNavigationDrawer` (Landing) cierra al pulsar Escape ✓ (Material3 lo gestiona). Pero los items del menú no llevan `Tab` order explícito; usan orden visual. | Bajo |
| 4 | `HorizontalPager` del lightbox (`PlaceDetailComponents.kt:196-213`) — TalkBack swipe-navigation funciona ✓, pero no hay indicación de cuántos hay más allá del contador `X/Y` (que sí existe). | OK |
| 5 | El mapa consume flechas; TalkBack en Android permite explorar el mapa ✓. | OK |

### Plan
- Para teclado físico en ChromeOS, validar que el orden de tabulación es coherente. Considerar `Modifier.focusGroup()` en la cabecera de cada pantalla para que el foco no "salte" del bottom sheet al mapa de fondo.
- Probar con un teclado Bluetooth en el emulador Android: ¿se llega al FAB MiUbicación sin pasar por el mapa? ¿Se puede abrir el drawer con teclado?

---

## 8. Movimiento y tamaño de fuente

| # | Hallazgo | Severidad |
|---|---|---|
| 1 | Las animaciones (`AnimatedContent`, `AnimatedVisibility`, transición del stack) no consultan `Settings.Global.ANIMATOR_DURATION_SCALE` ni `LocalConfiguration.current.fontScale`. Para usuarios con sensibilidad vestibular (WCAG 2.3.3) las animaciones pueden ser problemáticas. | **Grave** |
| 2 | No se respeta `fontScale` para evitar desbordes; los headers usan `maxLines = 1` (PlaceDetailScreen.kt:138), pero otros `Text` largos (descripción, comentario) sí pueden romper el layout. | Medio |

### Plan
- Ofrecer ajuste "Reducir movimiento" en el DrawerMenu (al lado del toggle Dark Mode).
- Para respetar el ajuste del sistema: leer `LocalConfiguration.current.fontScale` y aplicar `nonFontScaledSp` (vía `LocalDensity`) a textos críticos que no deben crecer. Compose aún no expone `nonFontScaledSp` en Multiplatform estable; usar `withCompositionLocalProvider` con `LocalDensity` ajustada en elementos clave.

---

## 9. Idioma y dirección de lectura

- Manifest declara `android:supportsRtl="true"` ✓
- No se observa `android:localeConfig` en `AndroidManifest.xml` — la app no declara idiomas soportados. **Medio** (no rompe nada, pero impide que el sistema ajuste por usuario).
- Strings en español (`strings.xml` debería estar internacionalizado — no se observa ningún recurso `values-en/` ni `values-ca/`). **Grave** si la app se va a publicar en App Store / Play Store fuera de España.

### Plan
- Mover todos los literales visibles a `androidMain/res/values/strings.xml` (hoy hay muchos hardcoded en `Text(text = "...")`). Crear `values-en/` y `values-ca/`. Mantener la UI en común pero con `stringResource(...)`.

---

## 10. Mapa y marcadores

| # | Hallazgo | Severidad |
|---|---|---|
| 1 | Los marcadores (`MapView.android.kt:74-93`) usan `title = place.name` + `snippet = place.address` — Google Maps provee TalkBack nativo para infoWindow ✓ |
| 2 | El color de los marcadores (verde/naranja/rojo/azul) es **solo color**. La información del estado está también en el `snippet`? `snippet = place.address` — no. El estado de accesibilidad se pierde en el info window. | **Grave** |
| 3 | `MapControlButtons` tienen buen `contentDescription` ✓. Pero al estar sobre el mapa, pueden robar foco accidentalmente. | Bajo |

### Plan
- En `MapView.android.kt`, ampliar `snippet` con `"${place.overallState.label} · ${place.address}"` o crear un `MarkerInfoWindowAdapter` que pinte el semáforo + texto.

---

## 11. Hallazgos menores / positivos

**Aciertos** (no requieren cambios):

- El semáforo siempre va con texto + color (`AccessibilityState.label` en cada chip): cumple WCAG 1.4.1 ✓
- El opt-in de perfil funcional con consentimiento explícito está bien diseñado (Privacy by Design) ✓
- Los iconos de acción tienen `contentDescription` en su mayoría ✓
- `Switch` (modo oscuro) lo provee Material3 ✓
- `PasswordVisualTransformation` + toggle "Mostrar/Ocultar" bien etiquetado ✓
- `Snackbar` para feedback tras acciones (copiar dirección, etc.) — solo falta `liveRegion` ✓
- `imePadding` en formularios (AuthScreen.kt:87) ✓
- Soporte RTL en Manifest ✓
- HTTPS-only en release (network_security_config.xml) ✓
- `supportsRtl` declarado ✓

---

## 12. Plan priorizado

### Sprint 1 (crítico — bloquea lanzamiento)

1. **Contraste de `textTertiary`**: subir a `#6E6E6E` light y `#A0A0A0` dark en `Theme.kt`. Afecta a todos los placeholders, consentimientos, fechas y textos empty.
2. **Campos con label persistente**: cambiar `BasicTextField` de `AuthField` y `SearchBar` a `OutlinedTextField` con `label = { Text(...) }`. Añadir `label` al `OutlinedTextField` de `CommentStep`.
3. **`stateDescription` en chips y filas seleccionables**: `FilterChip` (categoría y semáforo), `ProfileScreen` necesidades, `DrawerMenuItemRow` item seleccionado.
4. **Pildoras negras en lightbox** (`PlaceDetailComponents.kt:128-153`, `:230-242`): subir el alpha a 0.65–0.75 o sustituir fondo por `surface` semitransparente.

### Sprint 2 (grave)

5. Sustituir tabs custom de `AuthScreen.kt` por `TabRow` + `Tab`.
6. `onClickLabel` en `Surface.clickable` de menú, AddressCard, AuthField iconos.
7. `liveRegion` en Snackbar de "Dirección copiada", banner de ubicación, "Perfil guardado", "Comentario guardado".
8. Sustituir `PlaceBottomSheet` custom por `ModalBottomSheet` (Material3).
9. Touch targets: `IconButton` del visibility toggle (quitar `.size(20.dp)`), icono Menú de SearchBar envolver en IconButton o `.size(48.dp)`.
10. `snippet` del marcador incluir `place.overallState.label`.

### Sprint 3 (medio)

11. Subir `textSecondary` a `#5F5F5F` light.
12. Chip amarillo: ajustar `moderate` o invertir `selectedLabelColor` a negro.
13. Internacionalización: mover todos los `Text(text = "...")` a `strings.xml`.
14. Ofrecer toggle "Reducir movimiento" en DrawerMenu.
15. Manejar `fontScale` para evitar desbordes en headers y nombres largos.

### Sprint 4 (recomendado)

16. Test automatizado de contraste en `commonTest` con `createComposeRule` (ver siguiente sección).
17. Audit con TalkBack real en dispositivo físico y VoiceOver en simulador iOS.
18. Validar con `lighthouse-mobile` o `axe-android` en build debug.

---

## 13. Tests automatizados recomendados (KMP-native)

`kotlin.test` ya está disponible vía KMP. Compose Multiplatform trae `createComposeRule()` para `commonTest`. Patrón sugerido:

### 13.1 Test de contraste

```kotlin
// commonTest/kotlin/.../ThemeContrastTest.kt
class ThemeContrastTest {
    @Test fun `textTertiary on surface meets AA in light theme`() {
        val ratio = Wcag.contrast(LIGHT.textTertiary, LIGHT.surface)
        assertTrue(ratio >= 4.5, "ratio was $ratio")
    }
    @Test fun `textTertiary on surface meets AA in dark theme`() { ... }
    // replicar para cada par documentado en §1
}
```

### 13.2 Test de semántica

```kotlin
// commonTest/kotlin/.../AuthFieldSemanticsTest.kt
class AuthFieldSemanticsTest : ComposeTest() {
    @Test fun `AuthField expone label como semantics`() {
        composeTestRule.setContent { AuthField("a@b.com", {}, "Correo electronico", Icons.Filled.Email) }
        composeTestRule
            .onNode(hasContentDescription("Correo electronico").or(hasText("Correo electronico")))
            .assertExists()
    }
}
```

### 13.3 Test de touch target

```kotlin
@Test fun `Menu icon cumple 48dp`() {
    composeTestRule.setContent { SearchBar(...) }
    val node = composeTestRule.onNodeWithContentDescription("Menu")
    val size = node.fetchSemanticsNode().layoutInfo.coordinates.size
    assertTrue(size.width >= 48.dp && size.height >= 48.dp)
}
```

### 13.4 Test de liveRegion / stateDescription

```kotlin
@Test fun `FilterChip expone stateDescription`() {
    composeTestRule.setContent {
        FilterChip(selected = true, onClick = {}, label = { Text("Restaurante") })
    }
    composeTestRule
        .onNode(hasStateDescription("Seleccionado"))
        .assertExists()
}
```

---

## 14. Recursos

- WCAG 2.1: https://www.w3.org/TR/WCAG21/
- WCAG2Mobile: https://www.w3.org/TR/mobile-accessibility-mapping/
- Compose Accessibility: https://developer.android.com/jetpack/compose/accessibility
- Material 3 a11y guidance: https://m3.material.io/foundations/accessible-design/accessibility-basics
- Script de contraste usado para este análisis: `wcag_contrast.py` (Python, portable).