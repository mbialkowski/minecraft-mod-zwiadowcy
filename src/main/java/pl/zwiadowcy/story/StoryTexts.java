package pl.zwiadowcy.story;

import java.util.List;
import java.util.Map;

/** Teksty wprowadzenia fabularnego (wyświetlane na ekranie-kronice). */
public final class StoryTexts {
    /** Strona: tytuł, tekst i przedmiot-ilustracja (identyfikator rejestru). */
    public record Page(String title, String text, String icon) {}

    public static final String KRONIKA = "kronika";
    public static final String PROLOG = "prolog";
    /** Epilog po ostatniej misji - dwie wersje zależnie od wyboru przed królem. */
    public static final String EPILOG_ZWIADOWCA = "epilog_zwiadowca";
    public static final String EPILOG_RYCERZ = "epilog_rycerz";

    private static final Map<String, List<Page>> STORIES = Map.of(
            KRONIKA, List.of(
                    new Page("Stara kronika",
                            "W twoim plecaku leży księga, której wcześniej tam nie było. Oprawiona w spękaną skórę, z wytłoczonym "
                                    + "na okładce liściem dębu.\n\nKiedy ją otwierasz, czujesz zapach deszczu, dymu i lasu, a litery same "
                                    + "układają się w opowieść o dalekim królestwie Araluenu.",
                            "zwiadowcy:kronika_araluenu"),
                    new Page("Brama do Araluenu",
                            "Użyj Kroniki (prawy przycisk myszy), a przeniesiesz się do Araluenu - krainy zamków, puszcz "
                                    + "i zwiadowców w szarozielonych płaszczach. Tam staniesz się Willem, sierotą z zamku Redmont.\n\n"
                                    + "Do swojego świata wrócisz w każdej chwili, używając Kroniki ponownie.",
                            "zwiadowcy:brazowy_lisc_debu")),
            PROLOG, List.of(
                    new Page("Królestwo Araluenu",
                            "Araluen to królestwo pięćdziesięciu lenn, którym włada król Duncan. W każdym lennie stoi zamek barona, "
                                    + "ćwiczą rycerze i żyje jeden zwiadowca: milczący człowiek w szarozielonym płaszczu, z łukiem na plecach.\n\n"
                                    + "Prości ludzie szepczą, że zwiadowcy znają czarną magię, bo pojawiają się znikąd i o wszystkim wiedzą "
                                    + "pierwsi. Zwiadowcy nigdy temu nie zaprzeczają.",
                            "zwiadowcy:list_krolewski"),
                    new Page("Władca Deszczu i Nocy",
                            "Piętnaście lat temu Morgarath, baron Gorlanu, zdradził króla i poprowadził przeciw niemu armię Wargalów - "
                                    + "półzwierzęcych stworów posłusznych tylko jego woli. Przegrał i uciekł w Góry Deszczu i Nocy, "
                                    + "gdzie czeka do dziś.\n\nRuiny jego zamku zarastają trawą na Równinie Samotności. Królestwo zapomniało. "
                                    + "Zwiadowcy - nie.",
                            "zwiadowcy:znak_morgaratha"),
                    new Page("Zamek Redmont",
                            "Jesteś Will. Nie znasz swoich rodziców - baron Arald, pan zamku Redmont, przygarnął cię jako niemowlę "
                                    + "i wychował razem z innymi podopiecznymi zamku. Mówią, że twój ojciec zginął jak bohater.\n\n"
                                    + "Jesteś najmniejszy z całej gromadki, ale nikt w Redmont nie wspina się po murach szybciej od ciebie.",
                            "minecraft:stone_bricks"),
                    new Page("Dzień Wyboru",
                            "Dziś kończysz piętnaście lat i w zamku nadchodzi Dzień Wyboru. Mistrzowie rycerzy, skrybów, kuchni "
                                    + "i dyplomacji wybiorą sobie uczniów spośród podopiecznych barona.\n\nMarzysz o Szkole Rycerskiej. "
                                    + "Kłopot w tym, że Sir Rodney szuka chłopców rosłych jak dęby…\n\nA w cieniu muru stoi ktoś "
                                    + "w szarozielonym płaszczu i obserwuje cię od rana.",
                            "minecraft:iron_sword"),
                    new Page("Twoja droga",
                            "Baron Arald czeka na ciebie na dziedzińcu - podejdzie, gdy tylko cię zobaczy. Z postaciami rozmawiasz "
                                    + "prawym przyciskiem myszy.\n\n• Dziennik zwiadowcy (klawisz J): misje, cele i notatki.\n"
                                    + "• Lewy górny róg ekranu: bieżący cel i kierunek, w którym trzeba iść.\n"
                                    + "• Kronika zabierze cię z powrotem do twojego świata.\n\n"
                                    + "Powodzenia, Willu. Królestwo potrzebuje kogoś, kto patrzy uważniej niż inni.",
                            "zwiadowcy:dziennik_zwiadowcy")),
            EPILOG_ZWIADOWCA, epilog(new Page("Brązowy liść dębu",
                    "Przed królem Duncanem wybrałeś Korpus Zwiadowców. Na twojej szyi wisi brązowy liść dębu - znak ucznia, "
                            + "który pewnego dnia zamieni się w srebrny.\n\nHalt nic nie powiedział. Tylko skinął głową. Ci, którzy "
                            + "go znają, mówią, że to najwięcej, co kiedykolwiek od niego usłyszysz.",
                    "zwiadowcy:brazowy_lisc_debu")),
            EPILOG_RYCERZ, epilog(new Page("Ścieżka rycerza",
                    "Przed królem Duncanem wybrałeś Szkołę Rycerską. Sir Rodney klepnął cię w plecy tak mocno, że omal nie "
                            + "upadłeś - u rycerzy to oznaka najwyższego szacunku.\n\nAle Halt nie zamknął przed tobą drzwi chaty. "
                            + "Powiedział, że kto raz nauczył się patrzeć jak zwiadowca, nie przestanie tylko dlatego, że nosi miecz.",
                    "minecraft:iron_sword"))
    );

    /** Strony epilogu wspólne dla obu wyborów; {@code choice} to strona o wyborze przed królem. */
    private static List<Page> epilog(Page choice) {
        return List.of(
                new Page("Ogień w ciemności",
                        "Kalkarowie z Gorlanu nie żyją. Bestie, przed którymi drżeli rycerze, padły od płonących strzał "
                                + "ucznia zwiadowcy - tego samego, który jeszcze tej jesieni bał się, że nikt go nie wybierze.\n\n"
                                + "Halt wraca do zdrowia w chacie pod lasem. Narzeka na bandaże, na zupę i na to, że wszyscy "
                                + "się nad nim trzęsą. Czyli wszystko jest w porządku.",
                        "zwiadowcy:strzala_plonaca"),
                new Page("Droga, którą przeszedłeś",
                        "Pamiętasz Dzień Wyboru? Nocną wspinaczkę po bluszczu, pierwszą kawę dla Halta i pierwsze strzały, "
                                + "które leciały wszędzie, tylko nie w tarczę.\n\nPotem przyszła cierpliwość: bezruch w trawie, "
                                + "upór Wyrwija, polowanie z baronem i Zlot, na którym zwiadowcy z całego królestwa po raz pierwszy "
                                + "spojrzeli na ciebie jak na jednego z nich. Aż w końcu trop zaprowadził cię na Równinę Samotności.",
                        "zwiadowcy:dziennik_zwiadowcy"),
                choice,
                new Page("Przyjaciele z Redmont",
                        "Horace nie jest już chłopakiem, którego wszyscy się boją - jest tym, na którego wszyscy liczą. "
                                + "Alyss ćwiczy przed lustrem dyplomatyczne uśmiechy, a Jenny piecze w kuchni mistrza Chubba tak, "
                                + "że kolejka sięga dziedzińca.\n\nBaron Arald opowiada każdemu gościowi o podopiecznym, który "
                                + "podpalił Kalkara. Za każdym razem bestia jest trochę większa.",
                        "minecraft:cake"),
                new Page("Cień nad górami",
                        "Morgarath wciąż czeka w Górach Deszczu i Nocy. Zwiadowcy z północy donoszą o Wargalach "
                                + "zbierających się na przełęczach i o nowym moście nad przepaścią.\n\nAraluen jeszcze o tym nie "
                                + "wie. Ale zwiadowcy wiedzą. Zwiadowcy zawsze wiedzą pierwsi.",
                        "zwiadowcy:znak_morgaratha"),
                new Page("Co dalej",
                        "Opowieść o Ruinach Gorlanu dobiegła końca, ale Araluen nie zasypia.\n\n"
                                + "• Halt czeka w swojej chacie pod lasem i każdego dnia ma dla ciebie nowe zlecenie: patrol "
                                + "Wargalów, dziki w zbożu albo list do Starego Boba. Odwiedzaj go - zwiadowca nigdy nie kończy nauki.\n"
                                + "• W całym królestwie czekają obozy Wargalów, kryjówki bandytów, kurhany i zapiski do odnalezienia.\n"
                                + "• Epilog przeczytasz ponownie w dzienniku (klawisz J).\n\nDziękujemy za wspólną drogę, Willu.",
                        "minecraft:map"));
    }

    public static final String PROLOG_NOTE_TITLE = "Prolog: kim jestem";
    public static final String PROLOG_NOTE = "Jestem Will, podopieczny barona Aralda z zamku Redmont w królestwie Araluenu. "
            + "Piętnaście lat temu zdrajca Morgarath, Władca Deszczu i Nocy, przegrał wojnę z królem Duncanem i ukrył się w górach. "
            + "Dziś jest Dzień Wyboru - mistrzowie zamku wybierają uczniów. Chcę do Szkoły Rycerskiej, choć jestem najmniejszy. "
            + "Ktoś w szarozielonym płaszczu zwiadowcy obserwuje mnie od rana. Najpierw muszę porozmawiać z baronem na dziedzińcu.";

    public static List<Page> get(String id) {
        return STORIES.getOrDefault(id, List.of());
    }

    private StoryTexts() {}
}
