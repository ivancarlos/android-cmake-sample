from conan import ConanFile
from conan.tools.cmake import CMakeToolchain, cmake_layout


class AndroidProjectConan(ConanFile):
    name = "android-cmake-sample"
    version = "1.0"

    # Básico para controlar o toolchain
    settings = "os", "arch", "compiler", "build_type"

    # NDK como ferramenta de build (Conan 2 style)
    tool_requires = "android-ndk/r29"

    def layout(self):
        cmake_layout(self)

    def generate(self):
        tc = CMakeToolchain(self)
        tc.generate()

    # build() pode ficar ausente ou vazio se quem compila é o Gradle/CMake
    # def build(self):
    #     pass
