package codagebits;

/**
 * Codeur d'entiers "Sans Chevauchement".
 * <p>
 * Principe : chaque valeur est encodée sur {@code k} bits sans franchir les frontières
 * de mot (32 bits). On range {@code floor(32/k)} valeurs par int. Quelques bits
 * peuvent être perdus en fin de bloc.
 * </p>
 * <p>
 * Gestion des négatifs : un {@code offset = -min} est appliqué à la compression
 * puis retiré à la décompression et à l'accès direct.
 * </p>
 */
public class CodeurSansChev implements CodeurBits {

    /** Tampon compressé (inclut l'en-tête). */
    private int[] tampon;
    /** Taille logique du tableau source. */
    private int n;
    /** Nombre de bits par valeur (hors offset). */
    private int k;
    /** Décalage appliqué pour gérer les valeurs négatives. */
    private int offset;

    // En-tête : [0]=n, [1]=k, [2]=flags(0), [3]=offset
    private static final int ENTETE = 4;
    private static final int FLAG = 0;

    // ---------------- API ----------------

    /**
     * Compresse un tableau d'entiers en mode "sans chevauchement".
     * <p>
     * Étapes : validation → calcul offset → application offset → calcul de k →
     * allocation du tampon + écriture en-tête → empaquetage bloc par bloc.
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

        int parMot = Math.max(1, 32 / Math.max(1, k));
        int nbMots = (n + parMot - 1) / parMot;

        allouerTampon(nbMots);
        ecrireEntete();

        packerSansChevauchement(pos, parMot);
        return tampon;
    }

    /**
     * Décompresse un tampon produit par {@link #compresser(int[])}.
     * <p>
     * Étapes : validation → lecture de l'en-tête → dépaquetage bloc par bloc →
     * retrait de l'offset.
     * </p>
     *
     * @param compresse tampon compressé (avec en-tête)
     * @return tableau d'entiers d'origine
     * @throws IllegalArgumentException si le tampon est nul ou trop court
     */
    @Override
    public int[] decompresser(int[] compresse) {
        validerTampon(compresse);
        lireEntete(compresse);

        int[] pos = unpackSansChevauchement();
        retirerOffsetInPlace(pos, offset);
        return pos;
    }

    /**
     * Accède directement à l'élément {@code index} sans décompresser tout le tableau.
     * <p>
     * Calcule le bloc ({@code index/parMot}), le décalage interne, lit {@code k} bits,
     * puis retire l'offset.
     * </p>
     *
     * @param index position logique demandée (0 ≤ index &lt; n)
     * @return valeur d'origine à l'index
     * @throws IndexOutOfBoundsException si {@code index} est hors limites
     */
    @Override
    public int acceder(int index) {
        if (index < 0 || index >= n) throw new IndexOutOfBoundsException();
        int parMot = Math.max(1, 32 / Math.max(1, k));
        int bloc = index / parMot;
        int pos = index % parMot;
        int acc = tampon[ENTETE + bloc];
        acc >>>= (pos * k);
        int valPos = acc & OutilsBits.masque(k);
        return valPos - offset;
    }

    // ------------- helpers (lisibilité) -------------

    /**
     * Valide un tableau d'entrée.
     *
     * @param t tableau à vérifier
     * @throws IllegalArgumentException si {@code t} est {@code null}
     */
    private static void validerEntree(int[] t) {
        if (t == null) throw new IllegalArgumentException("tableau null");
    }

    /**
     * Valide le tampon compressé.
     *
     * @param t tampon à vérifier
     * @throws IllegalArgumentException si {@code t} est {@code null} ou trop court
     */
    private static void validerTampon(int[] t) {
        if (t == null || t.length < ENTETE) throw new IllegalArgumentException("tampon invalide");
    }

    /**
     * Calcule l'offset nécessaire pour rendre le tableau non négatif.
     *
     * @param t tableau source
     * @return {@code -min(t)} si un élément est négatif, sinon 0
     */
    private static int calculerOffset(int[] t) {
        int min = Integer.MAX_VALUE;
        for (int v : t) if (v < min) min = v;
        return (min < 0) ? -min : 0;
    }

    /**
     * Applique l'offset en retournant une copie positive du tableau.
     *
     * @param t      tableau source
     * @param offset décalage à ajouter
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
     * @param t      tableau transformé
     * @param offset décalage à retirer
     */
    private static void retirerOffsetInPlace(int[] t, int offset) {
        for (int i = 0; i < t.length; i++) t[i] -= offset;
    }

    /**
     * Retourne le maximum d'un tableau (ou 0 si vide).
     *
     * @param t tableau
     * @return max
     */
    private static int max(int[] t) {
        int m = 0;
        for (int v : t) if (v > m) m = v;
        return m;
    }

    /**
     * Alloue le tampon compressé (en-tête + blocs).
     *
     * @param nbMots nombre d'ints pour la zone data
     */
    private void allouerTampon(int nbMots) {
        this.tampon = new int[ENTETE + Math.max(1, nbMots)];
    }

    /** Écrit l'en-tête dans {@link #tampon}. */
    private void ecrireEntete() {
        tampon[0] = n;
        tampon[1] = k;
        tampon[2] = FLAG;
        tampon[3] = offset;
    }

    /**
     * Lit l'en-tête depuis un tampon compressé.
     *
     * @param compresse tampon avec en-tête
     */
    private void lireEntete(int[] compresse) {
        this.tampon = compresse;
        this.n = tampon[0];
        this.k = tampon[1];
        this.offset = tampon[3];
    }

    /**
     * Écrit les valeurs positives {@code pos} bloc par bloc, sans chevauchement.
     *
     * @param pos    valeurs positives
     * @param parMot nb de valeurs par mot (floor(32/k))
     */
    private void packerSansChevauchement(int[] pos, int parMot) {
        int idx = 0;
        for (int bloc = 0; bloc < (tampon.length - ENTETE); bloc++) {
            int acc = 0, shift = 0;
            for (int j = 0; j < parMot && idx < n; j++, idx++) {
                acc |= (pos[idx] & OutilsBits.masque(k)) << shift;
                shift += k;
            }
            tampon[ENTETE + bloc] = acc;
        }
    }

    /**
     * Dépaquette la zone data en restituant les valeurs positives.
     *
     * @return tableau positif (offset non retiré)
     */
    private int[] unpackSansChevauchement() {
        int[] out = new int[n];
        int parMot = Math.max(1, 32 / Math.max(1, k));
        int idx = 0;
        for (int bloc = 0; idx < n; bloc++) {
            int acc = tampon[ENTETE + bloc];
            for (int j = 0; j < parMot && idx < n; j++, idx++) {
                out[idx] = acc & OutilsBits.masque(k);
                acc >>>= k;
            }
        }
        return out;
    }
}
