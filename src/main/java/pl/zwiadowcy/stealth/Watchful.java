package pl.zwiadowcy.stealth;

import net.minecraft.world.entity.Mob;

/**
 * Wróg ze skradaniem 2.0 (tom II): ma stożek widzenia, pasek podejrzenia, trasę patrolu i można go ogłuszyć.
 * Działa tylko u mobów z tagiem {@link #TAG} - wrogowie tomu I zachowują się po staremu.
 */
public interface Watchful {
    String TAG = "zw_czujny";

    AlertBrain alertBrain();

    /** Stan widoczny u klienta (ikony nad głową): 0 spokój, 1 podejrzenie, 2 alarm, 3 ogłuszony. */
    int syncedAlertState();

    /** Pasek podejrzenia 0-100 widoczny u klienta. */
    int syncedAlertMeter();

    void setSyncedAlert(int state, int meter);

    /** Dobosz: alarm bębnem z 3-sekundowym rozbiegiem i zasięgiem 40 bloków. */
    default boolean isDrummer() {
        return false;
    }

    /** Nadzorca: gdy coś zauważy, krzyczy - pobliscy wartownicy idą sprawdzić to miejsce. */
    default boolean shoutsOnSuspicion() {
        return false;
    }

    default Mob asMob() {
        return (Mob) this;
    }

    static boolean active(Mob mob) {
        return mob instanceof Watchful && mob.getTags().contains(TAG);
    }
}
