package app; // <-- ou 'app' si tu préfères, mais reste cohérent

import codagebits.CodeurBits;
import java.util.*;

public final class BenchProto {
    private BenchProto() {}

    public static Map<String, Double> mesurer(CodeurBits codeur, int[] donnees, int repetitions) {
        if (repetitions <= 0) repetitions = 1;

        Map<String, Double> res = new LinkedHashMap<>();

        // Echauffement
        for (int i=0;i<3;i++) {
            int[] c = codeur.compresser(donnees);
            int[] d = codeur.decompresser(c);
            if (!Arrays.equals(donnees, d)) throw new AssertionError("roundtrip échauffement");
        }

        int sentinel = 0;

        // Compression
        long t = 0;
        for (int i=0;i<repetitions;i++) {
            long a = System.nanoTime();
            int[] c = codeur.compresser(donnees);
            long b = System.nanoTime();
            t += (b - a);
            if (c.length > 0) sentinel ^= c[0];
        }
        res.put("compresser_ms", t / 1e6 / repetitions);

        // Décompression (sur le dernier buffer)
        int[] dernier = codeur.compresser(donnees);
        t = 0;
        for (int i=0;i<repetitions;i++) {
            long a = System.nanoTime();
            int[] d = codeur.decompresser(dernier);
            long b = System.nanoTime();
            t += (b - a);
            if (d.length > 0) sentinel ^= d[0];
        }
        res.put("decompresser_ms", t / 1e6 / repetitions);

        // acceder()
        Random r = new Random(42);
        int essais = Math.min(1000, Math.max(1, donnees.length));
        t = 0;
        for (int i=0;i<repetitions;i++) {
            long a = System.nanoTime();
            for (int k=0;k<essais;k++) {
                sentinel ^= codeur.acceder(r.nextInt(Math.max(1, donnees.length)));
            }
            long b = System.nanoTime();
            t += (b - a);
        }
        res.put("acceder_ms", t / 1e6 / repetitions);

        // (Empêche le JIT de supprimer tout)
        if (sentinel == 42_4242) System.out.print(""); 

        return res;
    }
}
