NAME            := app
# ----------  CONFIGURAÇÕES ----------
MY_GRADLE_LOCAL ?= /opt/gradle/gradle-8.10.2/bin/gradle
SDK             ?= $(HOME)/Android/Sdk
VERSION         ?= 36.1.0
# build-tools a usar
GRADLE          := ./gradlew --warning-mode all
AAPT            := $(SDK)/build-tools/$(VERSION)/aapt

apk_debug       := app/build/outputs/apk/debug/$(NAME)-debug.apk
apk_release     := app/build/outputs/apk/release/$(NAME)-release-unsigned.apk
# Variável que aponta para o APK de debug
APK             := $(apk_debug)

# Verifica se o APK existe antes de rodar o AAPT.
# CAPTURA DA SAÍDA: Armazena o nome da Atividade (launchable-activity)
ACTIVITYNAME    := $(shell [ -f "$(APK)" ] && $(AAPT) dump badging "$(APK)" 2>/dev/null | sed -nE "s/launchable-activity: name='([^']+).*/\1/p")

# CAPTURA DA SAÍDA: Armazena o Nome do Pacote (package: name)
PACKAGE         := $(shell [ -f "$(APK)" ] && $(AAPT) dump badging "$(APK)" 2>/dev/null | sed -nE "s/package: name='([^']+).*/\1/p")


# ----------  HELP ----------
help: ## Mostra esta ajuda
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

## info: Mostra informações de build e do APK atual (debug).
info:
	@echo "========================================"
	@echo "          🤖 APP BUILD INFO 🤖"
	@echo "========================================"
	@echo "Nome do Aplicativo (NAME):    $(NAME)"
	@echo "Caminho do SDK (SDK):         $(SDK)"
	@echo "Build-Tools (VERSION):        $(VERSION)"
	@echo "Caminho do AAPT (AAPT):       $(AAPT)"
	@echo "Comando Gradle:               $(GRADLE)"
	@echo "----------------------------------------"
	@echo "APK Selecionado (APK):        $(APK)"
	@if [ -f "$(APK)" ]; then \
		echo "  -> Status:                  Arquivo Encontrado"; \
		echo "  -> Package Name (PACKAGE):  $(PACKAGE)"; \
		echo "  -> Activity Principal:      $(ACTIVITYNAME)"; \
	else \
		echo "  -> Status:                  APK NÃO EXISTE. Rode 'make debug' primeiro."; \
	fi
	@echo "========================================"

# ----------  BUILD ----------
init: ## Cria/ atualiza o wrapper Gradle
	$(MY_GRADLE_LOCAL) wrapper

build_library_debug:
	(cd shared_library; ./library_debug_build.sh)
	(cd static_library; ./library_debug_build.sh)

assembleDebug: ## Build APK debug
	$(GRADLE) assembleDebug
debug: assembleDebug   # atalho

assembleRelease: ## Build APK release (não assinado)
	$(GRADLE) assembleRelease
release: assembleRelease    # atalho

bundleDebugAar: ## Gera .aar debug
	$(GRADLE) bundleDebugAar
bundleReleaseAar: ## Gera .aar release
	$(GRADLE) bundleReleaseAar

clean: ## Limpa build/
	$(GRADLE) clean

# ----------  DEVICE ----------
install: assembleDebug ## Instala APK debug no device/emulador conectado
	adb logcat -c
	adb install -r $(apk_debug)

uninstall: ## Remove o pacote do device
	adb uninstall $(PACKAGE)

run: ## (Re)inicia a Activity principal
	adb shell am start -n $(PACKAGE)/$(ACTIVITYNAME)

back: ## Simula pressionar “Voltar”
	adb shell input keyevent KEYCODE_BACK

home: ## Simula pressionar “Home”
	adb shell input keyevent KEYCODE_HOME

start: ## (Re)inicia a Activity principal
	adb shell am start -n $(PACKAGE)/$(MAINACTIVITY)
stop: ## Stop app
	adb shell am force-stop $(PACKAGE)
log:## log do android
	adb logcat | grep $(PACKAGE)
# ----------  EMULADOR ----------
emu: ## Liga o emulador Pixel_XL_API_30 (Flutter)
	flutter emulators --launch Pixel_XL_API_30
# Caso use AVD puro, troque por:
# emu:
#	$(SDK)/emulator/emulator -avd Pixel_XL_API_30 &

# eof
