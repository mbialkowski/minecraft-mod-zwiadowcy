package pl.zwiadowcy.story;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/** Teksty zapisków znajdowanych w strukturach (przedmiot "Zapiski" z tagiem NBT notatka=&lt;id&gt;). */
public final class LoreNotes {
    public record Note(String title, String text) {}

    private static final Map<String, Note> NOTES = new LinkedHashMap<>();

    static {
        add("rozkaz_morgaratha", "Rozkaz z Gór Deszczu i Nocy",
                "Kawałek wyprawionej skóry pokryty kanciastym pismem. Ktoś przetłumaczył go na marginesie:\n\n"
                        + "„Obserwujcie trakty. Liczcie konnych. Nie atakujcie zwiadowców w szarozielonych płaszczach - "
                        + "ich śmierć ściągnie pościg. Czekajcie przy Gorlanie na znak. Pan Deszczu i Nocy pamięta o każdym, "
                        + "kto zawiedzie.”");
        add("pamietnik_bandyty", "Pogięty pamiętnik",
                "„Trzeci dzień w kryjówce. Herszt mówi, że kupcy z Wensley jeżdżą teraz tylko z eskortą, odkąd baron "
                        + "wysłał patrole. Grend chce iść na północ, ale tam ponoć widziano wargale. Wolę już stryczek barona "
                        + "niż te psy Morgaratha.\n\nPS. Ktoś znowu wyżarł cały ser.”");
        add("kurhan_napis", "Napis z kurhanu",
                "Na kamiennej płycie wyryto koło i miecz, a pod nimi słowa w starym języku królestwa:\n\n"
                        + "„Tu śpi ten, który strzegł brodu, gdy Araluen był jeszcze młody. Kto zakłóci jego spokój, "
                        + "niech zabierze ze sobą tylko to, co potrzebne do obrony słabszych.”");
        add("list_kupca", "List kupca",
                "„Szanowny bracie,\nna trakcie między Wensley a zajazdem znów grasują bandyci. Mój wóz z wełną ocalał "
                        + "tylko dlatego, że wynająłem dwóch strażników. Karczmarz twierdzi, że ich kryjówka leży w lesie, "
                        + "za starą strażnicą. Nie podróżuj sam i nie wierz nikomu, kto nosi czerwoną chustę.”");
        add("krag_kamieni", "Spisane podanie",
                "Pasterze mówią, że kamienny krąg jest starszy niż samo królestwo i że ci, którzy go postawili, "
                        + "patrzyli z niego na gwiazdy, by wiedzieć, kiedy siać i kiedy zbierać. Nikt nie wie, kim byli. "
                        + "Zwiadowcy często obozują w pobliżu: z kręgu widać wszystkie drogi w dolinie.");
        add("raport_straznicy", "Ostatni raport strażnicy",
                "„Do barona. Trzecią noc z rzędu widzimy ogniska na wzgórzach. Za duże jak na pasterzy. "
                        + "Prosimy o posiłki i świeże strzały. Jeśli nie odpowiecie do pełni, opuścimy wieżę i wrócimy do "
                        + "zamku.”\nPod spodem inna ręka dopisała: „Nikt nie przyjechał”.");
        add("zapiski_gorlanu", "Zapiski z krypty Gorlanu",
                "Klatki z grubych prętów, wbite w skałę łańcuchy, kości zbyt wielkie jak na wilka. Na ścianie wydrapano "
                        + "kreski, setki kresek, jakby ktoś liczył dni.\n\nKtokolwiek urządził to miejsce pod ruinami, trzymał tu "
                        + "coś bardzo dużego. I bardzo długo.");
        add("rozkaz_posterunku", "Mapa posterunku",
                "Prymitywna mapa narysowana węglem: Góry Deszczu i Nocy, ścieżka przez przełęcz, a przy niej trzy kreski - "
                        + "posterunki. Jeden z nich jest przekreślony. Obok narysowano liść dębu. Ktoś najwyraźniej wie, "
                        + "że zwiadowcy depczą im po piętach.");
    }

    private static void add(String id, String title, String text) {
        NOTES.put(id, new Note(title, text));
    }

    @Nullable
    public static Note get(String id) {
        return NOTES.get(id);
    }

    public static Iterable<String> ids() {
        return NOTES.keySet();
    }

    private LoreNotes() {}
}
