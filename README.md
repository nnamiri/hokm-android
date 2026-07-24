# Hokm für Android

Native Android-Portierung von **Hokm** (persisches Stichspiel) – Kotlin +
Jetpack Compose. Schwester-Projekt der iOS/macOS-App (`hokm-app`).

Basiert auf dem Portierungsleitfaden aus dem iOS-Repo
(`docs/android-port.md`): die UI-freie Spiel-Engine wird nach Kotlin portiert
(mit Tests als Spezifikation), die Oberfläche in Compose neu gebaut.

## Stand: v0.1 – Fundament

- **`:engine`** – reines Kotlin/JVM-Modul mit der kompletten Spiellogik
  (Karten, Sitze, Trick, Regeln, `HokmGame`-Zustandsmaschine, `HokmBot`,
  `GameSnapshot`, deterministischer `SeededGenerator`, Ass-Zieh). Faithful
  port der iOS-`HokmKit`; Tests laufen ohne Android-SDK auf jeder JVM.
- **`:app`** – Android-App (Compose). Enthält einen lauffähigen **Solo-Modus**
  (Mensch auf Süd gegen Bots), 2- und 4-Spieler, drei Bot-Stärken.
  Die Oberfläche ist bewusst schlicht (Text-Karten, Buttons) – der visuelle
  Feinschliff (Wortmarke, Kartendesign, Liquid-Glass-Pendant, Startscreen,
  Lokalisierung de/en/fa, Sounds/Haptik, Statistik, Spielstand) folgt in den
  nächsten Meilensteinen.

## Bauen

Erfordert JDK 17 und das Android SDK (Platform 34).

```bash
gradle :engine:test        # Engine-Tests (schnell, ohne Android-SDK)
gradle :app:assembleDebug  # Debug-APK bauen
```

Beim ersten Öffnen in Android Studio wird der Gradle-Wrapper erzeugt
(`gradle wrapper`), danach genügt `./gradlew …`.

## Roadmap (Phasen aus dem Leitfaden)

1. ✅ Engine → Kotlin (+ Tests)
2. 🔜 Compose-UI-Feinschliff + vollständiger Solo-Modus (Lokalisierung, Sounds,
   Haptik, Statistik, Spielstand, App-Icon/Startscreen)
3. Play Billing (Design-Paket), Play Games (Erfolge/Bestenliste)
4. Online-Multiplayer über eigenes WebSocket-Backend

## Nicht portierbar

FaceTime **SharePlay** hat auf Android kein Pendant und entfällt.
Googles Echtzeit-Multiplayer-APIs sind eingestellt – Online-Spiel braucht ein
eigenes Backend (Phase 4).

## applicationId

`eu.amiri.hokm`
