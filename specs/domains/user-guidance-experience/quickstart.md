# Quickstart: llm-download-onboarding

## Validation manuelle

### Scénario 1 — Premier lancement avec Wi-Fi

1. Désinstaller l'application (ou effacer les données).
2. S'assurer que l'appareil est connecté en Wi-Fi.
3. Lancer l'application.
4. **Vérifier** : l'écran plein de confirmation s'affiche avec le message "Connexion Wi-Fi détectée", la taille approximative du modèle, et les boutons "Confirmer" / "Plus tard".
5. Appuyer sur "Confirmer".
6. **Vérifier** : l'écran d'attente s'affiche avec :
   - Titre "Téléchargement du modèle de langage en cours..."
   - Barre de progression qui avance
   - Pourcentage affiché
   - Fouet mixeur animé
   - Phrases humoristiques qui changent toutes les ~5s
7. Attendre la fin du téléchargement.
8. **Vérifier** : redirection automatique vers l'écran capture (caméra).

### Scénario 2 — Premier lancement en données mobiles

1. Désinstaller l'application, passer en données mobiles (désactiver Wi-Fi).
2. Lancer l'application.
3. **Vérifier** : l'écran de confirmation affiche un avertissement explicite sur la consommation de données mobiles.
4. Appuyer sur "Confirmer".
5. **Vérifier** : téléchargement et écran d'attente fonctionnent normalement.

### Scénario 3 — Premier lancement hors-ligne

1. Désinstaller l'application, activer le mode avion.
2. Lancer l'application.
3. **Vérifier** : l'écran "Connexion requise" s'affiche avec explication et bouton "Réessayer".
4. Réactiver le réseau.
5. Appuyer sur "Réessayer".
6. **Vérifier** : transition vers l'écran de confirmation.

### Scénario 4 — Refus du téléchargement

1. Arriver sur l'écran de confirmation.
2. Appuyer sur "Plus tard".
3. **Vérifier** : un état explicatif indique que l'application ne peut pas fonctionner sans le modèle, avec possibilité de relancer le téléchargement.

### Scénario 5 — Erreur réseau pendant téléchargement

1. Démarrer le téléchargement.
2. Activer le mode avion pendant le téléchargement.
3. **Vérifier** : un message d'erreur clair s'affiche avec un bouton "Réessayer".
4. Réactiver le réseau et appuyer sur "Réessayer".
5. **Vérifier** : le téléchargement redémarre (V1 : depuis le début).

### Scénario 6 — Modèle déjà présent (relancement normal)

1. Lancer l'application avec le modèle déjà téléchargé.
2. **Vérifier** : aucun écran onboarding ne s'affiche ; accès direct à l'écran capture.
