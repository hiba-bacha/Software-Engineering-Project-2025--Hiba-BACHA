#  Projet Bit Packing – Génie Logiciel 2025

##  Description  

Ce projet implémente un système de **compression d’entiers par Bit Packing**.  
Le but est de réduire la taille d’un tableau d’entiers tout en gardant la possibilité d’accéder directement à chaque élément sans tout décompresser.  

Trois variantes sont disponibles :  
- **Sans chevauchement** : les valeurs sont alignées dans les mots de 32 bits.  
- **Avec chevauchement** : les valeurs peuvent être découpées entre deux mots.  
- **Avec débordement (overflow)** : les petites valeurs sont codées normalement, les grandes sont placées dans une zone spéciale de débordement.  

Chaque variante fournit trois fonctions principales :  
- `compresser(int[] tableau)`  
- `decompresser(int[] tampon)`  
- `acceder(int i)`  

---

##  Technologies utilisées  

- **Langage :** Java  
- **Version :** Java 21 (compatible à partir de Java 17)  
- **IDE recommandé :** Visual Studio Code, IntelliJ IDEA ou Eclipse  
- **Systèmes compatibles :** Windows, Linux, macOS  

---

##  Structure du projet  

```
├── src/main/java
│   ├── codagebits/
│   │   ├── CodeurBits.java              # Interface principale
│   │   ├── CodeurSansChev.java          # Version sans chevauchement
│   │   ├── CodeurAvecChev.java          # Version avec chevauchement
│   │   ├── CodeurDebordement.java       # Version avec overflow
│   │   ├── OutilsBits.java              # Lecture/écriture de bits
│   │   └── FactoryCodeurBits.java       # Fabrique de codeurs
│   └── app/
│       ├── DemoEnonce.java              # Démonstration avec mesures
│       └── BenchProto.java  
        └── Main.java
├── out/                                 # Fichiers compilés (.class)
└── README.md
```

---

##  Installation  

### 1. Cloner le projet  
```bash
git clone https://github.com/hiba-bacha/Software-Engineering-Project-2025--Hiba-BACHA
cd Software-Engineering-Project-2025--Hiba-BACHA

```

### 2. Compiler le projet  

**Sous Windows (PowerShell) :**
```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java).FullName
```

**Sous Linux / macOS :**
```bash
javac -d out $(find src -name "*.java")
```

---

##  Exécution  

Exécuter le projet en précisant le mode souhaité :  
```bash
# Mode sans chevauchement
java -cp out app.DemoEnonce sans

# Mode avec chevauchement
java -cp out app.DemoEnonce avec

# Mode avec débordement (overflow)
java -cp out app.DemoEnonce debordement
```

---

##  Fonctionnalités principales  

- **Compression d’entiers** optimisée en nombre de bits (`int[]`).  
- **Décompression sans perte**, le tableau original est entièrement retrouvé.  
- **Accès direct** à un élément (`acceder(i)`) sans décompresser tout le tableau.  
- **Gestion des entiers négatifs** avec un système d’offset.  
- **Mesures de performance** sur les fonctions principales : `compresser`, `decompresser`, `acceder`.  
- **Mode overflow** qui sépare petites et grandes valeurs pour un encodage plus efficace.  

---

##  Exemple d’utilisation  

### Exemple 1 : Mode sans chevauchement
```
Original (neg): [-5, -1, 0, 3, 7]
mode=sans get(2)=0 ok=true
Mesures (ms) : {compresser_ms=0.0015, decompresser_ms=0.0011, acceder_ms=0.0003}
```

### Exemple 2 : Mode avec chevauchement
```
Original (rand): [91, 37, -86, 76, -88, -41, -88, 48, -23, -42]
mode=avec get(5)=-41 ok=true
Mesures (ms) : {compresser_ms=0.0018, decompresser_ms=0.0012, acceder_ms=0.0003}
```

### Exemple 3 : Mode débordement (overflow)
```
Exemple overflow : [1, 2, 3, 1024, 4, 5, 2048]
Restauration     : [1, 2, 3, 1024, 4, 5, 2048]
Acces direct ex.: i=3 -> 1024, i=6 -> 2048
```

---

## Exemple de mesures de performance  

```
Mesures (ms) : {compresser_ms=0.0017, decompresser_ms=0.0010, acceder_ms=0.0003}
```
Les mesures sont calculées à l’aide de la classe `BenchProto`, avec plusieurs répétitions pour obtenir des moyennes fiables.  

---
### Documentation du code  

Une documentation complète du code (JavaDoc) est disponible.  

#### 🛠️ Génération (optionnelle)
Pour générer la documentation sur ton poste :  

##### Windows
```powershell
javadoc -d docs -sourcepath src/main/java -subpackages codagebits,app -encoding UTF-8 -private -author -version
```

##### Linux / macOS
```bash
javadoc -d docs -sourcepath src/main/java -subpackages codagebits,app -encoding UTF-8 -private -author -version
```

####  [Consulter la documentation JavaDoc](./docs/index.html)

> Cliquez sur le lien ci-dessus pour ouvrir la documentation dans votre navigateur  
> (ou double-cliquez sur `docs/index.html` depuis votre explorateur de fichiers).

La documentation inclut :  
- une description de chaque **classe**, **méthode** et **champ** ;  
- les tags `@param`, `@return`, `@throws` ;  
- des paragraphes explicatifs `<p>` pour faciliter la compréhension de la logique du code.

---

##  Contributrice  

- **Nom :** Hiba BACHA  
- **Université :** Université Nice Côte d'Azur 
- **Année :** 2025  

---


  
