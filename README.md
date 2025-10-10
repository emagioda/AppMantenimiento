# MyApp Mantenimiento 🔧📱

Aplicación Android desarrollada en **Kotlin + Jetpack Compose**,
siguiendo principios de **Clean Architecture**.\
La app permite **escanear códigos QR de máquinas**, acceder a un **flujo
de diagnóstico interactivo**, y consultar una **agenda de contactos
técnicos y proveedores**.

------------------------------------------------------------------------

## 📂 Estructura del proyecto

    app/
     └── src/main/java/com/emagioda/myapp
         ├── MainActivity.kt        # Entry point de la app
         ├── core/                  # (Reservado para utilidades/commons)
         ├── data/                  # Capa de datos (assets, repositorios, prefs)
         │   ├── datasource/        # Lectura de JSON desde assets
         │   ├── repository/        # Implementaciones de repositorios
        ├── di/                    # Inyección de dependencias simple (ServiceLocator)
         ├── domain/                # Capa de dominio (reglas de negocio)
         │   ├── model/             # Modelos puros del dominio
         │   ├── repository/        # Interfaces de repositorios
         │   └── usecase/           # Casos de uso
         ├── presentation/          # Capa de UI (Compose)
         │   ├── navigation/        # NavHost y rutas
         │   ├── screen/            # Pantallas principales
         │   │   ├── contacts/      # Listado de contactos
         │   │   ├── diagnostic/    # Flujo de diagnóstico paso a paso
         │   │   ├── home/          # Pantalla inicial
         │   │   └── settings/      # Configuración general
         │   ├── viewmodel/         # ViewModels para UI
         │   └── ui/                # Componentes reutilizables
         │       ├── scanner/       # Escáner QR con overlay
         │       └── theme/         # Colores, tipografía, estilos
    assets/
     ├── contacts/                  # Agenda de contactos
     │   ├── providers.json
     │   └── technicians.json
     └── diagnostics/               # Árboles de diagnóstico
         ├── machines.json          # Índice de máquinas -> templateId
         └── templates/             # Flujos en JSON
             ├── trimec_tb_v1.json
             └── trimec_tb_v1_es.json

------------------------------------------------------------------------

## 🚀 Funcionalidades principales

-   **Escaneo de QR** con cámara (ML Kit + CameraX).
-   **Diagnóstico guiado** por árbol de decisiones (JSON → modelo de
    dominio → UI).
-   **Agenda de contactos** técnicos y proveedores, con acciones rápidas
    (llamar, WhatsApp, email).
-   **Paleta oscura** consistente para toda la experiencia.

------------------------------------------------------------------------

## 🛠️ Tecnologías

-   **Kotlin**
-   **Jetpack Compose (Material3)**
-   **CameraX + ML Kit (Barcode Scanner)**
-   **AndroidX Navigation**
-   **Clean Architecture** (Data / Domain / Presentation)

------------------------------------------------------------------------

## 📦 Assets

-   `contacts/`: contactos de **técnicos** y **proveedores**.
-   `diagnostics/`: define las máquinas (`machines.json`) y sus flujos
    de diagnóstico (`templates/*.json`).

------------------------------------------------------------------------

## ⚙️ Gradle & Configuración

-   Dependencias centralizadas en `libs.versions.toml`.
-   Módulo `:app` configurado en `build.gradle.kts`.

------------------------------------------------------------------------

✍️ **Próximos pasos**:\
- \[ \] Mejorar los templates JSON de diagnóstico.\
- \[ \] Incorporar más máquinas/contactos.\
- \[ \] Agregar tests unitarios de ViewModels y UseCases.\
- \[ \] Preparar despliegue en Play Store.
