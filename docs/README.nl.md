<div align="center">

[🇨🇳 简体中文](./docs/README.zh-CN.md) · [🇯🇵 日本語](./docs/README.ja.md) · [🇰🇷 한국어](./docs/README.ko.md)

</div>

<div align="center">

# 𝓛𝓾𝓬𝓮𝓷𝓽

### Modern · Minimalistisch · Stilletjes overgekwalificeerd

**Een notitie- en takentoepassing waarvan de assistent uw gegevens daadwerkelijk mag aanraken — verzegeld
in een versleutelde database op uw eigen apparaat, bedreven in veertien talen, en van de eerste tot de
laatste regel geassembleerd met één druk op de knop op GitHub. Hij verschijnt zowel in zak- als
bureau-editie — een Android-APK en een Windows-installatieprogramma — met één kloppend hart, klaar om
verder te zwerven.**

![Lucent — Platform, Build, Interface, Assistent, Privacy, Licentie](badges/badges.svg)

</div>

---

## De grondgedachte

De meeste notitietoepassingen nodigen u vriendelijk uit om twee van de drie deugden te kiezen: de
mooie, de privacyvriendelijke of de intelligente. Kies uw paar en leg u neer bij het
gemis van de rest. Lucent slaat deze ruil beleefd af. Alles wat u schrijft, zit verzegeld in een versleutelde
database die uw apparaat nooit verlaat. De assistent kan een cloudmodel zijn waarvoor u betaalt, een model
dat draait op de machine recht voor uw neus, of helemaal niets. En het hele geheel wordt in elkaar gezet
zonder ook maar één regel lokale hulpmiddelen: druk op een knop op GitHub, loop even weg, en bij
terugkomst wacht er een APK of een Windows-installatieprogramma op u.

## Eén app, waar u hem ook neerzet

Lucent is **één product met een enkel gedeeld hart**: de huisregel dicteert dat elke functie
aanwezig is op elk platform waarop hij verschijnt. Uw broekzak en uw bureau spreken exact dezelfde taal:

- **`:app`** — de Android-toepassing (Kotlin en Jetpack Compose, Room over SQLCipher, llama.cpp
  via de NDK). Gebouwd door `.github/workflows/build.yml` tot een ondertekende release-APK.
- **`:desktop`** — de desktoptoepassing, heden ten dage Windows (Compose for Desktop, zuivere JVM, SQLite over
  JDBC, llama.cpp gecompileerd als DLL). Gebouwd door `.github/workflows/build-windows.yml` tot een
  dubbelklikbaar `.exe`-installatieprogramma.
- **`:shared`** — de ene broncodeboom die beide platforms compileren: bedrijfslogica, data, het grootste deel
  van de interface, één vertaalcatalogus in veertien talen. Eén keer bewerken; beide machines veranderen mee.

Alles wat gedeeld kan worden, huist in `shared/`; het weinige dat dat niet kan — `SettingsRepository`,
`Daos`/`Db`, `DocumentExport` en de grootste schermen — blijft één keer per platform bestaan omdat de naden
diep lopen. De afzonderlijke fasen van de native build zijn stuk voor stuk optioneel; struikelt er eentje, dan houdt u een werkende
app over in plaats van een wachtkamer.

## De assistent met handen, niet louter meningen

Dit is de eigenschap die Lucent kenmerkt. Neem uw eigen model mee — de verzoekstructuren van OpenAI, Anthropic en Google
worden vloeiend gesproken, meerdere profielen worden bewaard en met één tik gewisseld — of draai het geheel op
het apparaat zelf. Zegt u helemaal niets, dan schittert de assistent eenvoudigweg door afwezigheid; de notitie-app verliest er niets door.

Wat hem de moeite waard maakt, is dat hij kan *handelen*: notities maken, lezen, bewerken, kleuren, vastpinnen, archiveren en verwijderen;
taken aanmaken, voltooien, heropenen, inplannen en prioriteren; zoeken met een volwaardige filtertaal;
en bestanden bijvoegen, hernoemen of verwijderen. Voordat hij ook maar iets wijzigt, toont hij u precies wat hij
van plan is — in uw eigen taal, in een dialoogvenster dat tevens de editor is, waarbij elk betwistbaar veld
bewerkbaar is voordat er ook maar één byte geschreven wordt. Uw woord is altijd het laatste woord. Iemand met een
mening en zonder handen is een chatbox; dit is een butler die geïnstrueerd is eerst aan te kloppen.

Gesprekken duren zo lang als u wilt en reizen met u mee, en het gesprek waarin u zich het laatst bevond, is
het gesprek dat opent. Antwoorden arriveren als keurige Markdown — koppen, lijsten, code — waarbij de redenering
van het model daarboven ingeklapt blijft totdat u ernaar vraagt, en één regel per tool-aanroep in het logboek,
telkens met de verstreken tijd. Boven het gesprek bevinden zich vier panelen: de takenlijst die de assistent
tijdens het werk bijhoudt, het plan dat hij volgt, de achtergrondtaken die hij heeft gestart, en de sub-agenten
die hij te hulp heeft geroepen — stuk voor stuk live te volgen, te stoppen of van een extra instructie te voorzien. Geef hem
een doel en hij zal er ronde na ronde op afstevenen, totdat het doel bereikt is, gepauzeerd wordt of
naar waarheid als geblokkeerd wordt gemeld.

## De werkplaats achter de assistent

Notities en taken vormen het voorhuis. Daarachter, één schakelaar verwijderd onder Instellingen → Geavanceerd → Toolkit,
bevindt zich een werkplaats die de assistent mag betreden. Hij beschikt over een werkruimtemap naar uw keuze — de
kiezer doorzoekt het hele apparaat, niet slechts de gedeelde opslag — en daarbinnen
over echt gereedschap: lezen, schrijven, zoeken, bewerken met diff, momentopnamen maken en terugdraaien. Hij kan commando's
uitvoeren met een time-out en een werkmap, langdurige taken als achtergrondtaak aanhouden en u de uitvoer tonen.
Hij kan Word-, Excel- en PowerPoint-bestanden maken en bewerken — de formaten worden door
Lucent zelfstandig geschreven, zonder office-bibliotheek eronder — en PDF's lezen, omzetten in afbeeldingen, splitsen en
samenvoegen. Hij kan klonen, diffen, committen en pushen met Git, GitHub-issues, pull requests en
CI-logs aanmaken en inzien, webpagina's ophalen en lezen, SQLite bevragen, en een taak draaien in Docker, PRoot of een gewone
shell — wat de machine maar te bieden heeft.

Hij onthoudt op drie manieren: dit gesprek, dit project, en uzelf. Hij houdt een plan bij dat u kunt inzien,
noteert vaardigheden zodat hij niet telkens uw gewoontes opnieuw hoeft te leren, en kan sub-agenten oproepen wanneer een
taak zwaar genoeg is om dat te rechtvaardigen. Sub-agenten, momentopnamen, apparaatbediening en schermtoegang delen
een eigen pagina: Toolkit, en vervolgens Assistent-vaardigheden.

Alles van aanzienlijke omvang blijft buiten het installatieprogramma: een Linux-userland, Python met documentbibliotheken,
LibreOffice, Node.js, een browser-engine, OCR- en mediatools staan vermeld als plug-ins, en worden pas gedownload
als u daarom vraagt — van de eigen servers van het project of een snelle mirror, wie het eerst antwoordt. Voor de installatie
is een shell vereist — de Linux-omgeving die op een telefoon in Lucent is ingebouwd, winget op Windows — en Lucent bezint zich eer het begint: elke
plug-in wordt geïnspecteerd voordat er iets wordt gedownload, zodat een ontbrekende shell, een niet-beschrijfbare downloadmap
of een zwijgende mirror wordt opgemerkt voordat het bandbreedte kost, niet erna. Mislukt een
download of installatie,
dan opent Lucent een dialoogvenster dat de reden noemt, de eigen uitvoer van de shell toont en opsomt wat er hersteld moet worden. Een
lopende download is met één druk op de knop te stoppen, en stoppen laat geen enkel spoor na.
Machtigingen
hebben een eigen pagina — lezen, schrijven, verwijderen, commando's, netwerk, Git, GitHub, browsen, de ruimte
buiten de werkruimte en het apparaat zelf — elk in te stellen op toestaan, vragen of blokkeren, waarbij elke tool-aanroep
wordt vastgelegd in een activiteitenlogboek dat u kunt inzien en wissen.

Op een telefoon reikt diezelfde werkplaats tot in het apparaat zelf: het scherm aflezen, tikken, typen, vegen, een
screenshot maken, apps opsommen en openen, meldingen lezen, het klembord en de zaklamp gebruiken, en echte
opdrachtregelprogramma's uitvoeren binnen de meegeleverde Linux-omgeving. Hij spreekt tevens MCP, zodat elke server waar u hem naar verwijst
onderdeel wordt van de gereedschapskist.

## Notities die zich herinneren wat ze ooit waren

Van elke betekenisvolle bewerking wordt een momentopname gemaakt, zodat u altijd kunt zien wat een notitie ooit inhield en
deze kunt herstellen wanneer een zelfverzekerde herschrijving louter optimisme bleek te zijn. Typ `[[Shopping list]]` en het wordt een
aantikbare link; verwijs naar een titel die nog niet bestaat en de link licht rood op, totdat een tik de notitie genadig
tot leven wekt. Markdown wordt gerenderd wanneer u dat wilt en blijft exact zoals getypt staan wanneer u dat
niet wilt. Checklists zijn eersteklas burgers: pas punten ter plekke aan, of open een ruime pop-out editor
wanneer een beknopt punt ineens ambities ontwikkelt. Labels, kleuren, vastpinnen, individueel versleutelde bijlagen,
rijke tekst, een krabbeldoek voor gedachten waar woorden niet bij kunnen, en een privégedeelte met een eigen slot —
met concepten naast de prullenbak, zodat een onvoltooide gedachte nooit een achtergelaten gedachte wordt.

Een blanco notitie biedt vier snelle starters met één tik — dagboek, vergadering, projectidee, checklist — en vervolgens de
ware troef: uw eigen sjablonen. Sla er een op en hij verwelkomt u op elke toekomstige lege pagina; houd lang ingedrukt
om ze te bewerken of met pensioen te sturen, inclusief de ingebouwde exemplaren.

## Taken met einddatums die daadwerkelijk iets betekenen

Subtaken, prioriteiten, herhaalschema's en herinneringen die een herstart overleven. Vervaldatums worden ontleed
uit gewone spreektaal — *next Friday at 6* wordt een heuse tijdstempel met een heus alarm — en
herhaalritmes zijn eersteklas burgers in plaats van een spitsvondig zinnetje dat het uiteindelijk opgeeft. Het voltooien
van een taak vinkt meteen de hele checklist mee af, en voltooide taken trekken zich terug op een eigen scherm.

## Of draai het geheel op het apparaat zelf

Importeer een `.gguf`-bestand (of een `.zip` met eentje erin) en de assistent antwoordt via llama.cpp, draaiend
op het apparaat zelf — geen account, geen API-sleutel, geen netwerk, en het model wordt ontladen op het moment dat u
de app verlaat. Modellen van ruwweg 1–4 GB (Q4) vormen het ideale evenwicht op een telefoon; een desktop kan zich grotere
ambities permitteren. Tools zijn lokaal optioneel in te schakelen en GPU-versnelling is een keuze die gemaakt wordt na een duidelijke waarschuwing: de
CPU werkt altijd, en een GPU die tegensputtert valt daar gracieus op terug. Beeldherkenning is eveneens optioneel — importeer een
mmproj-bestand en de assistent bestudeert een foto en bespreekt deze als een lichtelijk helderziende
bibliothecaris.

## Vier talen, gewisseld zonder plichtplegingen

English, 中文, 日本語, 한국어 — elk scherm, elk dialoogvenster, elke datumnotatie en elk sjabloon. Schakel over naar een andere taal
en de hele app volgt binnen hetzelfde beeldframe, want er is één catalogus en dat is wat de
interface leest: elk woord één keer vastgelegd, uit architectonisch principe in plaats van uit louter zorgvuldigheid.

## De aanblik ervan: onbeschaamd vervaardigd uit glas

Een levend kleurverloop zweeft achter matglazen panelen die alles wat eronderdoor glijdt doen vervagen, zonder ooit precies
te herhalen — tegen een prijs waarmee het apparaat vooraf heeft ingestemd. Een royale schakering van paletten over
acht stijlfamilies, vergezeld van een automatische cyclus of een willekeurige metgezel die er op zijn gemak doorheen kuiert. Licht, donker,
systeem en een galerij van door Monet geïnspireerde thema's — en op Android 12 of nieuwer leent het palet voor de dag de kleuren van uw
achtergrond. Widgets brengen hetzelfde glazen oppervlak naar uw launcher; op Windows
glipt de app naar het systeemvak en neemt een zijbalk het over van de onderste tabbalk, want een breed beeldscherm
verdient beter dan een zijwaarts uitgerekt telefoonontwerp.

## Het slot dat telt, en de back-up die met u meegaat

Het app-slot is een onverbiddelijk beleid tegen brute-force-aanvallen in plaats van een beleefd verzoek: escalerende wachttijden, een
persistente teller die herstarts overleeft, een optionele beveiligingsvraag, een optionele zelfvernietiging,
en een vingerafdruk (Android) of Windows Hello (Windows) die gedurende de gehele wachttijd beleefd buiten dienst
blijft. De database is in rusttoestand versleuteld, bijlagen afzonderlijk, en back-ups bestaan uit een enkel,
met een wachtwoord te beveiligen `.lcb`-bestand dat notities, taken, geschiedenis, chats, bijlagen en instellingen
over apparaten heen meeneemt — voorafgegaan door een voorbeeldweergave voordat ook maar één item wordt gewijzigd, en gewapend om automatisch te draaien. Een back-up
die niemand kan lezen is eerder een aandenken dan een reservekopie; daarom wordt deze bij het verlaten en opnieuw bij
het binnenkomen gecontroleerd.

## Privacy die structureel is, niet louter beloofd

Niets verlaat het apparaat totdat u het exporteert of een model een reden geeft om een blik te werpen. Integratie met het
deelmenu staat standaard uit; diagnostische gegevens staan standaard uit en blijven lokaal; welke
assistentdiensten u koppelt, is geheel aan u; cloudsynchronisatie is een module die u zelf configureert,
via WebDAV. De enige weloverwogen uitzondering op de versleuteling — exporteren naar Markdown, Word, PDF of
Excel — levert een bestand op dat u overal elders kunt openen, en dat is tenslotte precies de bedoeling.

## Rust, maar alleen waar het zijn brood verdient

Twee kritieke trajecten zijn geschreven in Rust en worden bereikt via JNI — de PBKDF2- en AES-256-GCM-routines
achter back-ups en bijlageversleuteling, en de wiskunde achter de deinende achtergrond. Beide
vallen terug op identieke Kotlin-code wanneer de native bibliotheek ontbreekt, zodat de app nooit
gegijzeld wordt door een compiler.

## Het bouwen (jazeker, vanaf een telefoon, in uw ochtendjas)

Geen Android Studio, geen lokale SDK, geen opdrachtregel. Push naar GitHub, open **Actions**, start de gewenste
workflow en download het resultaat — een keurig ondertekende release of een dubbelklikbare installer. De
workflows bevinden zich in de repository, zodat het recept in de doos zit in plaats van in het hoofd van de maker, en
het bouwproces net zo transparant is als de code zelf.

## Projectindeling

```
shared/       DÉ ene gedeelde broncodeboom — bedrijfslogica en het merendeel van de UI leven hier
              ÉÉN KEER, en zowel :app als :desktop compileren deze. Bewerk hier een bestand om beide
              platforms tegelijk te wijzigen.
app/          Android-module (:app) — Activity-schil, Room-databaselaag, JNI-bruggen, widgets en
              uitsluitend code die waarlijk aan Android gebonden is
desktop/      Desktop-module (:desktop) — Compose for Desktop-schil, de android.* JVM-shims, de
              JDBC-databasekern, native CMake-build voor de engine-DLL
rust/         De Rust-versneller (gedeeld over platforms)
.github/      build.yml (Android APK), build-windows.yml (Windows-installer), check.yml
```

## Waar het heengaat

Lucent is buitengewoon veel applicatie voor een takenlijst, en heeft zich daar nog nooit voor verontschuldigd.
Notities met een geheugen, taken met tanden, een assistent met handen en manieren, versleuteling die
haar beloften nakomt en een oppervlak dat uitnodigt tot contemplatie. Gebruik het een week en let op wat u
opvalt; gebruik het een maand, en merk dat het u niet meer opvalt.

## Met dank aan de reuzen op wier schouders dit rust

Onder het glas is Lucent voor een aanzienlijk deel het uitmuntende werk van anderen. Het zou van slechte
smaak getuigen — en in een enkel geval ronduit een licentieovertreding zijn — om dat niet hardop uit te spreken. De volledige
teksten en auteursrechtkennisgevingen zijn te vinden in **[`THIRD-PARTY-NOTICES.md`](./docs/THIRD-PARTY-NOTICES.md)**;
de korte versie, vergezeld van onze dank, is als volgt:

| Geleende schittering | Doet dienst als | Onder |
|---|---|---|
| [Kotlin](https://github.com/JetBrains/kotlin) & [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | de taal, en haar geduld met gelijktijdigheid | Apache-2.0 |
| [Jetpack Compose & AndroidX](https://developer.android.com/jetpack/androidx) | de Android-helft van de interface | Apache-2.0 |
| [Compose Multiplatform & Skiko](https://github.com/JetBrains/compose-multiplatform) | de desktophelft van de interface | Apache-2.0 |
| [Material Icons](https://github.com/google/material-design-icons) | de kleine prentjes met betekenis | Apache-2.0 |
| [Haze](https://github.com/chrisbanes/haze) — © Chris Banes | al die modieuze onscherpte | Apache-2.0 |
| [OkHttp](https://github.com/square/okhttp) — © Square, Inc. | praten met de cloud | Apache-2.0 |
| [Apache PDFBox](https://pdfbox.apache.org/) | pdf's op de desktop | Apache-2.0 |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) — © Taro L. Saito et al. | de weg van de desktop naar SQLite | Apache-2.0 |
| [Skia](https://skia.org/) | het tekenwerk onder beide helften van de interface | BSD-3-Clause |
| [SQLite](https://www.sqlite.org/) | de database zelf, die stilletjes de wereld draaiende houdt | Public Domain |
| [SQLCipher](https://www.zetetic.net/sqlcipher/) — © Zetetic LLC | het slot op die database, op Android | BSD-style |
| [llama.cpp & GGML](https://github.com/ggml-org/llama.cpp) — © Georgi Gerganov & contributors | een heel taalmodel, op uw eigen silicium | MIT |
| [Shizuku API](https://github.com/RikkaApps/Shizuku-API) — © Rikka and contributors | de bevoorrechte shell, wanneer een telefoon die kan bieden | MIT |
| [org.json](https://github.com/stleary/JSON-java) — © JSON.org | JSON lezen op de desktop | JSON License |
| [Great Vibes](https://github.com/googlefonts/great-vibes) — © The Great Vibes Pro Project Authors | het schoonschrift waarmee de pen tekent | OFL-1.1 |

Het Model Context Protocol is een open specificatie van Anthropic, gelicentieerd onder de MIT-licentie;
de client van Lucent is een originele implementatie, en elke MCP-server waar u hem naar verwijst is software van derden
onder diens eigen voorwaarden. De lees- en schrijfmodules voor Word, Excel en PowerPoint zijn originele
implementaties van Office Open XML, gebouwd op de eigen zip- en XML-bibliotheken van het platform — er wordt geen office-bibliotheek
en geen pdf-bibliotheek van derden meegeleverd.

### En het gereedschap dat u kunt ophalen, op uw wenken

Lucent levert geen zwaarwichtige toolchains en geen kantoorpakket mee. Vanaf de plug-inspagina, of door het de
assistent te vragen, kan het een Linux-userland, een documentconversieprogramma of een mediatool rechtstreeks van de bron
ophalen naar opslagruimte die van uzelf is. Geen daarvan maakt deel uit van het pakket, tegen geen ervan is gelinkt, en elk blijft
onder zijn eigen licentie: **[het mededelingenbestand](./docs/THIRD-PARTY-NOTICES.md)** bevat de volledige lijst,
die loopt van PRoot en Ubuntu Base via LibreOffice, Python met zijn documentbibliotheken,
Node.js en Playwright, tot Git, Pandoc, FFmpeg, ripgrep, 7-Zip, Tesseract, ImageMagick,
qpdf, Poppler en yt-dlp. Lucents dank gaat uit naar elk van die auteurs, en de keuze om
ze te installeren — samen met de bijbehorende licentie — is aan u.

### En de notitie-apps die we bestudeerd hebben

Zes volwassen notitie- en takentoepassingen werden voor dit project gelezen, aan de tand gevoeld en op de thee gevraagd — niet
vanwege hun code, die Lucent niet meelevert, maar om de zaken die hun makers als eersten hebben uitgevogeld.
Hun vingerafdrukken zijn zichtbaar in Lucents structuur en manieren, eerder dan in de binaire bestanden, en een
ontwerpschuld die luidkeels wordt ingelost is wel het minste wat we kunnen doen:

| Bestudeerd om | Wat Lucent heeft geleend | Hun voorwaarden |
|---|---|---|
| [Quillpad](https://github.com/quillpad/quillpad) | de tweelaagse lade — vastpinnen binnen handbereik, configuratie een niveau lager | GPL-3.0 |
| [Omni-Notes](https://github.com/federicoiosue/Omni-Notes) | de eerlijkheid van systeempickers en langindruk-acties die voor zichzelf spreken | GPL-3.0 |
| [OpenNote-Compose](https://github.com/YangDai2003/OpenNote-Compose) | het dynamische kleurprioriteitsmodel — achtergrond eerst, opgeslagen keuzes nooit vernietigd | GPL-3.0 |
| [AppFlowy](https://github.com/AppFlowy-IO/AppFlowy) | openlijk zeggen wat er gebeurt, op de instellingenpagina en elders | AGPL-3.0 |
| [Logseq](https://github.com/logseq/logseq) | dat een notitie-app moet aanvoelen als een plek, niet als een dashboard | AGPL-3.0 |
| [MarkLeaf](https://github.com/jeiel85/markleaf-android) | dat het redactionele vlak sereen blijft terwijl de configuratie op haar beurt wacht | Apache-2.0 |

### En het harnas waaraan we een voorbeeld namen

Lucents assistent volgt het gereedschapsvocabulair en de werkgewoonten van de **DeepSeek Harness**
(dsh), en van **[DSHA](https://github.com/DSH-APP/DSHA)** — onder MIT-licentie — het Android-project dat
dat harnas naar een telefoon brengt zonder root en zonder Termux. Lucent levert geen van beide mee: de
tools, de loop, de panelen en de doelen zijn eigen Kotlin-code, geschreven om zich te gedragen zoals die twee
hebben neergezet. Exit-codes arriveren als resultaten in plaats van fouten, een bestand wordt eerst gelezen alvorens het wordt geschreven,
een doel houdt zijn eigen rondes bij, helpers kunnen worden aangesproken en gestopt, en bij alles wat voor discussie vatbaar is wordt
eerst toestemming gevraagd. Het patroon is van hen en wordt hier geëerd; de code is van ons.

Lucents eigen licentie is van toepassing op alles wat u installeert; de bovenstaande projecten worden geëerd als bronnen van
ideeën en ordeningen, niet als meegeleverde werken. De structuur is van ons; de hoffelijkheid is van hen.

Een speciaal woord voor **SQLCipher**, waarvan de licentie in BSD-stijl vraagt — niet onredelijk, aangezien het
dát is wat uw dagboek gesloten houdt — dat het auteursrecht en de kennisgeving worden gereproduceerd op een plek waar een gebruiker ze
ook daadwerkelijk kan vinden. Dat zijn ze dan ook, in het bovenstaande mededelingenbestand; verspreidt u een build van Lucent, houd ze dan
vindbaar. Het enige lettertype dat Lucent zelf meebrengt is Great Vibes, het schoonschrift van de pen op het opstartscherm,
ongewijzigd meegeleverd onder de SIL Open Font License met behoud van de gereserveerde naam.

## Licentie, en het ene kleine ding dat zij als tegenprestatie vraagt

Lucent wordt uitgebracht onder de **MIT-licentie** — zie [`LICENSE`](./LICENSE). Doe ermee vrijwel wat
u maar wilt: gebruik het, verander het, verwerk het in iets commercieels, bouw er iets beters bovenop
en schrijf ons nooit een bedankje. De enige, volstrekt redelijke voorwaarde is dat onze auteursrechtvermelding
en de licentietekst meereizen in elke kopie of substantieel deel van de code — dus als u Lucent hergebruikt, bewaar dan het
`LICENSE`-bestand (en de naam daarop) bij wat u verspreidt, en we staan quitte. De bovenstaande
componenten van derden stellen hun eigen, vergelijkbaar bescheiden verzoeken; respecteer die in dezelfde geest en we blijven allemaal vrienden.

Privacybeleid: **[`privacy.html`](https://lucent-project.github.io/lucentapp/privacy.html)** — wat Lucent opslaat, wat uw apparaat verlaat, en elke controleknop die u in handen heeft.

## Bijdragen

Mocht u worden bevangen door de drang om Lucent te verbeteren, dan zouden we stilletjes verheugd zijn. Foutrapporten,
doordachte suggesties en pull requests die met welwillendheid worden ingediend, zijn van harte welkom. We vragen
slechts dat iedereen strikt beschaafd blijft, de thee warm houdt, en medebijdragers behandelt met de
hoffelijkheid die men in een fatsoenlijke leeszaal mag verwachten.
