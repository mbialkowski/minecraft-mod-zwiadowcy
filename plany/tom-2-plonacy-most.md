# Tom II: Płonący most - plan

Plan drugiej części moda „Zwiadowcy”. Powstał na podstawie 36 odpowiedzi z 14.11.2026. Fabuła jest wolną interpretacją
wydarzeń z drugiego tomu serii Johna Flanagana. Dialogi i opisy piszemy własnymi słowami, bez cytatów z książki.
Nazwy oznaczone „do sprawdzenia” trzeba porównać z polskim wydaniem (tabela w rozdziale 3).

---

## Spis treści

1. [Decyzje z ankiety](#1-decyzje-z-ankiety)
2. [Zakres i zasady](#2-zakres-i-zasady)
3. [Nazwy własne do sprawdzenia](#3-nazwy-własne-do-sprawdzenia)
4. [Przejście z tomu I do tomu II](#4-przejście-z-tomu-i-do-tomu-ii)
5. [Fabuła w skrócie](#5-fabuła-w-skrócie)
6. [Mapa i lokacje](#6-mapa-i-lokacje)
7. [Misje główne](#7-misje-główne)
8. [Misje poboczne, postoje i zlecenia](#8-misje-poboczne-postoje-i-zlecenia)
9. [Mechaniki](#9-mechaniki)
10. [Przeciwnicy i postacie](#10-przeciwnicy-i-postacie)
11. [Przedmioty](#11-przedmioty)
12. [Teksty fabularne i przerywniki](#12-teksty-fabularne-i-przerywniki)
13. [Świat: generator terenu, biomy, klimat](#13-świat-generator-terenu-biomy-klimat)
14. [Architektura i zmiany w kodzie](#14-architektura-i-zmiany-w-kodzie)
15. [Wydajność](#15-wydajność)
16. [Testy](#16-testy)
17. [Etapy prac i wydania](#17-etapy-prac-i-wydania)
18. [Ryzyka](#18-ryzyka)

---

## 1. Decyzje z ankiety

| # | Temat | Decyzja |
|---|---|---|
| 1 | Forma | Ten sam mod, wersja **2.0**. Jeden plik .jar z oboma tomami |
| 2 | Start | **Kontynuacja** w świecie po tomie I (ekwipunek, koń i notatki zostają) + **szybki start** od tomu II w nowym świecie |
| 3 | Komunikat | Po epilogu ekran **„Koniec tomu I”** z przyciskiem **„Kontynuuj do części drugiej”**, przycisk „Tom II” w dzienniku, przypomnienie na czacie |
| 4 | Zapowiedź | **Bez wydania 1.0.3.** Ekran końca tomu I wychodzi razem z 2.0 |
| 5 | Wybór z tomu I | Jedna fabuła. Rycerz/zwiadowca zmienia prolog, kwestie Horace'a i sir Rodneya oraz daje drobny bonus |
| 6 | Wierność | Cały tom jako wolna interpretacja: Celtyka, kopalnie, Evanlyn, most, bitwa, Morgarath, niewola |
| 7 | Finał | **Niewola u Skandian** i zapowiedź tomu III |
| 8 | Długość | **15-17 misji** w 3 aktach (plan: 16) |
| 9 | Nazwy | Polskie, z tabelą do sprawdzenia |
| 10 | Celtyka | **Nowy region w Araluenie**, ok. 1500-2300 bloków od Redmontu, nowe biomy |
| 11 | Rozpadlina | **Wielka, w generatorze terenu**: ok. 750 bloków długości, 30-50 szerokości, ok. 85 głębokości |
| 12 | Podróże | Pierwszy raz konno z postojami, potem **szybka podróż ogniskami** (blokowana w niektórych etapach) |
| 13 | Drużyna | **Towarzysze z AI**: jeżdżą konno, walczą, słuchają rozkazów z koła, padają ranni zamiast ginąć |
| 14 | Pojedynek | **Will walczy z Morgarathem.** Gracz nie przełącza się na Horace'a |
| 15 | Morgarath | **Pojedynek w 3 fazach**: konno, pieszo z zapowiadanymi ciosami, szał |
| 16 | Most | **Prawdziwe spalenie**: olej w punktach podpór, płonące strzały, skrypt ognia, zawalenie przęseł, zwęglone resztki |
| 17 | Will i Morgarath | **Will z pomocą Horace'a.** Horace przyjmuje ciosy na tarczę, Will trafia w słabe punkty. W fazie 3 Horace pada i Will kończy sam |
| 18 | Niewola | **Po bitwie**: Skandianie Eraka napadają nad rzeką, potem okręt i epilog |
| 19 | Kopalnie | **Loch na 3 poziomach** |
| 20 | Śledztwo | **Oczy tropiciela** + wskazówki składane w dzienniku we wniosek |
| 21 | Skradanie | **Wersja 2.0**: trasy patroli, stożki widzenia, 3 poziomy czujności, ogłuszanie, odciąganie uwagi |
| 22 | Skandianie | **Nowa frakcja + Erak** jako postać fabularna |
| 23 | Nowi wrogowie | **Wargal-kusznik** (pomysł gracza: strzela jak zwiadowca, 2× obrażenia, 2× dłuższy naciąg), **Nadzorca z biczem**, **Wargal-dobosz**, **Jeździec Morgaratha** |
| 24 | Bitwa | **Zadania na skraju bitwy**: 30-40 żywych NPC i tłum w tle |
| 25 | Przerywniki | **Pergamin + 6 przerywników z kamerą** |
| 26 | Evanlyn | **Tylko wskazówki.** Prawdę zna dopiero epilog, z perspektywy króla |
| 27 | Rozwój | **10 umiejętności-odznak** za misje, bez punktów |
| 28 | Ekwipunek | **Linka z kotwiczką, Garnek z dymem, Mapa Gilana, Miecz zwiadowcy** (lekcje Gilana) |
| 29 | Wyrwij | **Łuk z siodła, gwizdek przywołujący, komendy, zryw** |
| 30 | Poboczne | **5 misji pobocznych** + postoje na szlaku |
| 31 | Wybory | **Ukryte zaufanie** Horace'a, Gilana i Evanlyn wpływa na kwestie, nagrody i pomoc w trudnych chwilach |
| 32 | Trudność | Domyślnie **trudniej niż w tomie I**. Tryby **Opowieść / Zwiadowca / Weteran** do zmiany w dzienniku |
| 33 | Klimat | **Mgła, deszcz, celtycki styl** budowli |
| 34 | Gra wieloosobowa | **Jednoosobowo**, jak tom I |
| 35 | Po tomie II | Epilog, ekran **„Koniec tomu II - kontynuuj do części trzeciej”**, potem wolna gra poza kanonem |
| 36 | Wydania | **Całość jako 2.0**, po drodze 2-3 wersje testowe (2.0-beta) jako GitHub pre-release |

---

## 2. Zakres i zasady

- **Jeden mod, dwa tomy.** Tom I zostaje bez zmian, poza ekranem końca tomu i podziałem dziennika na tomy.
  Zapisy z 1.0.x działają w 2.0.
- **Tom II zaczyna się dopiero po epilogu tomu I** (przycisk „Kontynuuj do części drugiej”) albo od razu w nowym świecie
  (szybki start, rozdział 4.3).
- **16 misji głównych w 3 aktach**, 5 pobocznych, 4 postoje na szlaku i 3 nowe zlecenia po fabule.
  Gra ma trwać ok. 6-8 godzin.
- **Teksty:** wszystko po polsku. Fabuła to wolna interpretacja książki, a dialogi piszemy od nowa, bez cytatów.
  Nieoficjalny charakter moda widać w prologu, na ekranach końca tomu i w README.
- **Gra jednoosobowa.** Na serwerze mod działa, ale fabuła zakłada jednego Willa, tak jak w tomie I.
- **Dźwięki** nadal z Minecrafta vanilla, żeby nie komplikować licencji.
- **Zasady z tomu I zostają:**
  - punkt odrodzenia ustawia się przy każdej nowej lokacji misji,
  - lokacje stoją na suchym lądzie, z dala od struktur losowych,
  - HUD ma strzałkę do celu, a dziennik przycisk „Śledź”,
  - sen do zmroku działa tam, gdzie misja tego wymaga,
  - postoje na szlaku są dobrowolne.

---

## 3. Nazwy własne do sprawdzenia

Kolumna „Źródło” mówi, czy nazwa pochodzi z książki (trzeba sprawdzić pisownię w polskim wydaniu), czy wymyśliliśmy
ją na potrzeby moda. Wszystkie nazwy siedzą w plikach danych (misje, dialogi, lokacje, język), więc poprawka nie wymaga
zmian w kodzie.

| Element | Nazwa w modzie | Oryginał | Źródło |
|---|---|---|---|
| Tytuł tomu | Płonący most | The Burning Bridge | książka, sprawdź |
| Kraina Celtów | Celtyka | Celtica | książka, sprawdź |
| Wielki kanion | Rozpadlina | the Fissure | książka, sprawdź |
| Równina bitwy | Równina Uthal | Plains of Uthal | książka, sprawdź |
| Jedyne zejście z gór Morgaratha | Przełęcz Trzech Stopni | Three Step Pass | książka, sprawdź |
| Lud z północy | Skandianie | Skandians | książka, sprawdź |
| Okręty Skandian | wilcze okręty | wolfships | książka, sprawdź |
| Skandyjski dowódca | Erak (jarl) | Erak | książka |
| Dziewczyna z Celtyki | Evanlyn | Evanlyn | książka |
| Jej prawdziwe imię (tylko w epilogu) | Cassandra | Cassandra | książka, sprawdź |
| Góry Morgaratha | Góry Deszczu i Nocy | Mountains of Rain and Night | już w modzie |
| Przełęcz do Celtyki | Przełęcz Wiatrów | - | własna |
| Górnicza osada | Tre-Gwyn | - | własna |
| Kopalnia | Kopalnia pod Kruczą Skałą | - | własna |
| Mglista dolina | Dolina Mgieł | - | własna |
| Ocalały górnik i jego syn | Dafydd, Rhys | - | własne |
| Celtycka pasterka | Morwenna | - | własna |
| Porucznik Morgaratha | Jeździec Morgaratha | - | własna |
| Król Celtów | nie występuje na ekranie, wspominany w dialogach | - | do decyzji |

---

## 4. Przejście z tomu I do tomu II

### 4.1 Ekran „Koniec tomu I”

- **Kiedy:** ekran otwiera się zaraz po zamknięciu epilogu tomu I. Gracze, którzy skończyli tom I przed aktualizacją, zobaczą
  go raz przy pierwszym wejściu do świata w 2.0 (flaga `koniec_tomu_1_pokazany`).
- **Wygląd:** pergamin taki jak prolog i epilog, w trzech częściach:
  1. **„Koniec tomu I: Ruiny Gorlanu”**: dwa zdania podsumowania, zależne od wyboru przed królem.
  2. **Napisy końcowe:** „Autorzy: ABialas & 🤖CC”, „Nieoficjalny mod fanowski, wolna interpretacja książek Johna Flanagana”
     i krótkie podziękowania dla testerów.
  3. **Zapowiedź:** „Ciąg dalszy: Tom II - Płonący most”.
- **Przyciski:**
  - **„Kontynuuj do części drugiej”** zaczyna misję `wezwanie` i od razu pokazuje prolog tomu II,
  - **„Później”** zamyka ekran, a świat zostaje otwarty (zlecenia Halta jak dotąd).
- **Przypomnienie:** dopóki tom II się nie zacznie, czat przy każdym wejściu do gry pokazuje klikalny komunikat
  „📜 Tom II: Płonący most czeka. [Kontynuuj]”.
- **Dziennik:** ma przycisk „Tom II” obok „Prolog” i „Epilog”, który działa tak samo jak „Kontynuuj”.

### 4.2 Wybór z tomu I w tomie II

| Element | Zwiadowca | Rycerz |
|---|---|---|
| Prolog, strona 1 | Pół roku nauki u Halta | Kilka tygodni w Szkole Rycerskiej i powrót do Halta, bo „łuk ciągnął bardziej niż kopia”. Baron zgodził się na zmianę |
| Sir Rodney (`wezwanie`, `noc_przed_bitwa`) | Uprzejmy, rzeczowy | Ciepły, jak do byłego ucznia, i trochę żałuje |
| Horace | Lekka rywalizacja i zaczepki | Koleżeństwo ze szkoły, wspólne żarty |
| Bonus | Zaufanie Gilana +5 na start | Okno parowania mieczem +1 tik na stałe („Szkolny fechtunek”) |

### 4.3 Szybki start od tomu II w nowym świecie

- **Wybór na starcie:** w nowym świecie ekran Kroniki (przy pierwszym wejściu) ma dwa przyciski:
  **„Zacznij od tomu I (zalecane)”** i **„Zacznij od tomu II…”**.
- **Potwierdzenie (prośba gracza w trakcie prac):** „Zacznij od tomu II…” otwiera osobny ekran. Mówi, co się pomija
  (14 misji tomu I), co gracz dostaje i że w tym świecie wyboru nie da się cofnąć. Ma dwa przyciski:
  „Tak, zaczynam od tomu II” i „Zacznij od tomu I (zalecane)”, a Esc wraca do Kroniki.
- **Prowadzenie do tomu I:** po wyborze tomu I (albo zamknięciu Kroniki) Kronika trafia do ręki, napis na ekranie
  mówi „Użyj Kroniki Araluenu (PPM)”, a czat podaje 3 kroki: podróż do Redmont, prolog i baron, dziennik (J) i strzałka.
- **Co się dzieje po wybraniu tomu II:**
  - stają Redmont i chata Halta,
  - Wyrwij jest już oswojony i stoi przy chacie,
  - tom I jest oznaczony w dzienniku jako „pominięty” i ma streszczenie na 2 strony,
  - wybór przed królem domyślnie ustawia się na zwiadowcę,
  - zaczyna się misja `wezwanie`.
- **Zestaw startowy:**
  - broń: łuk zwiadowcy, kołczan (48 zwykłych strzał, 8 płonących, 4 gwiżdżące, 4 z szerokim grotem), nóż saksoński, 6 noży do rzucania,
  - wyposażenie: płaszcz zwiadowcy, zestaw tropiciela, 3 flasze oleju, gwizdek,
  - zapasy: 12 racji podróżnych, 4 kawy zwiadowcy,
  - pamiątki: Brązowy liść dębu, Kronika, Dziennik.

### 4.4 Dziennik po zmianach

- **Tomy:** zakładka „Misje” ma sekcje **Tom I** i **Tom II**. Każdy plik misji dostaje pole `"tom": 1/2`, a stare pliki
  bez pola traktujemy jak tom 1.
- **Prolog i epilog:** przyciski działają dla bieżącego tomu, a strzałkami przełącza się między tomami.
- **Nowe zakładki:** Umiejętności, Wnioski (śledztwa), Drużyna (opisowe wskazówki o zaufaniu) i Trudność.

---

## 5. Fabuła w skrócie

**Akt I: Poselstwo.**
- Morgarath zbiera armię w Górach Deszczu i Nocy. Z płaskowyżu prowadzi tylko jedna droga, Przełęcz Trzech Stopni, więc
  armia Araluenu czeka na Równinie Uthal.
- Crowley wysyła Gilana jako posła do Celtyki, a Will jedzie z nim. Sir Rodney przydziela im Horace'a jako eskortę.
- W drodze Gilan uczy Willa walki mieczem. Na Przełęczy Wiatrów drużyna pierwszy raz zderza się z wargalskimi kusznikami.

**Akt II: Celtyka.**
- Celtyckie osady są puste. Śledztwo w Tre-Gwyn i w Kopalni pod Kruczą Skałą pokazuje, że Wargale uprowadzili górników na południe.
- W Dolinie Mgieł drużyna ratuje Evanlyn, która uciekła Wargalom.
- Gilan wraca ostrzec króla, a Will prowadzi Horace'a i Evanlyn dalej.
- Przy wargalskim posterunku pierwszy raz działa skradanie 2.0. Na wrzosowisku Jeździec Morgaratha zostawia rozkazy:
  most przez Rozpadlinę jest prawie gotowy, a armia Morgaratha przejdzie po nim i uderzy na tyły wojsk królewskich.

**Akt III: Płonący most.**
- Z krawędzi Rozpadliny Will oznacza cele. Nocą uwalnia jeńców, a o świcie, gdy kolumna Wargali wchodzi na most, polewa
  podpory olejem i podpala je płonącymi strzałami. Most się zawala.
- Wyścig do obozu królewskiego: spotkanie z Haltem, Crowleyem i królem. Evanlyn unika namiotu króla.
- W bitwie na Równinie Uthal Will z Haltem wykonuje zadania na skrzydle. Potem staje do pojedynku z Morgarathem, z pomocą
  Horace'a, i zadaje ostatni cios.
- Po zwycięstwie Will i Evanlyn pomagają rannym nad rzeką. Napadają ich Skandianie Eraka i oboje trafiają na wilczy okręt.
- **Epilog:** król dowiaduje się, że Evanlyn to jego córka Cassandra i że jest w niewoli.
- **„Koniec tomu II - kontynuuj do części trzeciej”.**

---

## 6. Mapa i lokacje

### 6.1 Układ świata

Współrzędne liczymy względem Redmontu (0, 0): +x to wschód, +z to południe. Tom I zajmuje okrąg ok. 1000 bloków.
Tom II dochodzi na zachód (Celtyka) i na południe (Rozpadlina, Uthal).

```
                         N
                         |
                    Obóz Zlotu (0,-450)
                         |
 Celtyka (wrzosowiska)   |
   Tre-Gwyn (-1750,250)  |            Chata Halta (210,-110)
   Kopalnia (-1820,180)  |   REDMONT (0,0) ----------------- Spalona osada (450,20)
   Dolina Mgieł (-1950,600)                                        Ruiny Gorlanu (850,450)
       \   Przełęcz Wiatrów (-1250,150)
        \  Posterunek dobosza (-1650,850)
         \ Wrzosowisko z kurhanem (-1500,980)
          \
  ~~~~~~~~~ ROZPADLINA ~~~~~~~~~~~~~~~~~~~~~~~ (od (-1650,1000) do (-950,1300))
     Krawędź (-1360,1080)  MOST (-1300,1150)
                                         Obóz królewski (300,1150)
          Góry Deszczu i Nocy            Równina Uthal (450,1350)  Rzeka i wilczy okręt (650,1250)
          (płaskowyż Morgaratha)         Przełęcz Trzech Stopni (520,1650, tylko jako tło)
                         |
                         S
```

### 6.2 Lokacje tomu II

Lokacje stoją tak jak w tomie I (`StoryLocation` + `StructurePlacer`): w pobliżu współrzędnych nominalnych, na płaskim
i suchym terenie, w pasującym biomie. Kotwice (anchors) to znaczniki w szablonach struktur.

| Id | Nazwa | Nominalnie | Biom | Rozmiar | Najważniejsze kotwice | Misje |
|---|---|---|---|---|---|---|
| `oboz_na_trakcie` | Obozowisko na zachodnim trakcie | -600, 80 | Łąki Araluenu | 24×24 | ognisko, gilan, horace, posłania, sparing | 2 |
| `przelecz_wiatrow` | Przełęcz Wiatrów | -1250, 150 | korytarz w górach | 60×120 (kilka szablonów) | podnoze, polka_1-2, szczyt, zejscie, kusznik_1-3, konie_obejscie | 3 |
| `tre_gwyn` | Tre-Gwyn | -1750, 250 | Łupkowe wzgórza | 56×56 | brama, wskazowka_1-6, karczma, dom_wojta, gilan | 4 |
| `kopalnia` | Kopalnia pod Kruczą Skałą | -1820, 180 | pod Tre-Gwyn | 48×48, 3 poziomy (`groundOffset` 30) | wejscie, poziom_2, poziom_3, zawal, dafydd, rhys, wyjscie_zbocze | 5, S1 |
| `dolina_mgiel` | Dolina Mgieł | -1950, 600 | Mgliste doliny | 40×40 | skraj, slad_1-5, wieza, evanlyn, wargal_1-4, ognisko | 6, 7, S3 |
| `posterunek_dobosza` | Wargalski posterunek | -1650, 850 | Wrzosowiska | 44×44 | skraj, wieza_dobosza, trasa_A_1-6, trasa_B_1-4, brama_poludniowa, dzwignia | 7 |
| `wrzosowisko` | Kurhan na wrzosowisku | -1500, 980 | Wrzosowiska | 32×32 | kurhan, jezdziec, wargal_1-3 | 8 |
| `krawedz` | Krawędź Rozpadliny | -1360, 1080 | Wrzosowiska nad kanionem | 24×24 | punkt_obserwacyjny, ognisko, evanlyn, punkt_zbiorczy, konie | 9-11 |
| `budowa_mostu` | Budowa mostu | -1300, 1150 (przez kanion) | Rozpadlina | 70×56 + most | zagroda, klodka_1-3, nadzorca_1-2, wieza_1-2, sklad_smoly, podpora_1-4, kolumna_start, trasy patroli | 9-11 |
| `oboz_krolewski` | Obóz królewski | 300, 1150 | Równina Uthal (brzeg) | 64×48 | brama, namiot_krola, halt, crowley, rodney, kowal, ognisko, namiot_willa | 12, 13 |
| `rownina_uthal` | Równina Uthal | 450, 1350 | Równina Uthal | 160×120 (spłaszczone) | wzgorze_kusznikow, oddzial, brod, szyki_A-D, arena_pojedynku | 14, 15 |
| `brzeg_rzeki` | Rozlewisko rzeki | 650, 1250 | Równina Uthal | 48×32, woda | ranni_1-3, skandianie, okret | 16 |
| `wilczy_okret` | Wilczy okręt | na rozlewisku | - | 28×8 | poklad, erak, evanlyn, will | 16 |

**Ogniska do szybkiej podróży:**

- **Tom I:** Redmont (brama), chata Halta i obóz Zlotu. Te ogniska odblokowują się przy starcie tomu II.
- **Tom II:**
  - Obozowisko na trakcie,
  - Przełęcz (zejście),
  - Tre-Gwyn,
  - Dolina Mgieł,
  - Szałas pasterski (za misję S2),
  - Krawędź,
  - Obóz królewski.

### 6.3 Długości tras przy pierwszym przejeździe

| Odcinek | Ok. bloków | Postoje |
|---|---|---|
| Redmont → Obozowisko na trakcie | 600 | 1 (w 1/2) |
| Obozowisko → Przełęcz | 650 | 1 (w 1/2) |
| Przełęcz → Tre-Gwyn | 510 | - |
| Krawędź → Obóz królewski | 1600 | 2 (w 1/3 i 2/3) + misja S4 |

Najdłuższy odcinek (1600) to fabularny wyścig. Wyrwij w galopie przejeżdża go w ok. 3 minuty, a postoje są dobrowolne.

---

## 7. Misje główne

Opis etapów: cel (typ celu w silniku misji) → miejsce → uwagi. Typy już istniejące w tomie I to `talk`, `reach`,
`sneak_reach`, `kill`, `collect`, `event`, `night` i `target_hits`. Nowe typy opisuje rozdział 14.2.

### Akt I: Poselstwo

#### M1 `wezwanie` - Wezwanie

**Start:** przycisk „Kontynuuj do części drugiej” albo szybki start. Najpierw prolog tomu II (5 stron).

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `talk` halt | chata_halta:halt | Halt: Morgarath ruszył, armia zbiera się pod Uthal. Przyszedł list od Crowleya |
| 2 | `reach` | redmont:dziedziniec | Halt jedzie przodem (jako NPC, nie towarzysz) |
| 3 | `talk` crowley | redmont:wielka_sala | Narada: Gilan jedzie jako poseł do króla Celtów, Will z nim. Halt dołącza do armii |
| 4 | `talk` sir_rodney | redmont:koszary | Horace jedzie jako eskorta. Wariant rycerski dialogu |
| 5 | `talk` kowal | redmont:kuznia | Zapasy: 32 strzały, 2 flasze oleju, 8 racji |
| 6 | `ride_with` gilan, horace | redmont:brama | Gilan i Horace dołączają do drużyny. Przerywnik **„Wyjazd z Redmontu”** |

**Nagrody:** 60 PD, notatka „Rozkazy Crowleya”. **Zaufanie na start:** Horace 50, Gilan 60 (+5 dla zwiadowcy), Evanlyn 40
(od M6).

#### M2 `miecz_zwiadowcy` - Miecz zwiadowcy

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `reach` | oboz_na_trakcie:ognisko | Wieczór. Odkrycie pierwszego ogniska otwiera samouczek szybkiej podróży |
| 2 | `talk` gilan | oboz_na_trakcie:gilan | „Zwiadowca nie zawsze ma miejsce na łuk.” Will dostaje drewniany miecz ćwiczebny |
| 3 | `parry` ×5 | oboz_na_trakcie:sparing | Sparing z Gilanem (tryb DUEL, Gilan nie może zginąć, ciosy zapowiadane). Samouczek parowania |
| 4 | `counter` ×3 | jw. | Kontry po udanym parowaniu |
| 5 | `talk` gilan | jw. | Nagroda: **Miecz zwiadowcy** i umiejętność **Lekcja Gilana** |
| 6 | `event` rozkaz_czekaj, rozkaz_za_mna | jw. | Samouczek koła rozkazów. Horace komentuje |
| 7 | `night` → sen | oboz_na_trakcie:poslania | Sen do rana, wyjazd o świcie |

**Zaufanie:** w dialogu przy ognisku Horace drwi z „noża do chleba”. Spokojna riposta daje Horace +5, zgryźliwa daje
Horace -5 i Gilan +3.

#### M3 `przelecz` - Przełęcz Wiatrów

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `reach` | przelecz_wiatrow:podnoze | Po drodze dobrowolny postój |
| 2 | `kill` wargal_kusznik ×3 (oznaczeni) | przelecz_wiatrow:kusznik_1-3 | Pierwsze spotkanie z kusznikami. Gilan: „Kusze! Za skały!” Uczy chowania się za osłoną |
| 3 | `talk` gilan | przelecz_wiatrow:zawal | Ścieżkę zasypał zawał. Gilan daje **Linkę z kotwiczką** |
| 4 | `event` kotwiczka ×2 | przelecz_wiatrow:polka_1-2 | Wspinaczka po półkach skalnych. Konie i drużyna idą dłuższą drogą (`konie_obejscie`) |
| 5 | `reach` | przelecz_wiatrow:szczyt | Przerywnik **„Rozpadlina”**: z grani widać kanion na południu i dym nad nim |
| 6 | `ride_with` | przelecz_wiatrow:zejscie | Spotkanie z drużyną. Ognisko przełęczy |

**Nagrody:** 80 PD, notatka „Kusze Wargalów”. Pergamin końca aktu I: **„Za górami”**.

### Akt II: Celtyka

#### M4 `ciche_wzgorza` - Ciche wzgórza

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `reach` | tre_gwyn:brama | Tytuł „Tre-Gwyn”. Cisza, skrzypiące okiennice, brak zwierząt. Gilan: „Za cicho” |
| 2 | `event` tropiciel_aktywny | tre_gwyn | Samouczek **Oczu tropiciela** (rozdział 9.4) |
| 3 | `clues` 4 z 6 | tre_gwyn:wskazowka_1-6 | Wskazówki opisane niżej |
| 4 | `deduce` | dziennik → Wnioski | Wybór 1 z 3 wniosków. Pomyłka daje podpowiedź Gilana i kolejną próbę. Trafienie za pierwszym razem: Gilan +3 |
| 5 | `talk` gilan | tre_gwyn:gilan | Decyzja: zejść do kopalni |

**Wskazówki w Tre-Gwyn:**
1. puste wózki z rudą wywleczone spod szybu,
2. wyłamane drzwi domu wójta,
3. ślady Wargalów prowadzące do kopalni,
4. porzucone kilofy i hełmy górnicze,
5. niedojedzona strawa przy wystygłym palenisku w karczmie,
6. celtycki znak ostrzegawczy wyryty na kamieniu.

**Poprawny wniosek:** mieszkańców wyprowadzono w kolumnie na południe, pod strażą Wargali.

**Nagrody:** umiejętność **Oko tropiciela**, notatka „Tre-Gwyn”.

#### M5 `kopalnia` - Kopalnia pod Kruczą Skałą

Loch na 3 poziomach:
- **Poziom 1:** chodniki z torami i wagonikami.
- **Poziom 2:** komory wydobywcze, zalana komora (woda do pasa spowalnia) i szyb z zerwaną drabiną.
- **Poziom 3:** stara sztolnia, boczny szyb dla misji S1 i wyjście na zbocze.

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `reach` | kopalnia:wejscie | Horace idzie z Willem. Gilan pilnuje koni (fabuła) |
| 2 | `reach` | kopalnia:poziom_2 | Szyb pokonuje się kotwiczką |
| 3 | `kill` wargal ×4 (oznaczeni) | poziom 2 | Ciemność: Wargale widzą lepiej niż Will. Pochodnie pomagają |
| 4 | `escape` 30 s | kopalnia:zawal → bezpieczna_komora | Zawał: spadający gruz zadaje lekkie obrażenia. Horace podpiera belkę (przerywnik bez kamery) |
| 5 | `talk` dafydd | kopalnia:dafydd | Ocalały górnik: Wargale kazali nosić liny i belki „do wielkiej szczeliny na południu”. Wspomina zaginionego syna (start S1) |
| 6 | `reach` | kopalnia:wyjscie_zbocze | Gilan czeka z końmi |

**Nagrody:** 2 Garnki z dymem i przepis na nie (od Dafydda), 100 PD.

#### M6 `evanlyn` - Dziewczyna z Doliny Mgieł

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `reach` | dolina_mgiel:skraj | Mgła gęstnieje. Gilan widzi ślady konia i Wargali |
| 2 | `follow_trail` | dolina_mgiel:slad_1-5 | Tropienie z Oczami tropiciela |
| 3 | `kill` wargal ×4 (oznaczeni) | dolina_mgiel:wieza | Evanlyn osaczona w ruinach wieży, 2 kuszników stoi na murku. Gdy ją trafią, jest ranna, ale misja trwa dalej |
| 4 | przerywnik **„Evanlyn”** | dolina_mgiel:wieza | Dziewczyna z nożem w dłoni i pierwsza wymiana spojrzeń |
| 5 | `talk` evanlyn | dolina_mgiel:evanlyn | Przedstawia się jako dama dworu, której orszak napadli Wargale. Widziała kolumnę jeńców. Pierścień z herbem to wskazówka |
| 6 | `companion_join` evanlyn | - | Evanlyn dołącza na własnej klaczy |

**Zaufanie:** pytanie o rodzinę („Nie naciskaj” daje Evanlyn +5, a dopytywanie Evanlyn -3), oddanie jej racji
daje Evanlyn +3.

#### M7 `dwie_drogi` - Dwie drogi

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `talk` gilan | dolina_mgiel:ognisko | Gilan wiezie wieść królowi, bo armia nie wie o moście. Will prowadzi resztę drużyny. Gilan daje **Mapę Gilana** i lunetę |
| 2 | `companion_leave` gilan | - | Gilan odjeżdża na wschód |
| 3 | `reach` | posterunek_dobosza:skraj | Palisada, wieża z doboszem, 2 patrole, 1 kusznik |
| 4 | `kill` dobosz (po cichu) | posterunek_dobosza:wieza_dobosza | Samouczek skradania 2.0. Alarm nie kończy misji: dobosz bije w bęben i przybiegają 4 posiłki |
| 5 | `event` dzwignia | posterunek_dobosza:dzwignia | Otwarcie południowej bramy |
| 6 | `ride_with` | posterunek_dobosza:brama_poludniowa | Drużyna przejeżdża |

**Nagrody:** umiejętność **Cichy krok**. Bez alarmu dodatkowo Horace +5.
**Zaufanie przy pożegnaniu z Gilanem:** odpowiedź „Dam radę” daje Gilan +5, a „Wolałbym, żebyś został” daje Horace +3.

#### M8 `czarny_jezdziec` - Czarny jeździec

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `reach` | wrzosowisko:kurhan | Evanlyn: „Ktoś jedzie za nami” |
| 2 | `saddle_hits` ×3 | wrzosowisko | Samouczek strzelania z siodła do 3 biegnących Wargali |
| 3 | `kill` jezdziec_morgaratha | wrzosowisko:jezdziec | Mini-boss z paskiem zdrowia, opis w rozdziale 10 |
| 4 | `collect` rozkazy_morgaratha | - | Wypada z Jeźdźca |
| 5 | `event` czytaj:rozkazy | - | Pergamin z rozkazami: most będzie gotowy za 3 dni, armia przejdzie i uderzy na tyły pod Uthal |
| 6 | `talk` horace | wrzosowisko:kurhan | Decyzja: idziemy do mostu |

**Nagrody:** umiejętność **Pewna ręka**. Pergamin końca aktu II: **„Most”**.

### Akt III: Płonący most

#### M9 `na_krawedzi` - Na krawędzi

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `reach` | krawedz:punkt_obserwacyjny | Ukryty obóz z ogniskiem. Szybka podróż blokuje się do końca M11 |
| 2 | przerywnik **„Most nad Rozpadliną”** | - | Kamera leci wzdłuż kanionu do budowy: jeńcy, belki, liny |
| 3 | `mark` ×2 | budowa_mostu | Przez lunetę: 2 dowolnych Wargali w obozie (`any_enemy`). Oznaczeni świecą do końca M11 (2.0.2: było 6 - nie dało się znaleźć wszystkich) |
| 4 | `talk` horace, evanlyn | krawedz:ognisko | Plan: nocą uwolnić jeńców, o świcie spalić most. Wybór: czy Horace ma odciągnąć uwagę na sygnał (flaga dla M10) |
| 5 | `night` | krawedz | Sen do zmroku |

#### M10 `lancuchy` - Łańcuchy

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `sneak_reach` | budowa_mostu:zagroda | Skradanie 2.0. Patrole chodzą po trasach |
| 2 | `kill` nadzorca ×2 (po cichu lub w walce) | nadzorca_1-2 | Wypadają **klucze nadzorcy** |
| 3 | `free` ×3 | zagroda:klodka_1-3 | 8 celtyckich górników ucieka ścieżką |
| 4 | `reach` | krawedz:punkt_zbiorczy | Gdy podniesie się alarm, rozkaz „Odciągnij uwagę” wysyła Horace'a pod bramę, gdzie przyjmuje walkę |

**Powiązanie z S1:** gdy misja S1 jest zrobiona, uwolnionych prowadzi Rhys. Górnicy podcinają liny kotwiczne,
więc w M11 wystarczą 3 punkty oleju zamiast 4.

**Nagrody:** umiejętność **Zimna krew** (zawsze). Bez alarmu dodatkowo Evanlyn +5.

#### M11 `plonacy_most` - Płonący most

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `collect` flasza oleju ×4 | budowa_mostu:sklad_smoly | Beczki smoły napełniają puste flasze |
| 2 | `oil_points` 4 (3 przy S1) | podpora_1-4 | 2 zastrzały pod pomostem przy północnej ścianie kanionu (zejście kotwiczką) i 2 kotwy wież linowych. Patrole na moście |
| 3 | `event` swit | - | Rogi: kolumna Wargali (ok. 20 w tle i 6 żywych) wchodzi na most od południa |
| 4 | `ignite_points` (płonące strzały) | jw. | Bez limitu czasu, ale im dłużej to trwa, tym więcej Wargali dochodzi na północny brzeg |
| 5 | skrypt spalenia + przerywnik **„Płonący most”** | - | Ok. 35 s: ogień, odpadające przęsła, spadający Wargale (rozdział 9.12) |
| 6 | `escape` | krawedz:konie | Pościg Wargali, którzy zdążyli przejść, i kusznicy strzelający z drugiego brzegu. Ucieczka z Horace'em i Evanlyn, przydaje się zryw |

**Nagrody:** umiejętność **Rozpalacz**, postęp „Płonący most”, Horace +10.

Pergamin przejściowy: **„Dym nad Rozpadliną”**.

#### M12 `wyscig_z_czasem` - Wyścig z czasem

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `reach` | oboz_krolewski:brama | ok. 1600 bloków z 2 postojami i misją S4 po drodze. Szybka podróż zablokowana |
| 2 | `talk` straznik → halt | oboz_krolewski:halt | Spotkanie z Haltem i rzadka pochwała |
| 3 | `talk` crowley, krol_duncan | oboz_krolewski:namiot_krola | Raport. Evanlyn zostaje „pilnować koni” i nie wchodzi do namiotu (wskazówka) |

#### M13 `noc_przed_bitwa` - Noc przed bitwą

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `talk` sir_rodney | oboz_krolewski:rodney | Horace staje w szyku. Wariant rycerski |
| 2 | `talk` kowal_obozowy | oboz_krolewski:kowal | Naprawa broni i wybór: 16 płonących strzał albo 16 z szerokim grotem |
| 3 | `talk` gilan | oboz_krolewski:ognisko | Gilan wrócił. Przy zaufaniu ≥ 70 daje **Pas na noże od Gilana** (+4 noże do rzucania w kołczanie) |
| 4 | `talk` evanlyn | oboz_krolewski:ognisko | Nocna rozmowa. Przy zaufaniu ≥ 70 mówi coś prawdziwego o sobie, ale nie zdradza imienia |
| 5 | `night` → sen | oboz_krolewski:namiot_willa | Punkt kontrolny przed bitwą |

Misja odblokowuje S5 („Czarny rumak”).

#### M14 `bitwa_uthal` - Bitwa na Równinie Uthal

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | przerywnik (krótki) | rownina_uthal | Szyki, sztandary, rogi |
| 2 | `kill` wargal_kusznik ×5 | rownina_uthal:wzgorze_kusznikow | Zadanie A, z Haltem jako towarzyszem |
| 3 | `defend` 60 s (min. 4 z 6 żołnierzy) | rownina_uthal:oddzial | Zadanie B: otoczony oddział i 3 fale |
| 4 | `mark` ×5 dobosz | rownina_uthal:szyki_A-D | Zadanie C: oznaczeni dobosze giną od salwy łuczników |
| 5 | `defend` 60 s | rownina_uthal:brod | Zadanie D: skandyjska flanka. Pierwsza walka ze Skandianami, razem z Haltem i Gilanem |
| 6 | przerywnik (krótki) | - | Wyłom w szyku, Morgarath rusza na czarnym rumaku. Punkt kontrolny |

#### M15 `wladca_deszczu_i_nocy` - Władca Deszczu i Nocy

Pojedynek opisany dokładnie w rozdziale 10.3. Arena to krąg o średnicy ok. 50 bloków. Granicę wyznaczają szyki
żołnierzy (tłum w tle), którzy odpychają uciekającego gracza.

| # | Cel | Uwagi |
|---|---|---|
| 1 | `boss` morgarath, faza 1 | Konno. Przy S5 faza krótsza |
| 2 | `boss` faza 2 | Pieszo, z Horace'em |
| 3 | `boss` faza 3 | Szał. Horace ogłuszony |
| 4 | przerywnik **„Upadek Morgaratha”** | Ostatni cios zawsze zadaje gracz |

Po śmierci gracza pojedynek zaczyna się od początku bieżącej fazy. **Nagrody:** postęp, 300 PD, Halt: „Dobra robota.”

#### M16 `wilcze_okrety` - Wilcze okręty

| # | Cel | Miejsce | Uwagi |
|---|---|---|---|
| 1 | `talk` halt | oboz_krolewski:halt | Świętowanie. Evanlyn chce pomóc rannym nad rzeką |
| 2 | `heal` ×3 | brzeg_rzeki:ranni_1-3 | Opatrywanie rannych (racja + bandaż z zestawu tropiciela) |
| 3 | `survive` 40 s albo HP < 30% | brzeg_rzeki:skandianie | Erak i 8 Skandian. Gracz nie może zginąć, kończy się powaleniem |
| 4 | przerywnik **„Niewola”** + pergamin | - | Will budzi się na pokładzie. Ekwipunek trafia do skrzyni (rozdział 9.15) |
| 5 | `talk` erak | wilczy_okret:erak | Szacunek dla „chłopaka, który spalił most”. Zapowiedź Skandii |
| 6 | `talk` evanlyn | wilczy_okret:evanlyn | Ostatnia rozmowa i wskazówka |

Po misji: **epilog tomu II** (6 stron), potem ekran **„Koniec tomu II - kontynuuj do części trzeciej”**, potem wolna gra.

---

## 8. Misje poboczne, postoje i zlecenia

### 8.1 Misje poboczne

| Id | Nazwa | Odblokowanie | Przebieg | Nagroda |
|---|---|---|---|---|
| S1 `syn_gornika` | Syn górnika | M5 (Dafydd) | Boczny szyb na poziomie 3. Trzeba usunąć gruz (kilof albo kotwiczka) i wyprowadzić Rhysa, który ma skręconą nogę i idzie wolno. Po drodze 3 nietoperze-szkodniki dla klimatu (vanilla) | Umiejętność **Górnicza zręczność**. W M10 Rhys prowadzi jeńców, a w M11 wystarczą 3 punkty oleju |
| S2 `pasterka` | Pasterka z wrzosowisk | M6 (dym w oddali) | Morwenna ukrywa się z owcami w szałasie. Nocą trzeba obronić stado przed 6 wilkami w 2 falach | Ognisko „Szałas pasterski”, 6 racji, notatka o celtyckich zwyczajach |
| S3 `strzal_gilana` | Strzał Gilana | M6, przed M7 | Konkurs strzelania z siodła z Gilanem: 3 przejazdy obok tarcz, 8 tarcz. Trzeba pobić wynik Gilana (6) | Umiejętność **Jeździec**, Gilan +5 |
| S4 `obcy_na_trakcie` | Obcy na trakcie | M12 (trasa) | 3 skandyjskich zwiadowców okrada kupca. Do wyboru walka albo rozmowa (Skandianin mówi o złocie Morgaratha i o jarlu Eraku) | Umiejętność **Czujne ucho**, notatka „Skandianie” |
| S5 `czarny_rumak` | Czarny rumak | M13 | Skradanie do koniowiązów wroga i poluzowanie popręgu czarnego rumaka. Koniom nic się nie dzieje | Umiejętność **Przyjaciel koni**. W M15 faza 1 krótsza (rumak potyka się przy 85% zamiast 70%) |

### 8.2 Postoje na szlaku

Działają jak w tomie I: Halt albo Gilan wspomina o nich przed wyjazdem, a gracz decyduje, czy się zatrzyma.

| Trasa | Postój | Zadanie | Nagroda |
|---|---|---|---|
| Redmont → Obozowisko | Wóz z prowiantem dla armii | Zbierz 6 rozsypanych worków | 8 racji, notatka |
| Obozowisko → Przełęcz | Spalona strażnica | Mała potyczka: 3 dezerterów | Garnek z dymem |
| Krawędź → Obóz (1/3) | Celtyccy uciekinierzy | Zbierz 5 jagód dla dzieci | 2 kawy, notatka |
| Krawędź → Obóz (2/3) | Wargalski zwiad | Mała potyczka: 2 Wargali i 1 kusznik | 8 płonących strzał |

### 8.3 Zlecenia po fabule (wolna gra)

Zlecenia odnawiają się raz na dobę gry, jak w tomie I. Trzy zlecenia Halta z tomu I zostają.

| Zlecenie | Zleceniodawca | Treść |
|---|---|---|
| Patrol nad Rozpadliną | Gilan (Krawędź) | Rozbij 4 Wargali przy zwęglonym moście |
| Droga do domu | Gilan | Odprowadź 3 celtyckich uciekinierów do Tre-Gwyn |
| Wilki na wrzosowisku | Morwenna | Obroń stado przed wilkami |

Po epilogu tomu II ekran końca tomu wyraźnie mówi, że to wolna gra poza kanonem.

---

## 9. Mechaniki

### 9.1 Towarzysze (Gilan, Horace, Evanlyn, w bitwie także Halt)

- **Podstawa:** rozbudowany tryb `COMPANION` z `StoryNpc`.
- **Konie:**
  - każdy towarzysz ma własnego konia (siodło, nie da się go dosiąść), tak jak Wyrwij nie ginie i ma obniżone obrażenia,
  - gdy gracz jedzie konno, towarzysze też jadą i trzymają szyk 4-6 bloków za nim,
  - gdy gracz zsiada albo wchodzi w strefę „pieszo” (kopalnia, obóz przy moście), towarzysze zsiadają, a konie czekają w najbliższym bezpiecznym miejscu,
  - towarzysz dalej niż 64 bloki i poza wzrokiem gracza zostaje przeniesiony za niego.
- **Role w walce:**
  - **Horace:** miecz i tarcza, blokuje ciosy z przodu, ściąga na siebie uwagę (prowokacja co 15 s).
  - **Gilan:** miecz i łuk, strzela z dystansu i dobija rannych wrogów.
  - **Evanlyn:** nie walczy wręcz. Trzyma się z tyłu i rzuca kamieniami, które ogłuszają na 1 s, odnowienie 8 s.
  - **Halt:** w bitwie łucznik, celny i szybki.
- **Rany:** przy 0 HP towarzysz pada ranny (tryb `WOUNDED`) i wstaje po walce z 50% zdrowia. Na Weteranie wstaje dopiero
  po opatrzeniu (PPM z racją).
- **Skradanie:** kiedy gracz się skrada, towarzysze kucają, trzymają się 4 bloki za nim i są niewidzialni dla systemu
  wykrywania. Nigdy nie podnoszą alarmu.
- **Koło rozkazów (przytrzymaj G):**
  - opcje: **Za mną**, **Czekaj tu**, **Ukryj się**, **Odciągnij uwagę** (tylko Horace),
  - spojrzenie na towarzysza i G wydaje rozkaz tylko jemu, bez celu rozkaz trafia do wszystkich.

### 9.2 Zaufanie

Każdy towarzysz ma ukryty licznik zaufania od 0 do 100. Start: Horace 50, Gilan 60, Evanlyn 40.

| Zdarzenie | Zmiana |
|---|---|
| Wybory w dialogach | od -5 do +5 (opisane przy misjach) |
| Porzucenie rannego towarzysza (odjazd dalej niż 80 bloków podczas walki) | -5 |
| Uratowanie towarzysza (zabicie wroga, który go atakuje, gdy ma < 30% HP) | +2, maks. 3 razy na misję |
| Misja bez alarmu (M7, M10) | +5 dla wskazanej osoby |

**Progi:**
- **≥ 70:** dodatkowe kwestie i prezenty (Gilan w M13). Horace raz w fazie 3 pojedynku wstaje i przyjmuje cios, który
  zabiłby Willa. Evanlyn mówi coś prawdziwego o sobie w M13.
- **≤ 30:** chłodne kwestie, bez prezentów. Fabuła się nie zmienia.

Zakładka „Drużyna” w dzienniku pokazuje tylko opisy w rodzaju „Horace polega na tobie” albo „Evanlyn wciąż ci nie ufa”,
bez liczb.

### 9.3 Skradanie 2.0

- **Trasy patroli:** kotwice `trasa_X_1..n` w szablonach. Patrol idzie od punktu do punktu, czeka 2-4 s i rozgląda się.
- **Stożek widzenia:**

  | Pora | Kąt | Zasięg |
  |---|---|---|
  | Dzień | 110° | 24 bloki |
  | Noc | 80° | 12 bloków (+8 w świetle ognisk i pochodni) |

  Działają też modyfikatory z tomu I: płaszcz, kucanie, liście i trawa, Bezruch.
- **Poziomy czujności:**
  1. **Spokój.**
  2. **Podejrzenie:** nad głową pojawia się „?” i pasek, który się napełnia (szybciej z bliska i w świetle). Wróg idzie
     sprawdzić ostatnie miejsce, gdzie coś widział lub słyszał, szuka przez 20 s i wraca na trasę.
  3. **Alarm:** pojawia się „!”, słychać róg albo bęben dobosza. Do misji dochodzą posiłki z namiotów, a wszyscy w promieniu
     40 bloków przechodzą w tryb walki. Po 90 s bez kontaktu alarm opada do podejrzenia.
- **Ciche ogłuszanie:** podejście od tyłu w kucaniu i cios (pięść, saksa albo miecz) usypia Wargala do końca misji.
  Ogłuszony leży na ziemi. Patrol, który go zobaczy, przechodzi w podejrzenie z pełnym paskiem.
- **Odciąganie uwagi:**
  - Kamyk (nowy przedmiot, ze żwiru) sprawdzają wrogowie w promieniu 8 bloków od miejsca upadku.
  - Strzała gwiżdżąca z tomu I działa w promieniu 16.
  - Garnek z dymem (rozdział 9.8).
- **HUD:** ikony nad głowami wrogów w promieniu 32 bloków (?, !, zz dla ogłuszonych) i pasek podejrzenia.

### 9.4 Oczy tropiciela

- **Działanie:** przytrzymanie klawisza **T** z zestawem tropiciela w ekwipunku. Ekran lekko szarzeje, a w promieniu
  16 bloków (24 z „Okiem tropiciela”) podświetlają się:
  - wskazówki: zarys i cząsteczki,
  - ślady stóp, kół i ciągnięcia: rząd cząsteczek-odcisków między kotwicami trasy,
  - świeże ślady wrogów: czerwone, znikają po 60 s.
- **Ruch:** w trybie tropiciela gracz porusza się wolniej (jak w kucaniu).
- **Wskazówki:** PPM na wskazówce dopisuje notatkę do zakładki **Wnioski**. Po zebraniu wymaganej liczby dziennik
  pokazuje 3 możliwe wnioski i gracz wybiera jeden.

### 9.5 Parowanie, miecz i zapowiadane ciosy

- **Parowanie:** przytrzymanie PPM z mieczem zwiadowcy albo saksą to garda. Liczy się, gdy cios trafia przed upływem okna
  parowania od podniesienia gardy:

  | Broń | Okno parowania |
  |---|---|
  | Miecz | 6 tików |
  | Saksa | 4 tiki |
  | Lekcja Gilana | +2 tiki |
  | Szkolny fechtunek (rycerz) | +1 tik |

- **Skutki:**
  - **udane parowanie:** zero obrażeń, przeciwnik zatacza się na 1 s, a kontra w tym czasie zadaje ×2 obrażenia,
  - **sama garda bez parowania:** 60% obrażeń,
  - **garda od tyłu:** nic nie daje.
- **Zapowiadane ciosy:** bossowie, mini-bossowie i Skandianie mają widoczny zamach (animacja, błysk na broni, dźwięk)
  0,6-0,9 s przed ciosem.

### 9.6 Linka z kotwiczką

- **Użycie:** PPM rzuca hak, który zaczepia się o górną krawędź bloku w zasięgu 20 bloków i podciąga gracza po prostej
  w ok. 1 s. Odnowienie 8 s (5 s z „Górniczą zręcznością”).
- **Ograniczenia:**
  - nie działa w walce, czyli w ciągu 3 s od zadania lub otrzymania obrażeń,
  - nie działa na blokach z tagiem `zwiadowcy:bez_kotwiczki` (np. mury obozów w misjach, gdzie skrót popsułby zadanie),
  - ma trwałość 120 użyć.
- **Przepis:** 3 żelazne sztabki i 2 liny. Liny z tomu I zostają.

### 9.7 Mapa Gilana i szybka podróż

- **Mapa:** PPM otwiera pergaminową mapę Araluenu i Celtyki.
  - Tło to ręcznie rysowana tekstura 512×512 ze stałym układem krain.
  - Ikony lokacji pokazują ich prawdziwe, postawione współrzędne. Nieodkryte lokacje widać jako „?”.
  - Pozycja gracza to strzałka.
- **Ogniska zwiadowców:** nowy blok w lokacjach. PPM na nim zapisuje je w mapie.
- **Szybka podróż:**
  - Klik w odkryte ognisko na mapie, potwierdzenie i ekran „W drodze…” (ok. 2 s), po którym gracz z Wyrwijem
    i towarzyszami pojawia się przy tym ognisku.
  - Koszt to 1 racja podróżna, a czas gry przesuwa się o 1 godzinę na każde 500 bloków.
  - **Blokady:** walka (8 s od obrażeń), etapy z flagą `no_fast_travel` (M9-M11 do ucieczki, cała M12, M14-M16) i alarm
    w pobliżu.

### 9.8 Garnek z dymem

- **Działanie:** rzucany jak flasza oleju. Tworzy kulę dymu o promieniu 4 bloków na 6 s.
  - Wrogowie w środku i patrzący przez dym tracą cel, a podejrzenie zamarza.
  - Gracz w dymie liczy się jako całkowicie ukryty.
- **Przepis:** glina, sucha trawa i flasza oleju dają 2 garnki. Przepis odblokowuje Dafydd w M5.

### 9.9 Wyrwij w tomie II

- **Łuk z siodła:** rozrzut strzał ×2 w galopie, ×1,3 w stępie i ×1 na stojąco. „Jeździec” zmniejsza karę o połowę.
- **Gwizdek:**
  - krótki dźwięk przywołuje Wyrwija z 100 bloków (150 z „Przyjacielem koni”),
  - gdy koń jest dalej, pojawia się za graczem, kiedy gracz na niego nie patrzy.
- **Komendy:** Shift + PPM na Wyrwiju otwiera małe koło z opcjami:
  - Zostań,
  - Wracaj do domu (do ostatniego ogniska),
  - Czuwaj (parska na wrogów, jak w tomie I).
- **Zryw:** klawisz sprintu w siodle daje 3 s z prędkością +60%. Odnowienie 30 s (20 s z „Przyjacielem koni”).

### 9.10 Umiejętności (10 odznak)

| Umiejętność | Skąd | Działanie |
|---|---|---|
| Lekcja Gilana | M2 | Okno parowania +2 tiki |
| Oko tropiciela | M4 | Zasięg Oczu tropiciela 24, ślady widać dłużej |
| Cichy krok | M7 | Kroki po żwirze, kamieniu i drewnie nie budzą podejrzeń |
| Pewna ręka | M8 | Naciąg łuku zwiadowcy -15% (14 → 12 tików) |
| Zimna krew | M10 | Pasek podejrzenia wrogów rośnie o 25% wolniej |
| Rozpalacz | M11 | Rozprysk oleju +1 blok, płonące strzały palą o 2 s dłużej |
| Górnicza zręczność | S1 | Odnowienie kotwiczki 5 s, wspinanie po drabinach szybsze |
| Jeździec | S3 | Kara do celności z siodła o połowę mniejsza |
| Czujne ucho | S4 | Po 2 s bezruchu w kucaniu wrogowie w promieniu 12 bloków widoczni przez ściany przez 3 s |
| Przyjaciel koni | S5 | Gwizdek na 150 bloków, zryw co 20 s |

Umiejętności działają też w wolnej grze po tomie II. Zakładka „Umiejętności” pokazuje zdobyte i zablokowane (z podpowiedzią,
skąd je wziąć).

### 9.11 Poziomy trudności

| Parametr | Opowieść | Zwiadowca (domyślnie) | Weteran |
|---|---|---|---|
| Obrażenia wrogów | ×0,6 | ×1,0 | ×1,4 |
| Zdrowie wrogów | ×0,8 | ×1,0 | ×1,25 |
| Narastanie podejrzenia | ×0,7 | ×1,0 | ×1,3 |
| Podświetlenie słabych punktów bossa | tak, dłużej | tak | nie |
| Zapowiedź ciosu | 1,1 s | 0,8 s | 0,6 s |
| Ranny towarzysz wstaje | po walce | po walce | po opatrzeniu |
| Punkty kontrolne w pojedynku | po każdej fazie | po każdej fazie | tylko przed walką |

- **Ustawienie:** tryb zmienia się w dowolnej chwili w dzienniku.
- **Postęp:** jest „Pokonaj Morgaratha na Weteranie”, który wymaga Weterana przez cały pojedynek.
- **Porównanie z tomem I:** „Zwiadowca” w tomie II jest wyraźnie trudniejszy, bo wrogowie są lepiej uzbrojeni, są
  kusznicy i jest skradanie 2.0.
- **Zakres:** tryb nie zmienia tomu I.

### 9.12 Spalenie mostu

- **Budowa mostu:**
  - Szablon `budowa_mostu/most`: pomost długości 46 bloków i szerokości 5, z belek (ciemny dąb i świerk) i płotów.
  - Podpierają go 4 zastrzały wbite w ściany kanionu i liny z dwóch wież w kształcie litery A, po jednej na każdym brzegu.
  - Most dzieli się na 4 przęsła z kotwicami `przeslo_1-4`.
- **Punkty oleju:** PPM z flaszą oleju na kotwicy podpory polewa ją olejem (smuga cząsteczek, ciemne bloki „oblanego drewna”).
  Stan zapisuje się w danych lokacji.
- **Podpalenie:** trafienie płonącą strzałą w oblaną podporę zapala ją. Gdy płoną wszystkie wymagane podpory, rusza skrypt
  `BridgeBurn`:

  | Czas | Co się dzieje |
  |---|---|
  | 0-8 s | Ogień biegnie od podpór w górę pomostu (własny blok „płonąca belka”, który nie rozprzestrzenia ognia poza most) |
  | 8-30 s | Przęsła odpadają kolejno: bloki przęsła zamieniają się w spadające bloki partiami po maks. 200 na tik. Wargale na moście dostają odrzut i spadają |
  | 30-35 s | Zostają zwęglone kikuty (własne bloki „zwęglona belka”) i dym nad kanionem przez 10 minut gry |

- **Niezawodność:**
  - stan skryptu jest zapisywany, więc po wczytaniu gry w trakcie palenia skrypt kończy się natychmiast (stan końcowy),
  - ogień vanilla na moście jest wyłączony, żeby nie podpalić lasu,
  - przerywnik z kamerą leci równolegle do skryptu.

### 9.13 Bitwa

- **`BattleDirector`:** steruje fazami M14 (zadania A-D) i pilnuje limitów:
  - maks. 40 żywych NPC (żołnierze Araluenu i Wargale) w promieniu 64 bloków od gracza,
  - maks. 160 manekinów tłumu: lekkich bytów bez AI z prostą animacją (kołysanie, unoszenie broni), ze sztandarami
    na szczytach szyków.
- **Fale:** pojawiają się w punktach poza wzrokiem gracza. Martwi znikają po 10 s.
- **Dźwięk:** rogi, bęben i szczęk broni w tle (dźwięki vanilla w pętli z losowym przesunięciem).

### 9.14 Przerywniki z kamerą

- **Pliki:** definicje w `data/zwiadowcy/cutscenes/*.json`, przykład:

  ```json
  {
    "location": "budowa_mostu",
    "lock_player": true,
    "keyframes": [
      {"t": 0,   "pos": [0, 40, -30], "look": [0, 10, 0], "fov": 70},
      {"t": 120, "pos": [10, 25, -5], "look": [0, 5, 10]}
    ],
    "subtitles": [{"t": 20, "text": "Most był prawie gotowy."}],
    "events": [{"t": 60, "action": {"type": "fire_event", "id": "kolumna_rusza"}}]
  }
  ```

- **Odtwarzanie:**
  - Kamera to byt tylko po stronie klienta, a współrzędne są względne wobec lokacji (podobnie jak w `PhotoTour`).
  - Między klatkami kluczowymi ruch jest gładki (krzywe Catmulla-Roma).
  - Napisy pojawiają się na dole, nad czarnymi pasami.
- **Gracz:** w czasie przerywnika jest nietykalny, a wrogowie w promieniu 32 bloków zamierają.
- **Pomijanie:** Esc pomija przerywnik, ale zdarzenia z osi czasu i tak się wykonują.
- **Lista przerywników:**
  - Wyjazd z Redmontu,
  - Rozpadlina (z przełęczy),
  - Evanlyn,
  - Most nad Rozpadliną,
  - Płonący most,
  - Upadek Morgaratha,
  - Niewola,
  - 2 krótkie w bitwie (szyki, wyłom).

### 9.15 Niewola i powrót do wolnej gry

- **Przechowanie ekwipunku:** przy powaleniu w M16 cały ekwipunek trafia do wirtualnej skrzyni (dane gracza), a Will
  dostaje zgrzebne ubranie.
- **Po ekranie końca tomu II:** gracz budzi się przy chacie Halta w Araluenie z całym ekwipunkiem. Pergamin wyjaśnia,
  że to wolna gra poza kanonem: „Ta opowieść trwa dalej w tomie III. Do tego czasu Araluen jest twój.”

---

## 10. Przeciwnicy i postacie

### 10.1 Nowi wrogowie (wartości dla trybu „Zwiadowca”)

| Wróg | HP | Atak | Zachowanie | Łup |
|---|---|---|---|---|
| **Wargal-kusznik** | 24 | Bełt: **2× obrażenia strzały zwiadowcy** przy pełnym naciągu (ok. 12 przed zbroją) | **Naciąg 28 tików, 2× dłużej niż łuk zwiadowcy (14).** Celność i lot pocisku jak u strzały zwiadowcy. Zapowiedź: zgrzyt korby i błysk grotu 0,5 s przed strzałem. Trzyma dystans 12-20 bloków i po strzale chowa się za osłoną | Czasem zapiski |
| **Nadzorca z biczem** | 34 | Bicz z zasięgiem 4 bloków: 4 obrażeń i Spowolnienie II na 3 s | Pilnuje jeńców. Widząc gracza, krzyczy (podejrzenie dla pobliskich) | Klucze nadzorcy (w M10) |
| **Wargal-dobosz** | 20 | Słaby cios | Na widok gracza 3 s rozbiega się do bębna, potem alarm w promieniu 40 bloków. Trafienie w czasie rozbiegu przerywa go | - |
| **Jeździec Morgaratha** (mini-boss) | 120 (pasek) | Kopia w szarży 10, miecz 7 | Faza konna: szarże z zapowiedzią, zawracanie. Przy 50% HP koń ucieka, a Jeździec walczy pieszo zapowiadanymi ciosami (parowanie, Horace) | Rozkazy Morgaratha |
| **Skandyjski wojownik** | 40 | Topór 8 | Okrągła tarcza blokuje pociski z przodu (stożek 120°). Obraca się wolno, więc trzeba zajść go z boku | Czasem srebrna moneta |
| **Skandyjski berserker** | 50 | 10 | Poniżej 50% HP szał: +40% prędkości, odporność na odrzut | - |

Wargale z tomu I (zwiadowca, wojownik, oszczepnik, herszt) zostają, a w tomie II dostają kolczugi.

### 10.2 Postacie

| Postać | Rola | Nowe/zmienione |
|---|---|---|
| Gilan | Towarzysz w aktach I-II, potem w bitwie | rozbudowane dialogi, tryb towarzysza konno |
| Horace | Towarzysz przez cały tom, tarcza w pojedynku | nowy strój (kolczuga, tarcza Redmontu) |
| Evanlyn | Towarzyszka od M6 | nowa skórka, pierścień jako wskazówka |
| Halt | Początek, obóz królewski, bitwa | dialogi tomu II |
| Crowley, król Duncan, sir Rodney | Redmont i obóz | dialogi tomu II |
| Dafydd, Rhys, Morwenna | Celtowie | nowe skórki |
| Celtyccy górnicy (8) | Jeńcy w M10 | wspólna skórka z losowymi wariantami |
| Żołnierze Araluenu | Bitwa | piechota (miecz, tarcza) i łucznicy, 3 warianty |
| Erak | Jarl Skandian, postać fabularna | nowa skórka (rogaty hełm, futro), nie da się go pokonać |
| Morgarath | Boss | nowy model (2,3 bloku wzrostu, czarna zbroja, blada twarz), czarny rumak |

### 10.3 Morgarath, Władca Deszczu i Nocy

- **Zdrowie:** 400 HP („Zwiadowca”), jeden pasek z podziałką faz.
- **Odporności:**
  - zwykłe strzały i noże zadają 50% obrażeń (zbroja),
  - słaby punkt ×3,
  - ogień ×2, a podpalenie po oblaniu olejem zadaje obrażenia przez 4 s.

**Faza 1, konno (100% → 70%):**
- Morgarath na czarnym rumaku szarżuje z mieczem.
- Zapowiedź szarży: koń staje dęba i rży, potem 1,5 s rozbiegu. Unik to zryw Wyrwija w bok albo ruch w poprzek toru.
- Gracz strzela z siodła tylko w Morgaratha. Konia nie da się zranić, strzały się od niego odbijają.
- Przy 70% (85% po misji S5) rumak się potyka, Morgarath spada i koń ucieka. Krótki przerywnik bez kamery.

**Faza 2, pieszo z Horace'em (70% → 30%):**
- Horace (tryb DUEL) wchodzi między Willa a Morgaratha.
- Morgarath zadaje zapowiadane ciosy:

  | Cios | Zapowiedź | Jak się bronić |
  |---|---|---|
  | Cięcie z góry | 0,8 s | Unik w bok albo blok tarczą Horace'a |
  | Cięcie poziome po łuku | 0,9 s | Odskok w tył |
  | Pchnięcie | 0,6 s | Parowanie mieczem możliwe |

- **Słaby punkt:** po każdym ciosie zablokowanym przez Horace'a albo wymanewrowanym przez Willa na 1,5 s świeci słaby punkt
  (szczelina pod pachą albo przy kolanie). Trafienie w niego zadaje ×3 obrażenia.
- **Olej:** oblanie i płonąca strzała dają podpalenie. Morgarath wpada wtedy w gniew i przez 4 s atakuje szybciej.
- **Horace:** po 6 przyjętych ciosach przyklęka na 5 s. W tym czasie Morgarath atakuje Willa.

**Faza 3, szał (30% → 0):**
- Przerywnik bez kamery: Morgarath ogłusza Horace'a uderzeniem tarczą.
- Ciosy są o 30% szybsze i dochodzą podstępy:
  - **piasek w oczy:** Ślepota na 2 s, zapowiedziana schyleniem się po garść ziemi,
  - **udawany upadek:** gdy gracz podejdzie bliżej niż 3 bloki, Morgarath kontruje z ziemi.
- Przy zaufaniu Horace'a ≥ 70 Horace raz wstaje i przyjmuje cios, który zabiłby Willa.
- **Koniec:** przy 0 HP przerywnik „Upadek Morgaratha”. Wrogowie wokół uciekają.

---

## 11. Przedmioty

| Id | Nazwa | Działanie | Skąd |
|---|---|---|---|
| `miecz_zwiadowcy` | Miecz zwiadowcy | 5 obrażeń, szybkość 1,8, parowanie (okno 6 tików) | M2, przepis po M2 |
| `miecz_cwiczebny` | Drewniany miecz ćwiczebny | Do sparingu w M2 | M2 |
| `linka_z_kotwiczka` | Linka z kotwiczką | Rozdział 9.6 | M3, przepis |
| `garnek_z_dymem` | Garnek z dymem | Rozdział 9.8 | M5, przepis |
| `mapa_gilana` | Mapa Gilana | Mapa i szybka podróż | M7 |
| `kamyk` | Kamyk | Rzucany, odciąga uwagę (8 bloków) | ze żwiru (1 żwir → 4 kamyki) |
| `luneta` | luneta vanilla | Oznaczanie celów | M7 (od Gilana) |
| `ognisko_zwiadowcow` | Ognisko zwiadowców (blok) | Punkt szybkiej podróży | tylko w lokacjach |
| `klucze_nadzorcy` | Klucze nadzorcy | Otwierają kłódki zagród | M10 |
| `rozkazy_morgaratha` | Rozkazy Morgaratha | PPM czyta pergamin | M8 |
| `pierscien_evanlyn` | Pierścień z herbem | Opis zawiera wskazówkę (lew i korona) | pokazywany w M6, wraca do Evanlyn |
| `pas_gilana` | Pas na noże od Gilana | +4 miejsca na noże w kołczanie | M13 (zaufanie ≥ 70) |
| `plonaca_belka`, `zweglona_belka`, `oblane_drewno` | Bloki mostu | Skrypt spalenia | tylko w strukturze |
| `beben_wargali` | Bęben (dekoracja) | Bez działania | posterunki |
| Zapiski tomu II | 12 nowych | Listy górników, rozkazy Wargali, skandyjski dziennik pokładowy | skrzynie w lokacjach |

---

## 12. Teksty fabularne i przerywniki

| Tekst | Stron | Treść |
|---|---|---|
| Prolog tomu II | 5 | 1. Po Gorlanie (wariant rycerz/zwiadowca, a przy szybkim starcie streszczenie tomu I). 2. Morgarath zbiera armię. 3. Jedyna droga z płaskowyżu: Przełęcz Trzech Stopni i armia pod Uthal. 4. Celtyka, dawny sojusznik. 5. Wezwanie |
| Koniec aktu I: „Za górami” | 1 | Pierwszy widok Celtyki |
| Koniec aktu II: „Most” | 2 | Treść rozkazów i decyzja drużyny |
| Przejście: „Dym nad Rozpadliną” | 1 | Po spaleniu mostu |
| „Niewola” | 1 | Przebudzenie na pokładzie |
| Epilog tomu II | 6 | 1. Most, który nie przetrwał. 2. Bitwa oczami Halta. 3. Upadek Morgaratha. 4. Wieść dla króla: Evanlyn to Cassandra, żyje i jest w niewoli. 5. Na pokładzie wilczego okrętu. 6. Przysięga Halta |
| Ekran „Koniec tomu I” | 1 | Rozdział 4.1 |
| Ekran „Koniec tomu II” | 1 | Podsumowanie, napisy końcowe, „Kontynuuj do części trzeciej” (przycisk nieaktywny, „wkrótce”), informacja o wolnej grze |

**Dialogi:**
- **Nowe pliki:** evanlyn, erak, dafydd, rhys, morwenna, zolnierz, kowal_obozowy.
- **Rozbudowane:** halt, gilan, horace, crowley, krol_duncan, sir_rodney (tematy tomu II pod warunkiem `tom2`).
- **Szacunek:** ok. 350-450 węzłów dialogowych.

---

## 13. Świat: generator terenu, biomy, klimat

### 13.1 Maski regionów

- **Nowa funkcja gęstości `zwiadowcy:region`:** zwraca płynną maskę 0-1 dla figury (elipsa albo kapsuła wzdłuż odcinka)
  z miękką krawędzią. Działa na tej samej zasadzie co `odleglosc_od_srodka`.
- **Maski:**

  | Maska | Kształt | Działanie |
  |---|---|---|
  | `celtyka` | Elipsa (-1900, 400), promienie 600×450, krawędź 200 | Łagodne wzgórza i wrzosowiska zamiast pełnych gór: osobne wartości kontynentalności i erozji, jak strefa `CALM_*`, ale wyżej (teren y 80-115) |
  | `przelecz` | Kapsuła (-1050, 100) → (-1450, 250), szerokość 70 | Obniża grzbiet gór do siodła na wysokości ok. 120-130 |
  | `uthal` | Elipsa (450, 1350), promienie 260×200 | Spłaszcza teren pod bitwę (y ok. 75) |

### 13.2 Rozpadlina

- **Nowa funkcja gęstości `zwiadowcy:rozpadlina`:** łamana z 6-8 punktów od (-1650, 1000) do (-950, 1300).
  - Szerokość zmienia się szumem od 30 do 50 bloków, a ściany są poszarpane drugim szumem i mają półki.
  - Wynik łączymy z `final_density` przez minimum, czyli wycinamy powietrze.
- **Dno na wysokości ok. y 55, czyli nad poziomem wody (50).** Dzięki temu warstwy wodonośne nie zaleją kanionu.
  Po dnie płynie wąski strumień (własna cecha świata: kanał szeroki na 2 bloki).
- **Przy moście** kanion ma ok. 40 bloków szerokości. Szablon mostu dopasowuje długość do zmierzonej szerokości (od 38 do 50).
- **Końce kanionu** wchodzą w pełne Góry Deszczu i Nocy. Obejście jest możliwe, ale wymaga długiej wspinaczki.

### 13.3 Biomy

- **Sposób:** biomy Celtyki dostajemy podmianą parametru temperatury w generatorze szumu:
  `temperature = lerp(oryginał, 0.97, maska_celtyka)`. Nowe biomy mają zakres temperatury 0,95-1,0, którego poza Celtyką
  nigdy nie ma. Pozostałe biomy Araluenu się nie zmieniają.
- **Biomy:**

  | Biom | Wygląd | Uwagi |
  |---|---|---|
  | **Wrzosowiska Celtyki** | Fioletowo-brązowa trawa, głazy, rzadkie krzewy | Owce, rzadkie wilki |
  | **Mgliste doliny** | Ciemnozielone, mech, olchy | Gęsta mgła |
  | **Łupkowe wzgórza** | Szary łupek (głęboki łupek i tuf na zboczach), karłowate sosny | Okolice kopalni |
  | **Równina Uthal** | Zielona, płaska | Wariant łąk |

### 13.4 Klimat

- **Mgła:** po stronie klienta w biomach Celtyki widoczność zaczyna się ograniczać od 20% zasięgu renderowania, a w Mglistych
  dolinach od 8%. Mgła ma szaro-niebieski odcień.
- **Deszcz:** pogoda jest globalna dla całego wymiaru, więc gdy gracz przebywa w Celtyce, mod (gra jednoosobowa) zwiększa
  szansę deszczu do ok. 60% czasu. Po wyjeździe pogoda wraca do normy.
- **Puste osady:** skrzypiące okiennice (dźwięki klap i drzwi vanilla, losowo co 20-60 s), wiatr, brak zwierząt w promieniu
  48 bloków.
- **Architektura celtycka:**
  - okrągłe kamienne chaty kryte strzechą,
  - kamienne kręgi i kurhany,
  - celtyckie krzyże,
  - górnicze wyciągi i wózki na torach (blok torów z tomu I).

### 13.5 Istniejące światy z wersji 1.x

- **Problem:** teren generuje się z funkcji gęstości zależnych od współrzędnych. Jeśli gracz w starym świecie odkrył już
  obszar Rozpadliny albo Celtyki, kanion i biomy urwą się na granicy starych chunków.
- **Rozwiązanie:** przy pierwszym uruchomieniu 2.0 mod sprawdza pliki regionów:
  - jeśli wszystkie potrzebne chunki są nowe, zostaje układ domyślny,
  - jeśli nie, cały układ tomu II (Celtyka, przełęcz, Rozpadlina, Uthal) obraca się wokół Redmontu co 30°, aż trafi
    na nieodkryty teren.
- **Zapis i odczyt:** wybrany kąt trafia do `LocationData`. Funkcje gęstości czytają go ze statycznego miejsca ustawianego
  przed generowaniem pierwszego chunka.
- **Ostatnia deska ratunku:** komunikat i zalecenie nowego świata.

---

## 14. Architektura i zmiany w kodzie

### 14.1 Nowe pakiety i klasy

| Pakiet | Klasy |
|---|---|
| `volume` | `Volumes` (stan tomów, flagi, szybki start), `EndOfVolumeScreen` (klient) |
| `companion` | `CompanionBrain` (konie, szyk, role, rany), `CommandWheelScreen`, `Trust` (licznik i progi) |
| `stealth` | `Alertness` (stany czujności), `VisionCone`, `PatrolRoute`, `Takedown`, `Distraction` (rozbudowa `Stealth`) |
| `tracking` | `TrackerSense` (Oczy tropiciela), `Clues`, `DeductionScreen` |
| `combat` | `Parry`, `TelegraphedAttack` (wspólny dla bossów) |
| `travel` | `Campfires`, `FastTravel`, `MapScreen` |
| `skill` | `Skill` (enum), `Skills` (stan gracza), zakładka dziennika |
| `difficulty` | `StoryDifficulty` (mnożniki, ustawienie) |
| `cutscene` | `CutsceneDefinition`, `CutsceneManager` (wczytuje JSON), `CutscenePlayer` (klient), `CutscenePacket` |
| `battle` | `BattleDirector`, `CrowdDummyEntity` |
| `bridge` | `BridgeBurn` (skrypt i stan), bloki mostu |
| `entity` | `MorgarathEntity`, `BlackSteedEntity`, `MorgarathRiderEntity`, `SkandianEntity` (warianty), `SoldierEntity`. W `WargalEntity` nowe warianty: KUSZNIK, NADZORCA, DOBOSZ |
| `world` | `RegionMask` (DF), `FissureDensity` (DF), `VolumeTwoLayout` (obrót układu), nowe wpisy w `StoryLocation` |
| `client` | `CelticFog` (mgła), modele: `MorgarathModel`, `SkandianModel`, warianty `WargalModel` (kusza, bicz, bęben) |

### 14.2 Silnik misji

**Nowe typy celów:**

| Typ | Pola | Znaczenie |
|---|---|---|
| `ride_with` | `location`, `radius`, `npcs` | Dotrzyj na miejsce razem z towarzyszami |
| `parry` / `counter` | `count`, `npc` | Udane parowania i kontry |
| `clues` | `anchors`, `required` | Zbierz wskazówki |
| `deduce` | `options`, `correct` | Wybór wniosku w dzienniku |
| `follow_trail` | `anchors` | Przejdź trop |
| `escape` | `to`, `seconds` | Ucieczka na czas (bez porażki) |
| `mark` | `entity`, `count`, `tagged` | Oznacz cele lunetą |
| `free` | `anchors` | Uwolnij jeńców (kłódki) |
| `oil_points` / `ignite_points` | `anchors`, `required` | Polej olejem / podpal |
| `defend` | `npcs` albo `area`, `seconds`, `min_alive`, `waves` | Obrona |
| `boss` | `entity`, `phase` | Faza bossa |
| `saddle_hits` | `count` | Trafienia z konia |
| `heal` | `anchors` | Opatrz rannych |
| `survive` | `seconds`, `until_health` | Przetrwaj (fabularne powalenie) |

**Nowe akcje:**
- `cutscene`,
- `companion_join` / `companion_leave` / `companion_order`,
- `trust` (npc, delta),
- `skill`,
- `fast_travel` (lock/unlock),
- `weather`,
- `bridge_burn`,
- `battle_phase`,
- `capture` (przechowanie ekwipunku i teleport na okręt),
- `restore_inventory`,
- `end_volume` (ekran końca tomu),
- `start_volume`,
- `checkpoint`.

**Pola i warunki misji:**
- `"tom": 2` grupuje misje w dzienniku,
- `"no_fast_travel": true` blokuje szybką podróż w etapie,
- warunek `trust_at_least` w dialogach i akcjach,
- warunek `difficulty`.

### 14.3 Dane gracza i zgodność

- **Wersja danych:** `QuestCapability` dostaje pole `dataVersion`. Migracja 1 → 2 dopisuje stan tomów: tom I ukończony
  albo nie, oraz flagę ekranu końca tomu.
- **Nowe dane:** zaufanie, umiejętności, odkryte ogniska, ustawienie trudności, przechowany ekwipunek (niewola).
  Wszystko zapisuje się w NBT gracza.
- **Zlecenia Halta z tomu I:** od M1 do M16 są niedostępne, bo Halt jest przy armii. Wracają po tomie II.

### 14.4 Generatory zasobów (`tools/assetgen`)

| Generator | Co dochodzi |
|---|---|
| `TextureGen` | Kusza, bicz, bęben, miecz zwiadowcy, kotwiczka, garnek z dymem, kamyk, mapa (tekstura 512×512), bloki mostu, ognisko zwiadowców, biomowe kolory trawy i liści |
| `SkinGen` | Evanlyn, Erak, Dafydd, Rhys, Morwenna, górnicy, żołnierze (3), Skandianie (wojownik, berserker), Morgarath, Horace w kolczudze |
| `StructureGen` | 13 lokacji z rozdziału 6.2 i szablon mostu dopasowany do szerokości |
| `RouteStopGen` | 4 postoje |
| `BiomeGen` | Maski, Rozpadlina, 4 biomy, zmiana parametru temperatury |
| `MobGen` | Modele i animacje nowych bytów |
| `PromoGen` | Baner tomu II |

---

## 15. Wydajność

| Element | Budżet | Sposób |
|---|---|---|
| Bitwa | 60 kl./s na średnim PC | maks. 40 żywych NPC, 160 manekinów bez AI i bez kolizji, sztandary jako bloki |
| Spalenie mostu | ≤ 2 ms na tik | ogień jako własny blok bez losowego rozprzestrzeniania, spadające bloki w partiach |
| Funkcje gęstości | +10% czasu generowania chunka | maski z szybkim odrzutem poza prostokątem obejmującym, Rozpadlina liczona tylko w swoim pasie |
| Towarzysze | 3-4 byty | ścieżki co 10 tików, przeniesienie zamiast długiego szukania drogi |
| Skradanie 2.0 | stożek widzenia tylko dla wrogów do 32 bloków od gracza | sprawdzanie co 4 tiki, rzut promienia tylko po pozytywnym teście kąta |
| Mgła i deszcz | bez kosztu serwera | mgła po stronie klienta |

---

## 16. Testy

### 16.1 Autotest (`-Pselftest=true`)

**Świat:**
- Rozpadlina istnieje: wymiary w 5 punktach (szerokość 30-50, dno nad y 50, brak wody poza strumieniem).
- Biomy Celtyki pojawiają się w masce, a poza nią nie.
- Teren w Uthal: rozrzut wysokości ≤ 6 w promieniu 120.
- Przełęcz da się przejść: wysokość siodła ≤ 135.

**Lokacje:**
- Wszystkie 13 lokacji staje: czas, kotwice, brak kolizji z wodą.
- Most: szerokość kanionu w miejscu mostu i dopasowanie szablonu.

**Skrypty:**
- Skrypt spalenia mostu w trybie przyspieszonym: kończy się, zostawia kikuty i nie zostawia ognia poza mostem.
- Bitwa: limity bytów w każdej fazie.

**Dane:**
- Każda kotwica użyta w misjach istnieje w szablonach.
- Każdy przerywnik ma poprawne klatki.
- Każdy dialog odsyła do istniejących węzłów.
- Migracja zapisu z 1.0.2 (przykładowy plik gracza w testach).

### 16.2 Scenariusze ręczne

1. Świat 1.0.2 po epilogu → aktualizacja → ekran końca tomu → „Kontynuuj” → M1.
2. Nowy świat → „Zacznij od tomu II” → zestaw startowy → M1.
3. Całe M10 bez alarmu i z alarmem: oba warianty da się ukończyć.
4. M11 z S1 i bez S1 (3 albo 4 punkty oleju).
5. Pojedynek na trzech poziomach trudności i z zaufaniem Horace'a poniżej i powyżej 70.
6. Zapis i wczytanie w trakcie spalenia mostu, w trakcie bitwy i w trakcie przerywnika.
7. Szybka podróż: blokady w M9-M12 i M14-M16.
8. Wolna gra po tomie II: zwrot ekwipunku, zlecenia Gilana i Halta.
9. Wydajność bitwy na słabszym komputerze (render 8, symulacja 6).

### 16.3 Zrzuty i promocja

- `PhotoTour` dostaje trasę tomu II: Celtyka, kopalnia, Evanlyn, most w ogniu, bitwa, Morgarath, okręt.
- Galeria na GitHub Pages dostaje sekcję „Tom II”.

---

## 17. Etapy prac i wydania

| Etap | Zawartość | Wydanie |
|---|---|---|
| **E0. Fundamenty** | Podział na tomy, ekran końca tomu I, szybki start, maski regionów, Rozpadlina, biomy Celtyki, ogniska i szybka podróż, mapa, towarzysze konno i koło rozkazów, zaufanie, umiejętności, trudność, przerywniki, nowe typy celów | - (testy wewnętrzne) |
| **E1. Akt I i II** | M1-M8, S1-S3, postoje 1-2, lokacje zachodnie, kusznik, dobosz, nadzorca, Jeździec, parowanie, kotwiczka, garnek z dymem, Oczy tropiciela, skradanie 2.0 | **2.0-beta1** |
| **E2. Most** | M9-M11, budowa mostu, skrypt spalenia, przerywniki, pergaminy | **2.0-beta2** |
| **E3. Wojna i niewola** | M12-M16, S4-S5, postoje 3-4, obóz królewski, bitwa, Morgarath, Skandianie, Erak, okręt, epilog, ekran końca tomu II, wolna gra, zlecenia | **2.0-beta3** |
| **E4. Szlif** | Balans trudności, poprawki z testów, README (instalacja, tom II, nowe sterowanie), galeria, opis na strony modów, wiadomość na Discorda | **2.0** |

**Wersje testowe:**
- Wychodzą jako GitHub pre-release z dopiskiem „wersja testowa, nie dla wszystkich” i ostrzeżeniem, że zapisy mogą się popsuć.
- Do każdej bety dołączona jest krótka lista „co sprawdzić” dla testerów.

**Nowe klawisze (do README):**

| Klawisz | Działanie |
|---|---|
| G | Koło rozkazów |
| T | Oczy tropiciela |
| PPM z mieczem lub saksą | Garda i parowanie |
| Sprint w siodle | Zryw |

---

## 18. Ryzyka

| Ryzyko | Skutek | Zabezpieczenie |
|---|---|---|
| Rozpadlina w starych światach (rozdział 13.5) | kanion urwany na granicy chunków | obrót układu, test w autoteście, zalecenie nowego świata |
| AI towarzyszy konno w lesie i górach | gubią się, blokują | przeniesienie za gracza, strefy „pieszo”, rzadsze drzewa przy trasach (jak w tomie I) |
| Skrypt mostu po wczytaniu gry | most w połowie spalony | zapis stanu i natychmiastowe dokończenie |
| Wydajność bitwy | spadki klatek | limity bytów, manekiny bez AI, test na słabszym PC |
| Pojedynek za trudny lub za łatwy | frustracja albo nuda | 3 tryby trudności, punkty kontrolne po fazach, testerzy bet |
| Zgodność nazw z polskim wydaniem | błędy dla fanów | tabela w rozdziale 3 do sprawdzenia przed betą 1 |
| Rozmiar pracy (najwięcej: skradanie 2.0, towarzysze, bitwa, most) | opóźnienia | etapy E0-E4 z betami po drodze, zakres misji pobocznych do ewentualnego przycięcia w E4 |
