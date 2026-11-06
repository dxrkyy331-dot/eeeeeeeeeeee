# Lite

Modularny mod kliencki (Fabric 1.21.4) z animowanym ClickGUI.

## Sterowanie
- **Right Shift** – otwiera/zamyka GUI (zmiana w Opcje > Sterowanie)
- **LPM** na module – włącz/wyłącz
- **PPM** na module – rozwiń ustawienia
- **Środkowy przycisk** na module – ustaw hotkey (Esc/Backspace = usuń)
- **PPM** na nagłówku panelu – zwiń panel, **LPM + przeciąganie** – przesuń

## Moduły
HUD, Fullbright, Zoom, SprintToggle, Interface (motyw kolorystyczny)

## Build
```
gradle wrapper      # jednorazowo, generuje gradlew
./gradlew build     # jar w build/libs/
./gradlew runClient
```
Config: `config/lite.json`.

## Dodawanie modułu
Dziedzicz po `Module`, dodaj ustawienia do `settings`, zarejestruj w `ModuleManager.init()`.
