# Hokm für Android

Native Android-Portierung von **Hokm** (persisches Stichspiel) – Kotlin +
Jetpack Compose. Schwester-Projekt der iOS/macOS-App (`hokm-app`).

Basiert auf dem Portierungsleitfaden aus dem iOS-Repo
(`docs/android-port.md`): die UI-freie Spiel-Engine wird nach Kotlin portiert
(mit Tests als Spezifikation), die Oberfläche in Compose neu gebaut.

## Stand: v0.3 – Echtes Menü

- **Navigations-Zentrale**: Die App nutzt nun ein modernes Grundgerüst
  (`Scaffold`) mit einer schwebenden, transluzenten **Bottom Navigation Bar**
  („Glassmorphism“). Direkter Zugriff auf Startseite, Spielregeln, Statistik
  und Einstellungen.
- **Home-Screen Redesign**: Vollständige visuelle Anpassung an das iOS-Vorbild:
  - **Authentisches Branding**: Großes goldenes Kalligraphie-Logo („حُکم“) und
    markante Typografie.
  - **Action Cards**: Hochwertige, abgerundete Karten für Hauptaktionen. „Spiel
    fortsetzen“ in Signalgrün, „Neues Solo-Spiel“ in dezenter Glas-Optik.
  - **Gesten-Steuerung**: Die „Neues Spiel“-Karte lässt sich nun sanft mit dem
    Finger nach links wischen (HorizontalPager), um die Spieleranzahl (2/4)
    direkt in der Karte zu wählen – für ein taktileres Erlebnis.
- **Globale Einstellungen**: Ein neuer Einstellungs-Tab erlaubt die Wahl der
  Bot-Stärke, die nun global über alle Spielmodi hinweg in den
  `SharedPreferences` persistiert wird.
- **UI-Feinschliff**:
  - Vollständige **Edge-to-Edge** Unterstützung mit korrekten Insets für
    Statusleiste, Notch und Gesten-Navigation auf allen Screens.
  - Symmetrische Abstände und korrigierte Zentrierungen im Tutorial und in
    den Einstellungs-Kacheln.

## Bauen

Erfordert **JDK 21** (wird via Gradle Toolchains automatisch aufgelöst) und
das Android SDK (Platform 34).

```bash
./gradlew :engine:test        # Engine-Tests (schnell, ohne Android-SDK)
./gradlew :app:assembleDebug  # Debug-APK bauen
```

Das Projekt nutzt den **Gradle Wrapper** und moderne Build-Standards:
- **Gradle 9.6.1**
- **Android Gradle Plugin 9.3.1**
- **Kotlin 2.2.10** (mit modernem `compilerOptions` DSL)
- **Java 21** (Daemon & Toolchain)


## Roadmap (Phasen aus dem Leitfaden)

1. ✅ Engine → Kotlin (+ Tests)
2. Compose-UI + vollständiger Solo-Modus
   - ✅ Kartendesign & Tischlayout, Statistik, Spielstandspeicher
   - ✅ Spielregeln & Tutorial
   - ✅ Modernes Menü & Navigations-Zentrale (v0.3)
   - 🔜 Wortmarke/App-Icon & Startscreen, Lokalisierung (en/fa + RTL),
     Sounds & Haptik
3. Play Billing (Design-Paket), Play Games (Erfolge/Bestenliste)
4. Online-Multiplayer über eigenes WebSocket-Backend

## Nicht portierbar

FaceTime **SharePlay** hat auf Android kein Pendant und entfällt.
Googles Echtzeit-Multiplayer-APIs sind eingestellt – Online-Spiel braucht ein
eigenes Backend (Phase 4).

## applicationId

`eu.amiri.hokm`
