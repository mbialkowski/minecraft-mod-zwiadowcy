## Zwiadowcy 2.0.2 (Forge 1.20.1)

Poprawki po kolejnych testach tomu II.

### Zmiany

- **Po śmierci nic nie tracisz:** mod przy każdym starcie świata włącza zasadę `keepInventory`.
- **Czarny jeździec:**
  - trafienia z siodła liczą się dla każdej strzały gracza na koniu (wcześniej tylko dla strzał specjalnych);
  - wrzosowiska, Wargali i Jeźdźca robisz sam: drużyna zostaje z tyłu i dołącza po pokonaniu Jeźdźca, więc
    towarzysze nie zabierają już celów;
  - gdy wszyscy Wargale padną przed trzecim trafieniem, zza kurhanu wypada kolejny.
- **Na krawędzi:** przez lunetę wystarczy oznaczyć dwóch dowolnych Wargali (wcześniej sześciu konkretnych strażników).
- **Płonący most:**
  - podpory do polania świecą na żółto, a polane i jeszcze niezapalone na pomarańczowo (obrys widoczny przez ściany);
  - beczki ze smołą napełniają flasze bez limitu, a na start Horace daje dwie zapasowe;
  - flasza rzucona w podporę też ją polewa, a PPM działa na każdy blok zastrzału i kotwy;
  - gdy olej się skończy, podpowiedź mówi, gdzie są beczki (kierunek i odległość).
- **Bitwa na Równinie Uthal:**
  - żołnierze-sojusznicy walczą z Wargalami;
  - licznik obrony nie zaczyna się od nowa, gdy oddział słabnie (jest tylko ostrzeżenie);
  - licznik nie zeruje się też po śmierci gracza.
- **Komenda `/zwiadowcy`:**
  - działa w świecie dla jednego gracza także bez włączonych kodów;
  - `skok` do wcześniejszej misji zeruje misje dalej w fabule, więc po „Na krawędzi” ruszają znowu „Łańcuchy”.

### Instalacja / aktualizacja

Usuń z folderu `mods` plik `zwiadowcy-forge-1.20.1-2.0.1.jar` i wrzuć `zwiadowcy-forge-1.20.1-2.0.2.jar`. Zapisy zostają.
Misje w toku same przechodzą na nowe zasady (np. zapis z co najmniej dwoma oznaczonymi strażnikami rusza dalej).

Dokładna instrukcja: [README](https://github.com/mbialkowski/minecraft-mod-zwiadowcy#instalacja-krok-po-kroku).

---

Fixes after more Volume II playtests:

- you keep your items on death;
- saddle shots count for every arrow;
- companions no longer steal targets in "Czarny jeździec";
- spyglass marking needs only two guards;
- bridge supports to oil or ignite are highlighted, with unlimited tar barrels and thrown flasks oiling supports;
- allied soldiers fight in the Uthal battle and its defence timers no longer reset;
- `/zwiadowcy` works in single player without cheats.
