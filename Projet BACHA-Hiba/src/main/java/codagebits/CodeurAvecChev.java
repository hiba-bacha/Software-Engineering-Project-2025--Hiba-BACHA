package codagebits;

public class CodeurAvecChev implements CodeurBits {
    private int[] tampon; 
    private int n; 
    private int k; 
    private int offset; // <-- ajout

    // En-tête : [0]=n, [1]=k, [2]=flags(1=avec chev), [3]=offset
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

        int[] pos = new int[n];
        for (int i=0;i<n;i++) pos[i] = tableau[i] + offset;

        int maxPos = (n==0?0:0);
        for (int v : pos) if (v > maxPos) maxPos = v;

        k = OutilsBits.bitsNecessaires(maxPos);

        long totalBits = (long)n * k;
        int dataInts = (int)((totalBits + 31) >>> 5);

        tampon = new int[ENTETE + Math.max(1, dataInts)];
        tampon[0]=n; tampon[1]=k; tampon[2]=1; tampon[3]=offset;

        long bitPos = ((long)ENTETE) << 5;
        for (int v : pos){
            OutilsBits.ecrireBits(tampon, bitPos, k, v);
            bitPos += k;
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
        long bitPos = ((long)ENTETE) << 5;
        for (int i=0;i<n;i++){
            pos[i] = OutilsBits.lireBits(tampon, bitPos, k);
            bitPos += k;
        }
        for (int i=0;i<n;i++) pos[i] -= offset; // retire l'offset
        return pos;
    }

    @Override
    public int acceder(int index){
        if (tampon == null) throw new IllegalStateException("Aucun buffer compressé");
        if (index < 0 || index >= n) throw new IndexOutOfBoundsException();

        long bitPos = (((long)ENTETE) << 5) + (long)index * k;
        int valPos = OutilsBits.lireBits(tampon, bitPos, k);
        return valPos - offset;
    }
}
