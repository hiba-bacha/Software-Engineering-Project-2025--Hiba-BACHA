package app;

public final class BenchProto {
    private BenchProto() {}

    public static double mesureMoyenneMs(Runnable r, int repetitions) {
        // petit échauffement JVM
        for (int i = 0; i < 3; i++) r.run();

        long start, end;
        double totalMs = 0.0;
        for (int i = 0; i < repetitions; i++) {
            start = System.nanoTime();
            r.run();
            end = System.nanoTime();
            totalMs += (end - start) / 1e6;
        }
        return totalMs / repetitions;
    }
}
