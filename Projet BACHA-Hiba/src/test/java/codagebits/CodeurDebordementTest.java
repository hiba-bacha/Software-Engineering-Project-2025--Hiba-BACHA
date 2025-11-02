package codagebits;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour {@link CodeurDebordement}.
 */
public class CodeurDebordementTest {

    @Test
    void testOverflowExample() {
        CodeurBits codeur = new CodeurDebordement();
        int[] original = {1, 2, 3, 1024, 4, 5, 2048};
        int[] compresse = codeur.compresser(original);
        int[] resultat = codeur.decompresser(compresse);
        assertArrayEquals(original, resultat);
    }

    @Test
    void testNegatifs() {
        CodeurBits codeur = new CodeurDebordement();
        int[] original = {-10, -2, 0, 15, 256};
        int[] compresse = codeur.compresser(original);
        assertArrayEquals(original, codeur.decompresser(compresse));
    }
}
