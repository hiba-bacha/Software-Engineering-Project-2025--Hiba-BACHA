package codagebits;

public class CodeurSansChev implements CodeurBits {
    private int[] tampon; 
    private int n; 
    private int k; 
    private int offset; // <-- ajout pour négatifs

    // En-tête : [0]=n, [1]=k, [2]=flags(0=sans chev), [3]=offset
    private static final int ENTETE = 4;

    @Override
    public int[] compresser(int[] tableau){
        if (tableau == null) throw new IllegalArgumentException("tableau null");
        n = tableau.length;

        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int v : tableau) { 
            if (v < min) min = v; 
            if (v > max) max = v; 
        }
        offset = (min < 0 ? -min : 0);

        // valeurs décalées (>=0)
        int[] pos = new int[n];
        for (int i=0;i<n;i++) pos[i] = tableau[i] + offset;

        int maxPos = (n==0?0:0);
        for (int v : pos) if (v > maxPos) maxPos = v;

        k = OutilsBits.bitsNecessaires(maxPos);
        int parMot = Math.max(1, 32/Math.max(1,k));
        int nbMots = (n + parMot - 1) / parMot;

        tampon = new int[ENTETE + Math.max(1, nbMots)];
        tampon[0]=n; tampon[1]=k; tampon[2]=0; tampon[3]=offset;

        int idx=0;
        for (int bloc=0; bloc<nbMots; bloc++){
            int acc=0, shift=0;
            for (int j=0; j<parMot && idx<n; j++, idx++){
                acc |= (pos[idx] & OutilsBits.masque(k)) << shift;
                shift += k;
            }
            tampon[ENTETE + bloc] = acc;
        }
        return tampon;
    }

    @Override
    public int[] decompresser(int[] compresse){
        if (compresse == null || compresse.length < ENTETE) 
            throw new IllegalArgumentException("buffer invalide");
        tampon = compresse; 
        n = tampon[0]; 
        k = tampon[1]; 
        offset = tampon[3];

        int[] pos = new int[n];
        int parMot = Math.max(1, 32/Math.max(1,k));
        int idx=0;
        for (int bloc=0; idx<n; bloc++){
            int acc = tampon[ENTETE + bloc];
            for (int j=0; j<parMot && idx<n; j++, idx++){
                pos[idx] = acc & OutilsBits.masque(k);
                acc >>>= k;
            }
        }
        // retire l'offset
        for (int i=0;i<n;i++) pos[i] -= offset;
        return pos;
    }

    @Override
    public int acceder(int index){
        if (index < 0 || index >= n) throw new IndexOutOfBoundsException();
        int parMot = Math.max(1, 32/Math.max(1,k));
        int bloc = index / parMot, pos = index % parMot;
        int acc = tampon[ENTETE + bloc];
        acc >>>= (pos * k);
        int valPos = acc & OutilsBits.masque(k);
        return valPos - offset; // reconstitue la valeur signée
    }
}
