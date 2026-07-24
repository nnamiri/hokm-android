# Hokm für Android

Native Android-Portierung von **Hokm** (persisches Stichspiel) – Kotlin +
Jetpack Compose. Schwester-Projekt der iOS/macOS-App (`hokm-app`).

Basiert auf dem Portierungsleitfaden aus dem iOS-Repo
(`docs/android-port.md`): die UI-freie Spiel-Engine wird nach Kotlin portiert
(mit Tests als Spezifikation), die Oberfläche in Compose neu gebaut.

## Stand: v0.2 – Echter Spieltisch

- **`:engine`** – reines Kotlin/JVM-Modul mit der kompletten Spiellogik
  (Karten, Sitze, Trick, Regeln, `HokmGame`-Zustandsmaschine, `HokmBot`,
  `GameSnapshot`, deterministischer `SeededGenerator`, Ass-Zieh). Faithful
  port der iOS-`HokmKit`; Tests laufen ohne Android-SDK auf jeder JVM.
  Der komplette Spielzustand ist über `GameState` serialisierbar (Pendant zu
  Swifts `Codable`) – die Basis für den Spielstandspeicher.
- **`:app`** – Android-App (Compose) mit vollständigem **Solo-Modus**
  (Mensch auf Süd gegen Bots), 2 und 4 Spieler, drei Bot-Stärken:
  - **Echtes Kartendesign**, komplett in Compose gezeichnet: klassisches
    Pip-Layout für 2–10, Bildkarten in Serif, großes Ass-Symbol, Eckzeichen
    oben/unten, Kartenrücken mit Verlauf – 1:1-Port der iOS-`CardView`.
  - **Tischlayout** wie auf iOS: Filz-Verlauf, gefächerte Handkarten (die
    spielbare Karte hebt sich an, illegale werden gedimmt), Stichbereich mit
    Zuordnung zum Sitz, Spielerplaketten mit Hakem-Krone, Zug-Hervorhebung
    und Kartenrücken-Fächer, kompakte Score-Leiste sowie Overlays für
    Trumpfansage, Abwurf (2 Spieler), Rundenende, Spielende und Pause.
  - **Ziehphase (2 Spieler)** wie auf iOS: gestapelter Nachziehstapel mit
    Restanzahl, offen aufgedeckte Karte, und nach jedem eigenen Zug die
    Anzeige, welche Karte in die Hand ging und welche dafür weggeworfen
    wurde – inklusive der iOS-Ziehzeiten (2,5 s, damit man es lesen kann).
  - **Spielregeln**: das vollständige Regelwerk in sieben Abschnitten
    (Spiel, Hakem, Hokm zu zweit, Stichspiel, Wertung, Hoch/Niedrig, App).
  - **Tutorial**: fünf illustrierte Schritte zum Durchwischen, beim ersten
    Start automatisch, danach jederzeit über das Menü.
  - **Statistik**: Spiele, Siege, Niederlagen, Siegquote, aktuelle und beste
    Serie, gewonnene Runden, Kut-Runden – in `SharedPreferences` persistiert.
  - **Spielstandspeicher**: Das laufende Solo-Spiel wird automatisch
    gesichert und lässt sich auch nach einem App-Neustart fortsetzen.

  Die Oberfläche ist derzeit **auf Deutsch**; Wortmarke, Lokalisierung
  (en/fa + RTL), Sounds/Haptik und Startscreen folgen.

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
2. Compose-UI + vollständiger Solo-Modus
   - ✅ Kartendesign & Tischlayout, Statistik, Spielstandspeicher
   - ✅ Spielregeln & Tutorial
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
