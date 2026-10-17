# Publikacja krok po kroku

Wszystkie pliki są w tym katalogu (`publikacja/`), a gotowy mod w `build/publikacja/` po uruchomieniu:

```bash
gradlew paczka
```

| Plik | Do czego |
|---|---|
| `build/publikacja/zwiadowcy-forge-1.20.1-1.0.1.jar` | plik moda do wgrania na Modrinth i CurseForge |
| `build/publikacja/Zwiadowcy-Ruiny-Gorlanu-1.0.1-forge-1.20.1.zip` | paczka dla graczy (jar, instrukcja, licencje) do GitHub Releases |
| `ikona.png` (512×512) | ikona projektu |
| `baner.png` (1280×400) | obraz w galerii / baner strony |
| `opis.md` | pełny opis strony projektu (Markdown, PL + EN) |
| `podsumowanie.txt` | krótki opis (pole „Summary”) |
| `zmiany-1.0.1.md` | lista zmian wersji (changelog) |

## Zanim opublikujesz

- [ ] Zagraj chwilę w zwykłym kliencie (launcher + Forge 47.x, jar w folderze `mods`): kronika, prolog, dialog,
      dziennik (J), Bezruch (V).
- [ ] Zrób 4-6 zrzutów ekranu (F2): dziedziniec Redmont, wielka sala, chata Halta, okno dialogu, dziennik,
      Ruiny Gorlanu. Zrzuty trafiają do `.minecraft/screenshots`.
- [ ] Wypchnij repozytorium na GitHuba (opis używa linku do `publikacja/baner.png` na gałęzi `main`).

## Modrinth (modrinth.com)

1. Zaloguj się i kliknij **Create a project**.
   - Name: `Zwiadowcy: Ruiny Gorlanu`
   - URL: `zwiadowcy-ruiny-gorlanu`
   - Visibility: Public
   - Summary: tekst z `podsumowanie.txt`
2. W ustawieniach projektu:
   - **Icon:** `ikona.png`
   - **Description:** cała zawartość `opis.md`
   - **Tags:** Adventure (główna), Mobs, Worldgen, Equipment
   - **Environment:** Client: *Required*, Server: *Required*
   - **License:** w polu SPDX wpisz `MIT AND CC-BY-NC-4.0`, a jako adres licencji podaj
     `https://github.com/mbialkowski/minecraft-mod-zwiadowcy/blob/main/LICENSE-ZASOBY.txt`
   - **Links:** Source `https://github.com/mbialkowski/minecraft-mod-zwiadowcy`,
     Issues `https://github.com/mbialkowski/minecraft-mod-zwiadowcy/issues`
   - **Gallery:** `baner.png` (zaznacz „Featured”) i zrzuty ekranu
3. **Versions → Create a version:**
   - plik: `zwiadowcy-forge-1.20.1-1.0.1.jar`
   - Version number: `1.0.1`, Name: `1.0.1 (Forge 1.20.1)`, Channel: Release
   - Loaders: Forge, Game versions: 1.20.1, Dependencies: brak
   - Changelog: `zmiany-1.0.1.md`
4. **Submit for review.** Weryfikacja trwa zwykle od jednego do kilku dni.

## CurseForge (authors.curseforge.com)

1. **Create Project → Minecraft → Mods.**
   - Name: `Zwiadowcy: Ruiny Gorlanu`
   - Summary: `podsumowanie.txt`
   - Description: przełącz edytor na Markdown i wklej `opis.md`
   - Main category: **Adventure and RPG**; dodatkowe: Mobs, Structures, Dimensions, Biomes
   - Avatar: `ikona.png`
   - License: **Custom License** z treścią pliku `LICENSE-ZASOBY.txt`
   - Source / Issues: adresy GitHuba jak wyżej
   - Program nagród dla twórców (Rewards): wyłącz, bo mod jest niekomercyjny
2. **Files → Upload File:**
   - plik: `zwiadowcy-forge-1.20.1-1.0.1.jar`
   - Display name: `Zwiadowcy: Ruiny Gorlanu 1.0.1 (Forge 1.20.1)`
   - Release type: Release
   - Game versions: Minecraft 1.20.1, Forge, Java 17; Environment: Client i Server
   - Changelog: `zmiany-1.0.1.md` (Markdown)
3. Projekt przechodzi moderację, zwykle od kilku godzin do kilku dni.

## GitHub Releases

1. Na stronie repozytorium: **Releases → Draft a new release**.
2. Tag `v1.0.1` (utwórz z gałęzi `main`), tytuł `Zwiadowcy: Ruiny Gorlanu 1.0.1`.
3. Opis: zawartość `zmiany-1.0.1.md`.
4. Załączniki: jar i zip z `build/publikacja/`. Kliknij **Publish release**.

## Kolejne wersje

1. Zmień `mod_version` w `gradle.properties` (np. `1.0.1`).
2. Dopisz plik `zmiany-<wersja>.md`.
3. Uruchom `gradlew paczka` i wgraj nowy jar jako nową wersję na każdej platformie.
