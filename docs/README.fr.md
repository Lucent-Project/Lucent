<div align="center">

[🇨🇳 简体中文](./docs/README.zh-CN.md) · [🇯🇵 日本語](./docs/README.ja.md) · [🇰🇷 한국어](./docs/README.ko.md)

</div>

<div align="center">

# 𝓛𝓾𝓬𝓮𝓷𝓽

### Moderne · Minimaliste · Discrètement surqualifié

**Une application de notes et de tâches dont l'assistant peut véritablement toucher à vos données — scellées dans une base de données chiffrée sur votre propre appareil, conversant en quatorze langues et assemblée de la première à la dernière ligne d'une simple pression sur un bouton sur GitHub. Elle se présente aussi bien en version de poche qu'en version de bureau — un APK Android et un installateur Windows — partageant un même battement de cœur, prête à voyager plus loin.**

![Lucent — Plateforme, Compilation, Interface, Assistant, Confidentialité, Licence](badges/badges.svg)

</div>

---

## L'idée générale

La plupart des applications de prise de notes vous invitent généreusement à choisir deux vertus parmi trois : la belle, la secrète ou l'intelligente. Choisissez votre duo et résignez-vous pour le reste. Lucent décline poliment le compromis. Tout ce que vous écrivez est scellé dans une base de données chiffrée qui ne quitte jamais votre appareil. L'assistant peut être un modèle dans le cloud pour lequel vous payez, un modèle tournant sur la machine même devant vous, ou absolument rien du tout. Et toute l'affaire est assemblée sans la moindre ligne d'outillage local : appuyez sur un bouton sur GitHub, allez faire un tour, et un APK ou un installateur Windows vous attendra à votre retour.

## Une seule application, où que vous la posiez

Lucent est **un seul produit doté d'un unique cœur partagé** : la règle maison exige que chaque fonctionnalité habite chaque plateforme où elle est distribuée. Votre poche et votre bureau parlent rigoureusement la même langue :

- **`:app`** — l'application Android (Kotlin et Jetpack Compose, Room sur SQLCipher, llama.cpp via le NDK). Compilée par `.github/workflows/build.yml` en un APK de version finale signé.
- **`:desktop`** — l'application de bureau, Windows aujourd'hui (Compose for Desktop, JVM pure, SQLite sur JDBC, llama.cpp compilé en DLL). Compilée par `.github/workflows/build-windows.yml` en un installateur `.exe` double-cliquable.
- **`:shared`** — l'arborescence source unique que compilent les deux plateformes : logique métier, données, l'essentiel de l'interface, un catalogue de traduction unique en quatorze langues. Modifiez une fois ; les deux machines changent de concert.

Tout ce qui peut être partagé réside dans `shared/` ; les rares éléments qui ne le peuvent pas — `SettingsRepository`, `Daos`/`Db`, `DocumentExport` et les écrans les plus vastes — subsistent en un exemplaire par plateforme, car leurs coutures sont profondes. Les étapes de compilation native sont chacune optionnelles, de sorte qu'un faux pas dans l'une d'elles vous laisse avec une application fonctionnelle plutôt qu'avec une salle d'attente.

## L'assistant avec des mains, pas seulement des opinions

C'est la fonctionnalité qui définit Lucent. Apportez votre propre modèle — les formes de requête OpenAI, Anthropic et Google sont parlées couramment, plusieurs profils étant conservés et commutables d'un geste — ou faites tourner le tout sur l'appareil. Ne dites mot et l'assistant n'est tout bonnement pas là ; l'application de notes n'y perd rien.

Ce qui vaut la peine de l'avoir à ses côtés, c'est qu'il peut *agir* : créer, lire, modifier, colorer, épingler, archiver et supprimer des notes ; créer, accomplir, rouvrir, planifier et prioriser des tâches ; chercher à l'aide d'un véritable langage de filtres ; et joindre, renommer ou retirer des fichiers. Avant de modifier quoi que ce soit, il vous montre précisément ce qu'il a l'intention de faire — dans votre propre langue, dans un dialogue qui est lui-même l'éditeur, où chaque champ discutable peut être retouché avant même qu'un seul octet ne soit écrit. C'est toujours votre parole qui est prononcée en dernier. Quelqu'un qui a une opinion sans mains n'est qu'une conversation de salon ; voici un majordome à qui l'on a ordonné de frapper d'abord.

Les conversations durent aussi longtemps que vous le désirez et voyagent avec vous, et la conversation dans laquelle vous vous trouviez en dernier est celle qui s'ouvre. Les réponses arrivent sous forme de Markdown impeccable — titres, listes, code — avec le raisonnement du modèle soigneusement replié au-dessus jusqu'à ce que vous le réclamiez, et une ligne par appel d'outil dans la trace, chacune précisant le temps qu'il a pris. Quatre panneaux surplombent la conversation : la liste de choses à faire que l'assistant tient à mesure qu'il avance, le plan qu'il suit, les tâches d'arrière-plan qu'il a lancées et les sous-agents auxquels il a fait appel, dont chacun peut être suivi en direct, interrompu ou gratifié d'une consigne supplémentaire. Donnez-lui un objectif, et il foncera droit vers lui, manche après manche, jusqu'à ce que le but soit atteint, suspendu ou honnêtement signalé comme bloqué.

## L'atelier derrière l'assistant

Les notes et les tâches sont la devanture. Derrière elles, à un commutateur près dans Paramètres → Avancé → Boîte à outils, se cache un atelier où l'assistant a permission d'entrer. Il dispose d'un dossier d'espace de travail de votre choix — le sélecteur parcourt l'appareil tout entier, et non le seul stockage partagé — et à l'intérieur, de véritables outils : lire, écrire, chercher, modifier avec un diff, créer un instantané et revenir en arrière. Il peut exécuter des commandes assorties d'un délai d'expiration et d'un répertoire de travail, conserver les plus longues en tâches de fond et vous afficher leur résultat. Il peut créer et modifier des fichiers Word, Excel et PowerPoint — les formats étant écrits nativement par Lucent, sans la moindre suite bureautique sous le capot — et lire des PDF, les convertir en images, les scinder et les fusionner. Il peut cloner, comparer, commiter et pousser avec Git, ouvrir et lire des issues GitHub, des pull requests et des journaux de CI, récupérer et parcourir des pages web, interroger SQLite et exécuter une tâche dans Docker, PRoot ou un simple shell, selon ce que la machine est en mesure d'offrir.

Sa mémoire s'articule de trois façons : cette conversation, ce projet, et vous. Il conserve un plan que vous pouvez regarder évoluer, consigne des compétences pour ne plus avoir à réapprendre vos habitudes, et peut convoquer des sous-agents lorsqu'une tâche est assez lourde pour le justifier. Sous-agents, instantanés, contrôle de l'appareil et accès à l'écran partagent une page qui leur est propre : Boîte à outils, puis Capacités de l'assistant.

Tout ce qui est volumineux reste en dehors de l'installateur : un environnement utilisateur Linux, Python et ses bibliothèques documentaires, LibreOffice, Node.js, un moteur de navigateur, des outils d'OCR et de traitement multimédia sont répertoriés comme greffons, téléchargés uniquement sur votre demande, depuis les serveurs du projet ou un miroir rapide, selon le premier qui répond. En installer un exige un shell — l'environnement Linux intégré à Lucent sur téléphone, winget sous Windows — et Lucent regarde où il met les pieds : chaque greffon est inspecté avant le moindre téléchargement, de sorte qu'un shell manquant, un dossier de téléchargement inaccessible en écriture ou un miroir silencieux est signalé avant qu'il ne coûte de la bande passante, et non après. En cas d'échec d'un téléchargement ou d'une installation, Lucent ouvre un dialogue qui en nomme la cause, montre la sortie brute du shell et dresse la liste des points à corriger. Un téléchargement en cours s'interrompt d'une simple pression de bouton, et l'interrompre ne laisse rien derrière lui. Les permissions ont droit à leur propre page — lecture, écriture, suppression, commandes, réseau, Git, GitHub, navigation, l'espace au-delà de l'espace de travail et l'appareil lui-même — chacune réglable sur autoriser, demander ou bloquer, et chaque appel d'outil est consigné dans un journal d'activité que vous pouvez consulter et effacer.

Sur un téléphone, ce même atelier s'étend jusqu'à l'appareil : lire l'écran, toucher, taper, balayer, faire une capture d'écran, lister et ouvrir des applications, lire les notifications, employer le presse-papiers et la lampe torche, et exécuter de vrais outils en ligne de commande au sein de l'environnement Linux intégré. Il parle également MCP, de sorte que n'importe quel serveur vers lequel vous le pointez rejoint la boîte à outils.

## Des notes qui se souviennent de ce qu'elles furent

Chaque modification d'importance fait l'objet d'un instantané, afin que vous puissiez toujours voir ce qu'une note disait jadis et la rétablir lorsqu'une réécriture pleine d'assurance s'avère n'avoir été que pur optimisme. Tapez `[[Liste de courses]]` et cela devient un lien cliquable ; pointez vers un titre qui n'existe pas encore et le lien rougeoie jusqu'à ce qu'un tapotement fasse gracieusement advenir la note au monde. Le Markdown s'affiche quand vous le désirez et reste rigoureusement tel qu'il a été saisi quand vous ne le voulez pas. Les listes à cocher sont des citoyennes de premier rang : reformulez les éléments sur place, ouvrez un éditeur flottant spacieux lorsqu'un élément anodin prend des ambitions. Étiquettes, couleurs, épingles, pièces jointes chiffrées individuellement, texte enrichi, un canevas de gribouillage pour les pensées que les mots ne sauraient atteindre, et un espace privé doté de son propre verrou — avec les brouillons aux côtés de la corbeille, pour qu'une pensée inachevée ne soit jamais une pensée abandonnée.

Une note vierge propose quatre déclencheurs en un geste — journal, réunion, idée de projet, liste à cocher — puis vient le véritable tour de passe-passe : vos propres modèles. Enregistrez-en un, et il vous accueillera sur chaque future page blanche ; un appui long permet de modifier ou de remiser n'importe lequel d'entre eux, modèles intégrés compris.

## Des tâches avec des échéances qui veulent vraiment dire quelque chose

Sous-tâches, priorités, calendriers de répétition et rappels qui survivent à un redémarrage. Les dates d'échéance sont analysées à partir du langage naturel — *vendredi prochain à 18h* devient un véritable horodatage doublé d'une véritable alarme — et les cadences de répétition sont traitées en citoyennes de premier rang, plutôt qu'en tournure astucieuse qui finit par déclarer forfait. L'accomplissement d'une tâche coche d'un coup toute sa liste de contrôle, et les tâches achevées prennent leur retraite sur un écran qui leur est propre.

## Ou faites tourner l'ensemble sur l'appareil lui-même

Importez un fichier `.gguf` (ou un `.zip` en contenant un) et l'assistant vous répond grâce à llama.cpp exécuté directement sur l'appareil — sans compte, sans clé API, sans réseau, le modèle étant déchargé à l'instant où vous quittez l'application. Les modèles Q4 d'environ 1 à 4 Go trouvent le point d'équilibre parfait sur un téléphone ; un ordinateur de bureau peut se permettre une ambition plus vaste. Les outils s'activent au cas par cas en local et l'accélération GPU est un choix consenti après un avertissement sans fard : le processeur fonctionne toujours, et un GPU qui n'est pas d'accord bat gracieusement en retraite. La vision est également en option — importez un fichier mmproj et l'assistant étudiera une photographie pour en deviser comme un bibliothécaire modérément clairvoyant.

## Quatre langues, sans la moindre cérémonie

English, 中文, 日本語, 한국어 — chaque écran, boîte de dialogue, format de date et modèle. Changez de langue et l'application tout entière suit au sein de la même trame, car il n'existe qu'un unique catalogue et c'est lui que lit l'interface : chaque mot n'est conservé qu'une fois, par choix de construction plutôt que par simple précaution.

## Son allure : faite, sans vergogne, de verre

Un dégradé vivant dérive derrière des panneaux dépolis qui troublent tout ce qui passe sous eux, sans jamais tout à fait se répéter, à un coût que l'appareil a approuvé par avance. Un généreux assortiment de palettes réparties en huit familles de styles, avec un compagnon à rotation automatique ou aléatoire qui flâne parmi elles. Clair, sombre, système et une galerie de thèmes aux nuances Monet — et sous Android 12 ou ultérieur, la palette emprunte la gamme de couleurs de votre fond d'écran pour la journée. Les widgets exportent cette même surface vitrée vers votre lanceur ; sous Windows, l'application se glisse dans la zone de notification et une barre latérale prend le relais de la barre d'onglets inférieure, car un grand écran mérite mieux qu'une interface de smartphone étirée sur les côtés.

## Le verrou qui compte, et la sauvegarde qui part avec vous

Le verrouillage de l'application applique une véritable politique anti-force brute plutôt qu'une prière courtoise : des délais de temporisation progressifs, un compteur persistant qui survit aux redémarrages, une question de sécurité optionnelle, une autodestruction facultative, et une empreinte digitale (Android) ou Windows Hello (Windows) poliment absente du bureau pendant toute la durée de la temporisation. La base de données est chiffrée au repos, les pièces jointes individuellement, et les sauvegardes tiennent en un unique fichier `.lcb` protégeable par mot de passe, véhiculant notes, tâches, historique, échanges, pièces jointes et réglages d'un appareil à l'autre — prévisualisé avant qu'un seul élément ne soit altéré, et armé pour s'exécuter automatiquement. Une sauvegarde que personne ne peut relire est une babiole plutôt qu'une sauvegarde ; celle-ci est donc lue à la sortie et vérifiée à nouveau à l'entrée.

## Une confidentialité structurelle, pas seulement promise

Rien ne quitte l'appareil avant que vous ne l'exportiez ou que vous ne donniez au modèle une raison de regarder. L'intégration à la feuille de partage est inactive par défaut ; les diagnostics sont désactivés par défaut et conservés localement ; les services d'assistance que vous raccordez relèvent strictement de votre choix ; le miroir cloud est un module que vous configurez vous-même, via WebDAV. La seule exception délibérée au chiffrement — l'exportation vers Markdown, Word, PDF ou Excel — produit un fichier que vous pouvez ouvrir n'importe où ailleurs, ce qui est précisément l'intérêt de la manœuvre.

## Du Rust, mais seulement là où il mérite son salaire

Deux chemins critiques sont rédigés en Rust et atteints via JNI — les routines PBKDF2 et AES-256-GCM qui président aux sauvegardes et au chiffrement des pièces jointes, ainsi que les mathématiques qui meuvent l'arrière-plan mouvant. Tous deux refluent vers une version Kotlin fonctionnellement identique lorsque la bibliothèque native n'est pas présente, si bien que l'application n'est jamais prise en otage par un compilateur.

## La compiler (oui, depuis un téléphone, en robe de chambre)

Pas d'Android Studio, pas de SDK local, pas de ligne de commande. Poussez sur GitHub, ouvrez **Actions**, déclenchez le workflow de votre choix et téléchargez le résultat — une mouture signée en bonne et due forme ou un installateur prêt au double-clic. Les flux de travail logent dans le dépôt, si bien que la recette repose dans la boîte plutôt que dans la tête de l'auteur, et que la compilation s'avère tout aussi inspectable que le code.

## Structure du projet

```
shared/       L'UNIQUE arborescence source partagée — logique métier et l'essentiel de l'UI résident
              ici UNE SEULE FOIS, et :app comme :desktop la compilent. Modifiez un fichier ici pour
              changer les deux plateformes ensemble.
app/          Module Android (:app) — enveloppe d'Activity, couche de base de données Room, ponts JNI,
              widgets, et exclusivement le code véritablement lié à Android
desktop/      Module bureau (:desktop) — enveloppe Compose for Desktop, cales JVM android.*,
              cœur de base de données JDBC, compilation CMake native pour la DLL du moteur
rust/         L'accélérateur Rust (partagé entre les plateformes)
.github/      build.yml (APK Android), build-windows.yml (installateur Windows), check.yml
```

## Vers où elle se dirige à présent

Lucent représente beaucoup d'application pour une liste de choses à faire, et ne s'en est jamais excusée une seule fois. Des notes avec de la mémoire, des tâches qui ont du mordant, un assistant doté de mains et de manières, un chiffrement qui honore ses promesses et une surface qui mérite d'être contemplée. Adoptez-la une semaine et observez ce qui vous frappe ; prenez-la pour un mois, et remarquez que vous avez cessé de le remarquer.

## Avec nos remerciements aux géants sur les épaules desquels nous nous tenons

Sous le verre, Lucent est fait d'une somme impressionnante d'excellents travaux d'autrui. Ce serait manquer d'éducation — et, dans un ou deux cas, violer carrément une licence — que de ne pas le proclamer haut et fort. L'intégralité des textes et des mentions de droit d'auteur se trouve dans **[`THIRD-PARTY-NOTICES.md`](./docs/THIRD-PARTY-NOTICES.md)** ; la version courte, accompagnée de notre gratitude, la voici :

| Génie d'emprunt | Dans le rôle de | Sous |
|---|---|---|
| [Kotlin](https://github.com/JetBrains/kotlin) & [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | le langage, et sa patience avec la concurrence | Apache-2.0 |
| [Jetpack Compose & AndroidX](https://developer.android.com/jetpack/androidx) | la moitié Android de l'interface | Apache-2.0 |
| [Compose Multiplatform & Skiko](https://github.com/JetBrains/compose-multiplatform) | la moitié bureau de l'interface | Apache-2.0 |
| [Material Icons](https://github.com/google/material-design-icons) | les petites images qui ont du sens | Apache-2.0 |
| [Haze](https://github.com/chrisbanes/haze) — © Chris Banes | tout ce flou si à la mode | Apache-2.0 |
| [OkHttp](https://github.com/square/okhttp) — © Square, Inc. | converser avec le nuage | Apache-2.0 |
| [Apache PDFBox](https://pdfbox.apache.org/) | les PDF sur ordinateur | Apache-2.0 |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) — © Taro L. Saito et al. | la porte d'entrée du bureau vers SQLite | Apache-2.0 |
| [Skia](https://skia.org/) | le tracé sous-jacent aux deux moitiés de l'interface | BSD-3-Clause |
| [SQLite](https://www.sqlite.org/) | la base de données elle-même, faisant tourner le monde en silence | Public Domain |
| [SQLCipher](https://www.zetetic.net/sqlcipher/) — © Zetetic LLC | le cadenas sur cette base, sous Android | BSD-style |
| [llama.cpp & GGML](https://github.com/ggml-org/llama.cpp) — © Georgi Gerganov & contributors | tout un modèle linguistique, sur votre propre silicium | MIT |
| [Shizuku API](https://github.com/RikkaApps/Shizuku-API) — © Rikka and contributors | le shell privilégié, quand un téléphone peut en offrir un | MIT |
| [org.json](https://github.com/stleary/JSON-java) — © JSON.org | la lecture du JSON sur le bureau | JSON License |
| [Great Vibes](https://github.com/googlefonts/great-vibes) — © The Great Vibes Pro Project Authors | la police manuscrite avec laquelle la plume trace | OFL-1.1 |

Le Model Context Protocol est une spécification ouverte d'Anthropic, sous licence MIT ; le client de Lucent est une implémentation originale, et tout serveur MCP vers lequel vous le dirigez est un logiciel tiers régi par ses propres conditions. Les moteurs de lecture et d'écriture Word, Excel et PowerPoint sont des implémentations originales d'Office Open XML bâties sur les bibliothèques zip et XML natives de la plateforme — aucune suite bureautique, et aucune bibliothèque PDF tierce, n'est incluse.

### Et les outils que vous pouvez recueillir, sur votre simple parole

Lucent n'embarque aucune chaîne d'outils lourde ni suite bureautique. Depuis la page des greffons, ou en le demandant à l'assistant, l'application peut rapatrier un espace utilisateur Linux, un convertisseur de documents ou un outil multimédia directement depuis les sources amont vers un espace de stockage qui vous appartient. Aucun d'eux ne fait partie de l'application, aucun n'y est lié, et chacun conserve sa propre licence : **[le fichier des mentions légales](./docs/THIRD-PARTY-NOTICES.md)** en donne la liste complète, qui s'étend de PRoot et Ubuntu Base jusqu'à Git, Pandoc, FFmpeg, ripgrep, 7-Zip, Tesseract, ImageMagick, qpdf, Poppler et yt-dlp, en passant par LibreOffice, Python et ses bibliothèques documentaires, Node.js et Playwright. Les remerciements de Lucent s'adressent à chacun de ces auteurs, et le choix d'installer l'un d'eux — tout comme la licence qui l'accompagne — vous revient entièrement.

### Et les applications de notes que nous avons étudiées

Six applications de notes et de tâches abouties ont été lues, disséquées et conviées à prendre le thé pour ce projet — non pour leur code, que Lucent n'embarque pas, mais pour ce que leurs auteurs ont su élucider les premiers. Leurs empreintes se lisent dans la structure et les manières de Lucent, plutôt que dans ses binaires, et une dette de conception acquittée à pleine voix est la moindre des politesses :

| Étudié pour | Ce que Lucent a emprunté | Leurs termes |
|---|---|---|
| [Quillpad](https://github.com/quillpad/quillpad) | le tiroir à deux niveaux — les épingles à portée de main, la configuration un niveau plus bas | GPL-3.0 |
| [Omni-Notes](https://github.com/federicoiosue/Omni-Notes) | l'honnêteté des sélecteurs système et des actions par appui long qui s'expliquent d'elles-mêmes | GPL-3.0 |
| [OpenNote-Compose](https://github.com/YangDai2003/OpenNote-Compose) | le modèle de priorité des couleurs dynamiques — fond d'écran d'abord, choix enregistrés jamais anéantis | GPL-3.0 |
| [AppFlowy](https://github.com/AppFlowy-IO/AppFlowy) | dire ouvertement ce qui se trame, dans la page des paramètres et partout ailleurs | AGPL-3.0 |
| [Logseq](https://github.com/logseq/logseq) | l'idée qu'une application de notes doit s'apparenter à un lieu, non à un tableau de bord | AGPL-3.0 |
| [MarkLeaf](https://github.com/jeiel85/markleaf-android) | le fait que la surface d'écriture demeure sereine pendant que la configuration attend son heure | Apache-2.0 |

### Et le harnais dont nous nous sommes inspirés

L'assistant de Lucent adopte le vocabulaire d'outils et les habitudes de travail du **DeepSeek Harness** (dsh), ainsi que de **[DSHA](https://github.com/DSH-APP/DSHA)** — sous licence MIT — le projet Android qui transporte ce harnais sur un téléphone sans accès root et sans Termux. Lucent n'embarque ni l'un ni l'autre : les outils, la boucle, les panneaux et les objectifs sont issus de son propre code Kotlin, écrit pour se comporter selon les usages établis par ces deux précurseurs. Les codes de sortie parviennent sous forme de résultats plutôt que d'erreurs, un fichier est lu avant d'être écrit, un objectif conserve le compte de ses tours, les aides peuvent être interpellés et stoppés, et tout point discutable fait d'abord l'objet d'une demande. Le modèle leur appartient et leur est ici attribué ; le code est le nôtre.

La propre licence de Lucent régit tout ce que vous installez ; les projets susmentionnés sont salués comme des sources d'idées et d'agencements, et non comme des œuvres incluses. La structure est nôtre ; la politesse leur revient.

Un mot particulier pour **SQLCipher**, dont la licence de style BSD demande — non sans raison, vu qu'il s'agit de la sentinelle gardant votre journal scellé — que son droit d'auteur et sa notice soient reproduits là où un utilisateur peut véritablement les trouver. Ils le sont donc, dans le fichier de mentions ci-dessus ; si vous distribuez une version de Lucent, veillez à les laisser faciles à trouver. L'unique police de caractères que Lucent transporte directement est Great Vibes, la cursive de la plume sur l'écran de démarrage, intégrée sans modification sous la licence SIL Open Font avec son nom réservé préservé.

## La licence, et la seule petite chose qu'elle réclame en échange

Lucent est publié sous la **licence MIT** — voir [`LICENSE`](./LICENSE). Faites-en à peu près tout ce qui vous chante : utilisez-le, modifiez-le, incorporez-le dans un produit commercial, bâtissez quelque chose de meilleur par-dessus sans jamais prendre la peine de nous remercier. L'unique condition, parfaitement raisonnable, est que notre mention de droit d'auteur et le texte de la licence fassent partie du voyage dans toute copie ou portion substantielle du code — ainsi, si vous réutilisez Lucent, joignez le fichier `LICENSE` (et le nom qui y figure) à ce que vous distribuez, et nous serons quittes. Les composants tiers cités plus haut formulent leurs propres exigences, tout aussi modestes ; honorez-les dans le même esprit et nous resterons tous bons amis.

Politique de confidentialité : **[`privacy.html`](https://lucent-project.github.io/lucentapp/privacy.html)** — ce que Lucent conserve, ce qui quitte votre appareil, et chacun des leviers de contrôle en votre possession.

## Contribuer

Si l'envie vous prenait d'améliorer Lucent, nous en serions discrètement enchantés. Les signalements de bugs, les suggestions réfléchies et les pull requests présentées de bonne grâce sont les très bienvenus. Nous vous demandons seulement de rester d'une courtoisie irréprochable, de garder le thé bien chaud et de traiter les autres contributeurs avec les égards que l'on attend dans un salon de lecture digne de ce nom.
