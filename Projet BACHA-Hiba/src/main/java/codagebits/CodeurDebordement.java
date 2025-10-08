package codagebits;

import java.util.Objects;

/**
 * Débordement (overflow) + gestion des négatifs via offset.
 *
 * Encodage par élément (kBase bits) :
 *  - 1 bit tag : 0 => valeur directe (kBase-1 bits), 1 => index overflow (kBase-1 bits)
 *  - zone overflow à la fin (valeurs stockées en 32 bits)
 *
 * En-tête : [0]=n, [1]=kBase, [2]=flags(2=overflow), [3]=overflowCount, [4]=indexBits, [5]=offset
 */
public class CodeurDebordement implements CodeurBits {

    private static final int ENTETE = 6;
    private static final int FLAG   = 2;

    private int[] tampon;
    private int n, kBase, overflowCount, indexBits, offset;

    @Override
    public int[] compresser(int[] tableau) {
        Objects.requireNonNull(tableau, "tableau");
        n = tableau.length;

        // min / max et offset
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int v : tableau) { if (v < min) min = v; if (v > max) max = v; }
        offset = (min < 0 ? -min : 0);

        // tableau décalé (>=0)
        int[] pos = new int[n];
        for (int i=0;i<n;i++) pos[i] = tableau[i] + offset;

        // cas trivial
        if (n == 0) {
            kBase = 1; overflowCount = 0; indexBits = 1;
            tampon = new int[ENTETE + 1];
            tampon[0]=0; tampon[1]=kBase; tampon[2]=FLAG; tampon[3]=overflowCount; tampon[4]=indexBits; tampon[5]=offset;
            return tampon;
        }

        // choisir kBase (1..31) : minimiser taille totale sous contrainte index
        int bestK = 1; long bestBits = Long.MAX_VALUE; int bestOverflow = 0;
        for (int kb = 1; kb <= 31; kb++) {
            int maxSmall = (kb == 1) ? -1 : ((1 << (kb - 1)) - 1);
            int over = 0;
            if (kb == 1) over = n; else for (int v : pos) if (v > maxSmall) over++;

            long cap = (kb == 1) ? 1 : (1L << (kb - 1));
            if (over > cap) continue;

            long dataBits   = (long) n * kb;
            long overBits   = (long) over * 32;
            long headerBits = (long) ENTETE * 32;
            long total      = headerBits + dataBits + overBits;

            if (total < bestBits) { bestBits = total; bestK = kb; bestOverflow = over; }
        }

        kBase = bestK;
        overflowCount = bestOverflow;
        indexBits = Math.max(1, OutilsBits.bitsNecessaires(overflowCount == 0 ? 1 : overflowCount - 1));

        long dataBits = (long) n * kBase;
        int dataInts  = (int) ((dataBits + 31) >>> 5);

        tampon = new int[ENTETE + Math.max(1, dataInts) + Math.max(0, overflowCount)];
        tampon[0] = n;
        tampon[1] = kBase;
        tampon[2] = FLAG;
        tampon[3] = overflowCount;
        tampon[4] = indexBits;
        tampon[5] = offset; // <-- stocke l'offset

        int overflowBase = ENTETE + Math.max(1, dataInts);
        int overIdx = 0;
        long bitPos = ((long) ENTETE) << 5;
        int maxSmall = (kBase == 1) ? -1 : ((1 << (kBase - 1)) - 1);

        for (int vPos : pos) {
            if (kBase == 1 || vPos > maxSmall) {
                // tag=1 + index overflow
                OutilsBits.ecrireBits(tampon, bitPos, 1, 1);
                OutilsBits.ecrireBits(tampon, bitPos + 1, Math.max(0, kBase - 1), overIdx);
                tampon[overflowBase + overIdx] = vPos; // valeur décalée stockée en clair
                overIdx++;
            } else {
                // tag=0 + valeur directe
                OutilsBits.ecrireBits(tampon, bitPos, 1, 0);
                OutilsBits.ecrireBits(tampon, bitPos + 1, kBase - 1, vPos);
            }
            bitPos += kBase;
        }
        return tampon;
    }

    @Override
    public int[] decompresser(int[] compresse) {
        Objects.requireNonNull(compresse, "compresse");
        tampon = compresse;
        if (tampon.length < ENTETE) throw new IllegalArgumentException("buffer invalide");

        n            = tampon[0];
        kBase        = tampon[1];
        // tampon[2] = FLAG
        overflowCount= tampon[3];
        indexBits    = tampon[4];
        offset       = tampon[5];

        int[] out = new int[n];

        long bitPos = ((long) ENTETE) << 5;
        int dataInts = (int) ((((long) n * kBase) + 31) >>> 5);
        int overflowBase = ENTETE + Math.max(1, dataInts);

        for (int i = 0; i < n; i++) {
            int tag = OutilsBits.lireBits(tampon, bitPos, 1);
            int payload = (kBase == 1 ? 0 : OutilsBits.lireBits(tampon, bitPos + 1, kBase - 1));
            int vPos = (tag == 0) ? payload : tampon[overflowBase + payload];
            out[i] = vPos - offset; // enlève l'offset pour retrouver la valeur signée
            bitPos += kBase;
        }
        return out;
    }

    @Override
    public int acceder(int index) {
        if (tampon == null) throw new IllegalStateException("Aucun buffer compressé");
        if (index < 0 || index >= n) throw new IndexOutOfBoundsException();

        long bitPos = (((long) ENTETE) << 5) + (long) index * kBase;
        int tag = OutilsBits.lireBits(tampon, bitPos, 1);
        int payload = (kBase == 1 ? 0 : OutilsBits.lireBits(tampon, bitPos + 1, kBase - 1));
        if (tag == 0) return payload - offset;

        int dataInts = (int) ((((long) n * kBase) + 31) >>> 5);
        int overflowBase = ENTETE + Math.max(1, dataInts);
        int vPos = tampon[overflowBase + payload];
        return vPos - offset;
    }
}
