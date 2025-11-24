Para tornar o Gradle verboso e mostrar todos os detalhes da compilação, adicione estas opções:Agora execute o Gradle com flags de verbosidade:

```bash
# Máxima verbosidade
./gradlew clean assembleDebug --info --stacktrace

# Ou ainda mais verboso (debug completo)
./gradlew clean assembleDebug --debug --stacktrace

# Ou para ver apenas a saída do CMake/compilação nativa
./gradlew clean assembleDebug --info 2>&1 | grep -A 50 "C/C++"

# Para salvar toda a saída em um arquivo
./gradlew clean assembleDebug --info --stacktrace > build.log 2>&1
```

**Opções do Gradle:**

- `--info` - Mostra informações detalhadas do build
- `--debug` - Mostra informações de debug (muito verboso!)
- `--stacktrace` - Mostra stack trace completo em caso de erro
- `--scan` - Cria um build scan online (requer conta Gradle)

**Para ver especificamente os comandos do compilador:**

```bash
# Ver comandos completos do compilador
./gradlew clean assembleDebug --info 2>&1 | grep -E "(clang|gcc|\-\-sysroot)"

# Ver apenas a fase de configuração do CMake
./gradlew clean assembleDebug --info 2>&1 | grep -A 100 "configureCMake"

# Ver arquivos de compilação gerados
cat app/build/.cxx/Debug/*/compile_commands.json
```

**Também é útil adicionar ao `gradle.properties`:**

```bash
cat >> gradle.properties << 'EOF'
# Tornar builds nativos verbosos
android.debug.obsoleteApi=true
android.native.buildOutput=verbose
EOF
```

Depois execute:

```bash
./gradlew clean assembleDebug --info --stacktrace
```

Isso vai mostrar:
- ✅ Comandos completos do CMake
- ✅ Flags do compilador (clang)
- ✅ Paths de includes
- ✅ Links de bibliotecas
- ✅ Todas as variáveis do CMake
