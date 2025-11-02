# Projet Bit Packing – Génie Logiciel 2025  
### *Hiba BACHA – Université Côte d’Azur – Master 1 Informatique*  

---

## Description  

Ce projet implémente un système de **compression d’entiers par Bit Packing**, visant à **accélérer la transmission** des données tout en **préservant l’accès direct** aux éléments du tableau compressé.

L’objectif est de :
- **Réduire la taille** d’un tableau d’entiers à transmettre,  
- **Mesurer la rentabilité** de la compression selon le **débit** et la **latence réseau**,  
- Comparer plusieurs **modes de compression**,  
- Conserver la possibilité d’accéder directement à un élément `i` sans décompression complète.  

---

## Fonctionnalités principales

- **Compression / Décompression d’entiers** en flux binaire optimisé (`int[]`)
- **Accès direct (`acceder(i)`)** sans décompression complète  
- **Gestion des entiers négatifs** via un offset automatique  
- **Mesures CPU précises** sur `compresser`, `decompresser`, `acceder`
- **Simulation de transmission réseau** avec **débit (Mb/s)** et **latence (ms)**
- **Calcul du taux de compression**, du **temps de transmission** et du **verdict de rentabilité**
- **Mode Overflow (débordement)** pour isoler les grandes valeurs dans une zone spéciale
- **Interface console interactive** (`Main.java`)
- **Documentation Javadoc complète**  
- **Tests unitaires** avec JUnit 5

---

## Technologies utilisées  

### Développement
- **Langage** : Java 21  
- **Framework de tests** : JUnit 5  
- **Gestionnaire de build** : Maven  

### Environnement
- Compatible **Windows**, **Linux** et **macOS**  
- IDE recommandés : *Visual Studio Code*, *IntelliJ IDEA*, *Eclipse*  
- Contrôle de version : **GitHub**

### Documentation
- **Javadoc** – Documentation technique générée automatiquement  
- **Markdown (.md)** – Documentation lisible sur GitHub  

---

## Structure du projet  


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
java -cp out app.DemoEnonce sans 50 10

# Mode avec chevauchement
java -cp out app.DemoEnonce avec 50 10

# Mode avec débordement (overflow)
java -cp out app.DemoEnonce debordement 50 10
```

---

##  Exemple d’utilisation  

### Exemple 1 : Mode sans chevauchement
```
Mode         : avec
Débit (Mb/s) : 50.0
Latence (ms) : 10.0

────────────────────────────────────────────────────
Cas 1 : Tableau positif/mixte
n=7
Original        : [5, 3, 12, 7, 0, 8, 1]
get(3)          : 7
Round-trip OK   : true
Mesures (ms)    : {compresser=0.0036, decompresser=0.0014, acceder=0.0003}
Tailles (octets): original=28, compressé=20, gain=28.57%
Transmission (ms)
 - brut (sans)           : 10.004
 - compressé (comp+tx+dec): 10.022  [comp=0.016, tx=10.003, decomp=0.003]
Rentable ? NON

```

### Exemple 2 : Mode débordement (overflow)
```
Overflow        : count=2, kBase=6, indexBits=2
Exemple overflow: [1, 2, 3, 1024, 4, 5, 2048]
Restauration    : [1, 2, 3, 1024, 4, 5, 2048]
Accès direct    : i=3 -> 1024, i=6 -> 2048

```
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


  
