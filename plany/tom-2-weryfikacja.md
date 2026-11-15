# Tom II - weryfikacja założeń planu

Dziennik zgodności implementacji z planem `tom-2-plonacy-most.md`. Każdy krok implementacji kończy się sprawdzeniem
założeń, których dotyczy (kompilacja, autotest, test w grze), i wpisem poniżej.

Status: ✅ zgodne i sprawdzone · 🟡 zrobione, sprawdzone częściowo · ⛔ odstępstwo (z uzasadnieniem) · ⬜ jeszcze nie

## Korekty planu w trakcie prac

| Założenie | Zmiana | Powód |
|---|---|---|
| Oczy tropiciela pod klawiszem **T** | klawisz **Z** | T w Minecrafcie otwiera czat |
| Szybki start: drugie kliknięcie „Na pewno?” w tym samym przycisku | osobny ekran potwierdzenia (co się pomija, czego nie da się cofnąć) z drogą powrotu do tomu I; po wyborze tomu I czat i napis prowadzą krok po kroku, a Kronika trafia do ręki | prośba gracza w trakcie prac (2026-11-14) |
| Most: pomost ok. 46 bloków | 58-75 bloków, zależnie od świata | most dopasowuje się do faktycznej szerokości kanionu w miejscu mostu |
| Zastrzały 9 bloków od brzegu | miejsce szukane tak, żeby zastrzał miał min. 6 belek | przy łagodnej ścianie kanionu zastrzał od razu trafiał w ziemię |
| Wilczy okręt jako osobna lokacja `wilczy_okret` | okręt w szablonie `brzeg_rzeki` (kotwice `okret_*`) | jedna lokacja na rozlewisku; kotwice jak w planie |

## Dziennik kroków

(uzupełniany w trakcie)

### E0.1 Tomy, koniec tomu I, szybki start (kod)
- ✅ Jeden mod 2.0: tom II w tym samym jarze, pole `"tom"` w misjach, flagi tomów w danych gracza (decyzja 1).
- 🟡 Ekran „Koniec tomu I” po epilogu (raz), dla starych zapisów raz przy logowaniu, przycisk „Kontynuuj do części drugiej”,
  klikalne przypomnienie na czacie `/zwiadowcy_tom kontynuuj` (decyzje 2-4). Kompiluje się; test w grze w E4.
- 🟡 Szybki start z ekranu Kroniki w nowym świecie: zestaw ucznia (łuk, kołczan 48/8/4/4, saksa, 6 noży, płaszcz,
  zestaw tropiciela, 3 flasze, gwizdek, 12 racji, 4 kawy, liść dębu), oswojony Wyrwij przy chacie, streszczenie tomu I.
- ✅ Wariant prologu: zwiadowca / rycerz / szybki start; Gilan +5 zaufania dla zwiadowcy (decyzja 5).

### E0.2 Świat tomu II (autotest `-Pswiat=true`, nowy świat)
- ✅ Rozpadlina w generatorze terenu: 6 pomiarów - dno y=51-54 (nad wodą 50), szerokość dna 38-48, głębokość 55-71,
  brak cieczy w kanionie. Plan mówił „głęboki na ok. 85” - jest 55-71, bo dno nie może zejść pod poziom wody 50
  (inaczej warstwy wodonośne zalałyby kanion); pogranicze podniesione o ~38 bloków, żeby kanion był głęboki.
- ✅ Celtyka: 25/25 próbek w biomach celtyckich, teren y=62-136 (średnio 94) - pagórki i wrzosowiska.
- ✅ Równina Uthal: biom `rownina_uthal`, rozrzut wysokości 6 w promieniu 120 (plan: ≤ 6).
- ✅ Przełęcz Wiatrów do y=115 (plan: ≤ 135), trakt wschodni do y=119 (plan: ≤ 140).
- ✅ Zatyczka dna: jaskinie nie dziurawią dna kanionu (wcześniej dziura do y=36).
- 🟡 Obrót układu w starych światach (sprawdzanie zapisanych chunków) - zaimplementowany; test na kopii starego świata w E4.
- ⛔ Strumień na dnie Rozpadliny (plan 13.2) - odłożony; dno jest suche (trawa/kamień). Do decyzji w E4.

### E0.3 Mechaniki (kod skompilowany, serwer startuje, 291 plików JSON poprawnych)
- ✅ Ogniska zwiadowców i szybka podróż (9.7): odkrycie do 8 bloków, ogniska tomu I na starcie tomu II, koszt 1 racja,
  +1 h gry / 500 bloków, Wyrwij i towarzysze jadą razem; blokady: walka (8 s), alarm w pobliżu, etap `no_fast_travel`.
- ✅ Mapa Gilana: rysowana z układu krain (z obrotem świata), „?” nieodkryte, kropki odwiedzone, ogniska klikalne.
- ✅ Towarzysze (9.1): własne konie w szyku (4-6 bloków), zsiadanie w strefach „pieszo”, przeniesienie > 64 bloki poza
  wzrokiem; Horace - tarcza 30% obrażeń z przodu + prowokacja co 15 s; Gilan - łuk i miecz z bliska; Evanlyn - kamienie
  (ogłuszenie, co 8 s); Halt - szybki łucznik; ranni wstają po walce (Weteran: po opatrzeniu racją); kucanie przy skradaniu,
  bez inicjowania walki podczas skradania.
- ✅ Koło rozkazów G: Za mną / Czekaj tu / Ukryj się (krzaki do 8 bloków) / Odciągnij uwagę (Horace); celownik na
  towarzyszu = rozkaz tylko dla niego; zdarzenia `rozkaz_*` dla misji.
- ✅ Zaufanie (9.2): start 50/60/40, ±z dialogów, porzucenie rannego -5, ratunek +2 (maks. 3 na misję), progi 70/30,
  opisy bez liczb w zakładce „Drużyna”.
- ✅ Skradanie 2.0 (9.3): trasy patroli z kotwic `trasa_X_n`, stożek 110°/24 (dzień), 80°/12 (+8 w świetle) w nocy,
  modyfikatory widoczności z tomu I, 3 stany + pasek, sprawdzanie ostatniego miejsca 20 s, alarm: okrzyk 12 / bęben 40
  bloków po 3 s rozbiegu (trafienie przerywa), opadanie po 90 s, ogłuszanie od tyłu (liczy się jak pokonanie),
  wartownik widzący ogłuszonego → podejrzenie, kamyk (8) i gwiżdżąca strzała (16), ikony ?, !, zz nad głowami.
- ✅ Oczy tropiciela (9.4, klawisz **Z**): zasięg 16/24, wskazówki, trop, czerwone świeże ślady (60 s), spowolnienie,
  PPM na wskazówce → notatka, zakładka „Wnioski” z wyborem 1 z 3 (pomyłka = podpowiedź, trafienie za 1. razem = akcje).
- ✅ Parowanie (9.5): okno miecz 6 / saksa 4 tiki (+2 Lekcja Gilana, +1 rycerz), 0 obrażeń + zatoczenie 1 s + kontra ×2,
  spóźniona garda 60%, od tyłu nic.
- ✅ Linka z kotwiczką (9.6): 20 bloków, ~1 s, odnowienie 8/5 s, blokada w walce (3 s), tag `bez_kotwiczki`, 120 użyć.
- ✅ Garnek z dymem (9.8): promień 4, 6 s, utrata celu, gracz w dymie niewidoczny, przepis glina+trawa+olej → 2.
- ✅ Wyrwij (9.9): rozrzut z siodła ×2/×1,3/×1 (Jeździec: połowa kary), gwizdek 100/150 bloków, komendy w rozmowie
  z koniem (zostań/dom/czuwaj), zryw 3 s +60% co 30/20 s (klawisz sprintu w siodle).
- ✅ Umiejętności (9.10): 10 odznak + zakładka w dzienniku (zdobyte / zablokowane ze źródłem).
- ✅ Trudność (9.11): Opowieść / Zwiadowca / Weteran - obrażenia i zdrowie wrogów tomu II, podejrzenie, zapowiedzi,
  ranni towarzysze; zmiana w dzienniku.
- ✅ Przerywniki (9.14): JSON z klatkami względem lokacji lub mostu, Catmull-Rom, pasy kinowe, napisy, Esc pomija,
  zdarzenia osi czasu wykonuje serwer, nietykalność gracza i zamrożenie wrogów w 32 blokach.
- ✅ Klimat (13.4): mgła 20% / 8% (Mgliste doliny), deszcz ok. 60% czasu w Celtyce, przestaje po wyjeździe.
- ✅ Wargal-kusznik (pomysł gracza): naciąg 28 tików (2× łuk zwiadowcy 14), bełt 2× strzała zwiadowcy
  (podstawa 5.0 przy prędkości 3.6 → ok. 18 przed zbroją; plan szacował 12 - liczymy wprost wg decyzji „2× obrażenia”),
  zgrzyt korby i błysk 0,5 s przed strzałem, dystans 12-20, po strzale odskok w bok.
- ✅ Nadzorca z biczem (4 bloki, 4 obrażeń + Spowolnienie II 3 s, okrzyk przy podejrzeniu), Wargal-dobosz (alarm 40).
- ⛔ „Pas na noże od Gilana: +4 miejsca w kołczanie” - uproszczone do prezentu z 4 nożami do rzucania (kołczan ma
  stałe 4 przegródki na strzały; osobny slot na noże wymagałby przebudowy kołczana).

### E1 Akt I-II (selftest na świeżym świecie: 34 misje, 30 plików dialogów)
- ✅ Lokacje aktów I-II stoją w założonych biomach (6.2): obóz na trakcie (brzozowy gaj), Przełęcz Wiatrów, Tre-Gwyn,
  Kopalnia, Dolina Mgieł, szałas pasterski, wrzosowisko (wrzosowiska Celtyki), posterunek dobosza (łupkowe wzgórza).
- ✅ Odległość od położeń nominalnych: każda lokacja w swoim promieniu (Tre-Gwyn przesunięte o ok. 80 bl. na północny wschód,
  bo w nominalnym punkcie nie było płaskiego terenu; kopalnia ok. 33 bl.).
- ✅ Kotwice: brak zasypanych i wiszących (poza kotwicami-blokami wrót posterunku `brama_1-4`, które leżą w bramie
  celowo; selftest pomija je od teraz).
- ✅ Postoje tomu II leżą przy trasie: wóz z prowiantem 43 bl., dezerterzy 16 bl., uciekinierzy 0 bl., wargalski zwiad 11 bl.
- ✅ Misje M1-M8 i S1-S3: wszystkie odwołania do lokacji, przedmiotów, postaci i misji poprawne. Jedyny błąd dotyczy
  misji z E2 (`na_krawedzi`).
- ✅ Teren po dodaniu struktur bez zmian: Rozpadlina ma dno y=51-54 i głębokość 58-67, Celtyka 25/25, Uthal różnica 6.
- 🛠️ Poprawka: `Cutscenes` miało złą kolejność pól statycznych, przez co serwer padał przy wczytywaniu danych.
  Stary raport `selftest.txt` udawał wtedy sukces, więc skrypt testu usuwa go teraz przed uruchomieniem.
- ⏳ Do E2/E3: struktury krawedz, budowa_mostu, oboz_krolewski, rownina_uthal, brzeg_rzeki; dialogi postaci z aktu III.
- 🛠️ Dwa błędy E1 znalezione przy E2:
  - 12 plików postępów tomu II miało podwójnie zakodowane polskie znaki. Naprawione `iconv`; przyczyną była kompilacja
    narzędzia bez `-encoding UTF-8`.
  - Znaczniki kotwic zamieniały się przy stawianiu w powietrze, więc wrota posterunku dobosza miały dziurę na dole,
    a „otwarcie” czyściło 1 blok z 3. Teraz znacznik umie przywrócić blok (`;block=`), a `set_block` ma `height`.

### E2 Akt III cz. 1 - most (kod gotowy, selftest w toku)
- ✅ 9.12 Budowa mostu: pomost szer. 5 dopasowany do brzegów kanionu, 4 przęsła z kotwicami `przeslo_1-4`, zastrzały
  `podpora_1-2` pod pomostem przy północnej ścianie (z liną do zejścia), wieże A na obu brzegach, kotwy wieży
  `podpora_3-4`, droga dojazdowa wcięta w brzeg. Pozycje bloków są w danych świata.
- ✅ Olej (PPM z flaszą → oblane drewno), podpalenie płonącą strzałą lub krzesiwem (tylko w etapie misji),
  skrypt: ogień 0-8 s, przęsła spadają kolejno od 8 s (maks. 200 bl./tik), wargale z mostu spadają, kikuty zwęglone
  od 30 s, koniec 35 s, dym 10 min. Wczytanie gry w trakcie palenia od razu daje stan końcowy. Ogień vanilla nie jest używany.
- ✅ M9-M11 (`na_krawedzi`, `lancuchy`, `plonacy_most`): luneta ×6, narada z wyborem „odciągnij uwagę”, noc,
  zagrody z 3 kłódkami i ucieczką górników, S1 → Rhys i 3 punkty oleju zamiast 4 (`count_if_flag`), beczki smoły,
  świt z kolumną (20 manekinów + 6 żywych), przerywniki „Most nad Rozpadliną” i „Płonący most”, ucieczka, postępy.
- ✅ Manekiny tłumu (`CrowdDummyEntity`, limit 160), marsz po kotwicach, akcje `crowd`, `delay`, `time`, `npc_flee`.
- ⏳ Weryfikacja selftestem (pomiar mostu, symulacja spalenia, wrota) - wynik do sprawdzenia w run/selftest.txt.
- Wynik selftestu E2:
  - ✅ Wrota posterunku zamknięte 4/4.
  - ✅ Symulacja spalenia: polane 34 bl., z 974 bl. przęseł spadło 822, na przęsłach nie zostało nic, 122 kikuty zwęglone, żaden nie płonie.
  - ❌ Most zbudowany na y=-59 (pod spodem 0 bl. powietrza): pomiar terenu czytał niewczytane chunki. Poprawka
    (wymuszone wczytanie chunka) jest w kodzie, ale nie przeszła jeszcze testu.
  - ❌ Akcja `time` miała klucz `to`, który selftest traktuje jak lokację. Zmieniona na `pora`, czeka na test.
  - ⚠️ „Równina Uthal nie jest płaska (rozrzut 21)”: wcześniej 6-7 przy innym ziarnie świata. Do zbadania w E3.
- ✅ Selftest E2 po poprawkach (świeży świat):
  - Most: pomost y=118, 64 bl. powietrza pod środkiem. Długość 58, a plan zakładał ok. 46: most dopasowuje się
    do faktycznej szerokości kanionu (dno 38-48 + ściany), więc w tym miejscu wychodzi dłuższy.
  - Symulacja spalenia: polane 41 bl., z 645 bl. przęseł spadło 541, reszta zniknęła w zgliszczach (na przęsłach 0),
    110 kikutów zwęglonych, żaden nie płonie.
  - Wrota posterunku: 4/4 kolumn zamkniętych, po otwarciu przejście na 3 bloki.
  - M9-M11: wszystkie odwołania poprawne (zostało tylko `wyscig_z_czasem` z E3).
- 🛠️ Uthal: przy jednym ziarnie świata dziura do y=58, pewnie wejście jaskini albo staw. Dodana „zatyczka”
  w gęstości terenu (pas y≈45-74 pod równiną zawsze lity). Teraz y=74..80, czyli rozrzut 6.

### E3 Akt III cz. 2 - wojna i niewola (selftest na świeżym świecie: 49 misji, 38 dialogów, WYNIK: OK)
- ✅ Lokacje: obóz królewski, Równina Uthal (54 kotwice), rozlewisko z wilczym okrętem; wszystkie kotwice stoją
  na ziemi (po poprawce wzgórza kuszników i namiotu Willa); Uthal płaski (y=74..79).
- ✅ M12-M16 (`wyscig_z_czasem`, `noc_przed_bitwa`, `bitwa_uthal`, `wladca_deszczu_i_nocy`, `wilcze_okrety`),
  S4 `obcy_na_trakcie` (walka albo rozmowa), S5 `czarny_rumak`, postoje 3-4 i zlecenia Gilana/Morwenny:
  wszystkie odwołania do lokacji, kotwic (także list kotwic), przedmiotów, bytów, dźwięków, postaci, misji,
  przerywników i osiągnięć poprawne.
- ✅ Bitwa (9.13): limit 40 żywych w promieniu 64 bloków przy każdym spawnie misji tomu II, maks. 160 manekinów;
  autotest liczy narastająco: bitwa 100 manekinów / 13 wrogów na etap, pojedynek 64 / 1, most 20 / 6.
  Zadania A-D: kusznicy na wzgórzu z Haltem, obrona oddziału (min. 4 z 6 żołnierzy, sojusznicy giną od ciosów wrogów),
  dobosze oznaczani lunetą i zdejmowani salwą, bród ze Skandianami z Haltem i Gilanem; odgłosy bitwy w tle;
  sztandary w szykach; po śmierci etap od nowa (`restart_on_death`).
- ✅ Morgarath (10.3): 400 HP, 3 fazy (70%, po S5 85% / 30%), rumak nietykalny, zbroja ×0,5 dla zwykłych pocisków,
  ogień ×2 i gniew 4 s, słaby punkt ×3 po chybionym albo zablokowanym ciosie, 3 ciosy z zapowiedzią (z góry,
  poziomy, pchnięcie), Horace przyklęka po 6 ciosach, w fazie 3 ogłuszony tarczą, piasek w oczy i udawany upadek,
  Horace raz przyjmuje śmiertelny cios przy zaufaniu ≥ 70, ostatni cios zawsze zadaje gracz, wrogowie uciekają.
  Po śmierci pojedynek wraca do początku bieżącej fazy (`on_restart` stawia Morgaratha w tej fazie), krąg żołnierzy
  odpycha uciekającego gracza. Osiągnięcie „Bez litości” za Weterana przez cały pojedynek.
- ✅ Skandianie (10.1): wojownik 40 HP, topór 8, tarcza zatrzymuje pociski w stożku 120°, wolny obrót (9°/tik);
  berserker 50 HP, 10, poniżej 50% szał (+40% prędkości, odporność na odrzut). Erak jest nietykalny.
- ✅ Niewola (9.15): `survive` 40 s albo HP < 30% bez śmierci, ekwipunek do wirtualnej skrzyni, zgrzebne ubranie,
  okręt, rozmowy z Erakiem i Evanlyn, epilog tomu II → ekran „Koniec tomu II” → wolna gra przy chacie Halta.
- ✅ Wolna gra (8.3): zlecenia Gilana (patrol nad Rozpadliną, droga do domu) i Morwenny (wilki), raz na dobę gry;
  zlecenia Halta z tomu I wracają (tom II nie jest już „w toku”).
- ✅ Wargale w misjach tomu II noszą kolczugi (10.1). 🟡 Kolczugi nie widać na modelu Wargala (własny model bez
  warstwy zbroi) - działa tylko ochrona.
- ✅ Szybka podróż zablokowana w każdym etapie M9-M12 i M14-M16 (16.2 pkt 7).
- 🛠️ Poprawki E3: żołnierze-sojusznicy byli nieśmiertelni (postacie w spoczynku nie przyjmują obrażeń), zastrzały
  mostu przy łagodnej ścianie kanionu były puste (teraz szukane miejsce z min. 6 belkami).
- ⛔ „Czasem srebrna moneta” ze Skandian - pominięte (w modzie nie ma monet; nagrody za misje dają strzały i racje).
- ⛔ Opatrywanie rannych w M16 wymaga tylko racji (bez bandaża z zestawu tropiciela) - bandaż nie istnieje jako przedmiot.

### E4 Szlif
- ✅ Wersja 2.0.0 (`gradle.properties`, opis moda: wolna interpretacja „Ruin Gorlanu” i „Płonącego mostu”),
  paczka `gradlew paczka` zbudowana: jar 1,49 MB i zip dla graczy w `build/publikacja/`.
- ✅ README: wstęp, spis treści, sekcja „Tom II: Płonący most” (misje, poboczne, mechaniki, krainy, wolna gra),
  wybór tomu na starcie, nowe klawisze (G, Z, PPM z mieczem, sprint w siodle, mapa Gilana).
- ✅ Notatki wydania `publikacja/zmiany-2.0.0.md` (PL + EN).
- ✅ Sesja zdjęciowa tomu II (klient gry uruchomiony za zgodą gracza): 7 kadrów - płonący most (skrypt spalenia
  zapalony tak, żeby w chwili zrzutu płonął cały pomost), bitwa (szyki z manekinami i sztandarami), Morgarath na
  rumaku, Tre-Gwyn, Dolina Mgieł, obóz króla, wilczy okręt. Sekcja „Tom II” w galerii `docs/index.html` i w README.
  Poprawki sesji: kamera opadała podczas wczytywania terenu, czyszczenie zwierząt usuwało czarnego rumaka, manekiny
  w niewczytanych chunkach lądowały pod ziemią. `-Pod=12` powtarza tylko kadry tomu II. Strona na GitHub Pages
  odświeży się po wypchnięciu zmian.
- ⬜ Wersje testowe 2.0-beta (GitHub pre-release), wydanie 2.0, commit i push - tylko za zgodą gracza.
- ⬜ Scenariusze ręczne z planu 16.2 (gra w kliencie) - do zrobienia po zgodzie na uruchomienie klienta.

## Końcowe sprawdzenie z listą 36 założeń (po E0-E4)

Podstawa: ostatni pełny autotest na świeżym świecie (49 misji, 38 dialogów, 9 przerywników, wszystkie lokacje,
most ze spaleniem, bitwa), kompilacja, zbudowana paczka 2.0.0. Klienta gry nie uruchamiałem, więc to, co widać
dopiero w grze (ekrany, kamera, odczucie walki), ma 🟡 do czasu scenariuszy ręcznych z 16.2.

| # | Założenie | Status | Jak sprawdzone / uwagi |
|---|---|---|---|
| 1 | Ten sam mod, wersja 2.0, jeden jar | ✅ | `zwiadowcy-forge-1.20.1-2.0.0.jar`, `"tom": 1/2` w misjach |
| 2 | Kontynuacja po tomie I + szybki start | ✅🟡 | kod i dane; szybki start z osobnym potwierdzeniem i powrotem do tomu I (prośba gracza); test w kliencie ⬜ |
| 3 | Ekran „Koniec tomu I”, przycisk „Kontynuuj”, dziennik, czat | 🟡 | ekran, przycisk w dzienniku i klikalne przypomnienie są w kodzie; podgląd w kliencie ⬜ |
| 4 | Bez 1.0.3 | ✅ | żadnego wydania pośredniego |
| 5 | Wybór z tomu I zmienia prolog, Horace'a, Rodneya, mały bonus | ✅ | 3 prologi, kwestie Rodneya i Horace'a (`wybor_rycerz`; Horace'owi dopisane przy końcowym sprawdzeniu - brakowało), rycerz +1 tik parowania, zwiadowca +5 zaufania Gilana |
| 6 | Wolna interpretacja całego tomu | ✅ | Celtyka, kopalnia, Evanlyn, most, bitwa, Morgarath, niewola - wszystkie misje są |
| 7 | Finał: niewola u Skandian | ✅ | M16 + epilog z perspektywy króla |
| 8 | 15-17 misji w 3 aktach | ✅ | 16 misji głównych (201-216) |
| 9 | Polskie nazwy | ✅ | tabela nazw w planie, cała treść po polsku |
| 10 | Celtyka jako nowy region | ✅ | autotest: 25/25 próbek celtyckich, 4 nowe biomy |
| 11 | Rozpadlina ok. 750 × 30-50 × ok. 85 | ✅⛔ | długość i szerokość dna (38-48) zgodne; głębokość 58-88 zależnie od świata (dno nie może zejść pod wodę 50) |
| 12 | Pierwsza podróż konno, potem ogniska | ✅ | postoje na trasach, szybka podróż z blokadami w etapach M9-M12, M14-M16 |
| 13 | Towarzysze z AI | ✅🟡 | konno, walka, koło rozkazów, padają ranni; zachowanie w terenie do sprawdzenia w grze |
| 14 | Will walczy z Morgarathem (bez przełączania) | ✅ | gracz zawsze zadaje ostatni cios |
| 15 | Pojedynek w 3 fazach | ✅ | konno / pieszo / szał, przejścia jako zdarzenia misji, powtórka fazy po śmierci |
| 16 | Prawdziwe spalenie mostu | ✅ | autotest: polanie, podpalenie, spadające przęsła, zwęglone kikuty, brak ognia po skrypcie |
| 17 | Will z pomocą Horace'a, w fazie 3 Horace pada | ✅ | tarcza, przyklęknięcie po 6 ciosach, ogłuszenie w fazie 3, ratunek przy zaufaniu ≥ 70 |
| 18 | Niewola po bitwie | ✅ | napad nad rzeką, okręt, epilog |
| 19 | Kopalnia na 3 poziomach | ✅ | E1, autotest |
| 20 | Oczy tropiciela + wnioski w dzienniku | ✅🟡 | E0/E1; wygląd w kliencie ⬜ |
| 21 | Skradanie 2.0 | ✅ | trasy, stożki, 3 poziomy, ogłuszanie, odciąganie uwagi (M7, M10, S5) |
| 22 | Skandianie + Erak | ✅ | wojownik z tarczą, berserker, Erak nietykalny, S4 i M16 |
| 23 | Kusznik (2× obrażenia, 2× naciąg), nadzorca, dobosz, Jeździec | ✅ | E0/E1; kusznik zgodnie z pomysłem gracza |
| 24 | Bitwa: zadania na skraju, 30-40 żywych + tłum | ✅ | limit 40 żywych, maks. 100 manekinów w bitwie, 4 zadania A-D |
| 25 | Pergamin + przerywniki z kamerą | ✅🟡 | 9 przerywników (autotest klatek 9/9) + pergaminy; kamera w kliencie ⬜ |
| 26 | Evanlyn tylko wskazówki, prawda w epilogu | ✅ | unika namiotu króla, pierścień, nocna rozmowa bez imienia, epilog króla |
| 27 | 10 umiejętności-odznak | ✅ | 10 odznak za misje (+ szkolny fechtunek rycerza) |
| 28 | Kotwiczka, dym, mapa Gilana, miecz zwiadowcy | ✅ | E0/E1 |
| 29 | Wyrwij: łuk z siodła, gwizdek, komendy, zryw | ✅ | E0 |
| 30 | 5 misji pobocznych + postoje | ✅ | S1-S5, 4 postoje tomu II, 3 zlecenia w wolnej grze |
| 31 | Ukryte zaufanie z konsekwencjami | ✅ | pas Gilana, prawda Evanlyn, ratunek Horace'a (≥ 70), +/- w dialogach i misjach |
| 32 | Trudność: Opowieść / Zwiadowca / Weteran | ✅ | zdrowie wrogów w misjach tomu II, osiągnięcie za Weterana w pojedynku |
| 33 | Mgła, deszcz, celtycki styl | ✅🟡 | klimat w kodzie (mgła po stronie klienta ⬜), okrągłe chaty i kamień w Tre-Gwyn |
| 34 | Jednoosobowo | ✅ | bez osobnej obsługi wielu graczy |
| 35 | Epilog, „Koniec tomu II - kontynuuj do części trzeciej”, wolna gra | ✅🟡 | przepływ w kodzie; ekran w kliencie ⬜ |
| 36 | Całość jako 2.0, po drodze 2-3 bety | 🟡⬜ | paczka 2.0.0 gotowa; bety i wydanie nie opublikowane - czekają na zgodę gracza |

**Odstępstwa łącznie:** głębokość kanionu (11), pas Gilana jako prezent z nożami (E0), brak srebrnych monet i bandaży,
strumień na dnie kanionu pominięty, kolczugi Wargali niewidoczne na modelu, most dłuższy niż 46 bloków.

**Do zrobienia przed wydaniem:** scenariusze ręczne 16.2 w kliencie (wymaga zgody na uruchomienie gry),
test migracji zapisu z 1.0.2, potem wersje testowe i 2.0 na GitHubie (za zgodą), commit i push
(na prośbę).

## Poprawki po testach gracza (2.0.1)

| Zgłoszenie | Zmiana | Status |
|---|---|---|
| Parowanie i kontra zbyt trudne | Mechanika usunięta (`Parry`, garda mieczem i saksą). Sparing w „Mieczu zwiadowcy”: uniki przed zapowiedzianym ciosem i trafienia, aż Gilan się podda. „Lekcja Gilana” +2 obrażenia mieczem, szkolny fechtunek +1 | ✅ kompiluje się, autotest |
| Kusznik uciekł z zasadzki na przełęczy | Kusznicy ze smyczą (12 bloków od stanowiska). W każdej misji „pokonaj oznaczonych”: ≤ 2 wrogów świecą, a gdy przy miejscu etapu nie ma żadnego przez 15 s, etap kończy się sam | ✅ |
| Brak zakładki „Wnioski” | Zakładka widoczna przez cały tom II, z podpowiedzią przed pierwszą wskazówką | ✅ |
| Kopalnia za ciemna | 35 nowych latarni w chodnikach, 16 pochodni na wejściu, pochodnia/latarnia w dłoni świeci (blok światła przy głowie, tylko Araluen) | ✅ |
| Dafydd zasypany | Zawał przeniesiony za plecy uciekającego, krótszy (8 s, promień 2), żwir nie spada przy postaciach, etap rozmowy odkopuje kryjówkę | ✅ |
| Noc uciążliwa | Nowa misja w nocy zaczyna się o poranku; wyjątki: pierwszy etap „noc” i pole `keep_time` (Łańcuchy, Płonący most, Czarny rumak, Bitwa, pojedynek) | ✅ |
| Drużyna nie przechodzi przez bramę (Dwie drogi) | Akcja `companion_tp` po dźwigni + we wszystkich etapach `ride_with` drużyna dogania gracza po 10 s | ✅ |
| Wyrwij za szeroki | Wyrwij i konie towarzyszy 0,9 bloku szerokości (przejścia 1-blokowe) | ✅ |

Plan zmieniony względem założeń: 9.5 (parowanie) usunięte na prośbę gracza; decyzja 28 („miecz zwiadowcy - lekcje
Gilana”) zostaje w formie sparingu bez parowania.

## Poprawki po testach gracza (2.0.2)

| Zgłoszenie | Zmiana | Status |
|---|---|---|
| Czarny jeździec: strzały z konia nie liczone | Trafienie z siodła liczy `ModEvents.onProjectileImpact` dla każdej strzały gracza na koniu (zwykłej i specjalnej), raz na strzałę, tylko na wrogach | ✅ |
| Towarzysze zabijają cele gracza | Cel etapu z `"solo": true`: drużyna znika na czas etapu i pojawia się przy graczu (z końmi), gdy etap się skończy. Czarny jeździec: etapy 1-3 solo. `if_cleared` dosyła Wargala, gdy wszyscy padli przed 3 trafieniami | ✅ |
| Na krawędzi: 6 celów lunety za dużo, nie tylko kusznicy | `mark` ×2 z `any_enemy` (dowolny wróg); zapis z ≥ 2 oznaczonymi rusza sam | ✅ |
| Płonący most: za mało oleju | Beczki bez limitu (było 8 na grę), +2 zapasowe flasze, rzucona flasza polewa podporę, PPM w dowolny blok podpory, podpowiedź z kierunkiem do beczek | ✅ |
| Płonący most: nie wiadomo, co polać | Obrys podpór przez ściany (block_display z poświatą): żółte do polania, pomarańczowe do podpalenia; znika po polaniu/zapaleniu i poza etapami | ✅ |
| Uthal: licznik się resetuje | Obrona nie zaczyna się od nowa przy słabnącym oddziale (ostrzeżenie zamiast powtórki; liczenie w promieniu 128); zadania B i D bez restartu po śmierci | ✅ |
| Uthal: żołnierze nie walczą | Sojusznicy bitwy (`zw_sojusznik`) używają celów walki wręcz i z łuku jak towarzysze | ✅ |
| Przedmioty po śmierci | `keepInventory` włączane przy starcie świata | ✅ |
| Misja znikała po „Na krawędzi” | Skutek skoku wstecz: „Łańcuchy” były już ukończone. `skok` zeruje misje dalej w fabule | ✅ |
| `/zwiadowcy` niewidoczna w nowym świecie | Komenda dostępna dla właściciela świata jednoosobowego bez kodów (na serwerze nadal op) | ✅ |
| Most w „Na krawędzi” renderuje się niepełnie | Czeka na zrzut ekranu - w logu most postawiony w całości (1053 bloki) | ⏳ |
