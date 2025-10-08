package app;

import codagebits.*;
import java.util.*;

public class DemoEnonce {

    // Petit générateur pseudo-aléatoire (comme avant)
    static int[] generer(int n, int graine, int max) {
        Random r = new Random(graine);
        int[] t = new int[n];
        for (int i=0;i<n;i++) t[i] = r.nextInt(max*2+1) - max; // valeurs entre -max et +max
        return t;
    }

    // Mesure simple (moyenne sur N répétitions)
    static double mesureMs(Runnable f, int rep) {
        long start, end; 
        double total = 0;
        for (int i=0;i<rep;i++) {
            start = System.nanoTime();
            f.run();
            end = System.nanoTime();
            total += (end-start)/1e6;
        }
        return total/rep;
    }

    public static void main(String[] args) {
        if (args.length==0) {
            System.out.println("Usage: java DemoEnonce [sans|avec|debordement]");
            return;
        }
        String mode = args[0];
        CodeurBits codeur = FactoryCodeurBits.creer(mode);

        // ---------- Test 1 : tableau fixe avec négatifs ----------
        int[] donnees = {-5, -1, 0, 3, 7};
        System.out.println("Original (neg): " + Arrays.toString(donnees));
        int[] comp = codeur.compresser(donnees);
        int mid = donnees.length/2;
        int val = codeur.acceder(mid);
        int[] dec = codeur.decompresser(comp);
        System.out.printf("mode=%s get(%d)=%d ok=%b%n", 
            mode, mid, val, Arrays.equals(donnees, dec));

        // ---------- Test 2 : tableau aléatoire ----------
        int n = 10, graine = 123, max = 100;
        int[] rand = generer(n, graine, max);
        System.out.println("Original (rand): " + Arrays.toString(rand));
        int[] comp2 = codeur.compresser(rand);
        int mid2 = rand.length/2;
        int val2 = codeur.acceder(mid2);
        int[] dec2 = codeur.decompresser(comp2);
        System.out.printf("mode=%s get(%d)=%d ok=%b%n", 
            mode, mid2, val2, Arrays.equals(rand, dec2));

        Map<String,Double> mesures = new LinkedHashMap<>();
        mesures.put("compresser_ms", mesureMs(() -> codeur.compresser(rand), 20));
        mesures.put("decompresser_ms", mesureMs(() -> codeur.decompresser(comp2), 20));
        mesures.put("acceder_ms", mesureMs(() -> codeur.acceder(rand.length/2), 20));
        System.out.println("Mesures (ms) : " + mesures);

        // ---------- Test 3 : exemple overflow (énoncé) ----------
        if (mode.equals("debordement")) {
            int[] exOver = {1,2,3,1024,4,5,2048};
            System.out.println("Exemple overflow : " + Arrays.toString(exOver));
            int[] compOver = codeur.compresser(exOver);
            int[] decOver = codeur.decompresser(compOver);
            System.out.println("Restauration     : " + Arrays.toString(decOver));
            System.out.printf("Acces direct ex.: i=3 -> %d, i=6 -> %d%n", 
                codeur.acceder(3), codeur.acceder(6));
        }
    }
}
