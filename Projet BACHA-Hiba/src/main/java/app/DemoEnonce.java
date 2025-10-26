package app;

import codagebits.*;
import java.util.*;

/**
 * Classe de démonstration principale du projet Bit Packing.
 * Elle teste les trois variantes (sans chevauchement, avec chevauchement, overflow),
 * affiche les résultats, et mesure les performances sur des tableaux négatifs et aléatoires.
 */
public class DemoEnonce {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Usage: java -cp out app.DemoEnonce [sans|avec|debordement]");
            return;
        }

        String mode = args[0];
        CodeurBits codeur = FactoryCodeurBits.creer(mode);

        // ----------- CAS 1 : Tableau positif/mixte simple -----------
        int[] tPos = {5, 3, 12, 7, 0, 8, 1};
        System.out.println("──────────────────────────────────────────────");
        System.out.println("Cas 1 : Tableau positif/mixte simple");
        runCase("Original (pos)", mode, codeur, tPos);
        Map<String, Double> mesuresPos = mesurer(codeur, tPos);
        afficherMesures("Mesures (ms) pour pos", mesuresPos);

        // ----------- CAS 2 : Tableau avec entiers négatifs -----------
        int[] tNeg = {5, -3, 12, -7, 0, -8, 1};
        System.out.println("\n──────────────────────────────────────────────");
        System.out.println("Cas 2 : Tableau avec entiers négatifs");
        runCaseEtAfficherCompression("Original (neg)", mode, codeur, tNeg);
        Map<String, Double> mesuresNeg = mesurer(codeur, tNeg);
        afficherMesures("Mesures (ms) pour neg", mesuresNeg);

        // ----------- CAS 3 : Tableau aléatoire mixte -----------
        int[] tRand = genererAleatoire(10, 123, 100);
        System.out.println("\n──────────────────────────────────────────────");
        System.out.println("Cas 3 : Tableau aléatoire mixte");
        runCase("Original (rand)", mode, codeur, tRand);
        Map<String, Double> mesuresRand = mesurer(codeur, tRand);
        afficherMesures("Mesures (ms) pour rand", mesuresRand);

        // ----------- CAS 4 : Exemple overflow officiel -----------
        if ("debordement".equalsIgnoreCase(mode)) {
            System.out.println("\n──────────────────────────────────────────────");
            System.out.println("Cas 4 : Exemple officiel overflow");
            runOverflowExample(codeur);
        }
    }

    /** Génère un tableau d'entiers aléatoires dans l'intervalle [-maxAbs, +maxAbs]. */
    private static int[] genererAleatoire(int n, int graine, int maxAbs) {
        Random r = new Random(graine);
        int[] t = new int[n];
        for (int i = 0; i < n; i++) t[i] = r.nextInt(maxAbs * 2 + 1) - maxAbs;
        return t;
    }

    /** Exécute un cas de test standard et affiche le résultat. */
    private static void runCase(String titre, String mode, CodeurBits codeur, int[] data) {
        System.out.println(titre + ": " + Arrays.toString(data));
        boolean ok = executerEtVerifier(mode, codeur, data);
        if (!ok) System.out.println("❌ ERREUR: round-trip invalide sur " + titre);
    }

    /** Version spéciale qui affiche aussi le tableau compressé (utile pour les négatifs). */
    private static void runCaseEtAfficherCompression(String titre, String mode, CodeurBits codeur, int[] data) {
        System.out.println(titre + ": " + Arrays.toString(data));
        int[] compresse = codeur.compresser(data);
        System.out.println("Tableau compressé : " + Arrays.toString(compresse));
        int[] decompresse = codeur.decompresser(compresse);
        System.out.println("Tableau décompressé : " + Arrays.toString(decompresse));

        int i = Math.max(0, (data.length - 1) / 2);
        int val = codeur.acceder(i);
        boolean ok = Arrays.equals(data, decompresse);
        System.out.printf("mode=%s get(%d)=%d ok=%b%n", mode, i, val, ok);
    }

    /** Teste la compression et la décompression d’un tableau. */
    private static boolean executerEtVerifier(String mode, CodeurBits codeur, int[] donnees) {
        int[] compresse = codeur.compresser(donnees);
        int i = Math.max(0, (donnees.length - 1) / 2);
        int val = codeur.acceder(i);
        int[] dec = codeur.decompresser(compresse);
        boolean ok = Arrays.equals(donnees, dec);
        System.out.printf("mode=%s get(%d)=%d ok=%b%n", mode, i, val, ok);
        return ok;
    }

    /** Mesure les temps d’exécution des trois fonctions principales du codeur. */
    private static Map<String, Double> mesurer(CodeurBits codeur, int[] donnees) {
        Map<String, Double> m = new LinkedHashMap<>();
        final int REP = 20;
        m.put("compresser_ms", BenchProto.mesureMoyenneMs(() -> codeur.compresser(donnees), REP));
        int[] comp = codeur.compresser(donnees);
        m.put("decompresser_ms", BenchProto.mesureMoyenneMs(() -> codeur.decompresser(comp), REP));
        m.put("acceder_ms", BenchProto.mesureMoyenneMs(() -> codeur.acceder(donnees.length / 2), REP));
        return m;
    }

    /** Affiche les mesures avec un libellé (pos/neg/rand). */
    private static void afficherMesures(String label, Map<String, Double> mesures) {
        System.out.println(label + " : " + mesures);
    }

    /** Exécute l’exemple officiel de l’énoncé pour le mode "overflow". */
    private static void runOverflowExample(CodeurBits codeur) {
        int[] ex = {1, 2, 3, 1024, 4, 5, 2048};
        System.out.println("Exemple overflow : " + Arrays.toString(ex));
        int[] comp = codeur.compresser(ex);
        System.out.println("Tableau compressé : " + Arrays.toString(comp));
        int[] dec = codeur.decompresser(comp);
        System.out.println("Restauration     : " + Arrays.toString(dec));
        System.out.printf("Accès direct ex.: i=3 -> %d, i=6 -> %d%n",
                codeur.acceder(3), codeur.acceder(6));
    }
}
