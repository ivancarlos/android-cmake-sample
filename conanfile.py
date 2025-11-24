from conan import ConanFile
from conan.tools.cmake import CMakeToolchain, cmake_layout


class AndroidProjectConan(ConanFile):
    name = "android-cmake-sample"
    version = "1.0"

    settings = "os", "arch", "compiler", "build_type"

    def build_requirements(self):
        # Baixa e usa o NDK do Conan em vez do NDK local
        # Verifique versões disponíveis com: conan search "android-ndk" -r=conancenter
        # self.tool_requires("android-ndk/r29")

    def layout(self):
        cmake_layout(self)

    def generate(self):
        # Gera o conan_toolchain.cmake que o CMake usará
        tc = CMakeToolchain(self)
        tc.generate()

    def build(self):
        # O Gradle/CMake fazem o build, não precisamos implementar aqui
        pass
