pluginManagement {
    repositories {
        google()          // Google's Maven repository for AndroidX & Firebase :contentReference[oaicite:4]{index=4}
        mavenCentral()    // Maven Central for Compose, Retrofit, etc. :contentReference[oaicite:5]{index=5}
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()        // AndroidX, Play services, Firebase :contentReference[oaicite:6]{index=6}
        mavenCentral()  // Third-party libraries :contentReference[oaicite:7]{index=7}
    }
}


rootProject.name = "NutritionLabelAnalyzer"
include(":app")
