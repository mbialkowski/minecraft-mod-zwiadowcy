package pl.zwiadowcy.entity.projectile;

public enum RangerArrowType {
    PLONACA("plonaca"),
    SZEROKI_GROT("szeroki_grot"),
    GWIZDZACA("gwizdzaca"),
    Z_LINA("z_lina");

    public final String id;

    RangerArrowType(String id) {
        this.id = id;
    }

    public static RangerArrowType byOrdinal(int i) {
        RangerArrowType[] v = values();
        return i >= 0 && i < v.length ? v[i] : PLONACA;
    }
}
