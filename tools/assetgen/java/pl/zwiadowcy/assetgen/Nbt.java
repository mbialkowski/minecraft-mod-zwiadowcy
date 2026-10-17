package pl.zwiadowcy.assetgen;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Minimalny zapis NBT (format Minecrafta, big-endian, gzip).
 * Map → Compound, List → List, Integer → Int, String → String, Byte → Byte, Long → Long, Float → Float, Double → Double.
 */
final class Nbt {
    static void writeGzip(File file, Map<String, Object> root) throws IOException {
        file.getParentFile().mkdirs();
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file))))) {
            out.writeByte(10);
            out.writeUTF("");
            writeCompoundBody(out, root);
        }
    }

    private static byte typeOf(Object v) {
        if (v instanceof Byte) return 1;
        if (v instanceof Short) return 2;
        if (v instanceof Integer) return 3;
        if (v instanceof Long) return 4;
        if (v instanceof Float) return 5;
        if (v instanceof Double) return 6;
        if (v instanceof String) return 8;
        if (v instanceof List) return 9;
        if (v instanceof Map) return 10;
        throw new IllegalArgumentException("Nieobsługiwany typ NBT: " + v.getClass());
    }

    @SuppressWarnings("unchecked")
    private static void writePayload(DataOutputStream out, Object v) throws IOException {
        switch (typeOf(v)) {
            case 1 -> out.writeByte((Byte) v);
            case 2 -> out.writeShort((Short) v);
            case 3 -> out.writeInt((Integer) v);
            case 4 -> out.writeLong((Long) v);
            case 5 -> out.writeFloat((Float) v);
            case 6 -> out.writeDouble((Double) v);
            case 8 -> out.writeUTF((String) v);
            case 9 -> {
                List<Object> list = (List<Object>) v;
                byte elem = list.isEmpty() ? 0 : typeOf(list.get(0));
                out.writeByte(elem);
                out.writeInt(list.size());
                for (Object o : list) writePayload(out, o);
            }
            case 10 -> writeCompoundBody(out, (Map<String, Object>) v);
            default -> throw new IllegalStateException();
        }
    }

    private static void writeCompoundBody(DataOutputStream out, Map<String, Object> map) throws IOException {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            out.writeByte(typeOf(e.getValue()));
            out.writeUTF(e.getKey());
            writePayload(out, e.getValue());
        }
        out.writeByte(0);
    }

    private Nbt() {}
}
