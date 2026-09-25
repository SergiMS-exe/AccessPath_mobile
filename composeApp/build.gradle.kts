import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.buildkonfig)
}

// Lee local.properties una vez por build y expone sus valores al plugin buildkonfig.
// local.properties esta en .gitignore: cada desarrollador (y CI) define sus
// propias API_BASE_URL / MAPS_API_KEY localmente. La build falla si falta
// API_BASE_URL para evitar que se compile un APK apuntando a un dominio
// fantasma.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

val apiBaseUrl: String = localProps.getProperty("API_BASE_URL")
    ?: error(
        "API_BASE_URL no definido en local.properties.\n" +
        "Anade una linea con este formato:\n" +
        "  API_BASE_URL=https://tu-dominio.example.com"
    )

buildkonfig {
    packageName = "org.s3m4su.accesspath"

    // API_BASE_URL es publico (se ve en el APK), pero no se hardcodea en este
    // fichero: vive solo en local.properties (gitignored) para que cada entorno
    // (dev local, CI, prod) defina el suyo sin modificar el repo.
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "API_BASE_URL", apiBaseUrl)
    }

    // MAPS_API_KEY viaja al AndroidManifest via manifestPlaceholders mas abajo.
    // Aun asi la exponemos aqui para mantener un unico punto de lectura desde
    // local.properties y poder usarla desde Kotlin si algun dia hace falta.
    targetConfigs {
        create("release") {
            buildConfigField(
                FieldSpec.Type.STRING,
                "MAPS_API_KEY",
                localProps.getProperty("MAPS_API_KEY", "")
            )
        }
        create("debug") {
            buildConfigField(
                FieldSpec.Type.STRING,
                "MAPS_API_KEY",
                localProps.getProperty("MAPS_API_KEY", "")
            )
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.google.maps.compose)
            implementation(libs.play.services.location)
            implementation(libs.play.services.maps)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings)
            implementation(libs.ktor.client.auth)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.s3m4su.accesspath"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.s3m4su.accesspath"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        // El MAPS_API_KEY se inyecta en el AndroidManifest.xml (vía placeholder)
        // Y se expone a Kotlin vía buildkonfig. Ambos leen del mismo `localProps`
        // cargado arriba, así que solo se lee el fichero una vez por build.
        manifestPlaceholders["MAPS_API_KEY"] =
            localProps.getProperty("MAPS_API_KEY", "")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Firma del release.
    //
    // IMPORTANTE: esta configuracion usa la DEBUG keystore (~/.android/debug.keystore)
    // unicamente para que la release sea instalable en un dispositivo de pruebas
    // sin tener que generar una keystore propia. NO es production-ready:
    //  - La debug keystore es publica y conocida (cualquiera puede firmar con ella).
    //  - NO deberia usarse para publicar en Google Play u otros stores.
    //  - Cambia entre maquinas, asi que dos builds del mismo codigo pueden tener
    //    firmas distintas y no podran actualizarse in-place.
    //
    // Para produccion real:
    //   1. keytool -genkey -v -keystore release.keystore -alias accesspath \
    //      -keyalg RSA -keysize 2048 -validity 10000
    //   2. Mover release.keystore a androidMain/keys/ (gitignored).
    //   3. Sustituir este bloque signingConfigs por uno que lea el alias/pass de
    //      local.properties (nunca hardcodearlos en build.gradle.kts).
    signingConfigs {
        create("release") {
            // Apunta a la debug keystore por defecto de Android Studio.
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}

