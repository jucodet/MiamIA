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

---

# Research — Feature IKB-B (Auto-update base additifs + enrichissement OFF/Ciqual)

**Date**: 2026-06-28 | **Spec**: [spec.md](./spec.md) (Feature IKB-B) | **Plan**: [plan.md](./plan.md)

## 9. Sources amont et format de fetch

**Decision** : 
- **OpenFoodFacts** — taxonomie additive (`https://world.openfoodfacts.org/data/taxonomies/additives.json` ou dump équivalent), structurée par E-number ; fournit E-number, dénominations, aliases, étiquettes de risque. Couverture **exhaustive** (vs extrait P1).
- **Ciqual** — table de composition (diff/open data ANSES) ; fournit des **attributs nutritionnels** (ex. énergie kcal/100 g) rattachés à une substance/additif quand une correspondance existe.

Fetch via `java.net.HttpURLConnection` (pattern existant `GemmaModelDownloader`) sur `Dispatchers.IO` ; parsing `kotlinx.serialization`.

**Rationale** : 
- `HttpURLConnection` déjà utilisé dans le projet → pas de nouvelle dépendance (principe V).
- OFF taxonomy = source authoritative des E-numbers/aliases/risque (cohérent IKB-A).
- Ciqual = source authoritative des attributs nutritionnels (interprétation clarify 2026-06-28).

**Alternatives considered** : 
- OkHttp/Retrofit : rejeté (dépendance inutile — `HttpURLConnection` suffit pour un fetch ponctuel au démarrage).
- Lookup code-barres OFF produit : rejeté (hors périmètre IKB-B — feature ultérieure).

## 10. Stratégie de cache offline

**Decision** : Cache persistant = **fichier JSON versionné** dans `Context.filesDir/ingredientkb/` (`additives.cache.json`, `allergens.cache.json`, `kb-version.cache.json`), écrit atomiquement (`.tmp` → rename). Index in-memory rechargé au démarrage. **Pas de Room**.

**Rationale** : 
- Base statique versionnée (pas d'écriture incrémentale relationnelle) → JSON fichier = plus simple (principe V), cohérent avec le pattern `GemmaModelDownloader` (cache `filesDir`).
- Écriture atomique évite un cache corrompu en cas d'interruption.
- Relecture rapide (index in-memory), offline intégral.

**Alternatives considered** : 
- Room : rejeté (pas de requêtes relationnelles, migrations inutiles pour une base statique).
- DataStore/SharedPreferences : rejeté (non adapté à un payload JSON de plusieurs Mo).
- Cache en mémoire seulement : rejeté (perdu au redémarrage — `IKB-B-FR-004` exige persistance).

## 11. Repli offline en cascade

**Decision** : Ordre de résolution de la base au démarrage :
1. **Cache persisté** (`filesDir`) si valide (présent, lisible, version cohérente) ;
2. à défaut, **baseline embarquée** (assets IKB-A) ;
3. en parallèle, **refresh asynchrone** depuis OFF/Ciqual ; si succès → écrase le cache + devient la version courante pour les lookups suivants.

**Rationale** : 
- Offline-first strict : lookup **toujours** exploitable (`IKB-B-SC-002`).
- Cascade cache → baseline = filet de sécurité ultime (`IKB-B-FR-005`).
- Refresh différé = non bloquant (`IKB-B-FR-002`).

**Alternatives considered** : 
- Bloquer le premier lookup jusqu'au refresh : rejeté (viole non-blocage UX).
- Refresh synchrone au démarrage : rejeté (bloque l'UI).

## 12. Refresh non bloquant

**Decision** : `KbRefreshCoordinator` lance le refresh sur `Dispatchers.IO` (fire-and-forget au démarrage). `RefreshableReferenceKb` expose immédiatement la version courante (cache/baseline) ; quand le refresh aboutit, il publie la nouvelle version (volatile swap de l'index in-memory) pour les lookups suivants.

**Rationale** : 
- L'UI démarre immédiatement ; `IKB-B-SC-006` (0 % blocage).
- Swap atomique de l'index = pas de verrou sur le chemin critique du lookup.

**Alternatives considered** : 
- Mutex sur l'index : rejeté (contention sur le lookup).
- WorkManager périodique : rejeté au P1 (la spec exige « à chaque démarrage » ; WorkManager = sur-engineering).

## 13. Rejet des entrées amont incohérentes

**Decision** : Validation à l'ingestion : E-number dupliqué → on garde la première / trace le rejet ; `risk_level` invalide → entrée rejetée/tracée ; attribut Ciqual incohérent (non numérique, hors bornes) → attribut omis (repli silencieux). Aucune invention, aucun blocage (`IKB-B-FR-011`).

**Rationale** : robustesse face à des données amont évolutives/imparfaites ; cohérent avec `IKB-A-FR-003` (pas d'invention).

**Alternatives considered** : 
- Tout ou rien (rejeter toute la base si une entrée est incohérente) : rejeté (cascade de blocage excessive).

## 14. Conformité Feature C avec attributs Ciqual

**Decision** : Les attributs Ciqual (ex. énergie) sont publiés dans le `ReferenceContext` comme **contenu général** (qualification `GENERAL` inchangée) ; aucune formulation de fait étiquette ; aucune extension de `EquivalencePolicy` v1 stricte (`IKB-B-FR-009`).

**Rationale** : respect strict `IHI-C-FR-004`/`IHI-C-FR-005` ; séparation propre faits généraux (ingredient-knowledge) / ancrage produit (core).

**Alternatives considered** : 
- Présenter les attributs Ciqual comme faits étiquette : rejeté (court-circuiterait Feature C).

## 15. Dépendances externes (IKB-B)

**Decision** : aucune nouvelle dépendance ajoutée (réutilise `HttpURLConnection` + `kotlinx.serialization` + `kotlinx-coroutines`). Permission réseau `INTERNET` déjà présente dans le manifest (utilisée par `GemmaModelDownloader`).

**Rationale** : minimisation des dépendances (principe V) ; cohérence avec l'existant.

## 16. Points reportés ( Deferred → implémentation / features ultérieures)

- **URLs exactes / parsing détaillé** de la taxonomie OFF et de Ciqual : à finaliser en implémentation (variations de format entre dumps).
- **Fréquence alternative** (refresh périodique en arrière-plan) : hors périmètre (spec = « à chaque démarrage »).
- **Lookup code-barres produit OFF** : feature ultérieure.
- **Stratégie de rétention du cache** (max versions gardées) : P1 = une seule version cachée ; rotation éventuelle reportée.
