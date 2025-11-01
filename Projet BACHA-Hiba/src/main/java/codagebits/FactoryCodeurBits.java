package codagebits;

public final class FactoryCodeurBits {
    private FactoryCodeurBits(){}

    public static CodeurBits creer(String mode) {
        return switch (mode.toLowerCase()) {
            case "sans"        -> new CodeurSansChev();
            case "avec"        -> new CodeurAvecChev();
            case "debordement" -> new CodeurDebordement();
            default -> throw new IllegalArgumentException("Mode inconnu: " + mode);
        };
    }
}
