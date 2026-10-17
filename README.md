# Zwiadowcy: Ruiny Gorlanu

Fabularny mod do Minecraft Java **1.20.1 (Forge 47.4.10)**, inspirowany pierwszym tomem „Zwiadowców” Johna Flanagana.
Grasz jako Will, podopieczny barona Aralda, który zostaje uczniem zwiadowcy Halta i staje do walki z Kalkarami Morgaratha.
Cała fabuła, dialogi i opisy są po polsku (plik `en_us.json` to kopia polskiego, więc gra zawsze wyświetla polski).

> Mod fanowski, niekomercyjny. Postacie i świat należą do autora serii. Dialogi są napisane od zera, bez cytatów z książki.

## Uruchomienie

Wymagany **JDK 17** (np. Eclipse Temurin 17). Ustaw `JAVA_HOME` na JDK 17 albo dopisz
`org.gradle.java.home=<ścieżka do JDK 17>` w swoim `~/.gradle/gradle.properties`.

```bash
gradlew runClient
```

```bash
gradlew build
```

Gotowy plik moda: `build/libs/zwiadowcy-1.0.0.jar`. Wrzuć go do folderu `mods` w instalacji Forge 1.20.1.

## Jak zacząć grę

1. Przy pierwszym wejściu do świata dostajesz **Kronikę Araluenu**, a ekran-kronika wyjaśnia, czym ona jest.
2. Użyj Kroniki (PPM), aby przenieść się na dziedziniec zamku Redmont w wymiarze **Araluen**. Tam też ustawia się punkt odrodzenia.
3. Po przybyciu zobaczysz **prolog** (5 stron): czym jest Araluen, kim jest Morgarath, kim jesteś ty (Will)
   i czym jest Dzień Wyboru. Potem **baron Arald sam do ciebie podejdzie** i zacznie rozmowę.
   Prolog można przeczytać ponownie w dzienniku (przycisk „Prolog”).
4. Ponowne użycie Kroniki w Araluenie zabiera cię z powrotem do zwykłego świata. Po każdym powrocie do gry
   czat przypomina, którą misję śledzisz.

W Araluenie nie pojawiają się zombie ani szkielety. Wrogami są stworzenia Morgaratha i bandyci, a w okolicy Redmont
(do ~400 bloków) panuje spokój.

## Świat Araluenu

**Biomy (9, z polskimi nazwami i własnymi kolorami):** Lasy Redmont, Łąki Araluenu, Brzozowy gaj, Mroczna puszcza,
Bory Północy, Stare bory, Pogórze Deszczu i Nocy, Szczyty Deszczu i Nocy (kamieniste, ponure) oraz Równina
Samotności (szara, wymarła). Równina otacza też Ruiny Gorlanu i spaloną osadę.

**Przeciwnicy:**

| Wróg | Opis |
|---|---|
| Wargal-zwiadowca | szybki i słaby, poluje w borach |
| Wargal (wojownik) | podstawowy żołnierz Morgaratha |
| Wargal-oszczepnik | rzuca oszczepami z dystansu, w zwarciu walczy wręcz |
| Herszt Wargalów | mini-boss z toporem i paskiem zdrowia; wycie wzmacnia całą watahę |
| Bandyta / Łucznik bandytów | losowe twarze, stroje i skórzana zbroja; czerwone chusty |
| Herszt bandytów | mini-boss w kolczudze, z tarczą |
| Niedźwiedź brunatny | atakuje, gdy podejdziesz bliżej niż 6 bloków, chyba że się skradasz |
| Kalkar | boss fabularny |

Nocne patrole Wargalów pojawiają się w ciemnych, odległych biomach: pogórze, szczyty, bory, puszcza, równina.

**Losowe struktury (tylko w Araluenie):** obóz Wargalów (czasem z hersztem), posterunek Morgaratha (wieża z ciemnego
kamienia), kryjówka bandytów (palisada, chaty, wieżyczka łucznika), ruina strażnicy, kurhan z kryptą (czasem
z rabusiami grobów), legowisko niedźwiedzia, kamienny krąg, przydrożny zajazd z karczmarzem (odpoczynek raz na dobę)
i zagroda chłopska ze zwierzętami. W skrzyniach są **Zapiski**: listy, rozkazy i pamiętniki, które po przeczytaniu
trafiają do dziennika.

**Rozbudowane lokacje fabularne:** kuźnia z kowalem (naprawa broni raz na dobę) i archiwum w Redmont, schody na mury,
kuźnia i wiatrak w Wensley, ul przy chacie Halta oraz krypta pod twierdzą Gorlanu.

### Sterowanie

| Klawisz | Działanie |
|---|---|
| PPM na postaci | rozmowa (okno dialogu, odpowiedzi także klawiszami 1-9) |
| **J** | Dziennik zwiadowcy (misje, cele, notatki, przycisk „Śledź”) |
| **V** | Bezruch (z płaszczem zwiadowcy): niewidzialność, dopóki się nie ruszysz, odnowienie 60 s |
| **R** | następny rodzaj strzały w kołczanie |
| Shift + PPM na Wyrwiju | rozmowa z koniem (przed oswojeniem wystarczy samo PPM) |

W lewym górnym rogu HUD pokazuje śledzoną misję, cel i strzałkę z odległością. Przy hotbarze widać poziom
ukrycia (gdy nosisz płaszcz) i wybraną strzałę (gdy trzymasz łuk zwiadowcy).

## Kampania

| # | Misja | Akt |
|---|---|---|
| 1 | Dzień Wyboru | I: Redmont |
| 2 | Nocna wspinaczka (skradanie się do gabinetu barona po bluszczu) | I |
| 3 | Chata w lesie (drewno, woda, kawa dla Halta) | I |
| 4 | Cięciwa i cierpliwość (strzelnica 20/40/60 kratek) | II: Szkolenie |
| 5 | Dwa noże (noże do rzucania, saksa i manekin) | II |
| 6 | Lekcja bezruchu (podejdź Halta niezauważony) | II |
| 7 | Koń od Starego Boba (oswojenie Wyrwija) | II |
| 8 | Polowanie na dzika | II |
| 9 | *Jarmark w Wensley* (misja poboczna, bójka z dręczycielami Horace'a) | poboczna |
| 10 | Zlot Zwiadowców (konkurs łuczniczy, wieści o Northolcie) | III: Cień Morgaratha |
| 11 | Tropem bestii (spalona osada, zasadzka Wargalów, Znak Morgaratha) | III |
| 12 | Równina Samotności (skradanie się w Ruinach Gorlanu) | III |
| 13 | Ogień w ciemności (boss: dwóch Kalkarów) | III |
| 14 | Przed obliczem króla (wybór: Szkoła Rycerska albo Korpus Zwiadowców) | Epilog |

Po epilogu świat zostaje otwarty, a Halt daje powtarzalne **zlecenia**: patrol Wargalów, dziki w zbożu, list do Starego Boba
(każde można powtórzyć raz na dobę gry).

### Lokacje

Struktury stoją w wymiarze Araluen. Redmont powstaje przy pierwszym wejściu. Pozostałe lokacje powstają automatycznie,
gdy zaczyna się etap misji, który ich potrzebuje: mod szuka wtedy płaskiego, suchego terenu i wyrównuje go.

Zamek Redmont (~0, 0) · Wensley (zachód) · Chata Halta ze strzelnicą (wschód) · Farma Starego Boba (południowy wschód) ·
Legowisko dzika (północny wschód) · Obóz Zlotu (daleko na północnym zachodzie) · Spalona osada (wschód) ·
Ruiny Gorlanu (daleko na południowym wschodzie).

## Zawartość

- **Przedmioty:** Łuk zwiadowcy (szybsze napinanie), Kołczan (4 przegródki), strzały: płonąca, z szerokim grotem,
  gwiżdżąca (odciąga uwagę strażników i wrogów), z liną (tworzy linę do wspinaczki), Nóż saksoński, Noże do rzucania,
  Płaszcz zwiadowcy (kamuflaż i Bezruch, własny model z kapturem), Zestaw tropiciela, Flasza oleju, Gwizdek,
  Racje podróżne, Kawa zwiadowcy (efekt Czujność), Kronika, Dziennik oraz przedmioty fabularne
  (Brązowy liść dębu, Znak Morgaratha, Fragment z Gorlanu, Strzała Zlotu, List królewski…).
  Wszystko ma przepisy rzemieślnicze; jaja przywołania mobów są tylko w trybie kreatywnym. Przedmioty mają własną
  zakładkę „Zwiadowcy: Ruiny Gorlanu” w trybie kreatywnym.
- **Moby:** Kalkar (boss z paskiem zdrowia, fazy, spojrzenie grozy, 35% obrażeń od zwykłej broni, 300% od ognia),
  Wargal (może pełnić wartę), Dzik (szarża; spawnuje się też w lasach), Wyrwij (koń przywiązany do gracza,
  nie ginie, parska na wrogów).
- **Łupy z wrogów:** Oszczep (broń miotana), Topór herszta, Sakiewka bandyty (PPM - zawartość), Zapiski (lore).
- **Postacie (22):** Halt, baron Arald, Sir Rodney, mistrz Nigel, mistrz Chubb, lady Pauline, Horace, Alyss, Jenny,
  Stary Bob, Gilan, Crowley, król Duncan, Alda, Bryn, Jerome, strażnicy, zwiadowcy, wieśniacy, karczmarz, kowal, chłopka.
- **Efekty:** Czujność, Groza (spowolnienie lub paraliż), Oblany olejem (podwójne obrażenia od ognia).

## Struktura projektu

```
src/main/java/pl/zwiadowcy/
  quest/       silnik misji, warunki, akcje, postęp gracza (capability)
  dialogue/    dialogi (wczytywane z JSON) i ich obsługa po stronie serwera
  world/       wymiar Araluen, lokacje, stawianie struktur
  entity/      NPC, Kalkar, Wargal, Dzik, Wyrwij, pociski
  stealth/     kamuflaż, Bezruch, tropienie
  item/, block/, registry/, network/, event/
  client/      GUI (dialog, dziennik, kołczan), HUD, modele, renderery
src/main/resources/data/zwiadowcy/
  quests/      17 misji (JSON)        dialogues/   20 plików dialogów (JSON)
  structures/  8 struktur (.nbt)      dimension/, dimension_type/, recipes/, loot_tables/, advancements/
tools/assetgen/java/   generatory tekstur (pixel-art) i struktur (.nbt) w czystej Javie
```

### Edycja fabuły bez kompilacji

Misje (`data/zwiadowcy/quests/*.json`) i dialogi (`data/zwiadowcy/dialogues/<postać>.json`) to zwykłe pliki JSON.
Po zmianie wystarczy w grze komenda `/reload`.

- Typy celów misji: `talk`, `reach`, `sneak_reach` (z obserwatorami i punktem powrotu), `collect`, `kill` (`tagged`:
  tylko moby z misji), `target_hits` (strzały lub noże z minimalnej odległości), `event`, `night`.
- Akcje: `start_quest`, `advance`, `give_item`, `take_item`, `set_flag`, `move_npc`, `npc_mode` (`idle`, `companion`,
  `sentry`, `duel`, `wounded`), `spawn`, `spawn_random`, `message`, `title`, `teleport`, `note`, `xp`, `sound`, `bond_tug`.
  Każda akcja może mieć warunki `when`.
- Warunki: `quest_active`, `quest_stage` + `stage`, `quest_completed`, `quest_not_started`, `quest_ready`, `flag`,
  `not_flag`, `has_item`, `night`, `in_araluen`.
- Odwołania do miejsc mają postać `lokacja:kotwica`, np. `chata_halta:linia_strzalu`. Kotwice to bloki struktury
  w trybie DATA wewnątrz plików `.nbt`.

### Tekstury i struktury

Wszystkie tekstury i struktury generuje kod, więc można je odtworzyć albo podmienić 1:1.

```bash
gradlew generateAssets
```

`generateAssets` uruchamia trzy generatory: tekstury, struktury (`structures/` i `structures/losowe/`) oraz biomy
Araluenu (budowane z biomów vanilla z pliku `client-extra.jar` w cache Gradle).

Losowe struktury mają definicje w `data/zwiadowcy/worldgen/` (structure, structure_set, template_pool), a biomy, w których
się pojawiają, są w tagach `tags/worldgen/biome/has_structure/`. Struktury można też otworzyć w grze Blokiem struktury,
poprawić ręcznie i nadpisać pliki `.nbt`.
Znaczniki (`npc:halt;yaw=0`, `anchor:gabinet`, `dummy`, `tug`) muszą zostać na swoich miejscach.

## Komendy testowe (op)

```
/zwiadowcy misja start|dalej|ukoncz <id>   /zwiadowcy misja reset
/zwiadowcy tp <lokacja[:kotwica]>           /zwiadowcy kotwice <lokacja>
/zwiadowcy podroz                            /zwiadowcy flaga <nazwa>
```

## Autotest

Autotest uruchamia serwer deweloperski, stawia wszystkie lokacje, sprawdza każde odwołanie w misjach i dialogach
(misje, węzły, przedmioty, moby, dźwięki, osiągnięcia, kotwice) oraz to, czy miejsca NPC nie są zasypane.
Odnajduje też każdą losową strukturę w świecie, liczy moby i NPC wokół niej i sprawdza biom przy Gorlanie oraz kryptę.
Raport trafia do `run/selftest.txt`, po czym serwer się zamyka. EULA Minecrafta jest zaakceptowana w `run/eula.txt`.

```bash
gradlew runServer -Pselftest=true
```

## Ograniczenia

- Dźwięki pochodzą z Minecrafta vanilla (konie, ravager, wilk, flet).
- Mod jest zaprojektowany dla jednego gracza. Działa w sieci klient-serwer Forge, ale fabuła zakłada jednego Willa.
