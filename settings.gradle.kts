pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useModule("com.android.tools.build:gradle:8.7.2")
            }
            if (requested.id.id == "org.jetbrains.kotlin.android") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
            }
            if (requested.id.id == "com.google.dagger.hilt.android") {
                useModule("com.google.dagger:hilt-android-gradle-plugin:2.51.1")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "SGC Teknisyen"
include(":app")