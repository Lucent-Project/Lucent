<div align="center">

[🇨🇳 简体中文](./docs/README.zh-CN.md) · [🇯🇵 日本語](./docs/README.ja.md) · [🇰🇷 한국어](./docs/README.ko.md)

</div>

<div align="center">

# 𝓛𝓾𝓬𝓮𝓷𝓽

### Moderno · Minimalista · Discretamente sobrecualificado

**Una aplicación de notas y tareas cuyo asistente puede tocar tus datos de verdad: sellados dentro de una
base de datos cifrada en tu propio dispositivo, versado en catorce idiomas y ensamblado de la primera
a la última línea con solo pulsar un botón en GitHub. Llega tanto en edición de bolsillo como de escritorio —un
APK de Android y un instalador de Windows—, compartiendo un mismo latido y listo para aventurarse aún más lejos.**

![Lucent — Plataforma, Compilación, Interfaz, Asistente, Privacidad, Licencia](badges/badges.svg)

</div>

---

## La idea general

La mayoría de las aplicaciones de notas te extienden la generosa invitación de escoger dos de tres virtudes: la
bella, la privada o la inteligente. Elige tu pareja y confórmate con lo que falta. Lucent declina amablemente
el trato. Todo cuanto escribes queda sellado en una base de datos cifrada que jamás abandona tu dispositivo.
El asistente puede ser un modelo en la nube que pagues de tu bolsillo, un modelo ejecutándose en la mismísima máquina
que tienes delante o absolutamente nada. Y todo el asunto se ensambla sin una sola línea de herramientas
locales: pulsa un botón en GitHub, date una vuelta, y un APK o un instalador de Windows te estará esperando
cuando regreses.

## Una sola aplicación, dondequiera que la pongas

Lucent es **un solo producto con un único corazón compartido**: la regla de la casa exige que cada funcionalidad
habite en cada plataforma a la que llega. Tu bolsillo y tu escritorio hablan con exactitud el mismo idioma:

- **`:app`** — la aplicación de Android (Kotlin y Jetpack Compose, Room sobre SQLCipher, llama.cpp
  a través del NDK). Compilada por `.github/workflows/build.yml` en un APK firmado de lanzamiento.
- **`:desktop`** — la aplicación de escritorio, Windows hoy por hoy (Compose for Desktop, JVM pura, SQLite sobre
  JDBC, llama.cpp compilado como DLL). Compilada por `.github/workflows/build-windows.yml` en un
  instalador `.exe` de doble clic.
- **`:shared`** — el árbol de código único que ambas plataformas compilan: lógica de negocio, datos, la mayor parte
  de la interfaz, un catálogo de traducción en catorce idiomas. Edita una vez; ambas máquinas cambian.

Todo lo que puede compartirse vive en `shared/`; las pocas cosas que no —`SettingsRepository`,
`Daos`/`Db`, `DocumentExport` y las pantallas más grandes— permanecen una vez por plataforma porque sus costuras
son profundas. Las etapas de compilación nativa son opcionales por separado, de modo que un tropiezo en una te deja con una
aplicación funcional en lugar de una sala de espera.

## El asistente con manos, no solo con opiniones

Esta es la característica que define a Lucent. Trae tu propio modelo —las estructuras de petición de OpenAI,
Anthropic y Google se hablan con fluidez, guardando varios perfiles que se alternan con un toque— o ejecuta todo
en el dispositivo. Si no dices nada en absoluto, el asistente simplemente no está; la aplicación de notas no pierde nada.

Lo que hace que valga la pena tenerlo es que puede *actuar*: crear, leer, editar, colorear, fijar, archivar y eliminar
notas; crear, completar, reabrir, programar y priorizar tareas; buscar con un auténtico lenguaje de filtros;
y adjuntar, renombrar o quitar archivos. Antes de alterar cualquier cosa, te muestra con precisión lo que
pretende hacer —en tu propio idioma, en un diálogo que es en sí mismo el editor, con cada campo discutible
editable antes de que se escriba un solo byte. Tu palabra es siempre la última en pronunciarse. Alguien con una
opinión y sin manos es un chat; esto es un mayordomo al que se le ha ordenado llamar primero a la puerta.

Las conversaciones se extienden tanto como desees y viajan contigo, y la última conversación en la que estuviste es
la que se abre. Las respuestas llegan en Markdown formal —encabezados, listas, código—, con el razonamiento
del modelo plegado encima hasta que lo pidas, y una línea por cada llamada a herramienta en la traza,
cada una con el tiempo que tomó. Cuatro paneles se posan sobre la conversación: la lista de tareas pendientes que el asistente
mantiene mientras trabaja, el plan que sigue, los trabajos en segundo plano que ha iniciado y los subagentes
a los que ha recurrido, cada uno de los cuales se puede observar en vivo, detener o enviarle una instrucción adicional. Dale
un objetivo y avanzará con empeño hacia él, ronda tras ronda, hasta que esté cumplido, en pausa o
se reporte con franqueza como bloqueado.

## El taller tras el asistente

Las notas y tareas son la fachada. Detrás de ellas, a un interruptor de distancia en Settings → Advanced → Toolkit,
se encuentra un taller al que el asistente tiene permiso para entrar. Dispone de una carpeta de espacio de trabajo a tu elección —el
selector explora todo el dispositivo, no solo el almacenamiento compartido— y en su
interior, herramientas reales: leer, escribir, buscar, editar con diff, tomar instantáneas y revertir cambios. Puede ejecutar comandos
con límite de tiempo y un directorio de trabajo, conservar los más largos como tareas en segundo plano y mostrarte la salida.
Puede crear y editar archivos de Word, Excel y PowerPoint —los formatos se escriben de forma nativa, por obra de
Lucent, sin ninguna biblioteca ofimática por debajo— y leer PDF, renderizarlos como imágenes, dividirlos y fusionarlos.
Puede clonar, hacer diff, commit y push con Git, abrir y leer issues, pull requests y
registros de CI de GitHub, obtener y leer páginas web, consultar SQLite y ejecutar tareas dentro de Docker, PRoot o un
shell convencional, lo que la máquina pueda ofrecer.

Recuerda de tres formas: esta conversación, este proyecto y a ti. Mantiene un plan que puedes vigilar,
toma nota de habilidades para dejar de reaprender tus convenciones y puede convocar subagentes cuando una
tarea es lo bastante pesada como para justificarlo. Los subagentes, las instantáneas, el control del dispositivo y el acceso a la pantalla comparten
su propia página: Toolkit y luego Assistant abilities.

Todo lo voluminoso se queda fuera del instalador: un espacio de usuario de Linux, Python con sus bibliotecas para documentos,
LibreOffice, Node.js, un motor de navegador, herramientas de OCR y multimedia figuran como complementos, descargados solo
si tú lo pides, desde los propios servidores del proyecto o un espejo veloz, el que responda primero. Instalar uno
requiere un shell —el entorno Linux integrado en Lucent en un teléfono, winget en Windows— y Lucent mira antes de saltar: cada
complemento se inspecciona antes de descargar nada, de modo que la falta de un shell, una carpeta de descargas sin permisos de escritura
o un espejo mudo se señalan antes de consumir ancho de banda, no después. Cuando una
descarga o una instalación falla,
Lucent abre un diálogo que señala el motivo, muestra la propia salida del shell e indica qué corregir. Una
descarga en curso está a una pulsación de cancelarse, y cancelarla no deja rastro alguno.
Los permisos
tienen su propia página —lectura, escritura, borrado, comandos, red, Git, GitHub, navegación, el espacio
más allá del espacio de trabajo y el propio dispositivo—, cada uno configurado para permitir, preguntar o bloquear, y cada llamada a una herramienta
queda asentada en un registro de actividad que puedes consultar y vaciar.

En un teléfono, el mismo taller llega hasta el dispositivo: leer la pantalla, tocar, teclear, deslizar, tomar una
captura de pantalla, listar y abrir aplicaciones, leer notificaciones, usar el portapapeles y la linterna, y ejecutar
auténticas herramientas de línea de comandos dentro del entorno Linux integrado. También habla MCP, por lo que cualquier servidor al que lo apuntes se convierte
en parte del conjunto de herramientas.

## Notas que recuerdan lo que solían ser

Cada edición relevante queda guardada en una instantánea, de modo que siempre puedes ver lo que una nota decía en su día y restaurarla
cuando una reescritura confiada resulte haber sido mero optimismo. Escribe `[[Shopping list]]` y se convierte en un
enlace pulsable; apunta a un título que aún no existe y el enlace brilla en rojo hasta que un toque, con gracia,
da vida a la nota. Markdown se renderiza cuando lo deseas y se queda exactamente como lo escribiste cuando
no. Las listas de verificación son ciudadanas de primera clase: reformula elementos in situ, abre un espacioso editor emergente
cuando un elemento rápido desarrolle ambiciones. Etiquetas, colores, fijado, adjuntos cifrados de forma individual,
texto enriquecido, un lienzo de garabatos para pensamientos que las palabras no alcanzan y un área privada con su propio cerrojo —
con borradores junto a la papelera, para que una idea inacabada jamás sea una idea abandonada.

Una nota en blanco ofrece cuatro puntos de partida con un solo toque —diario, reunión, idea de proyecto, lista de verificación— y luego el
verdadero truco: tus propias plantillas. Guarda una y te recibirá en cada página en blanco venidera; mantén presionado
para editar o retirar cualquiera de ellas, incluidas las integradas.

## Tareas con fechas de vencimiento que de verdad significan algo

Subtareas, prioridades, programas de repetición y recordatorios que sobreviven a un reinicio. Las fechas de vencimiento se interpretan
a partir del lenguaje natural —*el próximo viernes a las 6* se convierte en una marca de tiempo genuina con una alarma genuina— y
las cadencias de repetición son de primer nivel en lugar de una frase ingeniosa que termina dándose por vencida. Completar
una tarea marca toda su lista de verificación con ella, y las tareas completadas se retiran a una pantalla propia.

## O ejecuta todo en el propio dispositivo

Importa un archivo `.gguf` (o un `.zip` con uno en su interior) y el asistente responderá utilizando llama.cpp ejecutándose
directamente en el dispositivo: sin cuenta, sin clave de API, sin red, y con el modelo descargado de la memoria en cuanto
sales de la aplicación. Los modelos Q4 de aproximadamente 1–4 GB dan en el clavo en un teléfono; un ordenador de escritorio puede permitirse mayor
ambición. Las herramientas son opcionales en local y la aceleración por GPU es una elección que se toma tras una clara advertencia: la
CPU siempre funciona, y una GPU que discrepe se retira con elegancia a un segundo plano. La visión también es opcional: importa un
archivo mmproj y el asistente examinará una fotografía y conversará sobre ella como un bibliotecario discretamente clarividente.

## Cuatro idiomas, cambiados sin ceremonia

English, 中文, 日本語, 한국어: cada pantalla, cuadro de diálogo, formato de fecha y plantilla. Cambia el idioma
y toda la aplicación lo sigue en el mismo fotograma, porque hay un único catálogo y es lo que la
interfaz lee: cada palabra guardada una sola vez, por construcción antes que por mero esmero.

## Su apariencia: hecha, sin pudor alguno, de cristal

Un degradado vivo se desliza tras paneles esmerilados que difuminan cuanto pasa por debajo de ellos, sin repetirse jamás
del todo, a un coste que el dispositivo aceptó de antemano. Un generoso abanico de paletas a lo largo de
ocho familias de estilos, con un ciclo automático o un acompañante aleatorio que deambula entre ellas. Claro, oscuro,
del sistema y una galería de temas tintados al estilo Monet —y en Android 12 o posterior, la paleta toma prestada la de tu
fondo de pantalla para el día. Los widgets llevan la misma superficie vidriada a tu pantalla de inicio; en Windows,
la aplicación se desliza a la bandeja del sistema y una barra lateral toma el relevo de la barra de pestañas inferior, porque un monitor ancho
se merece algo mejor que el diseño de un teléfono estirado hacia los lados.

## El cerrojo que cuenta y la copia de seguridad que viaja contigo

El bloqueo de la aplicación es una auténtica política contra fuerza bruta antes que una petición cortés: tiempos de espera crecientes, un
contador persistente que sobrevive a los reinicios, una pregunta de seguridad opcional, una autodestrucción opcional,
y una huella dactilar (Android) o Windows Hello (Windows) que cortésmente se declara fuera de servicio durante todo el
tiempo de espera. La base de datos está cifrada en reposo, los adjuntos de forma individual, y las copias de seguridad son un único
archivo `.lcb` protegible por contraseña que transporta notas, tareas, historial, chats, adjuntos y ajustes
entre dispositivos: con vista previa antes de alterar un solo elemento y preparado para ejecutarse automáticamente. Una copia de seguridad
que nadie puede leer es un recuerdo entrañable antes que una copia de seguridad, de modo que esta se lee al salir y de nuevo al
entrar.

## Privacidad que es estructural, no una simple promesa

Nada abandona el dispositivo hasta que tú lo exportes o le des a un modelo un motivo para mirar. La integración con la
hoja para compartir está desactivada por defecto; los diagnósticos están desactivados por defecto y se conservan en local; los servicios
de asistente que conectes son por entero elección tuya; la sincronización en la nube es un módulo que configuras tú mismo,
mediante WebDAV. La única excepción deliberada al cifrado —exportar a Markdown, Word, PDF o
Excel— es un archivo que puedes abrir en cualquier otra parte, lo cual es precisamente de lo que se trata.

## Rust, pero solo donde se gana el pan

Dos rutas críticas están escritas en Rust y se accede a ellas a través de JNI: las rutinas PBKDF2 y AES-256-GCM
detrás de las copias de seguridad y el cifrado de adjuntos, y las matemáticas tras el fondo en movimiento. Ambas
recurren a código idéntico en Kotlin cuando la biblioteca nativa no está presente, de modo que la aplicación jamás queda de
rehén de un compilador.

## Compilándolo (sí, desde el móvil, en bata)

Sin Android Studio, sin SDK local, sin línea de comandos. Haz push a GitHub, abre **Actions**, ejecuta el workflow
que desees y descarga el resultado: una versión de lanzamiento debidamente firmada o un instalador de doble clic. Los
workflows residen en el repositorio, de modo que la receta está en la caja y no en la cabeza del autor, y
la compilación es tan inspeccionable como el código.

## Project layout

```
shared/       EL árbol de código compartido único: la lógica de negocio y la mayor parte de la interfaz viven aquí UNA SOLA VEZ, y tanto
              :app como :desktop lo compilan. Edita un archivo aquí para cambiar ambas plataformas a la vez.
app/          Módulo de Android (:app): el shell de Activity, la capa de base de datos Room, puentes JNI, widgets
              y únicamente código genuinamente ligado a Android
desktop/      Módulo de escritorio (:desktop): el shell de Compose for Desktop, los shims JVM de android.*,
              el núcleo de base de datos JDBC, compilación nativa con CMake para la DLL del motor
rust/         El acelerador en Rust (compartido entre plataformas)
.github/      build.yml (APK de Android), build-windows.yml (instalador de Windows), check.yml
```

## Hacia dónde va a partir de aquí

Lucent es una barbaridad de aplicación para una lista de tareas pendientes, y jamás se ha disculpado ni una sola vez por
ello. Notas con memoria, tareas con dientes, un asistente con manos y modales, un cifrado que
hace honor a sus promesas y una superficie digna de contemplar. Pruébalo durante una semana y observa qué
notas; pruébalo durante un mes, y nota que dejaste de notarlo.

## Con agradecimiento a los gigantes sobre cuyos hombros nos alzamos

Bajo el cristal, Lucent está compuesto en gran medida por el excelente trabajo de otras personas. Sería de mala
educación —y, en uno o dos casos, una auténtica infracción de licencia— no decirlo bien alto. Los textos
completos y avisos de copyright residen en **[`THIRD-PARTY-NOTICES.md`](./docs/THIRD-PARTY-NOTICES.md)**;
la versión abreviada, acompañada de nuestra gratitud, es la siguiente:

| Brillantez prestada | Cumpliendo la labor de | Bajo |
|---|---|---|
| [Kotlin](https://github.com/JetBrains/kotlin) & [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | el lenguaje, y su paciencia con la concurrencia | Apache-2.0 |
| [Jetpack Compose & AndroidX](https://developer.android.com/jetpack/androidx) | la mitad Android de la interfaz | Apache-2.0 |
| [Compose Multiplatform & Skiko](https://github.com/JetBrains/compose-multiplatform) | la mitad de escritorio de la interfaz | Apache-2.0 |
| [Material Icons](https://github.com/google/material-design-icons) | las pequeñas imágenes que significan cosas | Apache-2.0 |
| [Haze](https://github.com/chrisbanes/haze) — © Chris Banes | todo ese desenfoque tan de moda | Apache-2.0 |
| [OkHttp](https://github.com/square/okhttp) — © Square, Inc. | hablar con la nube | Apache-2.0 |
| [Apache PDFBox](https://pdfbox.apache.org/) | PDFs en el escritorio | Apache-2.0 |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) — © Taro L. Saito et al. | la puerta de entrada del escritorio a SQLite | Apache-2.0 |
| [Skia](https://skia.org/) | el dibujo bajo ambas mitades de la interfaz | BSD-3-Clause |
| [SQLite](https://www.sqlite.org/) | la base de datos en sí, haciendo funcionar el mundo en silencio | Public Domain |
| [SQLCipher](https://www.zetetic.net/sqlcipher/) — © Zetetic LLC | el candado de esa base de datos, en Android | BSD-style |
| [llama.cpp & GGML](https://github.com/ggml-org/llama.cpp) — © Georgi Gerganov & contributors | un modelo de lenguaje completo, en tu propio silicio | MIT |
| [Shizuku API](https://github.com/RikkaApps/Shizuku-API) — © Rikka and contributors | el shell con privilegios, cuando un teléfono puede ofrecerlo | MIT |
| [org.json](https://github.com/stleary/JSON-java) — © JSON.org | leer JSON en el escritorio | JSON License |
| [Great Vibes](https://github.com/googlefonts/great-vibes) — © The Great Vibes Pro Project Authors | la tipografía caligráfica con la que dibuja la pluma | OFL-1.1 |

El Model Context Protocol es una especificación abierta de Anthropic, bajo Licencia MIT;
el cliente de Lucent es una implementación original, y cualquier servidor MCP al que se le dirija es software de terceros
sujeto a sus propios términos. Los lectores y escritores de Word, Excel y PowerPoint son implementaciones originales
de Office Open XML construidas sobre las propias bibliotecas de zip y XML de la plataforma; no se incluye
ninguna biblioteca ofimática ni biblioteca PDF de terceros.

### Y las herramientas que puedes solicitar, bajo tu palabra

Lucent no incluye cadenas de herramientas pesadas ni suites ofimáticas. Desde la página de complementos, o pidiéndoselo al
asistente, puede descargar un espacio de usuario de Linux, un convertidor de documentos o una herramienta multimedia directamente desde el origen
al almacenamiento de tu propiedad. Ninguno forma parte del paquete, ninguno está vinculado en el código y cada cual permanece
bajo su propia licencia: **[el archivo de avisos](./docs/THIRD-PARTY-NOTICES.md)** recoge la lista completa,
que va desde PRoot y Ubuntu Base pasando por LibreOffice, Python con sus bibliotecas
para documentos, Node.js y Playwright, hasta Git, Pandoc, FFmpeg, ripgrep, 7-Zip, Tesseract, ImageMagick,
qpdf, Poppler y yt-dlp. El agradecimiento de Lucent va para cada uno de esos autores, y la decisión de
instalar cualquiera de ellos —junto con la licencia que lo acompaña— es únicamente tuya.

### Y las aplicaciones de notas que estudiamos

Seis aplicaciones maduras de notas y tareas fueron leídas, examinadas y llevadas a tomar el té para este proyecto —no
por su código, que Lucent no incorpora, sino por los hallazgos que sus autores descubrieron primero.
Sus huellas dactilares están en la estructura y en los modales de Lucent, antes que en sus binarios, y una
deuda de diseño saldada a viva voz es lo mínimo que podemos hacer:

| Motivo de estudio | Lo que Lucent tomó prestado | Sus palabras |
|---|---|---|
| [Quillpad](https://github.com/quillpad/quillpad) | el cajón de dos niveles: fija donde puedas alcanzarlo, la configuración un nivel más abajo | GPL-3.0 |
| [Omni-Notes](https://github.com/federicoiosue/Omni-Notes) | la honestidad de los selectores del sistema y acciones de pulsación prolongada que se explican solas | GPL-3.0 |
| [OpenNote-Compose](https://github.com/YangDai2003/OpenNote-Compose) | el modelo de prioridad de color dinámico: fondo de pantalla primero, elecciones guardadas nunca destruidas | GPL-3.0 |
| [AppFlowy](https://github.com/AppFlowy-IO/AppFlowy) | decir abiertamente lo que está sucediendo, en la página de ajustes y en cualquier otra parte | AGPL-3.0 |
| [Logseq](https://github.com/logseq/logseq) | que una aplicación de notas debe sentirse como un lugar, no como un panel de control | AGPL-3.0 |
| [MarkLeaf](https://github.com/jeiel85/markleaf-android) | que la superficie editorial conserve la calma mientras la configuración espera su turno | Apache-2.0 |

### Y el arnés en el que nos inspiramos

El asistente de Lucent sigue el vocabulario de herramientas y las costumbres de trabajo del **DeepSeek Harness**
(dsh), y de **[DSHA](https://github.com/DSH-APP/DSHA)** —con licencia MIT—, el proyecto para Android que
lleva dicho arnés a un teléfono sin necesidad de root y sin Termux. Lucent no incluye ninguno de los dos: las
herramientas, el bucle, los paneles y los objetivos son Kotlin propio, escrito para comportarse de la manera en que ambos
establecieron. Los códigos de salida se reciben como resultados y no como errores, un archivo se lee antes de escribirse,
un objetivo gestiona sus propias rondas, se puede hablar con los ayudantes y detenerlos, y sobre cualquier cosa discutible se pregunta
primero. El patrón es suyo y queda reconocido aquí; el código es nuestro.

La propia licencia de Lucent rige todo lo que instalas; los proyectos anteriores son homenajeados como fuentes de
ideas y organizaciones, no como obras incluidas. La estructura es nuestra; la cortesía, de ellos.

Una mención especial para **SQLCipher**, cuya licencia de tipo BSD exige —no sin razón, habida cuenta de que es
lo que mantiene cerrado tu diario— que su aviso de copyright se reproduzca en algún lugar donde el usuario pueda
encontrarlo de verdad. Y así es, en el archivo de avisos mencionado arriba; si distribuyes una compilación de Lucent, mantenlos
localizables. La única fuente tipográfica que Lucent incorpora por sí mismo es Great Vibes, el estilo caligráfico de la pluma en la pantalla
de bienvenida, incluida sin modificar bajo la SIL Open Font License con su nombre reservado intacto.

## Licencia, y la única pequeña cosa que pide a cambio

Lucent se publica bajo la **Licencia MIT** —consulta [`LICENSE`](./LICENSE). Haz casi cualquier cosa
que gustes con él: úsalo, modifícalo, intégralo en algo comercial, construye algo mejor por encima
y nunca nos escribas para darnos las gracias. La única condición, enteramente razonable, es que nuestro aviso de copyright
y el texto de la licencia nos acompañen en el viaje en cualquier copia o porción sustancial del código;
así que si reutilizas Lucent, mantén el archivo `LICENSE` (y el nombre que figura en él) con lo que distribuyas, y quedamos
en paz. Los componentes de terceros mencionados arriba hacen sus propias peticiones, igualmente modestas; respétalas con
el mismo espíritu y todos seguiremos siendo amigos.

Política de privacidad: **[`privacy.html`](https://lucent-project.github.io/lucentapp/privacy.html)** — lo que Lucent almacena, lo que sale de tu dispositivo y cada control que tienes en tus manos.

## Contribuir

Si te asalta el impulso de mejorar Lucent, nos sentiremos discretamente encantados. Los informes de errores,
las sugerencias meditadas y las pull requests enviadas de buena fe son plenamente bienvenidas. Solo pedimos
que todos permanezcan estrictamente cívicos, mantengan el té caliente y traten a sus compañeros colaboradores con la
cortesía que uno espera en una respetable sala de lectura.
