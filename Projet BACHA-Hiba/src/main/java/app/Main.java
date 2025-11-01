package app;

import java.util.Locale;
import java.util.Scanner;

/**
 * Point d'entrée du programme avec interface console.
 * <p>
 * Permet de choisir le mode de compression ("sans", "avec", "debordement")
 * et, optionnellement, de saisir le débit réseau (Mb/s) et la latence (ms).
 * </p>
 */
public class Main {

    /**
     * Méthode principale interactive.
     *
     * @param args inutilisés (lancement interactif)
     */
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Bit Packing - Lancement ===");
        System.out.println("Choisir un mode de compression :");
        System.out.println("  1) sans chevauchement   (\"sans\")");
        System.out.println("  2) avec chevauchement   (\"avec\")");
        System.out.println("  3) debordement/overflow (\"debordement\")");
        System.out.print("Votre choix [1-3] : ");
        String mode = lireMode(sc);

        System.out.print("Souhaitez-vous saisir le débit (Mb/s) et la latence (ms) ? [o/N] : ");
        String rep = sc.nextLine().trim().toLowerCase();

        if ("o".equals(rep) || "oui".equals(rep) || "y".equals(rep)) {
            double debit = lireDouble(sc, "Débit (Mb/s) [ex: 50] : ", 50.0);
            double lat   = lireDouble(sc, "Latence (ms) [ex: 10] : ", 10.0);
            DemoEnonce.main(new String[]{mode, String.valueOf(debit), String.valueOf(lat)});
        } else {
            DemoEnonce.main(new String[]{mode});
        }
        sc.close();
    }

    /**
     * Lit le choix utilisateur pour déterminer le mode.
     *
     * @param sc scanner console
     * @return "sans", "avec" ou "debordement"
     */
    private static String lireMode(Scanner sc) {
        String choix = sc.nextLine().trim();
        if (choix.isEmpty()) choix = "1";
        switch (choix) {
            case "1": return "sans";
            case "2": return "avec";
            case "3": return "debordement";
            default:
                System.out.println("Choix invalide, mode 'sans' sélectionné par défaut.");
                return "sans";
        }
    }

    /**
     * Lit une valeur numérique avec gestion d'erreur.
     *
     * @param sc scanner console
     * @param prompt texte affiché
     * @param def valeur par défaut
     * @return valeur numérique saisie ou défaut
     */
    private static double lireDouble(Scanner sc, String prompt, double def) {
        System.out.print(prompt);
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return def;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            System.out.println("Entrée invalide, valeur par défaut utilisée: " + def);
            return def;
        }
    }
}
