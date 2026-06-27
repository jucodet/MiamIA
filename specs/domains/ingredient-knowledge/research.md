# Research — ingredient-knowledge (Feature IKB-A)

**Date**: 2026-06-27 | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## 1. Source et format de la base référence

**Decision** : Base embarquée en JSON sous `app/src/main/assets/ingredientkb/` — `additives.json` (extrait structuré de la taxonomie OpenFoodFacts, indexé par E-number) + `allergens.json` (14 allergènes réglementaires UE) + `kb-version.json` (source + version).

**Rationale** :
- Assets Android = lecture seule, embarquée, offline par construction ; pas de surcoût DB au P1.
- JSON lisible, diffable, versionnable ; l'extrait OFF est déjà disponible en JSON/CSV structuré par E-number.
- `kb-version.json` permet de tracer la version de base (satisfait `IKB-A-FR-009`).

**Alternatives considered** :
- Room/SQLite : rejeté au P1 (base statique, pas d'écriture ; complexité non justifiée — principe V).
- CSV : rejeté (typage plus faible, parsing manuel ; JSON + kotlinx.serialization plus sûr).
- Base compilée en code Kotlin : rejeté (maintenance et diff lourds pour ~600 entrées).

## 2. Parsing et indexation

**Decision** : `kotlinx.serialization` pour parser les JSON assets en structures `AdditiveFactCard` / `AllergenFactCard` ; index in-memory au démarrage : `Map<E-number, AdditiveFactCard>` + `Map<alias normalisé, E-number>` pour la recherche par dénomination courante.

**Rationale** :
- kotlinx.serialization : léger, multiplatform, sans réflexion, déjà compatible Kotlin 2.x.
- Index alias→E-number permet le lookup par dénomination courante sans dupliquer la fiche (satisfait `IKB-A-FR-012`, clé primaire = E-number).
- Index in-memory : lookup O(1)/sous-linéaire, satisfait l'objectif p95 < 20 ms.

**Alternatives considered** :
- Gson/Moshi : rejetés (kotlinx.serialization = standard Kotlin, mieux intégré).
- Pas d'index alias (recherche linéaire) : rejeté (performance et clarté).

## 3. Matching lookup (désignation → fiche)

**Decision** : matching par **sous-chaîne littérale** après **normalisations mécaniques explicitement listées (casse, espaces, accents)** uniquement. Aucune table d'alias étendue, aucun synonyme métier.

**Rationale** :
- Cohérent avec `IHI-C-FR-005` (normalisations mécaniques admises, équivalence v1 stricte).
- Prévisible et testable (jeu fixe déterministe).
- Évite toute fuite d'équivalence hors `EquivalencePolicy` du core.

**Alternatives considered** :
- Token exact + pas de normalisation : rejeté (rappel trop faible sur OCR réel).
- Table d'alias orthographiques étendue : rejetée au P1 (équivaudrait à une politique de synonymes non versionnée — viole `IKB-A-FR-006`).

## 4. Plafond d'injection et priorisation

**Decision** : `ReferenceContextBuilder` applique un **plafond N fiches** (défaut 12, configurable) avec priorisation **allergènes d'abord, puis additifs à niveau de risque élevé, puis modéré, puis faible**. Les fiches au-delà du plafond sont omises (repli silencieux, aucun blocage).

**Rationale** :
- Protège le budget d'entrée LLM (`MAX_INPUT_CHARS = 12 000`) sans déléguer la responsabilité au core.
- Déterministe et testable (ordre de priorisation fixe).
- Les allergènes prioritaires répondent à l'attente santé utilisateur.

**Alternatives considered** :
- Tronquer chaque fiche en résumé compact : rejeté (perte d'information variable, moins testable).
- Plafond en caractères : rejeté (moins prévisible que par compte de fiches).
- Délégation au consommateur : rejeté (transfère une responsabilité métier hors domaine propriétaire).

## 5. Conformité Feature C (ancrage)

**Decision** : le `ReferenceContext` est publié avec une **qualification explicite « contenu général »** (balise/markup dédié dans le read-model) ; aucune formulation de fait étiquette. Le core (`ingredient-health-intelligence`) reste seul responsable de l'ancrage « fait produit » sur le `ValidatedIngredientSegment`.

**Rationale** :
- Respect strict de `IHI-C-FR-004` (contenu général identifiable) et `IHI-C-FR-005` (pas d'extension d'équivalence).
- Séparation propre : `ingredient-knowledge` = faits généraux ; `additive-risk-insights` = projection KPI ; `ingredient-health-intelligence` = ancrage produit.

**Alternatives considered** :
- Injecter les fiches comme faits produit : rejeté (viendrait court-circuiter Feature C et `additive-risk-insights`).

## 6. Testabilité (jeu fixe, JVM pur)

**Decision** : `InMemoryReferenceKb` (implémentation du contrat `ReferenceKb` alimentée par fixtures en code) pour les tests JVM pur, sans Robolectric. Robolectric réservé au test de charge des assets réels (`EmbeddedReferenceKbRobolectricTest`). Jeu fixe d'ingrédients dans `fixtures/ReferenceIngredientFixtures.kt`.

**Rationale** :
- Conforme à US-IKB-A3 (exécutable isolément, hors capture/OCR/runtime LLM) et au style `GemmaBilanParserTest`.
- L'interface `ReferenceKb` sert d'anti-corruption layer : tests métier ≠ détails Android.

**Alternatives considered** :
- Tous les tests via Robolectric : rejeté (lent, dépendant Android, contrairement à l'esprit du test bouchonné).

## 7. Dépendances externes

**Decision** : ajouter `kotlinx-serialization-json` aux `dependencies` de `app/build.gradle.kts` (avec le plugin `org.jetbrains.kotlin.plugin.serialization` si non présent). Aucune dépendance réseau au P1.

**Rationale** : parsing JSON type-safe ; aucune lib réseau ajoutée (offline-first).

**Alternatives considered** : parsing manuel : rejeté (verbeux, risqué pour ~600 entrées).

## 8. Points reportés ( Deferred → plan ultérieur)

- **Latence lookup** : objectif p95 < 20 ms retenu, mais benchmark formel reporté à l'implémentation (mesure guidée par profiling — principe IV).
- **Processus de refresh / mise à jour de la base** : détail d'exécution (regénérer l'extrait OFF, bumper `kb-version.json`) à documenter dans tasks/quickstart, hors design spec.
- **Couverture exhaustive des E-numbers** : non exigée au P1 (extrait + jeu fixe suffisent).
