# Tom II - scenariusz testów w grze

Gra uruchomiona z kodu (`gradlew runClient`) gra jako konto testowe „Dev”, bez logowania. Cały test zajmuje ok. 1,5-2 h,
ale części są niezależne - można zrobić tylko wybrane.

## Przygotowanie

1. **Tryb dla twórców:** *Single Player → Create New World*. **Allow Cheats: ON** (komendy testowe). Tryb gry
   **Survival** (bitwy i pojedynek mają sens tylko z prawdziwym zdrowiem). W razie potrzeby `/gamemode creative`
   i z powrotem.
2. **Przydatne klawisze:** **F3** - współrzędne, **F2** - zrzut ekranu (`run/screenshots`), **J** - dziennik,
   **G** - koło rozkazów, **Z** - Oczy tropiciela, **R** - rodzaj strzały; pochodnia w dłoni świeci.
3. **Zgłaszanie błędów:** zapisz, w której misji i etapie (dziennik, J), co się stało, co powinno się stać,
   i zrób zrzut F2. Log gry: `run/logs/latest.log`.

**Komendy testowe:**

| Komenda | Co robi |
|---|---|
| `/zwiadowcy skok <misja>` | przeskok do misji tomu II: zestaw zwiadowcy, gwizdek i Wyrwij (gdy brak), drużyna, teleport |
| `/zwiadowcy misja dalej <misja>` | następny etap bieżącej misji (gdy coś utknie) |
| `/zwiadowcy misja ukoncz <misja>` | kończy misję (z nagrodami i startem następnej) |
| `/zwiadowcy tp <lokacja[:kotwica]>` | teleport, np. `/zwiadowcy tp most:srodek` |
| `/zwiadowcy flaga <nazwa>` | przełącza flagę, np. `rhys_uratowany`, `popreg_poluzowany`, `wybor_rycerz` |
| `/time set night` / `/time set day` | pora dnia (Łańcuchy dzieją się nocą) |

Misje tomu II (do `skok`): `wezwanie`, `miecz_zwiadowcy`, `przelecz`, `ciche_wzgorza`, `kopalnia`, `evanlyn`,
`dwie_drogi`, `czarny_jezdziec`, `na_krawedzi`, `lancuchy`, `plonacy_most`, `wyscig_z_czasem`, `noc_przed_bitwa`,
`bitwa_uthal`, `wladca_deszczu_i_nocy`, `wilcze_okrety`; poboczne: `syn_gornika`, `pasterka`, `strzal_gilana`,
`obcy_na_trakcie`, `czarny_rumak`.

---

## A. Start i wybór tomu (5 min) - nowa funkcja

Nowy świat. Po wejściu otwiera się Kronika.

- [ ] Ostatnia strona Kroniki ma dwa przyciski: **„Zacznij od tomu I (zalecane)”** i **„Zacznij od tomu II…”**.
- [ ] „Zacznij od tomu II…” otwiera osobny pergamin: co się pomija (14 misji), co dostajesz, że nie da się cofnąć.
- [ ] **Esc** na tym ekranie wraca do Kroniki (nic się nie dzieje).
- [ ] „Zacznij od tomu I (zalecane)” zamyka Kronikę: Kronika jest w dłoni, na ekranie napis „Tom I: Ruiny Gorlanu”,
  na czacie 3 kroki. PPM Kroniką → Redmont, prolog, baron podchodzi.
- [ ] **Drugi nowy świat:** „Zacznij od tomu II…” → „Tak, zaczynam od tomu II”: zestaw zwiadowcy w plecaku,
  komunikat o tomie II, PPM Kroniką → Araluen, prolog „Tom I w skrócie”, start misji „Wezwanie”, Wyrwij przy chacie Halta.

## B. Akt I-II w skrócie (20-30 min)

Świat z szybkim startem (punkt A, drugi świat). Przechodzimy normalnie albo skaczemy:

- [ ] **Wezwanie:** przerywnik wyjazdu z Redmontu (kamera, pasy kinowe, napisy, Esc pomija), jazda z drużyną konno.
- [ ] **Miecz zwiadowcy** (`/zwiadowcy skok miecz_zwiadowcy`): sparing - gdy miecz Gilana błyśnie, uskocz i uderz,
  aż Gilan się podda.
- [ ] **Przełęcz** (`skok przelecz`): kusznicy - zgrzyt korby i błysk przed strzałem, bełt mocno boli;
  linka z kotwiczką (PPM w krawędź bloku do 20 bloków).
- [ ] **Ciche wzgórza / Kopalnia:** Oczy tropiciela (**Z**) - świecące wskazówki, PPM na wskazówce → notatka;
  zakładka „Wnioski” w dzienniku (wybór 1 z 3).
- [ ] **Dwie drogi** (`skok dwie_drogi`): skradanie 2.0 - „?” i „!” nad głowami, kucanie w trawie, ogłuszenie od tyłu;
  wrota posterunku otwierają się dźwignią **na całą wysokość** i drużyna przechodzi (był tu błąd).
- [ ] **Czarny jeździec** (`skok czarny_jezdziec`): szarże (koń staje dęba → zjedź z linii), strzały z siodła,
  przy połowie zdrowia jeździec spada i walczy pieszo.
- [ ] **Koło rozkazów (G):** Za mną / Czekaj tu / Ukryj się działają na Horace'a i Evanlyn.

## C. Most - akt III, część 1 (25-30 min) - najważniejsze

1. `/zwiadowcy skok na_krawedzi`
   - [ ] Krawędź: ukryty obóz z ogniskiem. Po dojściu na skałę przerywnik **„Most nad Rozpadliną”**:
     kamera leci kanionem, widać most nad przepaścią i obóz jeńców.
   - [ ] Luneta (jest w plecaku): PPM i patrz na strażnika ok. 1 s → „Oznaczono” i świecenie. 2 cele.
   - [ ] Narada: Horace daje wybór „odciągnij uwagę”, Evanlyn rozmowa, potem sen do zmroku.
2. Łańcuchy (startuje sama; nocą - w razie czego `/time set night`)
   - [ ] Zakradnięcie się do zagród (kucaj). Ognisko obozu oświetla teren.
   - [ ] Nadzorcy z biczem (4 bloki zasięgu, spowolnienie). Ogłuszenie od tyłu liczy się jako pokonanie.
   - [ ] Klucze → PPM przy 3 furtkach: furtki się otwierają, górnicy uciekają ścieżką na krawędź.
   - [ ] Alarm (daj się zauważyć): bęben dobosza, posiłki; rozkaz **G → Odciągnij uwagę** wysyła Horace'a pod bramę.
   - [ ] Powrót do punktu zbiórki; nagroda „Zimna krew”.
3. Płonący most
   - [ ] PPM na beczkach w składzie smoły napełnia flasze oleju (do 4).
   - [ ] Podpory: 2 zastrzały pod pomostem przy północnej ścianie - **zejście po linie z przerwy w poręczy**
     (albo kotwiczką), 2 kotwy za wieżą linową. PPM z flaszą → drewno ciemnieje.
   - [ ] Wariant z S1: `/zwiadowcy flaga rhys_uratowany` przed tym etapem → wystarczą 3 podpory (licznik 0/3).
   - [ ] Świt: rogi, kolumna Wargali (tłum + kilku żywych) wchodzi na most od południa.
   - [ ] **Płonące strzały** (R zmienia strzałę) w polane podpory → „Podpora płonie!”. Krzesiwo z bliska też działa.
   - [ ] Przerywnik **„Płonący most”** (ok. 35 s): ogień biegnie po pomoście, przęsła spadają kolejno, Wargale lecą
     w przepaść, zostają zwęglone kikuty i dym.
   - [ ] **Zapisz i wyjdź w trakcie palenia** (Esc → Save and Quit), wejdź ponownie: most jest od razu w stanie
     końcowym (bez połowy mostu w powietrzu).
   - [ ] Ucieczka do koni; szybka podróż zablokowana do końca (mapa Gilana pokazuje powód).

## D. Wojna - akt III, część 2 (25-30 min)

1. `/zwiadowcy skok wyscig_z_czasem`
   - [ ] Jazda do obozu króla (daleko na wschód; po drodze 3 postoje: uciekinierzy, Skandianie przy wozie kupca,
     wargalski zwiad). Przy Skandianach sprawdź **oba** wybory (rozmowa i walka - druga próba: `skok obcy_na_trakcie`).
   - [ ] Halt („Dobra robota”), Crowley, król. Evanlyn zostaje „pilnować koni”.
2. Noc przed bitwą
   - [ ] Rodney, kowal (wybór 16 płonących albo 16 z szerokim grotem), Gilan (pas na noże przy wysokim zaufaniu),
     Evanlyn (przy wysokim zaufaniu wyznanie bez imienia), sen.
   - [ ] (opcjonalnie) Czarny rumak: nocą do koniowiązów za szykami wroga, PPM przy koniu → popręg.
3. Bitwa na Równinie Uthal (po rozmowie z Haltem o świcie)
   - [ ] Szyki: setki manekinów ze sztandarami, odgłosy bitwy, krótki przerywnik.
   - [ ] **A** - 5 kuszników na wzgórzu z Haltem. **B** - obrona oddziału 60 s (min. 4 z 6 żołnierzy przeżywa;
     gdy padnie więcej, etap od nowa). **C** - luneta na 5 doboszy, salwa łuczników. **D** - bród: Skandianie,
     tarcza blokuje strzały z przodu (zajdź z boku), berserker wpada w szał.
   - [ ] Śmierć w trakcie zadania → odrodzenie i zadanie od nowa.
   - [ ] Przerywnik wyłomu.
4. Władca Deszczu i Nocy (pojedynek)
   - [ ] Faza 1 konno: szarże z zapowiedzią, rumaka nie da się zranić. Po S5 (`/zwiadowcy flaga popreg_poluzowany`)
     Morgarath spada wcześniej (85% zamiast 70%).
   - [ ] Faza 2 pieszo: 3 ciosy z zapowiedzią, Horace blokuje tarczą; słaby punkt świeci po chybionym ciosie (×3);
     Horace przyklęka po 6 ciosach. Olej + płonąca strzała → gniew.
   - [ ] Faza 3: Horace ogłuszony, piasek w oczy (schyla się), udawany upadek (nie podchodź!).
   - [ ] Krąg żołnierzy odpycha, gdy próbujesz uciec z areny.
   - [ ] Śmierć → pojedynek od początku bieżącej fazy.
   - [ ] Zaufanie Horace'a ≥ 70: raz przyjmuje cios, który by cię zabił (trudno wymusić - zgłoś, jeśli zobaczysz).
   - [ ] Ostatni cios zadajesz ty; przerywnik upadku, Wargale uciekają.
   - [ ] Poziomy trudności: dziennik → Trudność (Opowieść / Zwiadowca / Weteran) - zmienia zdrowie i zapowiedzi.

## E. Niewola i koniec tomu (10 min)

`/zwiadowcy skok wilcze_okrety`
- [ ] Halt w obozie, opatrywanie 3 rannych nad rzeką (PPM z racją).
- [ ] Skandianie Eraka: nie da się zginąć, po 40 s albo przy niskim zdrowiu → powalenie.
- [ ] Przerywnik „Niewola”, pergamin, budzisz się na okręcie w zgrzebnej koszuli (ekwipunek zabrany).
- [ ] Rozmowa z Erakiem i Evanlyn → epilog tomu II (6 stron) → ekran **„Koniec tomu II”**.
- [ ] „Wróć do Araluenu”: ekwipunek wraca, jesteś przy chacie Halta, pergamin o wolnej grze.
- [ ] Wolna gra: Gilan na Krawędzi i Morwenna przy szałasie dają zlecenia, Halt - zlecenia z tomu I.

## F. Kontynuacja po tomie I (opcjonalnie, 10 min)

Świat z ukończonym tomem I (albo: nowy świat → tom I → `/zwiadowcy misja ukoncz` po kolei aż do
„Przed obliczem króla”).
- [ ] Po epilogu ekran **„Koniec tomu I”** z przyciskiem „Kontynuuj do części drugiej”.
- [ ] Przycisk „Tom II ▶” w dzienniku; po wyborze rycerza (`/zwiadowcy flaga wybor_rycerz`) inne kwestie Horace'a
  i Rodneya.

## Co szczególnie obserwować

- Towarzysze w terenie: czy nie gubią się, czy zsiadają w ciasnych miejscach, czy dojeżdżają, gdy zostaną daleko.
- Wydajność bitwy (F3 → FPS).
- Teksty: literówki, ucięte zdania w oknach dialogu, polskie znaki.
- Strzałka HUD: czy prowadzi do właściwego celu każdego etapu.
