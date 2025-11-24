from conan import ConanFile
from conan.tools.cmake import CMakeToolchain, cmake_layout


class AndroidProjectConan(ConanFile):
    name = "android-cmake-sample"
    version = "1.0"

    # Contextos suportados pelo Conan 2
    settings = "os", "arch", "compiler", "build_type"

    # Forma recomendada: declarar o NDK como tool_requires
    tool_requires = "android-ndk/r29"

    # Se preferir a forma via método, seria:
    # def build_requirements(self):
    #     self.tool_requires("android-ndk/r29")

    def layout(self):
        # Layout padrão pra integração com CMake
        cmake_layout(self)

    def generate(self):
        # Gera o toolchain file para o CMake
        tc = CMakeToolchain(self)
        tc.generate()

    def build(self):
        # O Gradle/CMake vão fazer o build; aqui não precisa nada
        pass

