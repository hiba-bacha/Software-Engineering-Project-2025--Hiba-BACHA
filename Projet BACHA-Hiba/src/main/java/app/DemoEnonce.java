package app;

import codagebits.*;
import java.util.*;

/**
 * Classe de démonstration principale du projet Bit Packing.
 * <p>
 * Elle exécute différents scénarios de compression et affiche :
 * <ul>
 *   <li>Les tailles avant/après compression</li>
 *   <li>Les temps CPU (compresser, décompresser, accéder)</li>
 *   <li>Le temps de transmission réseau simulé (latence + débit)</li>
 *   <li>Un verdict de rentabilité ("OUI" si compression utile)</li>
 * </ul>
 * </p>
 * <p>
 * Arguments :
 * <ul>
 *   <li>args[0] : mode ("sans", "avec" ou "debordement")</li>
 *   <li>args[1] : (optionnel) débit réseau en Mbit/s (ex: 50)</li>
 *   <li>args[2] : (optionnel) latence réseau en millisecondes (ex: 10)</li>
 * </ul>
 * </p>
 */
public class DemoEnonce {

    /** Débit réseau simulé en Mbit/s (modifiable via args[1]). */
    private static double DEBIT_Mbps = 50.0;

    /** Latence réseau simulée en millisecondes (modifiable via args[2]). */
    private static double LATENCE_ms = 10.0;

    /**
     * Point d'entrée principal de la démonstration.
     *
     * @param args paramètres du programme : mode, débit, latence
     */
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        if (args == null || args.length == 0) {
            System.out.println("Usage: java -cp target/classes app.DemoEnonce [sans|avec|debordement] [debit_Mbps] [latence_ms]");
            return;
        }

        String mode = args[0];
        if (args.length >= 2) {
            try { DEBIT_Mbps = Double.parseDouble(args[1]); } catch (Exception ignore) {}
        }
        if (args.length >= 3) {
            try { LATENCE_ms = Double.parseDouble(args[2]); } catch (Exception ignore) {}
        }

        CodeurBits codeur = FactoryCodeurBits.creer(mode);

        // En-tête d’exécution
        System.out.println("Mode         : " + mode);
        System.out.println("Débit (Mb/s) : " + DEBIT_Mbps);
        System.out.println("Latence (ms) : " + LATENCE_ms);
        System.out.println("====================================================");

        // Cas 1 : petit tableau mixte positif
        int[] tPos = {5, 3, 12, 7, 0, 8, 1};
        runScenario("Cas 1 : Tableau positif/mixte", codeur, tPos);

        // Cas 2 : entiers négatifs
        int[] tNeg = {5, -3, 12, -7, 0, -8, 1};
        runScenario("Cas 2 : Tableau avec entiers négatifs", codeur, tNeg);

        // Cas 3 : aléatoire mixte (taille moyenne)
        int[] tRand = genererAleatoire(10, 123, 100);
        runScenario("Cas 3 : Tableau aléatoire mixte", codeur, tRand);

        // Cas 4 : grand tableau (100k) – utile pour voir la rentabilité basculer sur OUI
        int[] big = genererAleatoire(100_000, 42, 50); // k modéré → bon taux
        runScenario("Cas 4 : Grand tableau (100k)", codeur, big);

        // Cas 5 : (optionnel) overflow biaisé – seulement en mode débordement
        if ("debordement".equalsIgnoreCase(mode)) {
            int[] skew = genererBiaiseOverflow(100_000, 7);
            runScenario("Cas 5 : Skew overflow (100k) - 95% petits, 5% énormes", codeur, skew);
            runOverflowExample(codeur); // exemple officiel demandé dans l’énoncé
        }
    }

    /**
     * Exécute un scénario complet de test : compression, décompression, mesure et affichage.
     *
     * @param titre  libellé du scénario
     * @param codeur implémentation du codeur à tester
     * @param data   tableau d'entiers à compresser
     */
    private static void runScenario(String titre, CodeurBits codeur, int[] data) {
        System.out.println("\n────────────────────────────────────────────────────");
        System.out.println(titre);
        System.out.println("n=" + data.length);
        System.out.println("Original        : " + apercu(data, 12)); // petit aperçu pour les gros tableaux

        int[] comp = codeur.compresser(data);
        int i = Math.max(0, (data.length - 1) / 2);
        int val = codeur.acceder(i);
        int[] dec = codeur.decompresser(comp);

        System.out.printf("get(%d)          : %d%n", i, val);
        System.out.println("Round-trip OK   : " + Arrays.equals(data, dec));

        Map<String, Double> mesures = mesurer(codeur, data);
        afficherMesures(mesures);
        afficherTaillesEtTaux(data, comp);
        bilanTransmission(codeur, data, titre);
    }

    /**
     * Génère un tableau d'entiers aléatoires dans un intervalle centré sur zéro.
     *
     * @param n nombre d'éléments
     * @param graine graine aléatoire
     * @param maxAbs valeur absolue maximale
     * @return un tableau d'entiers pseudo-aléatoires
     */
    private static int[] genererAleatoire(int n, int graine, int maxAbs) {
        Random r = new Random(graine);
        int[] t = new int[n];
        for (int i = 0; i < n; i++) t[i] = r.nextInt(maxAbs * 2 + 1) - maxAbs;
        return t;
    }

    /**
     * Génère un tableau biaisé : 95% de petites valeurs (0..7) et 5% de très grandes (≈2^20).
     * <p>Utile pour mettre en évidence l'intérêt du mode "debordement".</p>
     *
     * @param n taille du tableau
     * @param graine graine pseudo-aléatoire
     * @return tableau biaisé
     */
    private static int[] genererBiaiseOverflow(int n, int graine) {
        Random rr = new Random(graine);
        int[] skew = new int[n];
        for (int i = 0; i < n; i++) {
            if (rr.nextInt(100) < 95) skew[i] = rr.nextInt(8);                   // très petit (0..7)
            else                      skew[i] = (1 << 20) + rr.nextInt(1 << 10); // très grand
        }
        return skew;
    }

    /**
     * Mesure les temps moyens (en ms) d’exécution des fonctions principales.
     *
     * @param codeur  codeur testé
     * @param donnees tableau d'entrée
     * @return dictionnaire {compresser, decompresser, acceder}
     */
    private static Map<String, Double> mesurer(CodeurBits codeur, int[] donnees) {
        Map<String, Double> m = new LinkedHashMap<>();
        final int REP = 20;

        m.put("compresser", BenchProto.mesureMoyenneMs(() -> codeur.compresser(donnees), REP));
        final int[] comp = codeur.compresser(donnees);
        m.put("decompresser", BenchProto.mesureMoyenneMs(() -> codeur.decompresser(comp), REP));
        m.put("acceder", BenchProto.mesureMoyenneMs(() -> codeur.acceder(donnees.length / 2), REP));

        return m;
    }

    /**
     * Affiche les mesures CPU.
     *
     * @param mesures dictionnaire des temps moyens
     */
    private static void afficherMesures(Map<String, Double> mesures) {
        System.out.printf(Locale.US, "Mesures (ms)    : {compresser=%.4f, decompresser=%.4f, acceder=%.4f}%n",
                mesures.get("compresser"), mesures.get("decompresser"), mesures.get("acceder"));
    }

    /**
     * Affiche les tailles avant/après compression et le gain en pourcentage.
     *
     * @param original  tableau original
     * @param compresse tableau compressé
     */
    private static void afficherTaillesEtTaux(int[] original, int[] compresse) {
        int to = original.length * 4;
        int tc = compresse.length * 4;
        double taux = (double) tc / Math.max(1, to);
        double gainPct = (1.0 - taux) * 100.0;
        System.out.printf(Locale.US, "Tailles (octets): original=%d, compressé=%d, gain=%.2f%%%n", to, tc, gainPct);
    }

    /**
     * Calcule et affiche :
     * <ul>
     *     <li>Transmission brute (latence + envoi brut)</li>
     *     <li>Transmission compressée totale (compresser + tx + décompresser)</li>
     *     <li>Rentabilité</li>
     * </ul>
     *
     * @param codeur  implémentation utilisée
     * @param donnees tableau d’entrée
     * @param label   étiquette de scénario
     */
    private static void bilanTransmission(CodeurBits codeur, int[] donnees, String label) {
        final int REP = 20;

        int[] comp = codeur.compresser(donnees);
        int to = donnees.length * 4;
        int tc = comp.length * 4;

        double tComp_ms = BenchProto.mesureMoyenneMs(() -> codeur.compresser(donnees), REP);
        double tDec_ms  = BenchProto.mesureMoyenneMs(() -> codeur.decompresser(comp), REP);

        double bitsParMs = (DEBIT_Mbps * 1_000_000.0) / 1000.0;
        double txSans_ms = LATENCE_ms + (to * 8.0) / bitsParMs;
        double txAvec_ms = LATENCE_ms + (tc * 8.0) / bitsParMs;

        double totalBrut_ms = txSans_ms;
        double totalCompresse_ms = tComp_ms + txAvec_ms + tDec_ms;

        boolean rentable = totalCompresse_ms < totalBrut_ms;

        System.out.println("Transmission (ms)");
        System.out.printf(Locale.US, " - brut (sans)           : %.3f%n", totalBrut_ms);
        System.out.printf(Locale.US, " - compressé (comp+tx+dec): %.3f  [comp=%.3f, tx=%.3f, decomp=%.3f]%n",
                totalCompresse_ms, tComp_ms, txAvec_ms, tDec_ms);
        System.out.println("Rentable ? " + (rentable ? "OUI ✅" : "NON ❌"));
    }

    /**
     * Teste l’exemple officiel de l’énoncé pour le mode "overflow".
     *
     * @param codeur instance de CodeurDebordement
     */
    private static void runOverflowExample(CodeurBits codeur) {
        System.out.println("\n────────────────────────────────────────────────────");
        System.out.println("Cas 6 : Exemple officiel (overflow)");
        int[] ex = {1, 2, 3, 1024, 4, 5, 2048};
        System.out.println("n=" + ex.length);
        System.out.println("Original        : " + Arrays.toString(ex));

        int[] comp = codeur.compresser(ex);
        int[] dec  = codeur.decompresser(comp);
        int mid = ex.length / 2;

        System.out.printf("get(%d)          : %d%n", mid, codeur.acceder(mid));
        System.out.println("Round-trip OK   : " + Arrays.equals(ex, dec));
        afficherTaillesEtTaux(ex, comp);
        bilanTransmission(codeur, ex, "overflow-exemple");

        // Si l'en-tête contient ces infos (cas CodeurDebordement fourni)
        if (comp.length >= 6) {
            int kBase = comp[1];
            int overflowCount = comp[3];
            int indexBits = comp[4];
            System.out.printf("Overflow        : count=%d, kBase=%d, indexBits=%d%n",
                    overflowCount, kBase, indexBits);
        }
        System.out.printf("Accès direct    : i=3 -> %d, i=6 -> %d%n",
                codeur.acceder(3), codeur.acceder(6));
    }

    /**
     * Retourne un aperçu lisible d’un grand tableau (premiers/derniers éléments).
     *
     * @param t tableau
     * @param max combien d’éléments maximum afficher
     * @return chaîne sous forme "[a, b, c, ..., y, z]"
     */
    private static String apercu(int[] t, int max) {
        if (t == null) return "[]";
        if (t.length <= max) return Arrays.toString(t);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int head = max / 2;
        int tail = max - head;
        for (int i = 0; i < head; i++) {
            if (i > 0) sb.append(", ");
            sb.append(t[i]);
        }
        sb.append(", ..., ");
        for (int i = t.length - tail; i < t.length; i++) {
            if (i > t.length - tail) sb.append(", ");
            sb.append(t[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
