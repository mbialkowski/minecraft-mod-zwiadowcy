package pl.zwiadowcy.world;

import javax.annotation.Nullable;

/**
 * Stałe miejsca fabularne w wymiarze Araluen. Współrzędne to przybliżony środek struktury;
 * dokładne miejsce wybierane jest przy pierwszym postawieniu (szukanie płaskiego, suchego terenu).
 */
public enum StoryLocation {
    REDMONT("redmont", "Zamek Redmont", 0, 0, true),
    WENSLEY("wensley", "Wioska Wensley", -150, 90, false),
    CHATA_HALTA("chata_halta", "Chata Halta", 210, -110, false),
    STAJNIA_BOBA("stajnia_boba", "Farma Starego Boba", 330, 190, false),
    LEGOWISKO_DZIKA("legowisko_dzika", "Legowisko dzika", 460, -300, false),
    OBOZ_ZLOTU("oboz_zlotu", "Obóz Zlotu Zwiadowców", 0, -450, false),
    SPALONA_OSADA("spalona_osada", "Spalona osada", 450, 20, false),
    RUINY_GORLANU("ruiny_gorlanu", "Ruiny Gorlanu", 850, 450, false, 6);

    public final String id;
    public final String displayName;
    public final int x;
    public final int z;
    /** Czy stawiać przy pierwszym wejściu do Araluenu (bliskie lokacje). */
    public final boolean placeOnArrival;
    /** Ile warstw szablonu leży pod ziemią (krypty, piwnice). */
    public final int groundOffset;

    StoryLocation(String id, String displayName, int x, int z, boolean placeOnArrival) {
        this(id, displayName, x, z, placeOnArrival, 0);
    }

    StoryLocation(String id, String displayName, int x, int z, boolean placeOnArrival, int groundOffset) {
        this.id = id;
        this.displayName = displayName;
        this.x = x;
        this.z = z;
        this.placeOnArrival = placeOnArrival;
        this.groundOffset = groundOffset;
    }

    @Nullable
    public static StoryLocation byId(String id) {
        for (StoryLocation l : values()) if (l.id.equals(id)) return l;
        return null;
    }
}
