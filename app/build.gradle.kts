plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val bundledStockAssets = layout.buildDirectory.dir("generated/stock-assets")

tasks.register<Copy>("prepareBundledStockAssets") {
    from(rootProject.file("Залишки.XLSX"))
    into(bundledStockAssets)
}

android {
    sourceSets.getByName("main").assets.srcDir(bundledStockAssets)
    namespace = "ua.oblik.sklad"
    compileSdk = 35

    defaultConfig {
        applicationId = "ua.oblik.sklad"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.named("preBuild").configure {
    dependsOn("prepareBundledStockAssets")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
}
