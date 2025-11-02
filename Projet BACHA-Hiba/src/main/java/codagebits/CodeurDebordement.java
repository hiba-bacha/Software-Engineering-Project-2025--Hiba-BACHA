package codagebits;

import java.util.Objects;

/**
 * Codeur d'entiers "Avec Débordement (Overflow)".
 * <p>
 * Principe : chaque élément est encodé sur {@code kBase} bits dont 1 bit de tag.
 * Si tag=0 → la valeur (payload) tient sur {@code kBase-1} bits (valeur "petite").
 * Si tag=1 → le payload est un index vers la zone overflow où la "grande" valeur
 * (32 bits) est stockée.
 * </p>
 * <p>
 * Gestion des négatifs : offset appliqué à la compression, retiré à la décompression
 * et dans l'accès direct.
 * </p>
 */
public class CodeurDebordement implements CodeurBits {

    // En-tête : [0]=n, [1]=kBase, [2]=flags(2), [3]=overflowCount, [4]=indexBits, [5]=offset
    private static final int ENTETE = 6;
    private static final int FLAG = 2;

    private int[] tampon;
    private int n, kBase, overflowCount, indexBits, offset;

    /**
     * Compresse un tableau avec séparation "petites" et "grandes" valeurs.
     * <p>
     * Étapes : validation → offset → choix de {@code kBase} optimal →
     * calcul de la taille → écriture du flux principal (tag+payload) →
     * écriture de la zone overflow pour les grandes valeurs.
     * </p>
     *
     * @param tableau tableau d'entrée
     * @return tampon compressé (en-tête + data + overflow)
     * @throws NullPointerException si {@code tableau} est {@code null}
     */
    @Override
    public int[] compresser(int[] tableau) {
        Objects.requireNonNull(tableau, "tableau");
        this.n = tableau.length;

        this.offset = calculerOffset(tableau);
        int[] pos = appliquerOffset(tableau, offset);

        if (n == 0) return buildVide();

        this.kBase = choisirKBaseOptimal(pos);
        this.overflowCount = compterOverflow(pos, kBase);
        this.indexBits = Math.max(1, OutilsBits.bitsNecessaires(Math.max(1, overflowCount - 1)));

        int dataInts = dataIntsCount(n, kBase);
        this.tampon = new int[ENTETE + Math.max(1, dataInts) + Math.max(0, overflowCount)];

        ecrireEntete();
        ecrireFluxEtOverflow(pos, dataInts);
        return tampon;
    }

    /**
     * Décompresse un tampon produit par {@link #compresser(int[])}.
     *
     * @param compresse tampon compressé
     * @return tableau d'origine
     * @throws NullPointerException     si {@code compresse} est {@code null}
     * @throws IllegalArgumentException si la taille est inférieure à l'en-tête
     */
    @Override
    public int[] decompresser(int[] compresse) {
        Objects.requireNonNull(compresse, "compresse");
        if (compresse.length < ENTETE) throw new IllegalArgumentException("tampon invalide");

        lireEntete(compresse);
        int[] out = new int[n];

        int dataInts = dataIntsCount(n, kBase);
        int overflowBase = ENTETE + Math.max(1, dataInts);

        long bitPos = debutFluxBits();
        for (int i = 0; i < n; i++) {
            int tag = OutilsBits.lireBits(tampon, bitPos, 1);
            int payload = (kBase == 1) ? 0 : OutilsBits.lireBits(tampon, bitPos + 1, kBase - 1);
            int vPos = (tag == 0) ? payload : tampon[overflowBase + payload];
            out[i] = vPos - offset;
            bitPos += kBase;
        }
        return out;
    }

    /**
     * Accès direct à l'élément {@code index}.
     * <p>
     * Lit le tag et le payload à {@code bitPos = headerBits + index * kBase} :
     * si tag=0 → retourne payload-offset ; si tag=1 → charge la valeur depuis la zone overflow.
     * </p>
     *
     * @param index position logique (0 ≤ index &lt; n)
     * @return valeur d'origine
     * @throws IndexOutOfBoundsException si l'index est hors bornes
     */
    @Override
    public int acceder(int index) {
        if (index < 0 || index >= n) throw new IndexOutOfBoundsException();
        long bitPos = debutFluxBits() + (long) index * kBase;
        int tag = OutilsBits.lireBits(tampon, bitPos, 1);
        int payload = (kBase == 1) ? 0 : OutilsBits.lireBits(tampon, bitPos + 1, kBase - 1);
        if (tag == 0) return payload - offset;

        int dataInts = dataIntsCount(n, kBase);
        int overflowBase = ENTETE + Math.max(1, dataInts);
        int vPos = tampon[overflowBase + payload];
        return vPos - offset;
    }

    // ---------------- helpers (lisibilité) ----------------

    /**
     * Calcule l'offset minimal pour rendre le tableau non négatif.
     *
     * @param t tableau
     * @return offset
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
     * Construit un tampon valide pour le cas vide.
     *
     * @return tampon minimal
     */
    private int[] buildVide() {
        this.kBase = 1;
        this.overflowCount = 0;
        this.indexBits = 1;
        this.tampon = new int[ENTETE + 1];
        ecrireEntete();
        return tampon;
    }

    /** Écrit l'en-tête dans le tampon. */
    private void ecrireEntete() {
        tampon[0] = n;
        tampon[1] = kBase;
        tampon[2] = FLAG;
        tampon[3] = overflowCount;
        tampon[4] = indexBits;
        tampon[5] = offset;
    }

    /**
     * Lit l'en-tête depuis {@code src}.
     *
     * @param src tampon compressé
     */
    private void lireEntete(int[] src) {
        this.tampon = src;
        this.n = tampon[0];
        this.kBase = tampon[1];
        // tampon[2] = FLAG;
        this.overflowCount = tampon[3];
        this.indexBits = tampon[4];
        this.offset = tampon[5];
    }

    /**
     * Nombre d'ints nécessaires pour la zone data (flux principal).
     *
     * @param n     nombre d'éléments
     * @param kBase largeur des éléments (incl. tag)
     * @return nombre d'ints data
     */
    private static int dataIntsCount(int n, int kBase) {
        long bits = (long) n * kBase;
        return (int) ((bits + 31) >>> 5);
    }

    /**
     * Début de la zone data en bits (après l'en-tête).
     *
     * @return position de bit
     */
    private long debutFluxBits() {
        return ((long) ENTETE) << 5;
    }

    /**
     * Valeur max "petite" stockable (sans overflow) pour un {@code kBase} donné.
     *
     * @param kBase largeur totale (tag + payload)
     * @return max payload ; -1 si {@code kBase<=1}
     */
    private static int maxSmall(int kBase) {
        return (kBase <= 1) ? -1 : ((1 << (kBase - 1)) - 1);
    }

    /**
     * Compte combien de valeurs dépassent la capacité "petite".
     *
     * @param pos   valeurs positives
     * @param kBase largeur totale (tag + payload)
     * @return nombre d'éléments qui iront en overflow
     */
    private static int compterOverflow(int[] pos, int kBase) {
        if (kBase <= 1) return pos.length;
        int lim = maxSmall(kBase);
        int c = 0;
        for (int v : pos) if (v > lim) c++;
        return c;
    }

    /**
     * Coût total en bits (en-tête + data + overflow).
     *
     * @param n              nombre d'éléments
     * @param kBase          largeur d'encodage
     * @param overflowCount  nombre d'éléments envoyés en overflow
     * @return coût en bits
     */
    private static long coutTotalBits(int n, int kBase, int overflowCount) {
        long header = (long) ENTETE * 32;
        long data = (long) n * kBase;
        long over = (long) overflowCount * 32;
        return header + data + over;
    }

    /**
     * Vérifie que la capacité de payload permet d'indexer tous les overflow.
     *
     * @param n             nombre d'éléments
     * @param kBase         largeur d'encodage
     * @param overflowCount nombre d'overflow
     * @return {@code true} si possible
     */
    private static boolean overflowPossible(int n, int kBase, int overflowCount) {
        long cap = (kBase <= 1) ? 1 : (1L << (kBase - 1));
        return overflowCount <= cap;
    }

    /**
     * Choisit automatiquement le {@code kBase} qui minimise le coût total.
     *
     * @param pos valeurs positives
     * @return {@code kBase} optimal
     */
    private int choisirKBaseOptimal(int[] pos) {
        long bestCost = Long.MAX_VALUE;
        int bestKb = 1;

        for (int kb = 1; kb <= 31; kb++) {
            int over = compterOverflow(pos, kb);
            if (!overflowPossible(pos.length, kb, over)) continue;
            long cost = coutTotalBits(pos.length, kb, over);
            if (cost < bestCost) {
                bestCost = cost;
                bestKb = kb;
            }
        }
        return bestKb;
    }

    /**
     * Écrit le flux principal (tag+payload) et remplit la zone overflow.
     *
     * @param pos      valeurs positives (offset appliqué)
     * @param dataInts taille de la zone data (en ints)
     */
    private void ecrireFluxEtOverflow(int[] pos, int dataInts) {
        int overflowBase = ENTETE + Math.max(1, dataInts);
        int overIdx = 0;
        long bitPos = debutFluxBits();
        int lim = maxSmall(kBase);

        for (int vPos : pos) {
            if (kBase <= 1 || vPos > lim) {
                // tag=1 + index overflow
                OutilsBits.ecrireBits(tampon, bitPos, 1, 1);
                if (kBase > 1) OutilsBits.ecrireBits(tampon, bitPos + 1, kBase - 1, overIdx);
                tampon[overflowBase + overIdx] = vPos;
                overIdx++;
            } else {
                // tag=0 + valeur directe
                OutilsBits.ecrireBits(tampon, bitPos, 1, 0);
                OutilsBits.ecrireBits(tampon, bitPos + 1, kBase - 1, vPos);
            }
            bitPos += kBase;
        }
    }
}
