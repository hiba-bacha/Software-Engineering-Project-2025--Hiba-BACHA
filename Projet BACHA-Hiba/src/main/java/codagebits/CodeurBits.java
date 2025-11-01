
/**
* STRATEGY : API stable imposée par le sujet.
* Préconditions générales :
* - Les valeurs sont des entiers >= 0 (gestion négatifs traitée séparément si souhaitée).
* Postconditions :
* - decompresser(compresser(A)) == A
* - acceder(i) retourne la i-ème valeur d'origine sans décompresser tout.
*/

package codagebits;

public interface CodeurBits {
int[] compresser(int[] tableau);
int[] decompresser(int[] compresse);
int acceder(int index);
}