<div align="center">

[🇬🇧 English](../README.md) · [🇨🇳 简体中文](./README.zh-CN.md) · [🇭🇰 繁體中文](./README.zh-Hant.md) · [🇪🇸 Español](./README.es.md)<br>
[🇸🇦 العربية](./README.ar.md) · [🇵🇹 Português](./README.pt.md) · [🇫🇷 Français](./README.fr.md) · [🇷🇺 Русский](./README.ru.md)<br>
[🇩🇪 Deutsch](./README.de.md) · [🇯🇵 日本語](./README.ja.md) · [🇹🇷 Türkçe](./README.tr.md) · [🇰🇷 한국어](./README.ko.md)<br>
[🇳🇱 Nederlands](./README.nl.md)

</div>

<div align="center">

# 𝓛𝓾𝓬𝓮𝓷𝓽

### Moderno · Minimalista · Discretamente sovraqualificato

**Un'applicazione per note e attività il cui assistente può davvero mettere le mani sui tuoi dati — sigillati all'interno di un database crittografato sul tuo dispositivo, capace di esprimersi in quattordici lingue e assemblato dalla prima all'ultima riga premendo un singolo pulsante su GitHub. Giunge tanto in edizione tascabile quanto da scrivania — un APK per Android e un programma di installazione per Windows — condividendo un unico battito, pronto a spingersi ancora più lontano.**

![Lucent — Piattaforma, Compilazione, Interfaccia, Assistente, Riservatezza, Licenza](../badges/badges.svg)

</div>

---

## L'idea di fondo

La maggior parte delle applicazioni per appunti vi rivolge un generoso invito a scegliere due qualsiasi tra tre virtù: quella bella, quella riservata o quella intelligente. Scegliete la vostra coppia e rassegnatevi al resto. Lucent rifiuta garbatamente l'accordo. Tutto ciò che scrivete è sigillato in un database crittografato che non lascia mai il vostro dispositivo. L'assistente può essere un modello nel cloud per cui pagate, un modello in esecuzione proprio sulla macchina davanti a voi, o assolutamente nulla. E l'intera opera viene assemblata senza una singola riga di strumenti locali: premete un pulsante su GitHub, fate due passi, e al vostro ritorno ci sarà un APK o un installer per Windows ad attendervi.

## Un'unica app, ovunque la posiate

Lucent è **un solo prodotto con un unico cuore condiviso**: la regola della casa impone che ogni funzionalità abiti ogni piattaforma su cui viene distribuita. La vostra tasca e la vostra scrivania parlano esattamente la medesima lingua:

- **`:app`** — l'applicazione Android (Kotlin e Jetpack Compose, Room su SQLCipher, llama.cpp tramite NDK). Compilata da `.github/workflows/build.yml` in un APK di rilascio firmato.
- **`:desktop`** — l'applicazione desktop, al momento per Windows (Compose for Desktop, pura JVM, SQLite su JDBC, llama.cpp compilato come DLL). Compilata da `.github/workflows/build-windows.yml` in un programma di installazione `.exe` con doppio clic.
- **`:shared`** — l'unico albero sorgente compilato da entrambe le piattaforme: logica di business, dati, gran parte dell'interfaccia, un unico catalogo di traduzione in quattordici lingue. Modificate una volta; cambiano entrambe le macchine.

Tutto ciò che può essere condiviso risiede in `shared/`; le poche cose che non possono esserlo — `SettingsRepository`, `Daos`/`Db`, `DocumentExport` e gli schermi più ampi — restano una volta per piattaforma perché le loro cuciture affondano in profondità. Le fasi di compilazione nativa sono ciascuna opzionale, così che un passo falso in una vi lasci con un'app funzionante anziché con una sala d'attesa.

## L'assistente con le mani, non soltanto con opinioni

È questa la caratteristica che definisce Lucent. Portate il vostro modello personale — le convenzioni di richiesta di OpenAI, Anthropic e Google sono parlate fluentemente, con svariati profili conservati e scambiabili con un tocco — oppure eseguite l'intera faccenda sul dispositivo. Non dite nulla e l'assistente semplicemente non sarà lì; l'app per gli appunti non perde alcunché.

Ciò che rende prezioso averlo è che può *agire*: creare, leggere, modificare, colorare, appuntare, archiviare ed eliminare note; creare, completare, riaprire, pianificare e dare priorità alle attività; cercare con un vero linguaggio di filtri; e allegare, rinominare o rimuovere file. Prima di alterare alcunché vi mostra con precisione ciò che intende fare — nella vostra lingua, in una finestra di dialogo che è essa stessa l'editor, con ogni campo discutibile modificabile prima che un singolo byte venga scritto. La vostra parola è sempre l'ultima pronunciata. Qualcuno con un'opinione e niente mani è una chat; questo è un maggiordomo a cui è stato insegnato a bussare prima.

Le conversazioni proseguono per tutto il tempo che desiderate e viaggiano con voi, e la conversazione in cui vi trovavate per ultima è quella che si apre. Le risposte arrivano in Markdown impeccabile — intestazioni, elenchi, codice — con il ragionamento del modello ripiegato con discrezione al di sopra di esse finché non lo richiedete, e una riga per ogni chiamata di strumento nella traccia, ciascuna con il tempo impiegato. Quattro pannelli sormontano la conversazione: la lista di cose da fare che l'assistente compila mentre lavora, il piano che sta seguendo, i processi in background che ha avviato e i sotto-agenti che ha convocato, ognuno dei quali può essere seguito in diretta, fermato o raggiunto da un'ulteriore istruzione. Dategli un obiettivo e punterà dritto verso di esso, round dopo round, finché l'obiettivo non sarà completato, messo in pausa o onestamente dichiarato bloccato.

## L'officina dietro l'assistente

Le note e le attività sono la facciata. Dietro di esse, a un interruttore di distanza in Impostazioni → Avanzate → Strumenti, si cela un'officina in cui l'assistente ha il permesso di entrare. Dispone di una cartella di lavoro a vostra scelta — il selettore esplora l'intero dispositivo, non soltanto l'archiviazione condivisa — e al suo interno, strumenti autentici: leggere, scrivere, cercare, modificare con un diff, creare istantanee e ripristinare. Può eseguire comandi con un tempo limite e una cartella di lavoro, mantenere quelli lunghi come processi in background e mostrarvene l'output. Può creare e modificare file Word, Excel e PowerPoint — i formati sono scritti nativamente da Lucent, senza alcuna suite d'ufficio sottostante — e leggere PDF, trasformarli in immagini, dividerli e unirli. Può clonare, confrontare, effettuare commit e push con Git, aprire e leggere issue di GitHub, pull request e log di CI, recuperare e consultare pagine web, interrogare SQLite ed eseguire un'operazione dentro Docker, PRoot o una comune shell, a seconda di ciò che la macchina è in grado di offrire.

Ricorda in tre modi: questa conversazione, questo progetto e voi. Mantiene un piano che potete osservare, annota le abilità così da non dover reimparare ogni volta le vostre consuetudini, e può evocare sotto-agenti quando un compito è abbastanza pesante da giustificarli. Sotto-agenti, istantanee, controllo del dispositivo e accesso allo schermo condividono una pagina tutta loro: Strumenti, poi Capacità dell'assistente.

Tutto ciò che è voluminoso resta fuori dal programma di installazione: uno spazio utente Linux, Python con le sue librerie per documenti, LibreOffice, Node.js, un motore di rendering per browser, strumenti di OCR e multimediali sono elencati come plugin, scaricati solo su vostra richiesta, dai server del progetto o da un mirror veloce, a seconda di chi risponda per primo. Installarne uno richiede una shell — l'ambiente Linux integrato dentro Lucent su telefono, winget su Windows — e Lucent guarda bene dove mette i piedi: ogni plugin viene esaminato prima che venga scaricato alcunché, così che una shell mancante, una cartella di download priva di permessi di scrittura o un mirror silenzioso vengano denunciati prima che costino preziosa banda, non dopo. Quando un download o un'installazione fallisce, Lucent apre una finestra di dialogo che indica la causa, mostra l'output stesso della shell ed elenca cosa correggere. Un download in corso si ferma premendo un pulsante, e fermarlo non lascia detriti dietro di sé. I permessi occupano una pagina tutta loro — lettura, scrittura, eliminazione, comandi, rete, Git, GitHub, navigazione, lo spazio oltre l'area di lavoro e il dispositivo stesso — ciascuno impostabile su consenti, chiedi o blocca, e ogni chiamata a uno strumento viene registrata in un registro delle attività che potete consultare e cancellare.

Su un telefono la medesima officina si spinge fino al dispositivo: leggere lo schermo, toccare, digitare, scorrere, catturare schermate, elencare e aprire applicazioni, leggere le notifiche, usare gli appunti e la torcia ed eseguire veri strumenti da riga di comando dentro l'ambiente Linux integrato. Parla inoltre MCP, così che qualunque server verso cui lo indirizzate diventi parte della cassetta degli attrezzi.

## Note che ricordano ciò che erano un tempo

Ogni modifica degna di nota viene catturata in un'istantanea, così da poter sempre verificare cosa dicesse una nota un tempo e ripristinarla quando una riscrittura spavalda si rivela puro ottimismo. Digitate `[[Lista della spesa]]` e diventa un collegamento toccabile; puntate a un titolo che non esiste ancora e il link si illumina di rosso finché un tocco non richiama benevolmente la nota all'esistenza. Il Markdown viene renderizzato quando lo desiderate e resta esattamente come è stato digitato quando non lo volete. Le liste di controllo sono cittadine di prima classe: riformulate le voci sul posto, aprite uno spazioso editor a scomparsa quando una modesta voce sviluppa delle ambizioni. Tag, colori, fissaggi, allegati crittografati singolarmente, testo formattato, una tela per scarabocchi per i pensieri che le parole non possono raggiungere, e un'area privata con il proprio lucchetto — con le bozze accanto al cestino, affinché un pensiero incompiuto non sia mai un pensiero abbandonato.

Una nota vuota offre quattro avvii rapidi a tocco singolo — diario, riunione, idea di progetto, lista di controllo — e poi il vero trucco: modelli tutti vostri. Salvatene uno e vi darà il benvenuto su ogni futura pagina bianca; premete a lungo per modificare o mandare a riposo ciascuno di essi, inclusi quelli predefiniti.

## Attività con scadenze che significano davvero qualcosa

Sotto-attività, priorità, pianificazioni di ripetizione e promemoria che resistono a un riavvio. Le date di scadenza vengono interpretate dal linguaggio quotidiano — *venerdì prossimo alle 18* diventa un autentico timestamp con un autentico allarme — e le cadenze di ripetizione sono trattate come elementi di primo piano anziché come un'arguta formula che alla fine capitola. Completare un'attività spunta con sé l'intera lista di controllo, e le attività concluse si ritirano in una schermata tutta loro.

## Oppure eseguite il tutto direttamente sul dispositivo

Importate un file `.gguf` (o un archivio `.zip` che ne custodisce uno) e l'assistente risponderà grazie a llama.cpp in esecuzione diretta sul dispositivo — niente account, niente chiave API, niente rete, e il modello viene scaricato nell'istante esatto in cui uscite dall'applicazione. I modelli Q4 da circa 1–4 GB rappresentano la quadratura del cerchio su un telefono; una postazione desktop può permettersi ambizioni superiori. Gli strumenti sono attivabili a piacimento in locale e l'accelerazione GPU è una scelta compiuta dopo un avvertimento schietto: la CPU funziona sempre, e una GPU recalcitrante ripiega con grazia. Anche la visione è facoltativa — importate un file mmproj e l'assistente studierà una fotografia per poi discuterne come un bibliotecario vagamente chiaroveggente.

## Quattordici lingue, senza cerimonie

English, 简体中文， 繁體中文， Español, العربية, Português, Français, Русский, Deutsch, 日本語， Türkçe, 한국어, Italiano, Nederlands — ogni schermata, finestra di dialogo, formato di data e modello. Cambiate la lingua e l'intera applicazione la segue nello stesso fotogramma, perché esiste un unico catalogo ed è quello che l'interfaccia consulta: ogni parola custodita una sola volta, per grazia di architettura e non per semplice scrupolo.

## Il suo aspetto: fatto, sfacciatamente, di vetro

Un gradiente dinamico fluttua dietro pannelli satinati che sfocano qualunque cosa transiti sotto di loro, senza mai ripetersi del tutto, a un costo computazionale pattuito con il dispositivo in anticipo. Una sontuosa scelta di palette distribuite in otto famiglie di stili, con un compagno a rotazione automatica o casuale che passeggia tra di esse. Chiaro, scuro, di sistema e una galleria di temi dalle tonalità Monet — e su Android 12 o successivi la palette prende in prestito per la giornata i colori del vostro sfondo. I widget esportano la medesima superficie vitrea sul vostro launcher; su Windows l'applicazione si accomoda nell'area di notifica e una barra laterale prende il testimone dalla barra delle schede inferiore, perché uno schermo panoramico merita ben altro che un'interfaccia da smartphone stirata sui fianchi.

## Il lucchetto che conta, e il backup che parte con voi

Il blocco dell'applicazione è una severa barriera contro la forza bruta anziché una cortese richiesta: tempi di attesa via via crescenti, un contatore persistente che sopravvive ai riavvii, una domanda di sicurezza opzionale, un'autodistruzione facoltativa e l'impronta digitale (Android) o Windows Hello (Windows) cortesemente assenti dall'ufficio per l'intera durata dell'attesa. Il database è crittografato a riposo, gli allegati singolarmente, e i backup sono custoditi in un unico file `.lcb` proteggibile da password che trasporta note, attività, cronologia, conversazioni, allegati e impostazioni da un dispositivo all'altro — visualizzato in anteprima prima che una sola virgola venga modificata, e pronto ad avviarsi automaticamente. Un backup che nessuno può leggere è un cimelio, non un backup; questo viene perciò verificato all'uscita e riletto all'ingresso.

## Una privacy strutturale, non semplicemente promessa

Nulla lascia il dispositivo prima che siate voi a esportarlo o a fornire a un modello un motivo per guardarci dentro. L'integrazione con il menu di condivisione è disattivata per impostazione predefinita; la diagnostica è disattivata di default e conservata localmente; i servizi di assistenza a cui vi collegate sono una decisione puramente vostra; la duplicazione su cloud è un modulo configurato da voi stessi, tramite WebDAV. L'unica deliberata deroga alla crittografia — l'esportazione in Markdown, Word, PDF o Excel — produce un file apribile ovunque altrove, il che è precisamente lo scopo.

## Rust, ma solo dove si guadagna il pane

Due percorsi critici sono scritti in Rust e raggiunti tramite JNI — le routine PBKDF2 e AES-256-GCM alla radice dei backup e della crittografia degli allegati, e l'apparato matematico che governa lo sfondo fluttuante. Entrambi ripiegano su una controparte Kotlin rigorosamente identica qualora la libreria nativa non sia presente, dimodoché l'applicazione non sia mai ostaggio degli umori di un compilatore.

## Compilarlo (sì, da uno smartphone, in vestaglia)

Niente Android Studio, niente SDK locale, nessuna riga di comando. Inviate il push su GitHub, aprite **Actions**, lanciate il workflow desiderato e scaricate il pacchetto risultante — una build di rilascio firmata a puntino o un programma di installazione a portata di doppio clic. I workflow risiedono nel repository, affinché la ricetta stia nella scatola anziché nella mente dell'autore, e la compilazione sia altrettanto ispezionabile del codice.

## Struttura del progetto

```
shared/       L'UNICO albero sorgente condiviso — logica di business e gran parte della UI risiedono
              qui UNA SOLA VOLTA, ed entrambe :app e :desktop lo compilano. Modificate un file qui per
              cambiare insieme entrambe le piattaforme.
app/          Modulo Android (:app) — guscio Activity, livello database Room, ponti JNI, widget,
              e soltanto il codice autenticamente vincolato ad Android
desktop/      Modulo desktop (:desktop) — guscio Compose for Desktop, shim JVM android.*,
              core del database JDBC, compilazione CMake nativa per la DLL del motore
rust/         L'acceleratore Rust (condiviso tra le piattaforme)
.github/      build.yml (APK Android), build-windows.yml (installer Windows), check.yml
```

## Verso dove proseguirà il cammino

Lucent è un'applicazione davvero imponente per una lista di cose da fare, e non ha mai domandato scusa per questo. Note fornite di memoria, compiti con gli artigli, un assistente con mani e buone maniere, una crittografia che fa onore alle proprie promesse e una superficie che merita contemplazione. Provatela per una settimana e osservate a cosa fate caso; usatela per un mese, e vi accorgerete di non farci più caso.

## Con gratitudine verso i giganti di cui queste sono le spalle

Sotto il vetro, Lucent racchiude una gran mole dell'eccellente lavoro di altri. Sarebbe cattiva educazione — e, in un caso o due, una palese violazione di licenza — non dirlo a voce alta. I testi integrali e le note di copyright risiedono in **[`THIRD-PARTY-NOTICES.md`](./docs/THIRD-PARTY-NOTICES.md)**; la versione breve, accompagnata dalla nostra gratitudine, è questa:

| Talento preso in prestito | Nel ruolo di | Sotto |
|---|---|---|
| [Kotlin](https://github.com/JetBrains/kotlin) & [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | il linguaggio, e la sua pazienza con la concorrenza | Apache-2.0 |
| [Jetpack Compose & AndroidX](https://developer.android.com/jetpack/androidx) | la metà Android dell'interfaccia | Apache-2.0 |
| [Compose Multiplatform & Skiko](https://github.com/JetBrains/compose-multiplatform) | la metà desktop dell'interfaccia | Apache-2.0 |
| [Material Icons](https://github.com/google/material-design-icons) | le piccole immagini che significano qualcosa | Apache-2.0 |
| [Haze](https://github.com/chrisbanes/haze) — © Chris Banes | tutta quell'elegante sfocatura di tendenza | Apache-2.0 |
| [OkHttp](https://github.com/square/okhttp) — © Square, Inc. | dialogare con il cloud | Apache-2.0 |
| [Apache PDFBox](https://pdfbox.apache.org/) | i PDF su desktop | Apache-2.0 |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) — © Taro L. Saito et al. | la via d'accesso del desktop a SQLite | Apache-2.0 |
| [Skia](https://skia.org/) | il disegno sottostante a entrambe le metà dell'interfaccia | BSD-3-Clause |
| [SQLite](https://www.sqlite.org/) | il database in persona, che fa girare il mondo in silenzio | Public Domain |
| [SQLCipher](https://www.zetetic.net/sqlcipher/) — © Zetetic LLC | il lucchetto su quel database, su Android | BSD-style |
| [llama.cpp & GGML](https://github.com/ggml-org/llama.cpp) — © Georgi Gerganov & contributors | un intero modello linguistico, sul vostro silicio | MIT |
| [Shizuku API](https://github.com/RikkaApps/Shizuku-API) — © Rikka and contributors | la shell privilegiata, quando un telefono è in grado di offrirne una | MIT |
| [org.json](https://github.com/stleary/JSON-java) — © JSON.org | leggere JSON su desktop | JSON License |
| [Great Vibes](https://github.com/googlefonts/great-vibes) — © The Great Vibes Pro Project Authors | il carattere corsivo con cui disegna la penna | OFL-1.1 |

Il Model Context Protocol è una specifica aperta di Anthropic, sotto licenza MIT; il client di Lucent è un'implementazione originale, e qualsiasi server MCP verso cui lo indirizzate è software di terze parti disciplinato dalle proprie condizioni. I motori di lettura e scrittura di Word, Excel e PowerPoint sono implementazioni originali di Office Open XML costruite sulle librerie zip e XML proprietarie della piattaforma — nessuna suite d'ufficio e nessuna libreria PDF di terze parti è inclusa nel pacchetto.

### E gli strumenti che potete procurarvi, a vostra parola

Lucent non include toolchain pesanti né suite da ufficio. Dalla pagina dei plugin, o chiedendolo all'assistente, può prelevare uno spazio utente Linux, un convertitore di documenti o uno strumento multimediale direttamente dai sorgenti a monte per riporlo nello spazio di archiviazione di vostra proprietà. Nessuno di essi fa parte del pacchetto, nessuno è collegato all'applicazione, e ciascuno rimane coperto dalla propria licenza: **[il file delle note](./docs/THIRD-PARTY-NOTICES.md)** ne riporta l'elenco completo, che spazia da PRoot e Ubuntu Base passando per LibreOffice, Python con le sue librerie di documenti, Node.js e Playwright, fino a Git, Pandoc, FFmpeg, ripgrep, 7-Zip, Tesseract, ImageMagick, qpdf, Poppler e yt-dlp. Il ringraziamento di Lucent va a ciascuno di questi autori, e la scelta di installare alcuno di essi — insieme alla licenza che ne consegue — appartiene solo a voi.

### E le applicazioni di note che abbiamo esaminato

Sei applicazioni mature per note e compiti sono state lette, esplorate e invitate a prendere un tè per questo progetto — non per il loro codice, che Lucent non include, ma per ciò che i loro autori hanno compreso prima di tutti. Le loro impronte digitali si riflettono nella struttura e nei modi di Lucent, piuttosto che nei suoi binari, e un debito di progettazione ripagato ad alta voce è il minimo che possiamo fare:

| Esaminato per | Ciò che Lucent ha preso in prestito | Le loro parole |
|---|---|---|
| [Quillpad](https://github.com/quillpad/quillpad) | il cassetto a due livelli — i promemoria fissati a portata di mano, la configurazione un livello più in basso | GPL-3.0 |
| [Omni-Notes](https://github.com/federicoiosue/Omni-Notes) | l'onestà dei selettori di sistema e delle azioni a pressione prolungata che si spiegano da sole | GPL-3.0 |
| [OpenNote-Compose](https://github.com/YangDai2003/OpenNote-Compose) | il modello di priorità dei colori dinamici — sfondo al primo posto, scelte salvate mai distrutte | GPL-3.0 |
| [AppFlowy](https://github.com/AppFlowy-IO/AppFlowy) | dichiarare apertamente cosa sta accadendo, nella pagina delle impostazioni e altrove | AGPL-3.0 |
| [Logseq](https://github.com/logseq/logseq) | l'idea che un'app di note debba dare la sensazione di un luogo, non di un cruscotto | AGPL-3.0 |
| [MarkLeaf](https://github.com/jeiel85/markleaf-android) | il principio per cui la superficie di scrittura resta pacata mentre la configurazione aspetta il suo turno | Apache-2.0 |

### E l'imbracatura a cui ci siamo ispirati

L'assistente di Lucent segue il vocabolario degli strumenti e le abitudini di lavoro del **DeepSeek Harness** (dsh) e di **[DSHA](https://github.com/DSH-APP/DSHA)** — con licenza MIT — il progetto Android che porta tale imbracatura su uno smartphone senza root e senza Termux. Lucent non include nessuno dei due nel pacchetto: gli strumenti, il ciclo operativo, i pannelli e gli obiettivi sono frutto del suo codice Kotlin, scritto per comportarsi nel modo stabilito da quei due predecessori. I codici di uscita arrivano come risultati anziché come errori, un file viene letto prima di essere scritto, un obiettivo tiene traccia dei propri round, gli aiutanti possono essere interpellati e fermati, e su qualunque aspetto discutibile viene prima chiesto il permesso. Il modello è loro ed è qui riconosciuto; il codice è nostro.

La licenza di Lucent disciplina tutto ciò che installate; i progetti sopra menzionati sono onorati come fonti di idee e configurazioni, non come opere incluse. La struttura è nostra; la cortesia spetta a loro.

Una parola particolare va a **SQLCipher**, la cui licenza in stile BSD chiede — non a torto, visto che è ciò che mantiene sigillato il vostro diario — che il suo copyright e la sua informativa vengano riprodotti dove un utente possa effettivamente trovarli. E così è, nel file delle note sopra indicato; se distribuite una build di Lucent, manteneteli reperibili. L'unico tipo di carattere che Lucent porta con sé è Great Vibes, il corsivo della penna sulla schermata iniziale, incluso intatto sotto licenza SIL Open Font con il suo nome riservato intatto.

## Licenza, e l'unica piccola cosa che chiede in cambio

Lucent è rilasciato sotto la **licenza MIT** — si veda [`LICENSE`](./LICENSE). Fatene quasi tutto ciò che vi pare: usatelo, modificatelo, incorporatelo in qualcosa di commerciale, costruiteci sopra qualcosa di migliore senza mai scriverci per ringraziarci. La sola condizione, del tutto ragionevole, è che la nostra nota di copyright e il testo della licenza salgano a bordo di qualsiasi copia o porzione sostanziale del codice — perciò, se riutilizzate Lucent, allegate il file `LICENSE` (e il nome ivi riportato) a ciò che distribuite, e saremo pari. I componenti di terze parti citati sopra avanzano le loro richieste, altrettanto modeste; onoratele con il medesimo spirito e resteremo tutti amici.

Informativa sulla privacy: **[`privacy.html`](https://lucent-project.github.io/lucentapp/privacy.html)** — cosa conserva Lucent, cosa lascia il vostro dispositivo e ogni controllo nelle vostre mani.

## Contribuire

Qualora foste colti dall'impulso di perfezionare Lucent, ne saremmo discretamente deliziati. Segnalazioni di bug, suggerimenti ponderati e pull request inoltrate di buon grado sono i benvenuti. Chiediamo solo che ciascuno mantenga una rigorosa civiltà, conservi il tè ben caldo e tratti i compagni di viaggio con la cortesia che ci si attende in una rispettabile sala di lettura.
