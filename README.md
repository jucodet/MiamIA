<div align="center">

# MiamIA

**Scannez, comprenez, maîtrisez ce que vous mangez — 100 % sur votre téléphone.**

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

Application Android qui scanne les étiquettes alimentaires, extrait les ingrédients par OCR, et les analyse via un modèle de langage local (Gemma) pour fournir un bilan santé complet — sans envoyer vos données sur Internet.

[Fonctionnalités](#fonctionnalités) · [Stack technique](#stack-technique) · [Démarrage rapide](#démarrage-rapide) · [Architecture](#architecture) · [Roadmap](#roadmap)

</div>

---

## A propos du projet

MiamIA permet à n'importe qui de **prendre en photo une étiquette alimentaire** et d'obtenir instantanément :

- La liste d'ingrédients structurée et corrigée (typos OCR)
- Un bilan de composition objectif
- Une critique santé adaptée par population (enfants, femmes enceintes, adultes, personnes âgées)
- Un classement des additifs par niveau de risque (rouge / orange / vert)

Tout le traitement se fait **localement sur l'appareil** grâce au modèle Gemma via LiteRT-LM. Aucune donnée ne quitte le téléphone.

<p align="right">(<a href="#miamia">haut de page</a>)</p>

## Fonctionnalités

| Fonctionnalité | Description |
|---|---|
| **Capture & OCR** | Preview caméra live, capture photo, reconnaissance de texte ML Kit |
| **Extraction d'ingrédients** | Isolation automatique de la liste (détection d'ancre "Ingrédients", gestion du point de fin) |
| **Validation utilisateur** | Édition et confirmation du segment avant analyse |
| **Bilan composition LLM** | Analyse Gemma locale : correction typos, identification, effets santé |
| **Critique santé ciblée** | Analyse par population (enfants, femmes enceintes, adultes, personnes âgées) |
| **KPI additifs** | Classement par risque avec justifications |
| **Onboarding LLM** | Téléchargement guidé du modèle avec détection Wi-Fi/4G, barre de progression et animations |
| **100 % offline** | Toute l'intelligence tourne sur l'appareil — aucun serveur distant |

<p align="right">(<a href="#miamia">haut de page</a>)</p>

## Stack technique

| Catégorie | Technologie |
|---|---|
| Langage | **Kotlin** (2.3.21, JVM 17) |
| UI | **Jetpack Compose** (Material 3) |
| Navigation | **Jetpack Navigation Compose** |
| Architecture | **ViewModel** + StateFlow + Coroutines |
| IA locale | **LiteRT-LM** 0.11.0 (Gemma sur appareil) |
| OCR | **Google ML Kit** Text Recognition |
| Caméra | **CameraX** 1.4.2 |
| Persistance | **Room** 2.8.4 (KSP) |
| Build | **Gradle** 9.0 (Kotlin DSL), AGP 8.12.3 |
| Tests | **JUnit 4**, Robolectric 4.13, Espresso, Compose UI Test |
| Méthodologie | **ATDD**, DDD, [Spec Kit](.specify/) |

<p align="right">(<a href="#miamia">haut de page</a>)</p>

## Démarrage rapide

### Prérequis

- **Android Studio** Ladybug ou supérieur
- **JDK 17**
- **Android SDK** API 34
- Un appareil ou émulateur Android (API 26 minimum)

### Installation

1. Cloner le dépôt

```sh
git clone https://github.com/jucodet/MiamIA.git
cd MiamIA
```

2. Placer le modèle Gemma

Télécharger le fichier `gemma_model.litertlm` et le placer dans :

```
app/src/main/assets/gemma/gemma_model.litertlm
```

> L'application propose aussi un **onboarding de téléchargement automatique** au premier lancement si le modèle est absent.

3. Builder et lancer

```sh
./gradlew assembleDebug
```

Installer l'APK généré dans `app/build/outputs/apk/debug/` sur votre appareil.

### Lancer les tests

```sh
# Tests unitaires JVM
./gradlew test

# Tests instrumentés (appareil/émulateur requis)
./gradlew connectedAndroidTest
```

<p align="right">(<a href="#miamia">haut de page</a>)</p>

## Architecture

Le projet suit une approche **Domain-Driven Design** avec 5 domaines métier :

```
specs/domains/
├── ingredient-health-intelligence/    # Core — bilan composition + critique santé
├── capture-recognition/               # Supporting — capture image, OCR
├── ingredient-normalization-validation/ # Supporting — isolation et validation du segment
├── additive-risk-insights/            # Supporting — KPI additifs et risques
└── user-guidance-experience/          # Supporting — UX transversale, onboarding
```

### Structure du code source

```
app/src/main/java/com/miamia/
├── additives/          # Extraction et classement des additifs
├── analysis/           # Orchestration de l'analyse LLM
├── camera/             # Intégration CameraX
├── composition/        # Bilan de composition
├── core/               # Utilitaires partagés
├── data/               # Couche données (Room)
├── gemma4local/        # Client LiteRT-LM / Gemma
├── healthcritique/     # Critique santé par population
├── home/               # Écran d'accueil
├── ingredients/        # Modèles et logique ingrédients
├── navigation/         # Navigation Compose
├── onboarding/         # Onboarding téléchargement modèle
├── permissions/        # Gestion des permissions caméra
├── recognition/        # OCR et extraction de texte
├── result/             # Écran de résultats streaming
├── scan/               # Flux de scan
├── ui/                 # Composants UI partagés (thème, animations)
└── welcome/            # Messages de bienvenue
```

### Gouvernance

Le projet est régi par une [constitution](.specify/memory/constitution.md) qui impose :

- **ATDD** — Scénarios d'acceptation Given/When/Then avant toute implémentation
- **Qualité non négociable** — Traçabilité spec → tests → code
- **Performance mesurable** — Objectifs chiffrés (latence, mémoire)
- **Frontières DDD strictes** — Autonomie des bounded contexts, contrats explicites

<p align="right">(<a href="#miamia">haut de page</a>)</p>

## Roadmap

- [x] Capture photo et OCR (ML Kit)
- [x] Preview caméra live (CameraX)
- [x] Extraction et isolation de la liste d'ingrédients
- [x] Intégration Gemma locale (LiteRT-LM)
- [x] Bilan de composition LLM
- [x] Critique santé par population
- [x] KPI additifs et risques
- [x] Messages de bienvenue rotatifs
- [x] Onboarding téléchargement modèle
- [ ] Historique des scans (Room)
- [ ] Comparaison de produits
- [ ] Support multi-langues (étiquettes)

<p align="right">(<a href="#miamia">haut de page</a>)</p>

## Contribuer

Les contributions sont les bienvenues.

1. Forker le projet
2. Créer une branche feature (`git checkout -b feature/ma-feature`)
3. Committer les changements (`git commit -m 'Ajouter ma feature'`)
4. Pousser la branche (`git push origin feature/ma-feature`)
5. Ouvrir une Pull Request

Chaque PR doit respecter la [constitution du projet](.specify/memory/constitution.md) : tests d'acceptation, pas de régression UX/performance, respect des frontières DDD.

<p align="right">(<a href="#miamia">haut de page</a>)</p>

## Licence

Distribué sans licence spécifiée pour le moment. Voir le dépôt pour les éventuelles mises à jour.

<p align="right">(<a href="#miamia">haut de page</a>)</p>

## Contact

Projet : [https://github.com/jucodet/MiamIA](https://github.com/jucodet/MiamIA)

<p align="right">(<a href="#miamia">haut de page</a>)</p>

## Remerciements

- [LiteRT-LM](https://ai.google.dev/edge/litert/lm) — Inférence LLM locale sur Android
- [Google ML Kit](https://developers.google.com/ml-kit) — Reconnaissance de texte OCR
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — UI déclarative moderne
- [CameraX](https://developer.android.com/training/camerax) — API caméra simplifiée
- [Best-README-Template](https://github.com/othneildrew/Best-README-Template) — Inspiration pour ce README

<p align="right">(<a href="#miamia">haut de page</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/jucodet/MiamIA.svg?style=for-the-badge
[contributors-url]: https://github.com/jucodet/MiamIA/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/jucodet/MiamIA.svg?style=for-the-badge
[forks-url]: https://github.com/jucodet/MiamIA/network/members
[stars-shield]: https://img.shields.io/github/stars/jucodet/MiamIA.svg?style=for-the-badge
[stars-url]: https://github.com/jucodet/MiamIA/stargazers
[issues-shield]: https://img.shields.io/github/issues/jucodet/MiamIA.svg?style=for-the-badge
[issues-url]: https://github.com/jucodet/MiamIA/issues
[license-shield]: https://img.shields.io/github/license/jucodet/MiamIA.svg?style=for-the-badge
[license-url]: https://github.com/jucodet/MiamIA/blob/master/LICENSE
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/jcodet
