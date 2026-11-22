# Enk Vorratsplaner

Eine schlichte, gemeinsame Einkaufsliste für Android (Kotlin, Jetpack Compose).

## Funktionen

- Artikel mit Menge hinzufügen, abhaken und löschen
- Alle abgehakten Artikel entfernen
- Einfaches Autovervollständigen aus früheren Eingaben
- Offline nutzbar (Room-Datenbank), Abgleich mit einem Server, sobald eine Verbindung besteht
- Liste teilen per sechsstelligem PIN, ohne Benutzerkonto
- Optional: Produkte per Sprache über einen Alexa Skill hinzufügen

## Technik

- Android, Kotlin, Jetpack Compose, Room
- Netzwerk ohne zusätzliche Bibliotheken (`HttpURLConnection`)
- Keine Google-Dienste, kein Tracking, keine Werbung
- Backend: Cloudflare Worker mit D1-Datenbank (nicht Teil dieses Repositories)

## Bauen

Das Projekt öffnen in Android Studio und die Variante `app` ausführen.

## Lizenz

GNU General Public License, Version 3 (siehe Datei `LICENSE`).
