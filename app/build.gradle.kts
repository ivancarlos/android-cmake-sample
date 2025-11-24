import org.gradle.api.tasks.Exec

plugins {
    id("com.android.application")
    kotlin("android")
}

val compileSdkProjectVersion: Int by project
val minSdkProjectVersion: Int by project
val targetSdkProjectVersion: Int by project

android {
    compileSdk = compileSdkProjectVersion

    defaultConfig {
        applicationId = "fr.bowserf.cmakesample"
        minSdk = minSdkProjectVersion
        targetSdk = targetSdkProjectVersion
        versionCode = 1
        versionName = "1.0"

        // specify for which architectures we want to generate native library files.
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "x86", "x86_64", "arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            proguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    externalNativeBuild {
        cmake {
            // define the path where the CMakeList has been put for this module.
            path("src/main/cpp/CMakeLists.txt")
            // specify the CMake version we want to use.
            version = "3.22.1"
        }
    }
    ndkVersion = "29.0.14033849"
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.21")

    // Android support
    implementation("com.android.support:appcompat-v7:28.0.0")
    implementation("com.android.support.constraint:constraint-layout:1.1.3")
}

// ====================================================================
// Tarefas Conan para cada ABI
// ====================================================================

val abiList = listOf(
    "arm64-v8a" to "armv8",
    "armeabi-v7a" to "armv7",
    "x86" to "x86",
    "x86_64" to "x86_64"
)

// Criar uma tarefa de Conan para cada ABI
abiList.forEach { (abi, conanArch) ->
    val conanBuildDir = project.layout.buildDirectory.dir(".cxx/conan/debug/${abi}").get().asFile

    val taskName = "conanInstall${abi.replace("-", "").capitalize()}"

    tasks.register<Exec>(taskName) {
        description = "Installs Conan dependencies for $abi"
        group = "conan"

        doFirst {
            conanBuildDir.mkdirs()
        }

        workingDir = conanBuildDir

        commandLine(
            "conan", "install",
            rootProject.file("conanfile.py").absolutePath,
            "-pr:h", rootProject.file("conan/android_profile").absolutePath,
            "-pr:b=default",
            "-s:h", "build_type=Debug",
            "-s:h", "arch=${conanArch}",
            "--output-folder", conanBuildDir.absolutePath
        )

        outputs.file(conanBuildDir.resolve("conan_toolchain.cmake"))
        outputs.upToDateWhen { conanBuildDir.resolve("conan_toolchain.cmake").exists() }
    }
}

// ====================================================================
// Configurar dependências das tarefas de build do CMake
// ====================================================================

// Garantir que as tarefas do Conan rodem antes do CMake configurar
tasks.configureEach {
    if (name.contains("configureCMake", ignoreCase = true)) {
        // Extrair o ABI do nome da tarefa, ex: configureCMakeDebug[arm64-v8a]
        val abiMatch = Regex("""\[(.*?)\]""").find(name)
        if (abiMatch != null) {
            val abi = abiMatch.groupValues[1]
            val taskName = "conanInstall${abi.replace("-", "").capitalize()}"
            dependsOn(taskName)
        }
    }
}

// Também adicionar dependência no preBuild como fallback
tasks.named("preBuild") {
    abiList.forEach { (abi, _) ->
        val taskName = "conanInstall${abi.replace("-", "").capitalize()}"
        dependsOn(taskName)
    }
}
