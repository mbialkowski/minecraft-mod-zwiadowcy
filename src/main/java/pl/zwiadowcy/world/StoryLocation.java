package pl.zwiadowcy.world;

import javax.annotation.Nullable;

/**
 * Stałe miejsca fabularne w wymiarze Araluen. Współrzędne to przybliżony środek struktury;
 * dokładne miejsce wybierane jest przy pierwszym postawieniu (szukanie płaskiego, suchego terenu).
 * Lokacje tomu I liczą się względem Redmontu, lokacje tomu II - w układzie krain tomu II ({@link VolumeTwoLayout}),
 * bo muszą trafić na Celtykę, Rozpadlinę i Równinę Uthal z generatora terenu.
 */
public enum StoryLocation {
    REDMONT("redmont", "Zamek Redmont", 0, 0, true),
    WENSLEY("wensley", "Wioska Wensley", -150, 90, false),
    CHATA_HALTA("chata_halta", "Chata Halta", 210, -110, false),
    STAJNIA_BOBA("stajnia_boba", "Farma Starego Boba", 330, 190, false),
    LEGOWISKO_DZIKA("legowisko_dzika", "Legowisko dzika", 460, -300, false),
    OBOZ_ZLOTU("oboz_zlotu", "Obóz Zlotu Zwiadowców", 0, -450, false),
    SPALONA_OSADA("spalona_osada", "Spalona osada", 450, 20, false),
    RUINY_GORLANU("ruiny_gorlanu", "Ruiny Gorlanu", 850, 450, false, 6),

    // ---- tom II: Płonący most
    OBOZ_NA_TRAKCIE("oboz_na_trakcie", "Obozowisko na trakcie", -600, 80, 0, 160),
    PRZELECZ_WIATROW("przelecz_wiatrow", "Przełęcz Wiatrów", -1250, 170, 0, 64),
    TRE_GWYN("tre_gwyn", "Tre-Gwyn", -1750, 250, 0, 160),
    KOPALNIA("kopalnia", "Kopalnia pod Kruczą Skałą", -1830, 170, 27, 96),
    DOLINA_MGIEL("dolina_mgiel", "Dolina Mgieł", -1950, 600, 0, 160),
    SZALAS_PASTERSKI("szalas_pasterski", "Szałas pasterski", -1700, 560, 0, 160),
    POSTERUNEK_DOBOSZA("posterunek_dobosza", "Wargalski posterunek", -1650, 850, 0, 96),
    WRZOSOWISKO("wrzosowisko", "Kurhan na wrzosowisku", -1500, 940, 0, 64),
    KRAWEDZ("krawedz", "Krawędź Rozpadliny", -1366, 1063, 0, 20),
    BUDOWA_MOSTU("budowa_mostu", "Budowa mostu", -1278, 1082, 0, 16),
    OBOZ_KROLEWSKI("oboz_krolewski", "Obóz królewski", 300, 1150, 0, 96),
    ROWNINA_UTHAL("rownina_uthal", "Równina Uthal", 450, 1350, 0, 64),
    BRZEG_RZEKI("brzeg_rzeki", "Rozlewisko rzeki", 650, 1250, 3, 96);

    public final String id;
    public final String displayName;
    public final int x;
    public final int z;
    /** Czy stawiać przy pierwszym wejściu do Araluenu (bliskie lokacje). */
    public final boolean placeOnArrival;
    /** Ile warstw szablonu leży pod ziemią (krypty, piwnice). */
    public final int groundOffset;
    /** Lokacja tomu II: współrzędne w układzie krain tomu II (obracanym), a nie względem Redmontu. */
    public final boolean volumeTwo;
    /** Jak daleko od punktu nominalnego wolno szukać miejsca (tom II: lokacje związane z kanionem stoją blisko). */
    public final int maxRadius;

    StoryLocation(String id, String displayName, int x, int z, boolean placeOnArrival) {
        this(id, displayName, x, z, placeOnArrival, 0);
    }

    StoryLocation(String id, String displayName, int x, int z, boolean placeOnArrival, int groundOffset) {
        this(id, displayName, x, z, placeOnArrival, groundOffset, false, 768);
    }

    StoryLocation(String id, String displayName, int x, int z, int groundOffset, int maxRadius) {
        this(id, displayName, x, z, false, groundOffset, true, maxRadius);
    }

    StoryLocation(String id, String displayName, int x, int z, boolean placeOnArrival, int groundOffset, boolean volumeTwo, int maxRadius) {
        this.id = id;
        this.displayName = displayName;
        this.x = x;
        this.z = z;
        this.placeOnArrival = placeOnArrival;
        this.groundOffset = groundOffset;
        this.volumeTwo = volumeTwo;
        this.maxRadius = maxRadius;
    }

    @Nullable
    public static StoryLocation byId(String id) {
        for (StoryLocation l : values()) if (l.id.equals(id)) return l;
        return null;
    }
}
