package app;

import java.util.Scanner;

/**
 * Classe principale du projet Bit Packing.
 * <p>
 * Elle affiche un menu interactif pour exécuter la démonstration dans l’un des trois modes :
 * sans chevauchement, avec chevauchement ou avec débordement (overflow).
 * </p>
 */
public class Main {

    /**
     * Point d'entrée du programme.
     * <p>
     * Si aucun argument n'est passé, un menu est affiché pour choisir le mode.
     * Sinon, l'argument est directement utilisé pour lancer la démonstration.
     * </p>
     *
     * @param args Argument optionnel : "sans", "avec" ou "debordement".
     */
    public static void main(String[] args) {
        printHeader();
        String mode = (args != null && args.length == 1) ? args[0] : askMode();
        runDemo(mode);
        printFooter();
    }

    /** 
     * Affiche l’en-tête du programme.
     */
    private static void printHeader() {
        System.out.println("====================================");
        System.out.println("       Projet Bit Packing - 2025     ");
        System.out.println("====================================");
    }

    /** 
     * Affiche le message de fin d’exécution.
     */
    private static void printFooter() {
        System.out.println("\nFin de l'exécution. Merci d'avoir testé !");
    }

    /**
     * Affiche un menu permettant de choisir le mode d’exécution.
     *
     * @return une chaîne correspondant au mode choisi ("sans", "avec" ou "debordement")
     */
    private static String askMode() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Choisissez le mode d'exécution :");
            System.out.println("1 - Sans chevauchement");
            System.out.println("2 - Avec chevauchement");
            System.out.println("3 - Avec débordement (overflow)");
            System.out.print("Votre choix : ");
            int choix = sc.nextInt();
            return switch (choix) {
                case 1 -> "sans";
                case 2 -> "avec";
                case 3 -> "debordement";
                default -> {
                    System.out.println("Choix invalide. Utilisation du mode 'sans'.");
                    yield "sans";
                }
            };
        }
    }

    /**
     * Lance la démonstration correspondant au mode choisi.
     *
     * @param mode Mode d’exécution ("sans", "avec" ou "debordement")
     */
    private static void runDemo(String mode) {
        System.out.println("\n--- Exécution du mode : " + mode + " ---\n");
        DemoEnonce.main(new String[]{mode});
    }
}
