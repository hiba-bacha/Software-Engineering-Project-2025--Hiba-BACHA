package app;

import codagebits.*;
import java.util.*;

/**
 * Classe de démonstration principale du projet Bit Packing.
 * <p>
 * Elle teste les trois variantes du codeur (sans chevauchement, avec chevauchement et overflow),
 * affiche les résultats, et mesure les performances.
 * </p>
 */
public class DemoEnonce {

    /**
     * Méthode principale exécutant la démonstration du mode spécifié.
     *
     * @param args Mode de test : "sans", "avec" ou "debordement".
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Usage: java -cp out app.DemoEnonce [sans|avec|debordement]");
            return;
        }

        String mode = args[0];
        CodeurBits codeur = FactoryCodeurBits.creer(mode);

        int[] tNeg = {-5, -1, 0, 3, 7};
        runCase("Original (neg)", mode, codeur, tNeg);

        int[] tRand = genererAleatoire(10, 123, 100);
        runCase("Original (rand)", mode, codeur, tRand);

        Map<String, Double> mesures = mesurer(codeur, tRand);
        afficherMesures(mesures);

        if ("debordement".equals(mode)) {
            runOverflowExample(codeur);
        }
    }

    /**
     * Génère un tableau d'entiers aléatoires dans l'intervalle [-maxAbs, +maxAbs].
     *
     * @param n      Nombre d'éléments à générer.
     * @param graine Graine du générateur aléatoire.
     * @param maxAbs Valeur absolue maximale.
     * @return Tableau d'entiers généré.
     */
    private static int[] genererAleatoire(int n, int graine, int maxAbs) {
        Random r = new Random(graine);
        int[] t = new int[n];
        for (int i = 0; i < n; i++) t[i] = r.nextInt(maxAbs * 2 + 1) - maxAbs;
        return t;
    }

    /**
     * Exécute un cas de test spécifique et affiche le résultat.
     *
     * @param titre  Nom du test affiché.
     * @param mode   Mode du codeur.
     * @param codeur Instance du codeur.
     * @param data   Tableau à tester.
     */
    private static void runCase(String titre, String mode, CodeurBits codeur, int[] data) {
        System.out.println(titre + ": " + Arrays.toString(data));
        boolean ok = executerEtVerifier(mode, codeur, data);
        if (!ok) System.out.println("ERREUR: round-trip invalide sur " + titre);
    }

    /**
     * Teste la compression et la décompression d’un tableau,
     * et vérifie si la valeur accédée est correcte.
     *
     * @param mode    Mode utilisé ("sans", "avec" ou "debordement")
     * @param codeur  Instance du codeur
     * @param donnees Tableau de test
     * @return {@code true} si la compression et la décompression sont correctes, sinon {@code false}.
     */
    private static boolean executerEtVerifier(String mode, CodeurBits codeur, int[] donnees) {
        int[] compresse = codeur.compresser(donnees);
        int i = Math.max(0, (donnees.length - 1) / 2);
        int val = codeur.acceder(i);
        int[] dec = codeur.decompresser(compresse);

        boolean ok = Arrays.equals(donnees, dec);
        System.out.printf("mode=%s get(%d)=%d ok=%b%n", mode, i, val, ok);
        return ok;
    }

    /**
     * Mesure les temps d’exécution des trois fonctions principales du codeur :
     * compresser, decompresser et acceder.
     *
     * @param codeur  Codeur à tester.
     * @param donnees Données d'entrée.
     * @return Un dictionnaire associant les temps moyens en millisecondes.
     */
    private static Map<String, Double> mesurer(CodeurBits codeur, int[] donnees) {
        Map<String, Double> m = new LinkedHashMap<>();
        final int REP = 20;
        m.put("compresser_ms", BenchProto.mesureMoyenneMs(() -> codeur.compresser(donnees), REP));
        int[] comp = codeur.compresser(donnees);
        m.put("decompresser_ms", BenchProto.mesureMoyenneMs(() -> codeur.decompresser(comp), REP));
        m.put("acceder_ms", BenchProto.mesureMoyenneMs(() -> codeur.acceder(donnees.length / 2), REP));
        return m;
    }

    /**
     * Affiche les mesures de performance.
     *
     * @param mesures Dictionnaire contenant les temps d’exécution.
     */
    private static void afficherMesures(Map<String, Double> mesures) {
        System.out.println("Mesures (ms) : " + mesures);
    }

    /**
     * Exécute l’exemple officiel de l’énoncé pour le mode "overflow".
     *
     * @param codeur Codeur avec débordement à tester.
     */
    private static void runOverflowExample(CodeurBits codeur) {
        int[] ex = {1, 2, 3, 1024, 4, 5, 2048};
        System.out.println("Exemple overflow : " + Arrays.toString(ex));
        int[] comp = codeur.compresser(ex);
        int[] dec = codeur.decompresser(comp);
        System.out.println("Restauration     : " + Arrays.toString(dec));
        System.out.printf("Acces direct ex.: i=3 -> %d, i=6 -> %d%n", codeur.acceder(3), codeur.acceder(6));
    }
}
