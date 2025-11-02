package codagebits;

public final class FactoryCodeurBits {
    private FactoryCodeurBits() {}

    public static CodeurBits creer(String mode) {
        if (mode == null) throw new IllegalArgumentException("Mode null");
        String m = mode.toLowerCase();
        if ("sans".equals(m))        return new CodeurSansChev();
        if ("avec".equals(m))        return new CodeurAvecChev();
        if ("debordement".equals(m)) return new CodeurDebordement();
        throw new IllegalArgumentException("Mode inconnu: " + mode);
        }
}