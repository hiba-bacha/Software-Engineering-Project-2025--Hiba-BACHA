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


##  Fonctionnalités principales

- **Compression d’entiers** optimisée en nombre de bits (`int[]`).
- **Décompression sans perte**, le tableau original est entièrement retrouvé.
- **Accès direct** à un élément (`acceder(i)`) sans décompresser tout le tableau.
- **Gestion des entiers négatifs** avec un système d’offset.
- **Mesures de performance** sur les fonctions principales : `compresser`, `decompresser`, `acceder`.
- **Mode overflow** qui sépare petites et grandes valeurs pour un encodage plus efficace.

---

##  Technologies utilisées  

### Développement
- **Java 21** - Langage de programmation
- **JUnit 5** - Tests unitaires
- **Maven** - Gestion du projet et dépendances

### Environnement
- **Visual Studio Code**, **IntelliJ IDEA** ou **Eclipse** - Environnement de développement
- **Git & GitHub** - Gestion de version
- **Windows**, **Linux**, **macOS** - Systèmes compatibles

### Documentation
- **Javadoc** - Documentation technique
- **Markdown** - Documentation projet

---

##  Structure du projet  

```
├── pom.xml
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
│       ├── BenchProto.java  
│       └── Main.java
└── test/java/           # Dossier contenant les tests unitaires
    └── codagebits/
        ├── CodeurSansChevTest.java
        ├── CodeurAvecChevTest.java
        ├── CodeurDebordementTest.java
├── out/                                 # Fichiers compilés (.class)
└── README.md
```


---

##  Installation  

### 1. Cloner le projet  
```bash
git clone https://github.com/hiba-bacha/Software-Engineering-Project-2025--Hiba-BACHA
```

### 2. Compiler le projet  

**Sous Windows (PowerShell) :**

Ce deplacer dans le dossier java 
```powershell
cd '.\Projet BACHA-Hiba\src\main\java\'
```

Compiler le projet
```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java).FullName
```

**Sous Linux / macOS :**

Se deplacer dans le dossier `Projet BACHA-Hiba`
```bash
cd '.\Projet BACHA-Hiba\
```

Compiler le projet
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

### Exemple 4 : Mesures de performance  

```
Mesures (ms) : {compresser_ms=0.0017, decompresser_ms=0.0010, acceder_ms=0.0003}
```
Les mesures sont calculées à l’aide de la classe `BenchProto`, avec plusieurs répétitions pour obtenir des moyennes fiables.  

---
## Génération de la Documentation Javadoc 

**Documentation technique du projet**  
Cette section explique comment générer la documentation Javadoc complète du projet, détaillant toutes les classes, méthodes et packages implémentés.  


### 1. Se deplacer dans le dossier `Projet BACHA-Hiba`
```powershell
cd '.\Projet BACHA-Hiba\'  
```
### 2. Générer la Javadoc
##### Pour Windows (CMD/PowerShell)
```powershell
javadoc -d documentation -sourcepath src/main/java -subpackages app:codagebits -encoding UTF-8 -charset UTF-8
```

##### Pour Linux / macOS
```bash
javadoc -d documentation/ -sourcepath src/main/java -subpackages app:codagebits -encoding UTF-8 -charset UTF-8
```

### 3. Accéder à la documentation
Une fois générée, ouvrez le fichier :
```
documentation/index.html
```


---

## Tests unitaires (JUnit 5)

Les tests unitaires ont été réalisés avec le framework **JUnit 5 (Jupiter)** afin de vérifier le bon fonctionnement des classes principales du projet.

###  Organisation
Les tests sont placés dans un dossier séparé :
```
src/
├── main/java/           # Code source principal
│   ├── codagebits/
│   └── app/
└── test/java/           # Dossier contenant les tests unitaires
    └── codagebits/
        ├── CodeurSansChevTest.java
        ├── CodeurAvecChevTest.java
        ├── CodeurDebordementTest.java
        
```

### Classes testées

| Classe | Fonctionnalités testées |
|--------|------------------------|
| `CodeurSansChev` | Compression, décompression et accès direct |
| `CodeurAvecChev` | Gestion des bits chevauchants |
| `CodeurDebordement` | Gestion des grandes valeurs et des négatifs |


### Prérequis : Installation de Maven

Avant de compiler ou exécuter le projet, il faut s’assurer que **Maven** est installé sur votre système.  
Maven est un outil de gestion et d’automatisation de projet Java utilisé ici pour compiler, tester et documenter le code.

#### Sous Windows

> Si vous avez déjà installé [Chocolatey](https://chocolatey.org/install), exécutez simplement cette commande dans **PowerShell (en mode Administrateur)** :

```powershell
choco install maven
```

#### Sous Linux / Ubuntu / Debian

Exécutez les commandes suivantes dans un terminal :
```bash
sudo apt update
sudo apt install maven -y
```

#### Sous macOS

Si vous utilisez **Homebrew**, tapez simplement :
```bash
brew install maven
```

#### Vérifiez l'installation :
```bash
mvn -v
```

### Exécution des tests 
#### Assurez vous d'être dans le dossier `Projet BACHA-Hiba`
```powershell
cd '.\Projet BACHA-Hiba\'  
```

#### Lancer tous les tests avec la commande :
```bash
mvn test
```

 > Maven télécharge automatiquement JUnit, compile le code et exécute les tests.

#### Exemple de sortie :
```
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

##  Contributrice  

- **Nom :** Hiba BACHA  
- **Université :** Université Nice Côte d'Azur 
- **Année :** 2025  

---


  
