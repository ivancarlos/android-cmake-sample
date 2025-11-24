NAME            := Emu48-v3.0
# ----------  CONFIGURAÇÕES ----------
MY_GRADLE_LOCAL ?= /opt/gradle/gradle-8.10.2/bin/gradle
SDK             ?= $(HOME)/Android/Sdk
VERSION         ?= 36.1.0                       # build-tools a usar
GRADLE          := ./gradlew
AAPT            := $(SDK)/build-tools/$(VERSION)/aapt

apk_debug       := app/build/outputs/apk/debug/$(NAME)-debug.apk
apk_release     := app/build/outputs/apk/release/$(NAME)-release-unsigned.apk
APK             := $(apk_debug)

# Avalia apenas quando o APK existe (evita erro em `make help`)
ACTIVITYNAME    := $(shell [ -f "$(APK)" ] && $(AAPT) dump badging "$(APK)" 2>/dev/null | sed -nE "s/launchable-activity: name='([^']+).*/\1/p")
PACKAGE         := $(shell [ -f "$(APK)" ] && $(AAPT) dump badging "$(APK)" 2>/dev/null | sed -nE "s/package: name='([^']+).*/\1/p")

# ----------  HELP ----------
help: ## Mostra esta ajuda
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

# ----------  BUILD ----------
init: ## Cria/ atualiza o wrapper Gradle
	$(MY_GRADLE_LOCAL) wrapper

assembleDebug: ## Build APK debug
	$(GRADLE) assembleDebug
debug: assembleDebug        # atalho

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
	adb install -r $(apk_debug)

uninstall: ## Remove o pacote do device
	adb uninstall $(PACKAGE)

run: ## (Re)inicia a Activity principal
	adb shell am start -n $(PACKAGE)/$(ACTIVITYNAME)

back: ## Simula pressionar “Voltar”
	adb shell input keyevent KEYCODE_BACK

home: ## Simula pressionar “Home”
	adb shell input keyevent KEYCODE_HOME

# ----------  EMULADOR ----------
emu: ## Liga o emulador Pixel_XL_API_30 (Flutter)
	flutter emulators --launch Pixel_XL_API_30
# Caso use AVD puro, troque por:
# emu:
#	$(SDK)/emulator/emulator -avd Pixel_XL_API_30 &

# eof
