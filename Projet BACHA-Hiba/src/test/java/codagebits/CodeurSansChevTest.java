package codagebits;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour {@link CodeurSansChev}.
 */
public class CodeurSansChevTest {

    /**
     * Vérifie que le tableau compressé puis décompressé est identique à l'original.
     */
    @Test
    void testCompresserDecompresser() {
        CodeurBits codeur = new CodeurSansChev();
        int[] original = {-5, -1, 0, 3, 7};
        int[] compresse = codeur.compresser(original);
        int[] resultat = codeur.decompresser(compresse);
        assertArrayEquals(original, resultat);
    }

    /**
     * Vérifie que l'accès direct renvoie la bonne valeur.
     */
    @Test
    void testAccesDirect() {
        CodeurBits codeur = new CodeurSansChev();
        int[] original = {10, 20, 30, 40, 50};
        int[] compresse = codeur.compresser(original);
        assertEquals(30, codeur.acceder(2));
    }
}
