## 📚 Documentação: Modelo Híbrido de Build CMake (Android NDK)

[](https://www.google.com/search?q=)
[](https://www.google.com/search?q=)

# Android & CMake Sample

Colocar as mãos no código nativo (C/C++) com o **Android NDK** pode ser uma tarefa difícil para um desenvolvedor Android acostumado com Java/Kotlin e Gradle.

Este exemplo mostra vários casos de uso sobre como utilizar o CMake:

  - para construir uma biblioteca nativa sem o Android Studio.
  - para usar bibliotecas pré-construídas (compartilhadas `(.so)` ou estáticas `(.a)`).
  - para depender de outro módulo nativo a partir do seu módulo nativo.
  - para construir aplicações Android com código nativo.

-----

## Project

A aplicação permite realizar algumas operações em 2 números fornecidos na entrada. Os cálculos são realizados por bibliotecas nativas, escritas em C/C++, incorporadas na aplicação. Cada cálculo é realizado por uma das bibliotecas e cada biblioteca é importada de uma forma diferente usando o CMake.

O projeto contém 4 módulos:

  - app
  - subdirectory
  - shared\_library
  - static\_library

### Modules

O módulo `app` contém o código da aplicação Android e depende de outros módulos para realizar os cálculos.

Outros módulos contêm código C/C++ e cada um realiza um cálculo diferente:

  - `subdirectory` faz uma multiplicação em 2 números.
  - `shared_library` faz uma adição em 2 números.
  - `static_library` faz uma subtração em 2 números.

#### App module

Este módulo contém o código da aplicação Android. Contém código Kotlin para gerenciar uma aplicação Android básica, mas também código C/C++ para comunicar com as bibliotecas nativas das quais esta aplicação depende.

As dependências das bibliotecas nativas são definidas no `CMakeLists.txt` referenciado pelo `build.gradle` do módulo:

```kotlin
externalNativeBuild {
    cmake {
        path "src/main/cpp/CMakeLists.txt"
    }
}
```

O código C/C++ usa **JNI** para criar uma ponte entre o código Kotlin e o código C/C++ puro. Permite escrever funções que são chamadas pelo código Kotlin e, de dentro da função JNI, chamar qualquer função C/C++.

#### Subdirectory module

Este módulo é importado pelo módulo `app` com uma dependência do CMake. O `CMakeLists.txt` usado pelo módulo `app` tem como alvo o `CMakeLists.txt` deste módulo com o comando `add_subdirectory`.

A biblioteca é construída como uma biblioteca compartilhada. Um arquivo `.so` é gerado e contém todo o código.

O diretório `include` contém todos os *headers* **`public`** exigidos pelo módulo `app` para poder usá-lo.

A biblioteca expõe seus *headers* usando `target_include_directories` com visibilidade **`PUBLIC`**.

#### Shared\_library module

Este módulo permite gerar uma biblioteca compartilhada, um arquivo **`.so`**, executando o *script* `library_built.sh`.

Este módulo **não é construído** cada vez que o módulo `app` é construído. O módulo `app` apenas tem como alvo o arquivo `.so` pré-gerado e seus *headers* dentro do diretório `include`.

Para importar *headers* no módulo `app`, é usado o comando `include_directories`.

#### Static\_library module

Este módulo permite gerar uma biblioteca estática, um arquivo **`.a`**, executando o *script* `library_built.sh`.

Este módulo **não é construído** cada vez que o módulo `app` é construído. O módulo `app` apenas tem como alvo o arquivo `.a` pré-gerado e seus *headers* dentro do diretório `include`.

Para importar *headers* no módulo `app`, é usado o comando `include_directories`.

-----

## 5\. Análise Detalhada do `app/src/main/cpp/CMakeLists.txt`

Este arquivo orquestra o *build* da biblioteca principal (`libcalculator.so`) e gerencia todas as suas dependências.

### 5.1. Definição do Alvo Principal (`calculator`)

Esta seção define o artefato de *build* que será gerado pelo módulo `app`.

| Comando | Descrição |
| :--- | :--- |
| `cmake_minimum_required(VERSION 3.4.1)` | Define a versão mínima do CMake. |
| `project(calculator)` | Define o nome do projeto (o *target* principal). |
| `add_library(${PROJECT_NAME} SHARED Calculator.cpp)` | Cria a biblioteca compartilhada **`libcalculator.so`** a partir de `Calculator.cpp`. |

### 5.2. Inclusão de Subdiretório de Código (`subdirectoryLibrary`)

O projeto integra código-fonte C++ de um diretório externo.

| Comando | Descrição |
| :--- | :--- |
| `set(subdirectory_DIR ...)` | Define o caminho relativo para a pasta `subdirectory`. |
| `add_subdirectory(...)` | Inclui e executa o `CMakeLists.txt` do `subdirectory`. Este *script* deve criar o alvo de biblioteca **`subdirectoryLibrary`**. |
| `target_link_libraries(${PROJECT_NAME} subdirectoryLibrary)` | Liga (`linka`) a biblioteca `calculator` ao código-fonte da biblioteca gerada pelo subdiretório. |

### 5.3. Dependência de Biblioteca Compartilhada Pré-Construída (`sharedLibrary` - `.so`)

Esta seção importa uma biblioteca já compilada, localizada estaticamente, sem recompilar seu código-fonte.

| Comando | Descrição |
| :--- | :--- |
| `include_directories(...)` | Adiciona o caminho dos **headers** (`.h`) para o compilador. |
| `set(SHARED_LIBRARY_SO ...)` | Define a localização exata do binário `.so`, usando `${CMAKE_ANDROID_ARCH_ABI}` (ex: `arm64-v8a`) para garantir a arquitetura correta. |
| `add_library(sharedLibrary SHARED IMPORTED)` | Cria um alvo virtual chamado `sharedLibrary`. O `IMPORTED` diz ao CMake: "Esta biblioteca já está construída." |
| `set_target_properties(sharedLibrary PROPERTIES IMPORTED_LOCATION ${SHARED_LIBRARY_SO})` | Define a localização no disco para o *linker*. **Crucial** para que o Ninja/Make saiba onde encontrar o `.so` durante a ligação. |
| `target_link_libraries(${PROJECT_NAME} sharedLibrary)` | Liga (`linka`) a `libcalculator.so` ao `.so` pré-construído. |

### 5.4. Dependência de Biblioteca Estática Pré-Construída (`staticLibrary` - `.a`)

Semelhante à biblioteca compartilhada, esta seção importa uma biblioteca estática binária.

| Comando | Descrição |
| :--- | :--- |
| `set(STATIC_LIBRARY_A ...)` | Define o caminho para o binário estático **`.a`**. |
| `add_library(staticLibrary STATIC IMPORTED)` | Cria um alvo virtual. O `STATIC` informa ao CMake que esta é uma biblioteca estática (seu código será incorporado à `libcalculator.so`). |
| `set_target_properties(staticLibrary PROPERTIES IMPORTED_LOCATION ${STATIC_LIBRARY_A})` | Define a localização no disco do arquivo `.a`. |
| `target_link_libraries(${PROJECT_NAME} staticLibrary)` | Liga (`linka`) a `libcalculator.so` à biblioteca estática. O conteúdo do `.a` é incorporado na `libcalculator.so`. |

-----

## Resources:

  - [Android NDK guides](https://developer.android.com/ndk/guides)
  - [Android NDK roadmap](https://android.googlesource.com/platform/ndk/+/master/docs/Roadmap.md)
  - [Configure CMake from developer android web site](https://developer.android.com/studio/projects/configure-cmake)
  - [Configure CMake from android ndk web site](https://developer.android.com/ndk/guides/cmake)
  - [CMake changelog](https://cmake.org/cmake/help/latest/release/index.html)
  - [Codelab to create a Hello-CMake](https://codelabs.developers.google.com/codelabs/android-studio-cmake/index.html#0)
  - [Android NDK samples](https://github.com/googlesamples/android-ndk/tree/master)
