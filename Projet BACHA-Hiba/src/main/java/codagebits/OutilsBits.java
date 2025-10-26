package codagebits;
/**
* Utilitaires bas niveau (bit twiddling). Responsabilité unique.
* Invariants :
* - nbBits ∈ [1,32]
* - Les écritures ne modifient que les zones visées (nettoyage par masque préalable).
*/
final class OutilsBits {
private OutilsBits(){}


static int bitsNecessaires(int maxNonNegatif){
return (maxNonNegatif<=0) ? 1 : 32 - Integer.numberOfLeadingZeros(maxNonNegatif);
}
static int masque(int b){ return (b>=32)? -1 : ((1<<b)-1); }


/** Écrit 'nbBits' bits de 'valeur' à la position globale 'positionBit'. */
static void ecrireBits(int[] tampon, long positionBit, int nbBits, int valeur){
int v = valeur & masque(nbBits);
int mot = (int)(positionBit >>> 5); // /32
int decal = (int)(positionBit & 31); // %32
int espace = 32 - decal;
if (espace >= nbBits) {
int m = masque(nbBits) << decal; // zone à écraser
tampon[mot] = (tampon[mot] & ~m) | (v << decal);
} else {
int bas = espace, haut = nbBits - espace;
int mBas = masque(bas) << decal, mHaut = masque(haut);
int partBas = (v & masque(bas)) << decal;
int partHaut = (v >>> bas) & mHaut;
tampon[mot] = (tampon[mot] & ~mBas) | partBas;
tampon[mot + 1] = (tampon[mot + 1] & ~mHaut) | partHaut;
}
}


/** Lit 'nbBits' bits à la position globale 'positionBit'. */
static int lireBits(int[] tampon, long positionBit, int nbBits){
int mot = (int)(positionBit >>> 5);
int decal = (int)(positionBit & 31);
int espace = 32 - decal;
if (espace >= nbBits) {
return (tampon[mot] >>> decal) & masque(nbBits);
}
int bas = espace, haut = nbBits - espace;
int valBas = (tampon[mot] >>> decal) & masque(bas);
int valHaut = tampon[mot + 1] & masque(haut);
return valBas | (valHaut << bas);
}
}