import org.gradle.api.tasks.Exec

plugins {
    id("com.android.application")
    kotlin("android")
}

val compileSdkProjectVersion =
    project.findProperty("compileSdkProjectVersion")!!.toString().toInt()
val minSdkProjectVersion =
    project.findProperty("minSdkProjectVersion")!!.toString().toInt()
val targetSdkProjectVersion =
    project.findProperty("targetSdkProjectVersion")!!.toString().toInt()

android {
    // Novo com AGP 8+
    namespace = "fr.bowserf.cmakesample"

    compileSdk = compileSdkProjectVersion

    defaultConfig {
        applicationId = "fr.bowserf.cmakesample"
        minSdk = minSdkProjectVersion
        targetSdk = targetSdkProjectVersion
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "x86", "x86_64", "arm64-v8a"))
        }

        externalNativeBuild {
            cmake {
                arguments(
                    "-DCMAKE_VERBOSE_MAKEFILE=ON",
                    "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"
                )
                cppFlags("-v")
                cFlags("-v")
            }
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
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.20")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
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

abiList.forEach { (abi, conanArch) ->
    val conanBuildDir = project.layout.buildDirectory.dir(".cxx/conan/debug/${abi}").get().asFile

    val taskName = "conanInstall${abi.replace("-", "").replaceFirstChar { it.uppercase() }}"

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
            "-pr:h=android_profile",
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

tasks.configureEach {
    if (name.contains("configureCMake", ignoreCase = true)) {
        val abiMatch = Regex("""\[(.*?)\]""").find(name)
        if (abiMatch != null) {
            val abi = abiMatch.groupValues[1]
            val taskName =
                "conanInstall${abi.replace("-", "").replaceFirstChar { it.uppercase() }}"
            dependsOn(taskName)
        }
    }
}

tasks.named("preBuild") {
    abiList.forEach { (abi, _) ->
        val taskName =
            "conanInstall${abi.replace("-", "").replaceFirstChar { it.uppercase() }}"
        dependsOn(taskName)
    }
}
