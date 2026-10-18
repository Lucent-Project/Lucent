<div align="center">

[🇬🇧 English](../README.md) · [🇨🇳 简体中文](./README.zh-CN.md) · [🇭🇰 繁體中文](./README.zh-Hant.md) · [🇪🇸 Español](./README.es.md)<br>
[🇸🇦 العربية](./README.ar.md) · [🇵🇹 Português](./README.pt.md) · [🇫🇷 Français](./README.fr.md) · [🇷🇺 Русский](./README.ru.md)<br>
[🇯🇵 日本語](./README.ja.md) · [🇹🇷 Türkçe](./README.tr.md) · [🇰🇷 한국어](./README.ko.md) · [🇮🇹 Italiano](./README.it.md)<br>
[🇳🇱 Nederlands](./README.nl.md)

</div>

<div align="center">

# 𝓛𝓾𝓬𝓮𝓷𝓽

### Modern · Minimalistisch · Dezent überqualifiziert

**Eine Notiz- und Aufgaben-App, deren Assistent Ihre Daten wahrhaftig berühren darf – versiegelt in einer
verschlüsselten Datenbank auf Ihrem eigenen Gerät, gewandt in vierzehn Sprachen und von der ersten bis zur
letzten Zeile auf einen einzigen Knopfdruck bei GitHub gebaut. Sie erscheint gleichermaßen im Taschen- wie im
Schreibtischformat – als Android-APK und Windows-Installer –, beseelt von einem gemeinsamen Herzschlag und
bereit für weitere Streifzüge.**

![Lucent — Plattform, Build, Benutzeroberfläche, Assistent, Privatsphäre, Lizenz](../badges/badges.svg)

</div>

---

## Der Grundgedanke

Die meisten Notiz-Apps laden großzügig dazu ein, zwei von drei Tugenden zu wählen: das
Schöne, das Private oder das Intelligente. Wählen Sie Ihr Paar und fügen Sie sich in den
Verzicht auf den Rest. Lucent lehnt diesen Handel höflich ab. Alles, was Sie schreiben, ist in einer verschlüsselten
Datenbank versiegelt, die Ihr Gerät niemals verlässt. Der Assistent kann ein Cloud-Modell sein, für das Sie bezahlen, ein Modell,
das direkt auf der Maschine vor Ihnen läuft, oder schlicht überhaupt nichts. Und das ganze Werk entsteht
ohne eine einzige Zeile lokaler Werkzeuge: Drücken Sie einen Knopf auf GitHub, spazieren Sie davon, und bei
Ihrer Rückkehr wartet bereits eine APK oder ein Windows-Installer auf Sie.

## Eine App, wohin man sie auch setzt

Lucent ist **ein Produkt mit einem einzigen gemeinsamen Herzen**: Die Hausregel verlangt, dass jede Funktion
auf jeder Plattform heimisch ist, auf die sie ausgeliefert wird. Ihre Hosentasche und Ihr Schreibtisch sprechen exakt dieselbe Sprache:

- **`:app`** — die Android-Anwendung (Kotlin und Jetpack Compose, Room über SQLCipher, llama.cpp
  über das NDK). Von `.github/workflows/build.yml` zu einer signierten Release-APK gebaut.
- **`:desktop`** — die Desktop-Anwendung, heute Windows (Compose for Desktop, reine JVM, SQLite über
  JDBC, llama.cpp als DLL kompiliert). Von `.github/workflows/build-windows.yml` zu einem
  doppelklickbaren `.exe`-Installer gebaut.
- **`:shared`** — der einzige Quelltextbaum, den beide Plattformen kompilieren: Geschäftslogik, Daten, der Großteil
  der Oberfläche, ein Übersetzungskatalog in vierzehn Sprachen. Einmal bearbeiten; beide Maschinen ändern sich.

Alles, was geteilt werden kann, wohnt in `shared/`; das Wenige, das sich nicht teilen lässt — `SettingsRepository`,
`Daos`/`Db`, `DocumentExport` und die größten Bildschirme —, bleibt einmal pro Plattform bestehen, weil ihre Nahtstellen
tief reichen. Die nativen Build-Schritte sind jeweils optional; gerät also einer ins Stolpern, bleibt Ihnen eine funktionierende
App statt eines Wartezimmers.

## Der Assistent mit Händen, nicht bloß Meinungen

Dies ist das Merkmal, das Lucent ausmacht. Bringen Sie Ihr eigenes Modell mit — die Anfrageformate von OpenAI, Anthropic und Google
werden fließend gesprochen, mehrere Profile verwaltet und mit einem Fingertipp gewechselt — oder betreiben Sie das Ganze direkt auf
dem Gerät. Sagen Sie gar nichts, und der Assistent glänzt schlicht durch Abwesenheit; der Notiz-App fehlt es an nichts.

Was ihn lohnenswert macht, ist, dass er *handeln* kann: Notizen erstellen, lesen, bearbeiten, einfärben, anheften, archivieren und löschen;
Aufgaben erstellen, erledigen, wieder öffnen, terminieren und priorisieren; mit einer echten Filtersprache suchen;
und Dateien anhängen, umbenennen oder entfernen. Bevor er irgendetwas anrührt, zeigt er Ihnen haargenau, was er
vorhat — in Ihrer eigenen Sprache, in einem Dialog, der selbst der Editor ist, wobei jedes diskutable Feld
anpassbar bleibt, ehe auch nur ein einziges Byte geschrieben wird. Ihr Wort ist stets das letzte. Jemand mit einer
Meinung und ohne Hände ist bloß ein Chat; dies hier ist ein Butler, dem aufgetragen wurde, zuvor anzuklopfen.

Unterhaltungen dauern so lange, wie Sie möchten, und reisen mit Ihnen; und das Gespräch, in dem Sie sich zuletzt aufhielten,
öffnet sich wieder. Antworten treffen als ordentliches Markdown ein — Überschriften, Listen, Code —, wobei die Gedankengänge
des Modells darüber eingeklappt ruhen, bis Sie danach verlangen, und im Verlauf steht eine Zeile pro Werkzeugaufruf,
jeweils samt der benötigten Zeit. Über der Konversation thronen vier Leisten: die To-do-Liste, die der Assistent
bei der Arbeit führt, der Plan, dem er folgt, die Hintergrundjobs, die er angestoßen hat, und die Unteragenten,
die er zu Hilfe rief – jeder davon lässt sich live beobachten, anhalten oder mit einer zusätzlichen Anweisung versehen. Setzen Sie ihm
ein Ziel, und er wird Runde um Runde darauf zusteuern, bis das Ziel erreicht ist, pausiert wird oder
ehrlicherweise als blockiert gemeldet werden muss.

## Die Werkstatt hinter dem Assistenten

Notizen und Aufgaben sind die Schauseite des Hauses. Dahinter, nur einen Schalter entfernt unter Einstellungen → Erweitert → Toolkit,
liegt eine Werkstatt, die der Assistent betreten darf. Sie besitzt einen Arbeitsbereichsordner Ihrer Wahl — die
Auswahl durchstöbert das gesamte Gerät, keineswegs bloß freigegebenen Speicher — und darin
echte Werkzeuge: Lesen, Schreiben, Suchen, Bearbeiten mit Diff, Momentaufnahmen anfertigen und zurückrollen. Er kann Befehle
mit Zeitlimit und Arbeitsverzeichnis ausführen, langwierige Prozesse als Hintergrundjobs halten und Ihnen die Ausgabe vorlegen.
Er vermag Word-, Excel- und PowerPoint-Dateien zu erstellen und zu bearbeiten — die Formate werden von
Lucent nativ geschrieben, ohne Office-Bibliothek im Unterbau — sowie PDFs zu lesen, in Bilder umzuwandeln, zu teilen und
zusammenzufügen. Er kann mit Git klonen, Diffs erstellen, committen und pushen, GitHub-Issues, Pull-Requests und
CI-Protokolle erstellen und lesen, Webseiten abrufen und durchforsten, SQLite abfragen und Aufträge in Docker, PRoot oder einer schlichten
Shell ausführen — was immer die Maschine eben hergibt.

Er erinnert sich auf dreierlei Weise: an diese Konversation, an dieses Projekt und an Sie. Er führt einen Plan, den Sie verfolgen können,
notiert sich Fähigkeiten, um nicht fortwährend Ihre Gepflogenheiten neu erlernen zu müssen, und kann Unteragenten herbeirufen, wenn eine
Aufgabe schwer genug wiegt, um sie zu rechtfertigen. Unteragenten, Momentaufnahmen, Gerätesteuerung und Bildschirmzugriff teilen sich
eine eigene Seite: Toolkit, dann Assistenten-Fähigkeiten.

Alles Sperrige bleibt dem Installer fern: ein Linux-Userland, Python samt Dokumentbibliotheken,
LibreOffice, Node.js, eine Browser-Engine, OCR- und Medienwerkzeuge werden als Plugins aufgeführt und erst
auf Ihr Geheiß heruntergeladen — von den projekteigenen Servern oder einem schnellen Spiegelserver, je nachdem, wer zuerst antwortet. Die Installation
setzt eine Shell voraus — die im Telefon mitgelieferte Linux-Umgebung von Lucent, winget unter Windows — und Lucent schaut erst hin, bevor es springt: Jedes
Plugin wird geprüft, ehe irgendetwas heruntergeladen wird, sodass eine fehlende Shell, ein schreibgeschützter Download-Ordner
oder ein stummer Spiegelserver benannt werden, bevor Bandbreite vergeudet wird, nicht erst hinterher. Schlägt ein
Download oder eine Installation fehl,
öffnet Lucent einen Dialog, der den Grund nennt, die eigene Ausgabe der Shell zeigt und auflistet, was zu beheben ist. Ein
laufender Download ist nur einen Tastendruck vom Abbruch entfernt, und ein Abbruch hinterlässt keinerlei Spuren.
Berechtigungen
bilden eine eigene Seite — Lesen, Schreiben, Löschen, Befehle, Netzwerk, Git, GitHub, Web-Browsing, der Raum
jenseits des Arbeitsbereichs und das Gerät selbst —, jeweils einstellbar auf Erlauben, Nachfragen oder Blockieren, wobei jeder Werkzeugaufruf
in ein Aktivitätsprotokoll geschrieben wird, das Sie einsehen und leeren können.

Auf dem Telefon greift dieselbe Werkstatt auf das Gerät zu: den Bildschirm ablesen, tippen, schreiben, wischen, Bildschirmfotos
anfertigen, Apps auflisten und öffnen, Benachrichtigungen lesen, die Zwischenablage und die Taschenlampe nutzen und
handfeste Kommandozeilenwerkzeuge in der mitgelieferten Linux-Umgebung ausführen. Zudem versteht er MCP, sodass jeder Server, auf den Sie ihn verweisen,
Teil des Werkzeugkastens wird.

## Notizen, die wissen, was sie einmal waren

Jede bedeutsame Bearbeitung wird per Momentaufnahme festgehalten, sodass Sie jederzeit sehen können, was eine Notiz einst besagte, und
sie wiederherstellen können, wenn sich ein selbstbewusstes Umschreiben als bloßer Optimismus entpuppt. Tippen Sie `[[Shopping list]]`, und es verwandelt sich in einen
antippbaren Link; zielen Sie auf einen noch nicht existierenden Titel, glimmt der Link rot, bis ein Antippen die Notiz gnädig
ins Dasein ruft. Markdown rendert, wenn Sie es wünschen, und bleibt exakt so stehen, wie getippt, wenn Sie es
nicht tun. Checklisten sind Bürger erster Klasse: Formulieren Sie Einträge direkt an Ort und Stelle um, oder öffnen Sie ein großzügiges Pop-out-Editorfenster,
wenn ein kurzer Punkt plötzlich Ambitionen entwickelt. Tags, Farben, Anheften, einzeln verschlüsselte Anhänge,
Rich-Text, eine Skizzenleinwand für Gedanken, an die Worte nicht heranreichen, und ein privater Bereich mit eigenem Schloss —
mit Entwürfen gleich neben dem Papierkorb, damit ein unvollendeter Gedanke niemals ein verlassener wird.

Eine leere Notiz bietet vier Einstiege mit nur einem Tippen — Tagebuch, Meeting, Projektidee, Checkliste — und dann das
eigentliche Kunststück: eigene Vorlagen. Speichern Sie eine, und sie begrüßt Sie künftig auf jeder leeren Seite; langes Drücken
genügt, um jede davon zu bearbeiten oder in den Ruhestand zu schicken, die mitgelieferten eingeschlossen.

## Aufgaben mit Fälligkeiten, die tatsächlich etwas bedeuten

Teilaufgaben, Prioritäten, Wiederholungszeitpläne und Erinnerungen, die einen Neustart überstehen. Fälligkeitsdaten werden
aus ganz gewöhnlicher Sprache verstanden — *nächsten Freitag um 6* wird zu einem echten Zeitstempel mit echtem Weckruf — und
Wiederholungsintervalle sind Bürger erster Klasse statt bloß ein kluger Satz, der irgendwann kapituliert. Das Erledigen
einer Aufgabe hakt ihre gesamte Checkliste gleich mit ab, und vollendete Aufgaben ziehen sich auf einen eigenen Bildschirm zurück.

## Oder das Ganze direkt auf dem Gerät betreiben

Importieren Sie eine `.gguf`-Datei (oder eine `.zip` mit einer solchen darin), und der Assistent antwortet mittels llama.cpp direkt
auf dem Gerät — kein Konto, kein API-Schlüssel, kein Netzwerk, und das Modell wird in dem Augenblick entladen, in dem Sie
die App verlassen. Modelle mit etwa 1–4 GB (Q4) treffen auf dem Telefon den Idealpunkt; ein Desktop darf sich größeren
Ehrgeiz leisten. Werkzeuge sind lokal optional aktivierbar, und die GPU-Beschleunigung ist eine Wahl, die nach einer deutlichen Warnung getroffen wird: die
CPU funktioniert immer, und eine widerspenstige GPU weicht anstandslos auf sie zurück. Bilderkennung ist ebenfalls optional — importieren Sie eine
mmproj-Datei, und der Assistent wird ein Foto studieren und darüber sprechen wie ein leise hellsichtiger
Bibliothekar.

## Vierzehn Sprachen, ohne Umstände gewechselt

English, 简体中文， 繁體中文， Español, العربية, Português, Français, Русский, Deutsch, 日本語， Türkçe, 한국어, Italiano, Nederlands — jeder Bildschirm, jeder Dialog, jedes Datumsformat und jede Vorlage. Wechseln Sie die Sprache,
und die gesamte App folgt noch im selben Frame, denn es gibt nur einen Katalog, und genau den liest die
Benutzeroberfläche: jedes Wort nur einmal hinterlegt, aus Prinzip der Konstruktion und nicht bloß aus Sorgfalt.

## Das Erscheinungsbild: ungeniert aus Glas geschaffen

Ein lebendiger Farbverlauf zieht hinter matten Scheiben dahin, die alles verschwimmen lassen, was unter ihnen vorüberzieht, ohne sich je ganz
zu wiederholen – zu Rechenkosten, denen das Gerät vorab zugestimmt hat. Eine großzügige Auswahl an Paletten über
acht Stilfamilien hinweg, begleitet von einem automatischen Wechsel oder einem zufälligen Begleiter, der gemütlich durch sie hindurchspaziert. Hell, dunkel,
System und eine Galerie monet-getönter Themen — und unter Android 12 oder neuer leiht sich die Palette für den Tag die Farben Ihres
Hintergrundbildes. Widgets bringen dieselbe gläserne Oberfläche auf Ihren Launcher; unter Windows
schlüpft die App ins System-Tray, und eine Seitenleiste löst die untere Tableiste ab — denn ein breiter Monitor
verdient Besseres als ein quergezogenes Telefon-Layout.

## Die Sperre, die zählt, und das Backup, das mit Ihnen geht

Die App-Sperre ist eine handfeste Brute-Force-Richtlinie statt einer höflichen Bitte: eskalierende Wartezeiten, ein
dauerhafter Zähler, der Neustarts übersteht, eine optionale Sicherheitsfrage, eine optionale Selbstzerstörung
und ein Fingerabdruck (Android) oder Windows Hello (Windows), der für die gesamte Wartezeit höflich abwesend
bleibt. Die Datenbank ist im Ruhezustand verschlüsselt, Anhänge einzeln, und Backups sind eine einzige,
passwortschützbare `.lcb`-Datei, die Notizen, Aufgaben, Verläufe, Chats, Anhänge und Einstellungen
über Geräte hinweg transportiert — mit Vorschau, ehe auch nur ein einziges Element angetastet wird, und bereit für automatische Ausführung. Ein Backup,
das niemand lesen kann, ist eher ein Andenken als eine Datensicherung; daher wird dieses hier beim Verlassen und erneut beim
Einlesen geprüft.

## Datenschutz, der strukturell ist, nicht bloß versprochen

Nichts verlässt das Gerät, es sei denn, Sie exportieren es oder geben einem Modell Anlass hinzusehen. Die Integration in das
Teilen-Menü ist standardmäßig deaktiviert; Diagnosedaten sind standardmäßig abgeschaltet und verbleiben lokal; welche
Assistentendienste Sie anbinden, bleibt ganz Ihnen überlassen; Cloud-Spiegelung ist ein Modul, das Sie selbst konfigurieren,
über WebDAV. Die einzige wohlüberlegte Ausnahme von der Verschlüsselung — der Export nach Markdown, Word, PDF oder
Excel — ist eine Datei, die Sie überall sonst öffnen können, was ja auch der Sinn der Sache ist.

## Rust, aber nur dort, wo es sein Brot verdient

Zwei heiße Pfade sind in Rust geschrieben und über JNI angebunden — die PBKDF2- und AES-256-GCM-Routinen
hinter Backups und Anhangsverschlüsselung sowie die Mathematik hinter dem dahintreibenden Hintergrund. Beide
weichen auf identisches Kotlin aus, sollte die native Bibliothek nicht vorhanden sein; so wird die App niemals
zur Geisel eines Compilers.

## Der Bau (ja, vom Telefon aus, im Morgenmantel)

Kein Android Studio, kein lokales SDK, keine Kommandozeile. Auf GitHub pushen, **Actions** öffnen, den gewünschten
Workflow anstoßen und das Ergebnis herunterladen — ein ordentlich signiertes Release oder einen doppelklickbaren Installer. Die
Workflows liegen direkt im Repository; das Rezept befindet sich also in der Schachtel statt im Kopf des Autors, und
der Build-Prozess lässt sich genauso einsehen wie der Code.

## Projektaufbau

```
shared/       DER einzige gemeinsame Quelltextbaum — Geschäftslogik und der Großteil der Oberfläche
              leben hier EINMAL, und sowohl :app als auch :desktop kompilieren ihn. Eine Datei hier
              bearbeiten, um beide Plattformen zusammen zu ändern.
app/          Android-Modul (:app) — Activity-Hülle, Room-Datenbankschicht, JNI-Brücken, Widgets und
              ausschließlich wahrhaft Android-gebundener Code
desktop/      Desktop-Modul (:desktop) — Compose for Desktop-Hülle, die android.* JVM-Shims, der
              JDBC-Datenbankkern, nativer CMake-Build für die Engine-DLL
rust/         Der Rust-Beschleuniger (plattformübergreifend geteilt)
.github/      build.yml (Android APK), build-windows.yml (Windows-Installer), check.yml
```

## Wohin die Reise geht

Lucent ist verdammt viel Anwendung für eine To-do-Liste – und hat sich noch nie dafür entschuldigt.
Notizen mit Gedächtnis, Aufgaben mit Biss, ein Assistent mit Händen und Manieren, eine Verschlüsselung, die
ihre Versprechen einlöst, und eine Oberfläche, die zum Verweilen einlädt. Nutzen Sie sie eine Woche lang und beobachten Sie, was Ihnen
auffällt; nutzen Sie sie einen Monat lang und bemerken Sie, dass Sie aufgehört haben, es zu bemerken.

## Mit Dank an die Riesen, auf deren Schultern wir stehen

Unter dem Glas ist Lucent zu einem beträchtlichen Teil das hervorragende Werk anderer Menschen. Es wäre schlechter
Ton — und in dem einen oder anderen Fall ein handfester Lizenzverstoß —, dies nicht laut auszusprechen. Die vollständigen
Texte und Urheberrechtshinweise finden sich in **[`THIRD-PARTY-NOTICES.md`](./docs/THIRD-PARTY-NOTICES.md)**;
die Kurzfassung lautet, verbunden mit unserer Dankbarkeit, wie folgt:

| Geliehene Brillanz | Verrichtet das Werk als | Unter |
|---|---|---|
| [Kotlin](https://github.com/JetBrains/kotlin) & [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | die Sprache und ihre Geduld mit Nebenläufigkeit | Apache-2.0 |
| [Jetpack Compose & AndroidX](https://developer.android.com/jetpack/androidx) | die Android-Hälfte der Benutzeroberfläche | Apache-2.0 |
| [Compose Multiplatform & Skiko](https://github.com/JetBrains/compose-multiplatform) | die Desktop-Hälfte der Benutzeroberfläche | Apache-2.0 |
| [Material Icons](https://github.com/google/material-design-icons) | die kleinen Bilder, die etwas bedeuten | Apache-2.0 |
| [Haze](https://github.com/chrisbanes/haze) — © Chris Banes | all die modische Unschärfe | Apache-2.0 |
| [OkHttp](https://github.com/square/okhttp) — © Square, Inc. | Zwiesprache mit der Cloud | Apache-2.0 |
| [Apache PDFBox](https://pdfbox.apache.org/) | PDFs auf dem Desktop | Apache-2.0 |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) — © Taro L. Saito et al. | des Desktops Pfad zu SQLite | Apache-2.0 |
| [Skia](https://skia.org/) | die Zeichnung unter beiden Hälften der Benutzeroberfläche | BSD-3-Clause |
| [SQLite](https://www.sqlite.org/) | die Datenbank selbst, die im Stillen die Welt antreibt | Public Domain |
| [SQLCipher](https://www.zetetic.net/sqlcipher/) — © Zetetic LLC | das Schloss an jener Datenbank, unter Android | BSD-style |
| [llama.cpp & GGML](https://github.com/ggml-org/llama.cpp) — © Georgi Gerganov & contributors | ein ganzes Sprachmodell auf Ihrem eigenen Silizium | MIT |
| [Shizuku API](https://github.com/RikkaApps/Shizuku-API) — © Rikka and contributors | die privilegierte Shell, sofern ein Telefon eine bieten kann | MIT |
| [org.json](https://github.com/stleary/JSON-java) — © JSON.org | JSON auf dem Desktop lesen | JSON License |
| [Great Vibes](https://github.com/googlefonts/great-vibes) — © The Great Vibes Pro Project Authors | die Schreibschrift, mit der die Feder zeichnet | OFL-1.1 |

Das Model Context Protocol ist eine offene Spezifikation von Anthropic, lizenziert unter der MIT-Lizenz;
der Client von Lucent ist eine eigenständige Implementierung, und jeder MCP-Server, auf den Sie ihn verweisen, ist Drittanbieter-Software
unter ihren eigenen Bedingungen. Die Lese- und Schreibmodule für Word, Excel und PowerPoint sind eigenständige
Implementierungen von Office Open XML, die auf den plattformeigenen ZIP- und XML-Bibliotheken aufbauen — es wird keinerlei Office-Bibliothek
und keine PDF-Bibliothek von Drittanbietern gebündelt.

### Und die Werkzeuge, die Sie herbeirufen können, ganz nach Ihrem Wort

Lucent liefert weder schwergewichtige Toolchains noch ein Office-Paket mit. Über die Plugin-Seite oder auf Bitte an den
Assistenten kann es ein Linux-Userland, einen Dokumentenkonverter oder ein Medienwerkzeug direkt von den Quellen
in Ihren eigenen Speicher laden. Nichts davon ist Teil des Pakets, nichts davon fest verlinkt, und jedes verbleibt
unter seiner eigenen Lizenz: **[die Hinweisedatei](./docs/THIRD-PARTY-NOTICES.md)** führt die vollständige Liste,
die von PRoot und Ubuntu Base über LibreOffice, Python samt seinen Dokumentbibliotheken,
Node.js und Playwright bis hin zu Git, Pandoc, FFmpeg, ripgrep, 7-Zip, Tesseract, ImageMagick,
qpdf, Poppler und yt-dlp reicht. Lucents Dank gebührt jedem einzelnen dieser Autoren, und die Entscheidung,
eines davon zu installieren — mitsamt der damit einhergehenden Lizenz —, liegt ganz bei Ihnen.

### Und die Notiz-Apps, die wir studiert haben

Sechs ausgereifte Notiz- und Aufgaben-Apps wurden für dieses Projekt studiert, durchleuchtet und zum Tee gebeten — nicht
wegen ihres Codes, den Lucent nicht bündelt, sondern wegen der Dinge, die ihre Autoren als Erste herausgefunden haben.
Ihre Fingerabdrücke finden sich in Lucents Struktur und seinen Umgangsformen statt in seinen Binärdateien, und eine
lautstark beglichene Designschuld ist das Mindeste, was wir tun können:

| Studiert wegen | Was Lucent entliehen hat | Deren Lizenz |
|---|---|---|
| [Quillpad](https://github.com/quillpad/quillpad) | die zweistufige Schublade — Anheften griffbereit, Konfiguration eine Stufe tiefer | GPL-3.0 |
| [Omni-Notes](https://github.com/federicoiosue/Omni-Notes) | die Ehrlichkeit nativer Auswahldialoge und selbsterklärender Aktionen bei langem Drücken | GPL-3.0 |
| [OpenNote-Compose](https://github.com/YangDai2003/OpenNote-Compose) | das dynamische Farb-Prioritätsmodell — Hintergrundbild zuerst, gespeicherte Auswahlen niemals zerstört | GPL-3.0 |
| [AppFlowy](https://github.com/AppFlowy-IO/AppFlowy) | offen auszusprechen, was vor sich geht, auf der Einstellungsseite und andernorts | AGPL-3.0 |
| [Logseq](https://github.com/logseq/logseq) | dass eine Notiz-App sich wie ein Ort anfühlen sollte, nicht wie ein Dashboard | AGPL-3.0 |
| [MarkLeaf](https://github.com/jeiel85/markleaf-android) | dass die editorische Fläche gelassen bleibt, während die Konfiguration geduldig wartet, bis sie an der Reihe ist | Apache-2.0 |

### Und das Geschirr, dem wir nacheiferten

Lucents Assistent folgt dem Werkzeugvokabular und den Arbeitsgewohnheiten des **DeepSeek Harness**
(dsh) sowie von **[DSHA](https://github.com/DSH-APP/DSHA)** — MIT-lizenziert —, jenem Android-Projekt, das
dieses Geschirr ohne Root und ohne Termux auf ein Telefon bringt. Lucent bündelt keines von beiden: die
Werkzeuge, die Schleife, die Bedienleisten und die Ziele sind eigenes Kotlin, geschrieben, um sich genau so zu verhalten, wie jene zwei
es begründet haben. Exit-Codes treffen als Resultate statt als Fehler ein, eine Datei wird erst gelesen, ehe sie geschrieben wird,
ein Ziel zählt seine eigenen Runden, Helfer lassen sich ansprechen und anhalten, und bei allem Diskutablen wird zuvor
nachgefragt. Das Muster stammt von ihnen und wird hier gewürdigt; der Code ist von uns.

Lucents eigene Lizenz regelt alles, was Sie installieren; die oben genannten Projekte werden als Quellen von
Ideen und Arrangements geehrt, nicht als enthaltene Werke. Die Struktur ist unser Werk; der Anstand gebührt ihnen.

Ein besonderes Wort gebührt **SQLCipher**, dessen BSD-artige Lizenz verlangt — nicht unbillig, bedenkt man, dass es
das Tagebuch verschlossen hält —, dass Urheberrecht und Hinweis an einem Ort wiedergegeben werden, an dem ein Nutzer sie
auch tatsächlich finden kann. Das sind sie, in der obigen Hinweisedatei; sollten Sie einen Build von Lucent vertreiben, halten Sie sie
auffindbar. Die einzige Schriftart, die Lucent selbst mitführt, ist Great Vibes, jene geschwungene Schrift der Feder auf dem Startbildschirm,
unmodifiziert gebündelt unter der SIL Open Font License mit unangetastetem reserviertem Namen.

## Die Lizenz und die eine kleine Bitte als Gegenleistung

Lucent wird unter der **MIT-Lizenz** veröffentlicht — siehe [`LICENSE`](./LICENSE). Machen Sie damit so ziemlich alles,
was Ihnen beliebt: nutzen Sie es, verändern Sie es, verweben Sie es mit etwas Kommerziellem, bauen Sie etwas Besseres darauf
auf und schreiben Sie uns niemals ein Dankeschön. Die einzige, vollkommen vernünftige Bedingung ist, dass unser Urheberrechtshinweis
und der Lizenztext in jeder Kopie oder jedem wesentlichen Teil des Codes mitreisen — wenn Sie Lucent also weiterverwenden, behalten Sie die
Datei `LICENSE` (und den darin genannten Namen) bei dem, was Sie ausliefern, und wir sind quitt. Die oben genannten
Drittanbieter-Komponenten stellen ihre eigenen, ähnlich bescheidenen Bitten; respektieren Sie diese im selben Geiste, und man bleibt einander gewogen.

Datenschutzerklärung: **[`privacy.html`](https://lucent-project.github.io/lucentapp/privacy.html)** — was Lucent speichert, was Ihr Gerät verlässt, und jede Kontrolle, die Sie in Händen halten.

## Mitwirken

Sollte Sie der Drang überkommen, Lucent zu verbessern, wären wir leise entzückt. Fehlerberichte,
wohlüberlegte Vorschläge und mit Anstand eingereichte Pull-Requests sind von Herzen willkommen. Wir bitten
lediglich darum, dass alle strikt zivilisiert bleiben, den Tee warm halten und ihren Mitstreitern mit jener
Höflichkeit begegnen, die man in einem ehrbaren Lesesaal erwarten darf.

