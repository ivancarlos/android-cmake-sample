from conan import ConanFile
from conan.tools.cmake import CMakeToolchain, cmake_layout

class AndroidProjectConan(ConanFile):
    name = "android-cmake-sample"
    version = "1.0"
    
    # 1. Configurações: Necessário para definir a plataforma alvo (Android)
    settings = "os", "compiler", "build_type", "arch"
    
    # Adicione suas dependências C/C++ de terceiros aqui (se houver)
    # Exemplo: 
    # def requirements(self):
    #     self.requires("fmt/10.1.1")
    
    def layout(self):
        # 2. Layout: Define estrutura de pastas
        cmake_layout(self)
    
    def generate(self):
        # 3. Geração do Toolchain: Cria os arquivos de configuração para o CMake
        tc = CMakeToolchain(self)
        
        # Configurações específicas do Android são automaticamente
        # inferidas do perfil (android_profile)
        
        # Opcional: Adicionar variáveis personalizadas do CMake
        # tc.variables["MY_CUSTOM_FLAG"] = "ON"
        
        tc.generate()
    
    def build(self):
        # O Gradle fará o build, então este método fica vazio
        pass
