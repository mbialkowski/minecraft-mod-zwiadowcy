package pl.zwiadowcy.world;

import javax.annotation.Nullable;

/**
 * Postoje na szlaku: małe scenki na długich trasach kampanii (dobrowolne misje "szlak_*").
 * Stawiane w punkcie leżącym na odcinku między dwiema lokacjami (ułamek drogi {@code along}), na lądzie.
 */
public enum RouteStop {
    WOZ_KUPCA("szlak_woz_kupca", "postoje/woz_kupca", "oboz_zlotu", "spalona_osada", 0.5),
    ZASADZKA_WARGALOW("szlak_zasadzka_wargalow", "postoje/zasadzka_wargalow", "spalona_osada", "ruiny_gorlanu", 0.5),
    RANNY_DRWAL("szlak_ranny_drwal", "postoje/ranny_drwal", "ruiny_gorlanu", "redmont", 0.35),
    ZASADZKA_BANDYTOW("szlak_zasadzka_bandytow", "postoje/zasadzka_bandytow", "ruiny_gorlanu", "redmont", 0.7),
    // tom II (szablony postojów z tomu I w nowych miejscach)
    WOZ_PROWIANTU("szlak_woz_prowiantu", "postoje/woz_kupca", "redmont", "oboz_na_trakcie", 0.5),
    SPALONA_STRAZNICA("szlak_spalona_straznica", "postoje/zasadzka_bandytow", "oboz_na_trakcie", "przelecz_wiatrow", 0.5),
    CELTYCCY_UCIEKINIERZY("szlak_celtyccy_uciekinierzy", "postoje/zasadzka_wargalow", "krawedz", "oboz_krolewski", 0.33),
    WARGALSKI_ZWIAD("szlak_wargalski_zwiad", "postoje/zasadzka_wargalow", "krawedz", "oboz_krolewski", 0.67),
    // misja poboczna S4 „Obcy na trakcie” (skandyjscy zwiadowcy przy wozie kupca)
    OBCY_NA_TRAKCIE("szlak_obcy_na_trakcie", "postoje/woz_kupca", "krawedz", "oboz_krolewski", 0.5);

    public final String id;
    /** Szablon w data/zwiadowcy/structures/. */
    public final String template;
    public final String from;
    public final String to;
    public final double along;

    RouteStop(String id, String template, String from, String to, double along) {
        this.id = id;
        this.template = template;
        this.from = from;
        this.to = to;
        this.along = along;
    }

    @Nullable
    public static RouteStop byId(String id) {
        for (RouteStop s : values()) if (s.id.equals(id)) return s;
        return null;
    }

    /** Szablon struktury dla postawionego miejsca (lokacji fabularnej albo postoju). */
    public static String templateOf(String placedId) {
        RouteStop stop = byId(placedId);
        return stop != null ? stop.template : placedId;
    }
}
