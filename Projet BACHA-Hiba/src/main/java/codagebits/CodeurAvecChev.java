package codagebits;

/**
 * Codeur d'entiers "Avec Chevauchement".
 * <p>
 * Principe : les valeurs sont écrites dans un flux binaire continu, sans se soucier
 * des frontières de mots (32 bits). Une valeur peut être répartie sur deux mots.
 * </p>
 * <p>
 * Gestion des négatifs : un {@code offset = -min} est appliqué à la compression
 * puis retiré à la décompression et à l'accès direct.
 * </p>
 */
public class CodeurAvecChev implements CodeurBits {

    /** Tampon compressé (inclut l'en-tête). */
    private int[] tampon;
    /** Taille logique. */
    private int n;
    /** Bits par valeur. */
    private int k;
    /** Offset appliqué pour négatifs. */
    private int offset;

    // En-tête : [0]=n, [1]=k, [2]=flags(1), [3]=offset
    private static final int ENTETE = 4;
    private static final int FLAG = 1;

    /**
     * Compresse un tableau en flux binaire continu (avec chevauchement).
     * <p>
     * Étapes : validation → offset → k → allocation (taille en bits arrondie aux 32 bits) →
     * écriture séquentielle avec {@link OutilsBits#ecrireBits(int[], long, int, int)}.
     * </p>
     *
     * @param tableau tableau d'entrée (peut contenir des négatifs)
     * @return tampon compressé incluant l'en-tête
     * @throws IllegalArgumentException si {@code tableau} est {@code null}
     */
    @Override
    public int[] compresser(int[] tableau) {
        validerEntree(tableau);
        this.n = tableau.length;

        this.offset = calculerOffset(tableau);
        int[] pos = appliquerOffset(tableau, offset);

        int maxPos = max(pos);
        this.k = OutilsBits.bitsNecessaires(maxPos);

        long totalBits = (long) n * k;
        int dataInts = (int) ((totalBits + 31) >>> 5);

        this.tampon = new int[ENTETE + Math.max(1, dataInts)];
        ecrireEntete();

        ecrireFlux(pos);
        return tampon;
    }

    /**
     * Décompresse un tampon produit par {@link #compresser(int[])}.
     *
     * @param compresse tampon compressé (avec en-tête)
     * @return tableau d'origine
     * @throws IllegalArgumentException si le tampon est invalide
     */
    @Override
    public int[] decompresser(int[] compresse) {
        validerTampon(compresse);
        lireEntete(compresse);

        int[] pos = new int[n];
        lireFlux(pos);
        retirerOffsetInPlace(pos, offset);
        return pos;
    }

    /**
     * Accède à l'élément {@code index} à partir du flux binaire compressé.
     * <p>
     * Calcule la position en bits {@code bitPos = headerBits + index*k} puis lit {@code k} bits.
     * </p>
     *
     * @param index position logique (0 ≤ index &lt; n)
     * @return valeur d'origine à l'index
     * @throws IndexOutOfBoundsException si l'index est hors bornes
     */
    @Override
    public int acceder(int index) {
        if (index < 0 || index >= n) throw new IndexOutOfBoundsException();
        long bitPos = debutFluxBits() + (long) index * k;
        int valPos = OutilsBits.lireBits(tampon, bitPos, k);
        return valPos - offset;
    }

    // ---------------- helpers ----------------

    /**
     * Vérifie que le tableau d'entrée n'est pas nul.
     *
     * @param t tableau
     */
    private static void validerEntree(int[] t) {
        if (t == null) throw new IllegalArgumentException("tableau null");
    }

    /**
     * Vérifie que le tampon compressé est valable (taille en-tête).
     *
     * @param t tampon
     */
    private static void validerTampon(int[] t) {
        if (t == null || t.length < ENTETE) throw new IllegalArgumentException("tampon invalide");
    }

    /**
     * Calcule l'offset pour rendre le tableau non négatif.
     *
     * @param t tableau source
     * @return offset {@code -min} ou 0
     */
    private static int calculerOffset(int[] t) {
        int min = Integer.MAX_VALUE;
        for (int v : t) if (v < min) min = v;
        return (min < 0) ? -min : 0;
    }

    /**
     * Retourne une copie avec offset appliqué.
     *
     * @param t      tableau
     * @param offset décalage
     * @return copie positive
     */
    private static int[] appliquerOffset(int[] t, int offset) {
        int[] r = new int[t.length];
        for (int i = 0; i < t.length; i++) r[i] = t[i] + offset;
        return r;
    }

    /**
     * Retire l'offset en place.
     *
     * @param t      tableau
     * @param offset décalage
     */
    private static void retirerOffsetInPlace(int[] t, int offset) {
        for (int i = 0; i < t.length; i++) t[i] -= offset;
    }

    /**
     * Retourne le max d'un tableau (ou 0 si vide).
     *
     * @param t tableau
     * @return max
     */
    private static int max(int[] t) {
        int m = 0;
        for (int v : t) if (v > m) m = v;
        return m;
    }

    /** Écrit l'en-tête. */
    private void ecrireEntete() {
        tampon[0] = n;
        tampon[1] = k;
        tampon[2] = FLAG;
        tampon[3] = offset;
    }

    /**
     * Lit l'en-tête du tampon compressé.
     *
     * @param compresse tampon
     */
    private void lireEntete(int[] compresse) {
        this.tampon = compresse;
        this.n = tampon[0];
        this.k = tampon[1];
        this.offset = tampon[3];
    }

    /**
     * Position du début de la zone data en bits.
     *
     * @return index de bit de début de flux
     */
    private long debutFluxBits() {
        return ((long) ENTETE) << 5;
    }

    /**
     * Écrit séquentiellement chaque valeur positive dans le flux binaire.
     *
     * @param pos valeurs positives (offset appliqué)
     */
    private void ecrireFlux(int[] pos) {
        long bitPos = debutFluxBits();
        for (int v : pos) {
            OutilsBits.ecrireBits(tampon, bitPos, k, v);
            bitPos += k;
        }
    }

    /**
     * Lit séquentiellement chaque valeur positive depuis le flux binaire.
     *
     * @param out tableau de sortie (positif, avant retrait offset)
     */
    private void lireFlux(int[] out) {
        long bitPos = debutFluxBits();
        for (int i = 0; i < n; i++) {
            out[i] = OutilsBits.lireBits(tampon, bitPos, k);
            bitPos += k;
        }
    }
}
