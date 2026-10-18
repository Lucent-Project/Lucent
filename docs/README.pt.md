<div align="center">

[🇨🇳 简体中文](./docs/README.zh-CN.md) · [🇯🇵 日本語](./docs/README.ja.md) · [🇰🇷 한국어](./docs/README.ko.md)

</div>

<div align="center">

# 𝓛𝓾𝓬𝓮𝓷𝓽

### Moderno · Minimalista · Discretamente Sobrequalificado

**Uma aplicação de notas e tarefas cujo assistente pode verdadeiramente mexer nos seus dados — selados dentro de
uma base de dados encriptada no seu próprio dispositivo, versado em catorze idiomas e montado da primeira
à última linha ao carregar num único botão no GitHub. Chega tanto em edição de bolso como de secretária — um
APK para Android e um instalador para Windows —, partilhando o mesmo bater de coração, pronto para ir ainda mais longe.**

![Lucent — Plataforma, Compilação, Interface, Assistente, Privacidade, Licença](badges/badges.svg)

</div>

---

## A ideia geral

A maioria das aplicações de notas faz o generoso convite de escolher duas de três virtudes: a
bela, a privada ou a inteligente. Escolha o seu par e resigne-se ao que sobra. Lucent recusa educadamente
o acordo. Tudo o que escreve fica selado numa base de dados encriptada que nunca sai do seu dispositivo.
O assistente pode ser um modelo na nuvem pelo qual paga, um modelo a correr na própria máquina
à sua frente ou absolutamente nada. E todo o processo é montado sem uma única linha de ferramentas
locais: carregue num botão no GitHub, dê uma volta, e um APK ou um instalador de Windows estará à sua espera
quando voltar.

## Uma só aplicação, onde quer que a ponha

Lucent é **um único produto com um só coração partilhado**: a regra da casa exige que cada funcionalidade
habite em cada plataforma para onde é lançada. O seu bolso e a sua secretária falam precisamente a mesma língua:

- **`:app`** — a aplicação Android (Kotlin e Jetpack Compose, Room sobre SQLCipher, llama.cpp
  através do NDK). Construída por `.github/workflows/build.yml` num APK de lançamento assinado.
- **`:desktop`** — a aplicação de desktop, Windows hoje (Compose for Desktop, JVM pura, SQLite sobre
  JDBC, llama.cpp compilado como uma DLL). Construída por `.github/workflows/build-windows.yml` num
  instalador `.exe` de duplo clique.
- **`:shared`** — a árvore de código única que ambas as plataformas compilam: lógica de negócio, dados, a maior parte
  da interface, um catálogo de tradução em catorze idiomas. Edite uma vez; ambas as máquinas mudam.

Tudo o que pode ser partilhado vive em `shared/`; as poucas coisas que não podem — `SettingsRepository`,
`Daos`/`Db`, `DocumentExport` e os ecrãs maiores — permanecem uma vez por plataforma porque as suas costuras
são profundas. As etapas de compilação nativa são individualmente opcionais, pelo que um tropeço numa deixa-o com uma
aplicação a funcionar em vez de uma sala de espera.

## O assistente com mãos, e não apenas opiniões

Esta é a funcionalidade que define o Lucent. Traga o seu próprio modelo — os formatos de pedido da OpenAI,
Anthropic e Google são falados fluentemente, mantendo vários perfis que se alternam com um toque — ou corra tudo
no dispositivo. Se não disser nada, o assistente simplesmente não está lá; a aplicação de notas não perde nada.

O que faz valer a pena tê-lo é o facto de poder *agir*: criar, ler, editar, colorir, afixar, arquivar e apagar
notas; criar, concluir, reabrir, agendar e priorizar tarefas; pesquisar com uma verdadeira linguagem de filtros;
e anexar, renomear ou remover ficheiros. Antes de alterar o que quer que seja, mostra-lhe com precisão o que
pretende fazer — na sua própria língua, numa caixa de diálogo que é ela mesma o editor, com cada campo passível de dúvida
sendo editável antes de um único byte ser gravado. A sua palavra é sempre a última a ser dita. Alguém com uma
opinião e sem mãos é uma conversa; isto é um mordomo que foi instruído a bater à porta primeiro.

As conversas duram o tempo que desejar e viajam consigo, e a última conversa em que esteve é
a que se abre. As respostas chegam em Markdown como deve ser — títulos, listas, código —, com o raciocínio
do modelo recolhido acima delas até que o peça, e uma linha por chamada de ferramenta no rasto,
cada uma com o tempo que demorou. Quatro painéis assentam sobre a conversa: a lista de afazeres que o assistente
mantém enquanto trabalha, o plano que está a seguir, os processos em segundo plano que iniciou e os subagentes
que convocou, cada um dos quais pode ser acompanhado em direto, parado ou receber uma instrução adicional. Dê-lhe
um objetivo e ele avançará com afinco em direção a ele, ronda após ronda, até estar concluído, em pausa ou
ser honestamente dado como bloqueado.

## A oficina por trás do assistente

Notas e tarefas são a fachada. Por trás delas, à distância de um interruptor em Settings → Advanced → Toolkit,
jaz uma oficina onde o assistente tem permissão para entrar. Tem uma pasta de área de trabalho à sua escolha — o
seletor navega por todo o dispositivo, não apenas pelo armazenamento partilhado — e, dentro
dela, ferramentas reais: ler, escrever, pesquisar, editar com diff, criar instantâneos e reverter alterações. Consegue executar comandos
com um tempo limite e um diretório de trabalho, manter os mais longos como tarefas em segundo plano e mostrar-lhe a saída.
Pode criar e editar ficheiros do Word, Excel e PowerPoint — os formatos são gravados nativamente, pelo
Lucent, sem qualquer biblioteca de escritório por baixo — e ler PDFs, renderizá-los em imagens, dividi-los e fundi-los.
Pode clonar, fazer diff, commit e push com o Git, abrir e ler issues, pull requests e
registos de CI do GitHub, obter e ler páginas web, consultar SQLite e correr tarefas dentro do Docker, PRoot ou de uma
shell simples, o que a máquina tiver para oferecer.

Lembra-se de três formas: desta conversa, deste projeto e de si. Mantém um plano que pode acompanhar,
regista competências para que deixe de reaprender as suas convenções, e pode convocar subagentes quando uma
tarefa é pesada o suficiente para o justificar. Subagentes, instantâneos, controlo do dispositivo e acesso ao ecrã partilham
uma página própria: Toolkit, e depois Assistant abilities.

Tudo o que é volumoso fica fora do instalador: um espaço de utilizador Linux, Python com as suas bibliotecas de documentos,
LibreOffice, Node.js, um motor de navegação, ferramentas de OCR e multimédia surgem listados como suplementos, descarregados apenas
se pedir, a partir dos servidores do próprio projeto ou de um espelho rápido, o que responder primeiro. Instalar um
deles requer uma shell — o ambiente Linux integrado no Lucent num telemóvel, winget no Windows — e o Lucent olha antes de saltar: cada
suplemento é inspecionado antes de se descarregar qualquer coisa, de modo que uma shell ausente, uma pasta de transferências sem permissão de escrita
ou um espelho silencioso sejam identificados antes de gastarem largura de banda, e não depois. Quando uma
transferência ou instalação falha,
o Lucent abre uma caixa de diálogo que aponta o motivo, mostra o próprio resultado da shell e lista o que corrigir. Uma
transferência em curso está a um toque de botão de ser parada, e pará-la não deixa nada para trás.
As permissões
têm uma página própria — leitura, escrita, eliminação, comandos, rede, Git, GitHub, navegação, o espaço
além da área de trabalho e o próprio dispositivo —, cada uma configurada para permitir, perguntar ou bloquear, e cada chamada de ferramenta
é registada num histórico de atividade que pode ler e limpar.

Num telemóvel, a mesma oficina alcança o dispositivo: ler o ecrã, tocar, digitar, deslizar, tirar uma
captura de ecrã, listar e abrir aplicações, ler notificações, usar a área de transferência e a lanterna, e executar
ferramentas de linha de comandos a sério dentro do ambiente Linux integrado. Também fala MCP, pelo que qualquer servidor para onde o aponte se torna
parte da caixa de ferramentas.

## Notas que se lembram do que costumavam ser

Cada edição com significado é registada num instantâneo, para que possa sempre ver o que uma nota dizia outrora e restaurá-la
quando uma reescrita confiante se revelar mero otimismo. Escreva `[[Shopping list]]` e torna-se numa
ligação clicável; aponte para um título que ainda não existe e a ligação brilha a vermelho até que um toque traga, graciosamente,
a nota à existência. O Markdown renderiza-se quando quiser e mantém-se exatamente como foi digitado quando
não quiser. As listas de verificação são cidadãs de primeira classe: reformule itens no próprio local, abra um espaçoso editor destacado
quando um apontamento rápido ganhar ambições. Etiquetas, cores, afixação, anexos individualmente encriptados,
texto formatado, uma tela de rabiscos para pensamentos onde as palavras não chegam, e uma área privada com o seu próprio cadeado —
com rascunhos ao lado do lixo, para que um pensamento inacabado nunca seja abandonado.

Uma nota em branco oferece quatro pontos de partida a um toque — diário, reunião, ideia de projeto, lista de verificação — e depois o
verdadeiro truque: modelos próprios. Guarde um e ele dar-lhe-á as boas-vindas em cada página em branco futura; prima demoradamente
para editar ou aposentar qualquer um deles, incluindo os de fábrica.

## Tarefas com datas de vencimento que realmente significam algo

Subtarefas, prioridades, agendamentos de repetição e lembretes que sobrevivem a uma reinicialização. As datas de vencimento são interpretadas
a partir da linguagem quotidiana — *próxima sexta às 6* torna-se numa marca temporal autêntica com um alarme genuíno — e
as cadências de repetição são de primeira classe, em vez de uma frase engenhosa que acaba por se render. Concluir
uma tarefa assinala toda a sua lista de verificação com ela, e as tarefas concluídas retiram-se para um ecrã próprio.

## Ou corra tudo no próprio dispositivo

Importe um ficheiro `.gguf` (ou um `.zip` com um lá dentro) e o assistente responde usando llama.cpp a correr
diretamente no dispositivo — sem conta, sem chave de API, sem rede, e com o modelo descarregado da memória no instante em que
sai da aplicação. Modelos Q4 de sensivelmente 1–4 GB acertam em cheio num telemóvel; um computador de secretária pode dar-se a maiores
ambições. As ferramentas são de adesão voluntária a nível local e a aceleração por GPU é uma escolha feita após um aviso claro: a
CPU funciona sempre, e uma GPU que discorde recua graciosamente. A visão também é opcional — importe um
ficheiro mmproj e o assistente estudará uma fotografia e debaterá sobre ela como um bibliotecário suavemente clarividente.

## Quatro idiomas, alternados sem cerimónia

English, 中文, 日本語, 한국어 — cada ecrã, diálogo, formato de data e modelo. Mude de idioma
e a aplicação inteira acompanha-o no mesmo fotograma, porque existe um único catálogo e é isso que a
interface lê: cada palavra guardada uma só vez, por conceção e não por mero cuidado.

## O seu aspeto: feito, descaradamente, de vidro

Um gradiente vivo flutua atrás de painéis foscos que desfocam tudo o que passa por baixo deles, nunca se repetindo
por completo, a um custo previamente acordado pelo dispositivo. Uma generosa variedade de paletas ao longo de
oito famílias de estilos, com um ciclo automático ou um companheiro aleatório que passeia por entre elas. Claro, escuro,
do sistema e uma galeria de temas em tons de Monet — e no Android 12 ou posterior a paleta pede emprestada a paleta do seu
papel de parede para o dia. Os widgets levam a mesma superfície envidraçada para o seu ecrã inicial; no Windows
a aplicação recolhe à bandeja do sistema e uma barra lateral substitui a barra de separadores inferior, porque um monitor largo
merece melhor do que o esquema de um telemóvel esticado de lado.

## O fecho que se faz respeitar e a cópia de segurança que vai consigo

O bloqueio da aplicação é uma verdadeira política contra força bruta e não um pedido educado: períodos de arrefecimento crescentes, um
contador persistente que resiste a reinicializações, uma pergunta de segurança opcional, uma autodestruição opcional,
e uma impressão digital (Android) ou Windows Hello (Windows) que fica educadamente fora de serviço durante todo o
tempo de espera. A base de dados é encriptada em repouso, os anexos individualmente, e as cópias de segurança consistem num único
ficheiro `.lcb` protegível por palavra-passe que transporta notas, tarefas, histórico, conversas, anexos e definições
entre dispositivos — pré-visualizado antes de um único item ser alterado, e armado para correr automaticamente. Uma cópia de segurança
que ninguém consegue ler é uma recordação e não uma cópia de segurança, pelo que esta é lida à saída e novamente à
entrada.

## Privacidade que é estrutural, não apenas prometida

Nada sai do dispositivo até que o exporte ou dê a um modelo um motivo para espreitar. A integração com o
menu de partilha está desativada por omissão; os diagnósticos estão desativados por omissão e mantidos localmente; os serviços
de assistente a que se liga dependem inteiramente da sua escolha; o espelhamento na nuvem é um módulo configurado por si,
via WebDAV. A única exceção deliberada à encriptação — exportar para Markdown, Word, PDF ou
Excel — é um ficheiro que pode abrir em qualquer outro lugar, o que é, no fundo, todo o objetivo.

## Rust, mas apenas onde faz por merecer o lugar

Dois caminhos críticos estão escritos em Rust e são acedidos através de JNI — as rotinas de PBKDF2 e AES-256-GCM
por trás das cópias de segurança e da encriptação de anexos, e a matemática por trás do fundo em movimento. Ambas
recorrem a Kotlin idêntico quando a biblioteca nativa não está presente, para que a aplicação nunca fique feita
refém de um compilador.

## Como compilar (sim, a partir de um telemóvel, de roupão)

Sem Android Studio, sem SDK local, sem linha de comandos. Faça push para o GitHub, abra **Actions**, execute o fluxo de trabalho
que quiser e transfira o resultado — uma versão de lançamento devidamente assinada ou um instalador de duplo clique. Os
fluxos de trabalho vivem no repositório, pelo que a receita está na caixa e não na cabeça do autor, e
a compilação é tão inspecionável quanto o código.

## Project layout

```
shared/       A árvore de código partilhada ÚNICA — a lógica de negócio e a maior parte da interface vivem aqui UMA SÓ VEZ, e tanto
              :app como :desktop compilam-na. Edite um ficheiro aqui para alterar ambas as plataformas em conjunto.
app/          Módulo Android (:app) — casca de Activity, camada de base de dados Room, pontes JNI, widgets,
              e apenas outro código genuinamente vinculado ao Android
desktop/      Módulo de desktop (:desktop) — casca de Compose for Desktop, os shims JVM de android.*,
              o núcleo de base de dados JDBC, compilação nativa com CMake para a DLL do motor
rust/         O acelerador em Rust (partilhado entre plataformas)
.github/      build.yml (APK Android), build-windows.yml (instalador Windows), check.yml
```

## Para onde vai a seguir

O Lucent é demasiada aplicação para uma lista de afazeres, e nunca pediu desculpa por
isso nem por uma vez. Notas com memória, tarefas com garras, um assistente com mãos e boas maneiras, encriptação que
honra as suas promessas e uma superfície digna de contemplação. Use-o durante uma semana e observe o que
nota; use-o durante um mês, e repare que deixou de reparar.

## Com agradecimento aos gigantes em cujos ombros nos apoiamos

Por baixo do vidro, o Lucent é, em grande parte, o excelente trabalho de outras pessoas. Seria falta
de educação — e, num caso ou outro, uma violação direta de licença — não dizê-lo em voz alta. Os textos
integrais e os avisos de direitos de autor vivem em **[`THIRD-PARTY-NOTICES.md`](./docs/THIRD-PARTY-NOTICES.md)**;
a versão resumida, com a nossa gratidão, é esta:

| Brilhantismo emprestado | A desempenhar a função de | Sob |
|---|---|---|
| [Kotlin](https://github.com/JetBrains/kotlin) & [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | a linguagem, e a sua paciência com a simultaneidade | Apache-2.0 |
| [Jetpack Compose & AndroidX](https://developer.android.com/jetpack/androidx) | a metade Android da interface | Apache-2.0 |
| [Compose Multiplatform & Skiko](https://github.com/JetBrains/compose-multiplatform) | a metade desktop da interface | Apache-2.0 |
| [Material Icons](https://github.com/google/material-design-icons) | as pequenas imagens que têm significado | Apache-2.0 |
| [Haze](https://github.com/chrisbanes/haze) — © Chris Banes | todo aquele desfoque na moda | Apache-2.0 |
| [OkHttp](https://github.com/square/okhttp) — © Square, Inc. | falar com a nuvem | Apache-2.0 |
| [Apache PDFBox](https://pdfbox.apache.org/) | PDFs no desktop | Apache-2.0 |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) — © Taro L. Saito et al. | o caminho do desktop para o SQLite | Apache-2.0 |
| [Skia](https://skia.org/) | o desenho por baixo de ambas as metades da interface | BSD-3-Clause |
| [SQLite](https://www.sqlite.org/) | a própria base de dados, a mover o mundo em silêncio | Public Domain |
| [SQLCipher](https://www.zetetic.net/sqlcipher/) — © Zetetic LLC | o cadeado dessa base de dados, no Android | BSD-style |
| [llama.cpp & GGML](https://github.com/ggml-org/llama.cpp) — © Georgi Gerganov & contributors | um modelo de linguagem inteiro, no seu próprio silício | MIT |
| [Shizuku API](https://github.com/RikkaApps/Shizuku-API) — © Rikka and contributors | a shell privilegiada, quando um telemóvel a consegue disponibilizar | MIT |
| [org.json](https://github.com/stleary/JSON-java) — © JSON.org | ler JSON no desktop | JSON License |
| [Great Vibes](https://github.com/googlefonts/great-vibes) — © The Great Vibes Pro Project Authors | a caligrafia com que a caneta desenha | OFL-1.1 |

O Model Context Protocol é uma especificação aberta da Anthropic, licenciada sob a Licença MIT;
o cliente do Lucent é uma implementação original, e qualquer servidor MCP para o qual seja apontado é software de terceiros
regido pelos seus próprios termos. Os leitores e gravadores de Word, Excel e PowerPoint são implementações originais
do Office Open XML concebidas sobre as bibliotecas de zip e XML nativas da própria plataforma — nenhuma biblioteca
de escritório, nem qualquer biblioteca de PDF de terceiros, vem embutida.

### E as ferramentas que pode obter, à sua ordem

O Lucent não traz toolchains pesadas nem nenhuma suite de escritório. A partir da página de suplementos, ou pedindo ao
assistente, ele consegue descarregar um espaço de utilizador Linux, um conversor de documentos ou uma ferramenta multimédia diretamente do projeto original
para o armazenamento que lhe pertence. Nenhum deles faz parte do pacote, nenhum é associado por ligação direta, e cada um permanece
sob a sua própria licença: **[o ficheiro de avisos](./docs/THIRD-PARTY-NOTICES.md)** traz a lista completa,
que vai desde o PRoot e o Ubuntu Base, passando pelo LibreOffice, Python com as suas bibliotecas
de documentos, Node.js e Playwright, até Git, Pandoc, FFmpeg, ripgrep, 7-Zip, Tesseract, ImageMagick,
qpdf, Poppler e yt-dlp. Os agradecimentos do Lucent vão para cada um desses autores, e a decisão de
instalar qualquer um deles — juntamente com a licença que o acompanha — é sua.

### E as aplicações de notas que estudámos

Seis aplicações maduras de notas e tarefas foram lidas, cutucadas e convidadas para um chá para este projeto — não
pelo seu código, que o Lucent não inclui, mas pelas coisas que os seus autores descobriram primeiro.
As suas impressões digitais estão na estrutura e nas maneiras do Lucent, e não nos seus binários, e uma
dívida de design saldada a plenos pulmões é o mínimo que podemos fazer:

| Motivo de estudo | O que o Lucent pediu emprestado | As suas palavras |
|---|---|---|
| [Quillpad](https://github.com/quillpad/quillpad) | o menu lateral de dois níveis — afixar onde estiver ao alcance, configuração um nível abaixo | GPL-3.0 |
| [Omni-Notes](https://github.com/federicoiosue/Omni-Notes) | a honestidade dos seletores de sistema e ações de toque longo que se explicam por si | GPL-3.0 |
| [OpenNote-Compose](https://github.com/YangDai2003/OpenNote-Compose) | o modelo de prioridade de cor dinâmica — papel de parede primeiro, escolhas guardadas nunca destruídas | GPL-3.0 |
| [AppFlowy](https://github.com/AppFlowy-IO/AppFlowy) | dizer abertamente o que está a acontecer, na página de definições e noutros locais | AGPL-3.0 |
| [Logseq](https://github.com/logseq/logseq) | que uma aplicação de notas deve parecer um lugar, e não um painel de instrumentos | AGPL-3.0 |
| [MarkLeaf](https://github.com/jeiel85/markleaf-android) | que a superfície de edição permaneça calma enquanto a configuração espera pela sua vez | Apache-2.0 |

### E o arnês em que nos inspirámos

O assistente do Lucent segue o vocabulário de ferramentas e os hábitos de trabalho do **DeepSeek Harness**
(dsh), e do **[DSHA](https://github.com/DSH-APP/DSHA)** — sob licença MIT —, o projeto Android que
coloca esse arnês num telemóvel sem root e sem Termux. O Lucent não inclui nenhum dos dois: as
ferramentas, o ciclo, os painéis e as metas são código Kotlin próprio, escrito para se comportar da forma que esses dois
estabeleceram. Os códigos de saída chegam como resultados em vez de erros, um ficheiro é lido antes de ser escrito,
uma meta guarda as suas próprias rondas, pode-se conversar com os ajudantes e pará-los, e qualquer coisa passível de dúvida é questionada
primeiro. O padrão é deles e é aqui reconhecido; o código é nosso.

A licença do próprio Lucent governa tudo o que instala; os projetos acima são homenageados como fontes de
ideias e disposições, não como obras incluídas. A estrutura é nossa; a cortesia é deles.

Uma palavra especial para o **SQLCipher**, cuja licença de estilo BSD pede — não sem razão, dado ser
aquilo que mantém o seu diário fechado — que os seus direitos de autor e aviso sejam reproduzidos onde um utilizador os possa
realmente encontrar. E assim estão, no ficheiro de avisos acima; se distribuir uma versão do Lucent, mantenha-os
acessíveis. A única fonte que o Lucent traz consigo é a Great Vibes, a caligrafia da caneta no ecrã
de abertura, incluída sem alterações sob a SIL Open Font License com o seu nome reservado intacto.

## Licença, e a única pequena coisa que pede em troca

O Lucent é lançado sob a **Licença MIT** — consulte [`LICENSE`](./LICENSE). Faça praticamente tudo o
que lhe apetecer com ele: use-o, altere-o, integre-o em algo comercial, construa algo melhor por
cima e nunca nos escreva para agradecer. A única condição, inteiramente razoável, é que o nosso aviso de direitos de autor
e o texto da licença vão à boleia em qualquer cópia ou parte substancial do código —
portanto, se reutilizar o Lucent, mantenha o ficheiro `LICENSE` (e o nome que nele consta) com aquilo que distribuir, e ficamos
quites. Os componentes de terceiros acima fazem os seus próprios pedidos, igualmente modestos; respeite-os
com o mesmo espírito e todos continuaremos amigos.

Política de privacidade: **[`privacy.html`](https://lucent-project.github.io/lucentapp/privacy.html)** — o que o Lucent guarda, o que sai do seu dispositivo e todos os controlos que tem nas mãos.

## Contribuir

Caso seja tomado pela vontade súbita de melhorar o Lucent, ficaremos discretamente encantados. Relatórios de erros,
sugestões ponderadas e pull requests submetidos de bom grado são inteiramente bem-vindos. Pedimos
apenas que todos se mantenham estritamente civilizados, conservem o chá quente e tratem os demais colaboradores com a
cortesia que se espera numa respeitável sala de leitura.
