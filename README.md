# AccessPath — Mobile

App movil para la plataforma AccessPath: busca y valora la accesibilidad de lugares publicos.

Desarrollada con **Kotlin Multiplatform** y **Compose Multiplatform**. Codigo compartido en Android e iOS.

## Stack

| Componente         | Tecnologia                              |
|--------------------|-----------------------------------------|
| Lenguaje           | Kotlin 2.1.0                            |
| UI                 | Compose Multiplatform 1.7.3             |
| HTTP               | Ktor 3.1.3                              |
| Serializacion      | kotlinx.serialization 1.7.3             |
| Persistencia local | multiplatform-settings 1.3.0            |
| Mapas (Android)    | Google Maps Compose 6.4.0               |
| Min Android SDK    | 24                                      |

## Estructura

```
composeApp/src/
  commonMain/         # Codigo compartido
    App.kt            # Navegacion (stack de pantallas)
    data/
      api/            # Cliente HTTP y endpoints
      auth/           # Estado de autenticacion y persistencia
    ui/
      auth/           # Pantalla de login y registro
      components/     # Componentes reutilizables (drawer, mapa, busqueda...)
      theme/          # AccessPathTheme con colores y modo oscuro
  androidMain/        # Implementaciones Android (mapa, ubicacion)
  iosMain/            # Implementaciones iOS
iosApp/               # Wrapper nativo iOS (SwiftUI)
```

## Configuracion

### Android

1. Abre el proyecto en Android Studio
2. Sincroniza Gradle
3. Configura la clave de Google Maps en `local.properties`:
   ```
   MAPS_API_KEY=tu_clave_aqui
   ```
4. Ajusta `API_BASE_URL` en `data/api/ApiClient.kt` con la IP del backend

### iOS

1. Abre `iosApp/iosApp.xcodeproj` en Xcode
2. Configura `TEAM_ID` en `iosApp/Configuration/Config.xcconfig`

## Auth

El flujo de autenticacion es transparente para el usuario:

- Login → guarda `token` (1h) + `refresh_token` (30 dias) en almacenamiento local
- Al relanzar la app → restaura sesion automaticamente desde almacenamiento
- Si el token expira → Ktor lo renueva en silencio con el refresh token
- Solo vuelve al login si han pasado mas de 30 dias sin abrir la app

## Desarrollo

No se necesita compilar el backend para trabajar en la UI: hay datos mock en `data/MockPlaces.kt`.

Para pruebas end-to-end con el backend real ver [AccessPath_backend](../AccessPath_backend/README.md).

Ver `.claude/CLAUDE.md` para convenciones de trabajo y arquitectura detallada.
