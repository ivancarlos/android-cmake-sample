# Define the minimum version of CMake required to use this CMakeList.
# Some functions don't exist in previous version of CMake by exampe.
cmake_minimum_required(VERSION 3.5)

# Define a name for this project. It will be accessible with ${PROJECT_NAME}.
project(calculator)

# ----------------------------------------
# Opções de build para debug
# ----------------------------------------
set(CMAKE_VERBOSE_MAKEFILE ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# ----------------------------------------
# SHARED LIBRARY - Compilar em vez de usar prebuilt
# ----------------------------------------
set(shared_library_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../shared_library)

# Adicionar include directory da shared library
include_directories(${shared_library_DIR}/include)

# Compilar a shared library a partir do código fonte
add_library(
    sharedLibrary
    SHARED
    ${shared_library_DIR}/src/Addition.cpp
)

# ----------------------------------------
# STATIC LIBRARY - Compilar em vez de usar prebuilt
# ----------------------------------------
set(static_library_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../static_library)

# Adicionar include directory da static library
include_directories(${static_library_DIR}/include)

# Compilar a static library a partir do código fonte
add_library(
    staticLibrary
    STATIC
    ${static_library_DIR}/src/Subtraction.cpp
)

# ----------------------------------------
# SUBDIRECTORY LIBRARY
# ----------------------------------------
set(subdirectory_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../subdirectory)

# Adicionar o CMakeLists.txt do subdirectório
add_subdirectory(
    ${subdirectory_DIR}
    ./subdirectory
)

# ----------------------------------------
# MAIN CALCULATOR LIBRARY
# ----------------------------------------
# Create a library with the name defined for the project.
# Set it as SHARED library (will generate a .so file)
# Set the source file
add_library(
    ${PROJECT_NAME}
    SHARED
    Calculator.cpp
)

# ----------------------------------------
# LINK ALL LIBRARIES
# ----------------------------------------
# Link todas as bibliotecas ao projeto principal
target_link_libraries(
    ${PROJECT_NAME}
    subdirectoryLibrary
    sharedLibrary
    staticLibrary
)