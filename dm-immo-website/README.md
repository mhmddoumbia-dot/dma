# Site web — DM IMMO

Site vitrine statique (HTML/CSS/JS, sans dépendance de build) pour **DM IMMO**,
filiale immobilière du groupe **DM Holding Invest**.

## Contenu

- `index.html` — page unique avec sections : Accueil, À propos, Services,
  Comment nous travaillons, Projets, Le Groupe DM, Gouvernance, Contact.
- `css/styles.css` — thème visuel (bleu marine / or / gris, aux couleurs du
  groupe DM et de la filiale DM Immo).
- `js/main.js` — menu mobile, effets au défilement, animations d'apparition,
  formulaire de contact (ouvre un e-mail pré-rempli vers `contact@dmimmo.ci`).
- `assets/logo.svg`, `assets/favicon.svg` — logo et favicon vectoriels DM Immo.

## Aperçu local

Aucune installation requise : ouvrir `index.html` dans un navigateur, ou
servir le dossier avec un petit serveur statique, par exemple :

```bash
cd dm-immo-website
python3 -m http.server 8080
```

Puis ouvrir `http://localhost:8080`.

## Déploiement

Le site est 100 % statique : il peut être publié tel quel sur GitHub Pages,
Netlify, Vercel (mode statique) ou tout hébergement web classique.

## À personnaliser avant mise en ligne

- Coordonnées réelles (téléphone, adresse exacte, e-mail définitif) dans
  `index.html` (section `#contact` et le pied de page).
- Liens réseaux sociaux (`.social-row`).
- Le formulaire de contact utilise actuellement un lien `mailto:` côté
  navigateur ; pour une réception directe des messages, le brancher sur un
  service d'envoi d'e-mail (Formspree, EmailJS, backend propre, etc.).
- Photos réelles des projets/réalisations, une fois disponibles, en
  remplacement des vignettes illustratives de la section « Nos projets ».
