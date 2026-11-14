package pl.zwiadowcy.story;

import pl.zwiadowcy.story.StoryTexts.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Teksty fabularne tomu II („Płonący most”): prolog, pergaminy między aktami, epilog i wolna gra.
 * Fabuła jest wolną interpretacją wydarzeń z drugiego tomu serii - teksty są pisane własnymi słowami.
 */
public final class StoryTextsTom2 {
    public static final String PROLOG_RYCERZ = "prolog_tom2_rycerz";
    public static final String PROLOG_SZYBKI = "prolog_tom2_szybki";
    public static final String AKT1 = "tom2_akt1";
    public static final String AKT2 = "tom2_akt2";
    public static final String ROZKAZY = "tom2_rozkazy";
    public static final String DYM = "tom2_dym";
    public static final String NIEWOLA = "tom2_niewola";

    public static final String TOM1_SUMMARY_TITLE = "Tom I w skrócie";
    public static final String TOM1_SUMMARY = "Jestem Will, podopieczny barona Aralda z zamku Redmont. W Dniu Wyboru zostałem uczniem zwiadowcy "
            + "Halta. Nauczyłem się strzelać, skradać i tropić, oswoiłem konia Wyrwija, a na Równinie Samotności pokonałem "
            + "Kalkarów - bestie Morgaratha. Król Duncan przyjął mnie do Korpusu Zwiadowców. Na szyi noszę brązowy liść dębu.";

    private static final Map<String, List<Page>> TEXTS = new HashMap<>();

    private static Page page(String title, String text, String icon) {
        return new Page(title, text, icon);
    }

    static {
        Page army = page("Armia Morgaratha",
                "Morgarath, Władca Deszczu i Nocy, przestał się ukrywać. Na płaskowyżu w Górach Deszczu i Nocy zebrał armię "
                        + "Wargali, jakiej Araluen nie widział od piętnastu lat.\n\nZwiadowcy donoszą o ogniskach na przełęczach "
                        + "i o zapasach liczonych na długą kampanię. Wojna jest kwestią tygodni.",
                "zwiadowcy:znak_morgaratha");
        Page pass = page("Przełęcz Trzech Stopni",
                "Z płaskowyżu Morgaratha prowadzi w dół tylko jedna droga: wąska Przełęcz Trzech Stopni. Resztę granicy strzeże "
                        + "Rozpadlina - kanion tak głęboki, że na jego dnie w samo południe panuje półmrok.\n\nKról Duncan zbiera "
                        + "wojska na Równinie Uthal, u wylotu przełęczy. Jeśli Wargale zejdą, trafią prosto na rycerzy Araluenu.",
                "minecraft:iron_sword");
        Page celtica = page("Celtyka",
                "Na zachodzie, za górami, leży Celtyka - kraina wrzosowisk, mgieł i kopalń. Celtowie są starymi sprzymierzeńcami "
                        + "Araluenu, a ich górnicy słyną z tego, że przekopią się przez każdą skałę.\n\nKról potrzebuje ich pomocy. "
                        + "Ktoś musi zawieźć im wiadomość.",
                "minecraft:iron_pickaxe");
        Page call = page("Wezwanie",
                "Halt czeka w chacie pod lasem. Przyszedł list od Crowleya, komendanta Korpusu Zwiadowców.\n\n"
                        + "• Porozmawiaj z Haltem - strzałka w rogu ekranu wskaże drogę.\n"
                        + "• Towarzysze z drużyny słuchają rozkazów (przytrzymaj G).\n"
                        + "• Ogniska zwiadowców i Mapa Gilana pozwolą wracać szybko w odwiedzone miejsca.\n"
                        + "• Poziom trudności zmienisz w dzienniku (J).",
                "zwiadowcy:list_krolewski");

        TEXTS.put(StoryTexts.PROLOG_TOM2, List.of(page("Po Gorlanie",
                "Od nocy na Równinie Samotności minęło pół roku. Will spędził zimę w chacie Halta: strzelał, aż drętwiały palce, "
                        + "czytał ślady na śniegu i uczył się milczeć tak długo jak jego mistrz.\n\nNa szyi wciąż nosi brązowy liść "
                        + "dębu. Halt mówi, że do srebrnego jeszcze daleko. Will podejrzewa, że Halt mówi tak każdemu.",
                "zwiadowcy:brazowy_lisc_debu"), army, pass, celtica, call));
        TEXTS.put(PROLOG_RYCERZ, List.of(page("Po Gorlanie",
                "Po nocy na Równinie Samotności Will wybrał Szkołę Rycerską. Wytrzymał kilka tygodni: miecz był ciężki, kolczuga "
                        + "obcierała, a myśli wciąż uciekały do lasu i do łuku.\n\nW końcu baron Arald zgodził się na zmianę. "
                        + "Halt przyjął ucznia z powrotem bez słowa. Tylko kawa była tego dnia wyjątkowo mocna.",
                "minecraft:iron_sword"), army, pass, celtica, call));
        TEXTS.put(PROLOG_SZYBKI, List.of(page("Tom I w skrócie",
                "Will, sierota z zamku Redmont, w Dniu Wyboru został uczniem zwiadowcy Halta. Nauczył się strzelać, skradać "
                        + "i tropić, oswoił konia Wyrwija, a na Równinie Samotności pokonał Kalkarów - bestie Morgaratha.\n\n"
                        + "Na jego szyi wisi brązowy liść dębu, znak ucznia zwiadowcy. Streszczenie znajdziesz też w notatkach.",
                "zwiadowcy:brazowy_lisc_debu"), army, pass, celtica, call));

        TEXTS.put(AKT1, List.of(page("Za górami",
                "Z grani Przełęczy Wiatrów widać Celtykę: fale wrzosowisk, doliny w mgle i dymy nad górniczymi osadami. "
                        + "Tylko że dymów jest zaskakująco mało.\n\nGilan długo patrzy w dół i mruży oczy. „O tej porze roku "
                        + "w Celtyce pali się w każdej chacie” - mówi. - „Coś tu nie gra.”\n\nKoniec aktu I.",
                "minecraft:campfire")));
        Page orders = page("Rozkazy",
                "Pismo jest staranne, a pieczęć przedstawia czarną koronę. Rozkazy mówią o moście nad Rozpadliną, który budują "
                        + "jeńcy z Celtyki - „gotów za trzy dni”. Gdy tylko most stanie, kolumna Wargali ma przejść na drugą "
                        + "stronę i ruszyć na wschód.\n\nNa tyły armii królewskiej, która czeka pod Przełęczą Trzech Stopni.",
                "zwiadowcy:rozkazy_morgaratha");
        TEXTS.put(ROZKAZY, List.of(orders));
        TEXTS.put(AKT2, List.of(orders, page("Decyzja",
                "Gilan odjechał z wieścią do króla, ale nie zdąży wrócić na czas. Armia nie przyśle pomocy - nawet nie wie "
                        + "o moście.\n\nZostajesz ty, Horace i Evanlyn. Trzy dni. Jeden most. Halt powiedziałby, że to aż nadto."
                        + "\n\nKoniec aktu II.",
                "zwiadowcy:flasza_oleju")));
        TEXTS.put(DYM, List.of(page("Dym nad Rozpadliną",
                "Dym nad Rozpadliną widać było z wielu mil. Celtyccy górnicy, którzy uciekli w nocy, patrzyli na niego ze "
                        + "wzgórz i nikt nie mówił ani słowa.\n\nAle kilku Wargali zdążyło przejść na tę stronę, a armia "
                        + "królewska wciąż o niczym nie wie. Trzeba jechać - szybko.",
                "zwiadowcy:strzala_plonaca")));
        TEXTS.put(NIEWOLA, List.of(page("Niewola",
                "Ciemność pachnie smołą, solą i mokrym drewnem. I kołysze się.\n\nKiedy otwierasz oczy, nad tobą jest maszt, "
                        + "a nad masztem szare niebo. Nadgarstki masz związane, a obok, oparta o burtę, śpi Evanlyn.\n\n"
                        + "Jesteś na skandyjskim okręcie.",
                "minecraft:oak_boat")));

        TEXTS.put(StoryTexts.EPILOG_TOM2, List.of(
                page("Most, którego nie ma",
                        "Most nad Rozpadliną spłonął o świcie. Belki, które celtyccy górnicy cięli przez tygodnie pod batem "
                                + "nadzorców, runęły w przepaść razem z pierwszą kolumną Wargali.\n\nArmia Morgaratha nie uderzyła "
                                + "na tyły wojsk królewskich. Plan, który miał rozstrzygnąć wojnę, zamienił się w dym.",
                        "zwiadowcy:strzala_plonaca"),
                page("Bitwa oczami Halta",
                        "Halt patrzył na Równinę Uthal z pagórka, z łukiem w dłoni. Widział, jak szyki Wargali łamią się pod "
                                + "salwami łuczników i jak rycerze Araluenu spychają je pod przełęcz.\n\nWidział też chłopaka "
                                + "w szarozielonym płaszczu, który biegł pod prąd uciekających - prosto na czarnego jeźdźca.",
                        "zwiadowcy:luk_zwiadowcy"),
                page("Upadek Morgaratha",
                        "Morgarath zginął na Równinie Uthal z ręki ucznia zwiadowcy, którego nikt nie chciał wybrać w Dniu "
                                + "Wyboru. Horace, poobijany i dumny, opowiada wszystkim, że trzymał tarczę. Nikt w to nie wątpi.\n\n"
                                + "Wargale bez woli swojego pana rozpierzchli się po górach. Wojna, na którą Araluen czekał "
                                + "piętnaście lat, skończyła się w jedno popołudnie.",
                        "minecraft:wither_skeleton_skull"),
                page("Wieść dla króla",
                        "Wieczorem do namiotu króla przyszedł Gilan z wieścią o dziewczynie z Celtyki. Opisał jej twarz, "
                                + "pierścień z herbem i to, jak unikała królewskiego namiotu.\n\nKról Duncan zbladł. Jego córka, "
                                + "królewna Cassandra, zaginęła w Celtyce przed miesiącami. „Evanlyn” żyła - i właśnie trafiła "
                                + "w ręce Skandian.",
                        "zwiadowcy:pierscien_evanlyn"),
                page("Wilczy okręt",
                        "Okręt Eraka płynie na północ. Will i Evanlyn siedzą przy maszcie, skuci jednym łańcuchem. Erak co "
                                + "wieczór przynosi im miskę gulaszu i opowiada, że w Skandii zima trwa pół roku.\n\nWill patrzy "
                                + "na znikający brzeg i liczy gwiazdy. Zwiadowca zawsze wie, gdzie jest północ. A Halt zawsze wie, "
                                + "gdzie jest jego uczeń.",
                        "minecraft:compass"),
                page("Przysięga Halta",
                        "Halt stoi nad rzeką, tam gdzie ślady Willa urywają się w błocie. Za nim Gilan, Horace i Crowley. "
                                + "Nikt się nie odzywa.\n\n„Znajdę go” - mówi w końcu Halt. Ci, którzy go znają, wiedzą, że to "
                                + "nie jest obietnica. To stwierdzenie faktu.",
                        "zwiadowcy:brazowy_lisc_debu")));

        TEXTS.put(StoryTexts.WOLNA_GRA, List.of(page("Wolna gra",
                "Opowieść o Płonącym moście dobiegła końca, a tom III jeszcze nie powstał.\n\nDo tego czasu Araluen jest twój. "
                        + "Wracasz do chaty Halta z całym ekwipunkiem - to wolna gra poza kanonem książki.\n"
                        + "• Halt ma codzienne zlecenia, a Gilan czeka przy zwęglonym moście.\n"
                        + "• Ogniska zwiadowców i Mapa Gilana nadal działają.\n"
                        + "• Epilogi i ekrany końca tomów są w dzienniku (J).",
                "minecraft:map")));
    }

    public static List<Page> get(String id) {
        return TEXTS.getOrDefault(id, List.of());
    }

    private StoryTextsTom2() {}
}
