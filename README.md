# Gestion Finances

Application Android de gestion des finances personnelles, **multi-projets** et
**multi-utilisateurs**, développée en Kotlin avec Jetpack Compose.

## Fonctionnalités

- **Comptes utilisateurs locaux** : inscription/connexion par e-mail et mot de passe
  (haché avec PBKDF2-HMAC-SHA256, sel aléatoire par utilisateur).
- **Projets financiers multiples** : chaque utilisateur peut créer et gérer plusieurs
  projets (ex. "Famille", "Association", "Petit commerce"), chacun avec sa propre
  devise, ses comptes, catégories, transactions et budgets.
- **Collaboration multi-utilisateurs** : un projet peut être partagé avec d'autres
  comptes de l'application via leur e-mail, avec des rôles (Propriétaire,
  Administrateur, Membre, Lecteur) qui bornent les permissions.
- **Comptes** : espèces, banque, mobile money, carte, épargne — soldes calculés
  automatiquement à partir des transactions.
- **Transactions** : revenus, dépenses et virements entre comptes, avec catégories,
  notes et historique par utilisateur (qui a saisi quoi).
- **Catégories personnalisables** : couleur et type (revenu/dépense), catégories par
  défaut proposées à la création d'un projet.
- **Budgets** : limites par catégorie et par période (hebdomadaire, mensuelle,
  annuelle), avec suivi de la progression et alerte de dépassement.
- **Rapports** : répartition des dépenses par catégorie et évolution mensuelle
  revenus/dépenses (graphiques natifs Compose, sans dépendance externe).
- **100 % hors-ligne** : toutes les données sont stockées localement (Room /
  SQLite), aucune connexion internet n'est requise.

## Architecture

- **UI** : Jetpack Compose + Material 3, architecture MVVM.
- **Navigation** : Navigation Compose (`ui/navigation`), un seul graphe racine ;
  l'écran principal (`HomeScreen`) gère ses onglets (tableau de bord, transactions,
  budgets, rapports, paramètres) sans NavHost imbriqué.
- **Persistance** : Room (`data/local`) — entités, DAO, base de données typée.
- **Logique métier** : Repositories (`data/repository`) entre les DAO et les
  ViewModels.
- **Injection de dépendances** : Hilt (`di/`, `FinanceApp.kt`).
- **Session** : DataStore Preferences (`data/session/SessionManager.kt`) pour
  mémoriser l'utilisateur connecté et le projet actif.

```
app/src/main/java/com/dma/finance/
├── data/
│   ├── local/          Room : entités, DAO, base de données, convertisseurs
│   ├── repository/     Logique métier (Auth, Project, Account, Category, ...)
│   ├── security/        Hachage des mots de passe (PBKDF2)
│   └── session/          Session utilisateur / projet actif (DataStore)
├── di/                    Modules Hilt
├── ui/
│   ├── auth/            Connexion, inscription
│   ├── projects/        Liste, création, membres de projet
│   ├── dashboard/        Tableau de bord
│   ├── accounts/         Comptes
│   ├── categories/        Catégories
│   ├── transactions/      Transactions
│   ├── budgets/           Budgets
│   ├── reports/           Rapports / graphiques
│   ├── settings/          Paramètres
│   ├── navigation/        Graphe de navigation, écran principal à onglets
│   ├── theme/             Thème Material 3
│   └── components/        Composants réutilisables (montants, graphiques, ...)
└── util/                  Formatage monétaire, dates, périodes
```

## Prérequis pour compiler

- Android Studio (Koala ou plus récent) avec le SDK Android 34 installé.
- JDK 17.

## Compiler et lancer

```bash
./gradlew assembleDebug
```

Puis ouvrir le projet dans Android Studio et lancer sur un émulateur ou un
appareil physique (minSdk 26 / Android 8.0).

> Remarque : ce projet a été généré et vérifié statiquement dans un environnement
> sandbox sans accès au SDK Android ni au dépôt Maven de Google (`dl.google.com`),
> donc sans compilation réelle possible sur place. Le code suit scrupuleusement
> les API stables d'AndroidX/Compose/Room/Hilt ; une vérification de compilation
> dans Android Studio est recommandée avant la première exécution.

## Devise par défaut

Le Franc CFA (XOF) est proposé par défaut à la création d'un projet ; d'autres
devises courantes (XAF, EUR, USD, GBP, CAD, MAD, CHF) sont disponibles.
