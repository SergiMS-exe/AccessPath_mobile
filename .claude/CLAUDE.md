# CLAUDE.md — AccessPath Mobile

Instrucciones para Claude Code al trabajar en este repositorio.

## Convenciones de codigo

- **Sin acentos ni diacriticos** en codigo, comentarios, identificadores ni strings de log.
  El texto de UI visible al usuario puede tener acentos donde importe.
- **No ejecutar Gradle builds** (`assembleDebug`, `build`, `installDebug`, etc.).
  El usuario compila e instala por su cuenta.
- El proyecto usa KMP: codigo compartido en `commonMain`, plataforma especifica en
  `androidMain` / `iosMain`. Preferir `commonMain` siempre que sea posible.

## Stack

- **Kotlin Multiplatform** + **Compose Multiplatform 1.7.3**
- **Kotlin 2.1.0**, **AGP 8.8.0**
- **Ktor 3.1.3** — cliente HTTP KMP
- **kotlinx.serialization 1.7.3**
- **multiplatform-settings 1.3.0** (SharedPreferences / NSUserDefaults)
- **Google Maps Compose 6.4.0** (Android only)

## Arquitectura

```
commonMain/
  App.kt                         # Navegacion por stack (listOf<Screen>)
  data/
    api/
      ApiClient.kt               # httpClient con Auth plugin (Bearer + refresh automatico)
      AuthApi.kt                 # object AuthApi { login(), register() }
      AuthEvents.kt              # Obsoleto — puede eliminarse
    auth/
      AuthModels.kt              # LoginRequest, RegisterRequest, AuthUser, LoginResponse, RefreshResponse
      AuthRepository.kt          # object singleton; StateFlow<AuthState>; persistencia con Settings
  ui/
    auth/AuthScreen.kt           # Login + registro en una sola pantalla (tabs)
    LandingScreen.kt             # Mapa + drawer + barra de busqueda
    components/DrawerMenu.kt     # Drawer con perfil de usuario y logout
    ...
```

## Auth — flujo completo

1. `AuthScreen` llama `AuthRepository.login()` o `AuthRepository.register()`
2. `AuthRepository` llama a la API, guarda `token` + `refreshToken` + `user` en `Settings`
3. Al arrancar la app, `AuthRepository.init` restaura el estado desde `Settings`
4. Ktor `Auth` plugin añade `Authorization: Bearer <token>` en cada peticion automaticamente
5. Si recibe 401, llama a `POST /api/v1/auth/refresh` de forma transparente y reintenta
6. Si el refresh falla (token de 30 dias expirado), llama `AuthRepository.logout()`
7. `App.kt` observa `AuthRepository.state`; si pasa a `Unauthenticated` redirige a `AuthScreen`

## API

- Base URL en `data/api/ApiClient.kt` → `API_BASE_URL`
- En desarrollo: IP local del PC (dispositivo fisico en la misma red)
- El cliente HTTP maneja refresh de tokens de forma transparente

## Navegacion

Stack-based en `App.kt`: `var stack by remember { mutableStateOf(listOf<Screen>(...)) }`.
Sin libreria de navegacion externa. Screens: `Auth`, `Landing`, `Detail`.

## Configuracion Android

- `AndroidManifest.xml` referencia `@xml/network_security_config` para permitir
  trafico HTTP cleartext a la IP de desarrollo.
- Actualizar `network_security_config.xml` si cambia la IP del servidor.
