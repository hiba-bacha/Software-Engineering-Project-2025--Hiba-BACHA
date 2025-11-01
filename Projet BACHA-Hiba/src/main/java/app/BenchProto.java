package app;

/**
 * Outil simple de mesure de performance.
 * <p>
 * Permet d'obtenir un temps moyen d'exécution d'une action en millisecondes.
 * </p>
 */
public final class BenchProto {

    private BenchProto() {}

    /**
     * Mesure le temps moyen (en ms) d'exécution d'une action répétée plusieurs fois.
     * <p>
     * Un léger "warmup" est effectué avant la mesure pour stabiliser la JVM.
     * </p>
     *
     * @param action action à mesurer
     * @param repetitions nombre de répétitions
     * @return temps moyen en millisecondes
     */
    public static double mesureMoyenneMs(Runnable action, int repetitions) {
        if (repetitions <= 0) return 0.0;
        for (int i = 0; i < Math.min(5, repetitions); i++) {
            action.run(); // warmup
        }
        long start = System.nanoTime();
        for (int i = 0; i < repetitions; i++) action.run();
        long end = System.nanoTime();
        return (end - start) / (1_000_000.0 * repetitions);
    }
}
