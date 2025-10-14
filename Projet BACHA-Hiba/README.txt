# Software-Engineering-Project-2025--Hiba-BACHA
Projet Bit Packing
Description
Ce projet implémente un système de compression d’entiers par Bit Packing.
Trois variantes sont disponibles :
•	Sans chevauchement : les valeurs sont alignées dans les mots de 32 bits.
•	Avec chevauchement : les valeurs peuvent être découpées entre deux mots.
•	Avec débordement (overflow) : les petites valeurs sont codées normalement, les grandes sont placées dans une zone spéciale de débordement.

Chaque variante fournit trois fonctions :
•	compresser(int[] tableau)
•	decompresser(int[] tampon)
•	acceder(int i)


Structure du projet
├── src/main/java
│   ├── codagebits/
│   │   ├── CodeurSansChev.java          # Version sans chevauchement
│   │   ├── CodeurAvecChev.java # Version avec chevauchement
│   │   ├── CodeurDebordement.java   # Version avec overflow
│   │   ├── OutilsBits.java              # Lecture/écriture de bits
│   │   └── FactoryCodeurBits.java       # Fabrique de codeurs
│   └── app/
│       ├── DemoEnonce.java              # Démonstration avec mesures
│       └── BenchProto.java              # Banc de test pour les temps
├── out/                                 # Fichiers compilés (.class)
└── README.txt



Fonctionnalités principales
•	Compression d’entiers en int[] optimisé en nombre de bits.
•	Décompression avec restitution exacte du tableau original.
•	Accès direct à un élément acceder(i) sans décompression complète.
•	Variante overflow qui sépare petites et grandes valeurs.
•	Mesure du temps d’exécution des opérations principales.

Compilation : ( Depuis src ) 

WINDOWS
javac -d out (Get-ChildItem -Recurse -Filter *.java).FullName

LINUX/MAC
javac -d out $(find src -name "*.java")

Exécution : 
java -cp out app.DemoEnonce sans
java -cp out app.DemoEnonce avec
java -cp out appl.DemoEnonce debordement
