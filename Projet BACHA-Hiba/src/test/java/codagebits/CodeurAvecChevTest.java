package codagebits;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour {@link CodeurAvecChev}.
 */
public class CodeurAvecChevTest {

    @Test
    void testRoundTrip() {
        CodeurBits codeur = new CodeurAvecChev();
        int[] original = {91, 37, -86, 76, -88, -41};
        int[] compresse = codeur.compresser(original);
        int[] resultat = codeur.decompresser(compresse);
        assertArrayEquals(original, resultat);
    }
}
