<div align="center">

[🇬🇧 English](../README.md) · [🇨🇳 简体中文](./README.zh-CN.md) · [🇭🇰 繁體中文](./README.zh-Hant.md) · [🇪🇸 Español](./README.es.md)<br>
[🇸🇦 العربية](./README.ar.md) · [🇵🇹 Português](./README.pt.md) · [🇫🇷 Français](./README.fr.md) · [🇷🇺 Русский](./README.ru.md)<br>
[🇩🇪 Deutsch](./README.de.md) · [🇯🇵 日本語](./README.ja.md) · [🇰🇷 한국어](./README.ko.md) · [🇮🇹 Italiano](./README.it.md)<br>
[🇳🇱 Nederlands](./README.nl.md)

</div>

<div align="center">

# 𝓛𝓾𝓬𝓮𝓷𝓽

### Modern · Minimalist · Sessizce Fazlasıyla Yetkin

**Asistanı verilerinize sahiden dokunabilen bir not ve görev uygulaması — kendi cihazınızda şifrelenmiş bir veritabanına mühürlenmiş, on dört dil konuşabilen ve GitHub'da tek bir düğmeye basılarak ilk satırından sonuncusuna kadar derlenen. Tek bir kalp atışını paylaşan, daha da uzaklara uzanmaya hazır, hem cep hem de masaüstü sürümleriyle (bir Android APK'sı ve bir Windows yükleyicisi) karşınızda.**

![Lucent — Platform, Derleme, Arayüz, Asistan, Gizlilik, Lisans](../badges/badges.svg)

</div>

---

## İşin özü

Çoğu not alma uygulaması, üç erdemden herhangi ikisini seçmeniz için cömert bir davette bulunur: güzel olan, gizli olan veya zeki olan. İkili tercihinizi yapın ve geri kalanına boyun eğin. Lucent bu pazarlığı kibarca reddediyor. Yazdığınız her şey, cihazınızı asla terk etmeyen şifreli bir veritabanına mühürlenir. Asistanınız parasını ödediğiniz bir bulut modeli, tam karşınızdaki makinede çalışan yerel bir model veya düpedüz hiçbir şey olabilir. Üstelik tüm bu düzenek, yerel ortamda tek bir satır araç zinciri kurulmadan derlenir: GitHub'da bir düğmeye basın, biraz uzaklaşın; döndüğünüzde bir APK veya Windows yükleyicisi sizi bekliyor olacak.

## Nereye koyarsanız koyun, tek bir uygulama

Lucent, **ortak tek bir kalbe sahip tek bir ürün**: kurum içi kural, her özelliğin sunulduğu her platformda yer almasını şart koşar. Cebinizle çalışma masanız tamı tamına aynı dili konuşur:

- **`:app`** — Android uygulaması (Kotlin ve Jetpack Compose, SQLCipher üzerinde Room, NDK aracılığıyla llama.cpp). `.github/workflows/build.yml` tarafından imzalı bir sürüm APK'sı olarak derlenir.
- **`:desktop`** — masaüstü uygulaması, bugün için Windows (Compose for Desktop, saf JVM, JDBC üzerinde SQLite, DLL olarak derlenmiş llama.cpp). `.github/workflows/build-windows.yml` tarafından çift tıklamalı bir `.exe` yükleyicisi olarak derlenir.
- **`:shared`** — her iki platformun da derlediği ortak kaynak ağacı: iş mantığı, veriler, arayüzün büyük bölümü, on dört dil içeren tek bir çeviri kataloğu. Bir kez düzenleyin, her iki makine birden değişsin.

Paylaşılabilecek her şey `shared/` altında yaşar; paylaşılamayan az sayıdaki parça — `SettingsRepository`, `Daos`/`Db`, `DocumentExport` ve en kapsamlı ekranlar — dikiş izleri derine indiği için platform başına birer kez yazılmış halde kalır. Yerel derleme aşamalarının her biri isteğe bağlıdır; dolayısıyla birindeki tökezleme sizi bir bekleme odasında değil, çalışan bir uygulamayla baş başa bırakır.

## Yalnızca fikirleri değil, bizzat elleri olan bir asistan

Lucent'ı asıl tanımlayan özellik işte bu. Kendi modelinizi getirin — OpenAI, Anthropic ve Google istek biçimleri akıcı bir şekilde konuşulur, birden fazla profil tek dokunuşla saklanıp değiştirilebilir — ya da her şeyi doğrudan cihaz üzerinde çalıştırın. Hiçbir şey söylemezseniz asistan ortalıkta görünmez; not uygulaması ise hiçbir şey kaybetmez.

Onu değerli kılan şey *eyleme geçebilmesidir*: notlar oluşturabilir, okuyabilir, düzenleyebilir, renklendirebilir, sabitleyebilir, arşivleyebilir ve silebilir; görevler yaratabilir, tamamlayabilir, yeniden açabilir, zamanlayabilir ve önceliklendirebilir; gerçek bir filtreleme diliyle arama yapabilir; dosyaları ekleyebilir, yeniden adlandırabilir veya kaldırabilir. Bir şeyi değiştirmeden önce, ne yapmayı planladığını size harfi harfine gösterir — kendi dilinizde, bizzat kendisi bir düzenleyici olan bir iletişim kutusunda, tartışmaya açık her alan tek bir bayt dahi yazılmadan önce düzenlenebilecek şekilde. Son söz her zaman sizindir. Fikri olup eli kolu olmayan şey sıradan bir sohbettir; bu ise önce kapıyı çalması tembihlenmiş bir uşaktır.

Sohbetler dilediğiniz kadar sürer ve sizinle birlikte yolculuk eder; en son hangi sohbette kaldıysanız uygulama açıldığında karşınıza o gelir. Yanıtlar usulüne uygun Markdown biçiminde gelir — başlıklar, listeler, kodlar — ve siz talep edene dek modelin akıl yürütme süreci bunların üzerinde katlanmış halde durur; işlem dökümündeki her araç çağrısı ise harcanan süreyle birlikte tek bir satırda gösterilir. Sohbetin üzerinde dört panel yer alır: asistanın çalışırken tuttuğu yapılacaklar listesi, takip ettiği plan, başlattığı arka plan işleri ve yardıma çağırdığı alt ajanlar; bunların her biri canlı olarak izlenebilir, durdurulabilir veya kendilerine ek talimatlar gönderilebilir. Ona bir hedef verin; hedef tamamlanana, duraklatılana ya da dürüstçe engellendiği bildirilene kadar tur üstüne tur atarak hedefin peşinden koşacaktır.

## Asistanın arkasındaki atölye

Notlar ve görevler işin vitrinidir. Arkalarında, Settings → Advanced → Toolkit menüsündeki bir anahtarın ardında, asistanın girmesine izin verilen bir atölye uzanır. Sizin seçeceğiniz bir çalışma alanı klasörüne sahiptir — dosya seçici yalnızca paylaşılan depolamayı değil, tüm cihazı tarar — ve içinde gerçek araçlar barındırır: okuma, yazma, arama, diff ile düzenleme, anlık görüntü alma ve geri alma. Komutları bir zaman aşımı süresi ve bir çalışma diziniyle çalıştırabilir, uzun sürenleri arka plan işi olarak saklayabilir ve size çıktıyı gösterebilir. Word, Excel ve PowerPoint dosyaları oluşturabilir ve düzenleyebilir — bu biçimler, altında hiçbir ofis kütüphanesi olmadan doğrudan Lucent tarafından yerel olarak yazılır — ve PDF'leri okuyabilir, resimlere dönüştürebilir, bölebilir ve birleştirebilir. Git ile klonlayabilir, diff alabilir, commit ve push yapabilir, GitHub sorunları, çekme istekleri ve CI günlükleri açıp okuyabilir, web sayfalarını çekip okuyabilir, SQLite sorgulayabilir ve makinenin sunduğu imkânlara bağlı olarak bir işi Docker, PRoot veya sade bir kabuk içinde çalıştırabilir.

Üç yönüyle hatırlar: bu sohbet, bu proje ve siz. İzleyebileceğiniz bir plan tutar, alışkanlıklarınızı tekrar tekrar öğrenmek zorunda kalmamak için becerileri kaydeder ve bir görev onları gerektirecek kadar ağır olduğunda alt ajanları çağırabilir. Alt ajanlar, anlık görüntüler, cihaz denetimi ve ekran erişimi kendilerine ait tek bir sayfayı paylaşır: Toolkit, ardından Assistant abilities.

Hacimli olan hiçbir şey yükleyicinin içine dahil edilmez: bir Linux kullanıcı alanı, belge kütüphaneleriyle birlikte Python, LibreOffice, Node.js, bir tarayıcı motoru, OCR ve medya araçları eklenti olarak listelenir ve yalnızca siz isterseniz, projenin kendi sunucularından veya hızlı bir yansıdan (hangisi önce yanıt verirse) indirilir. Bunlardan birini yüklemek bir kabuk gerektirir — telefonda Lucent içine gömülü Linux ortamı, Windows'ta winget — ve Lucent adımını atmadan önce etrafına bakar: herhangi bir şey indirilmeden önce her eklenti denetlenir; böylece eksik bir kabuk, yazılamayan bir indirme klasörü veya sessiz kalan bir yansı, bant genişliğinize mal olmadan önce tespit edilir. Bir indirme veya yükleme başarısız olduğunda Lucent, nedenini belirten, kabuğun kendi çıktısını gösteren ve nelerin düzeltileceğini listeleyen bir iletişim kutusu açar. Devam eden bir indirmeyi durdurmak tek bir düğmeye basmak kadar basittir ve durdurulduğunda arkasında hiçbir iz bırakmaz. İzinler kendilerine ait ayrı bir sayfadadır — okuma, yazma, silme, komutlar, ağ, Git, GitHub, gezinme, çalışma alanının ötesindeki alan ve cihazın kendisi — her biri izin ver, sor veya engelle olarak ayarlanabilir ve her araç çağrısı okuyup temizleyebileceğiniz bir etkinlik günlüğüne yazılır.

Bir telefonda aynı atölye doğrudan cihaza uzanır: ekranı okuma, dokunma, yazma, kaydırma, ekran görüntüsü alma, uygulamaları listeleme ve açma, bildirimleri okuma, panoyu ve el fenerini kullanma ve gömülü Linux ortamında gerçek komut satırı araçlarını çalıştırma. Ayrıca MCP dilini de konuşur, böylece işaret ettiğiniz her sunucu araç takımının bir parçası haline gelir.

## Eskiden ne olduklarını hatırlayan notlar

Her anlamlı düzenlemenin anlık görüntüsü alınır; böylece bir notun eskiden ne söylediğini her zaman görebilir ve kendinizden emin bir şekilde yaptığınız baştan yazımın saf bir iyimserlikten ibaret olduğu anlaşıldığında onu geri yükleyebilirsiniz. `[[Shopping list]]` yazdığınızda dokunulabilir bir bağlantıya dönüşür; henüz var olmayan bir başlığı işaret ettiğinizde ise bağlantı, tek bir dokunuşla notu lütfedip var edene kadar kırmızı renkte parlar. Markdown istediğinizde işlenir, istemediğinizde ise tam olarak yazıldığı gibi kalır. Kontrol listeleri birinci sınıf vatandaştır: maddeleri yerinde yeniden ifade edin, alelacele yazılmış bir madde büyük hedeflere yelken açtığında ferah bir açılır düzenleyiciye geçin. Etiketler, renkler, sabitleme, ayrı ayrı şifrelenmiş ekler, zengin metin, kelimelerin yetmediği düşünceler için bir karalama tuvali ve kendi kilidine sahip özel bir alan — üstelik taslaklar çöp kutusunun hemen yanı başındadır, böylece tamamlanmamış hiçbir düşünce terk edilmiş sayılmaz.

Boş bir not, tek dokunuşla başlayan dört seçenek sunar — günlük, toplantı, proje fikri, kontrol listesi — ve ardından asıl numara: kendi şablonlarınız. Bir tanesini kaydedin, gelecekteki her boş sayfada sizi karşılasın; yerleşikler de dahil olmak üzere herhangi birini düzenlemek veya emekliye ayırmak için üzerine uzun basın.

## Gerçekten bir anlam ifade eden son teslim tarihlerine sahip görevler

Yeniden başlatmaya meydan okuyan alt görevler, öncelikler, yineleme çizelgeleri ve anımsatıcılar. Son teslim tarihleri günlük dilden ayrıştırılır — *next Friday at 6* ifadesi gerçek bir alarmla donatılmış gerçek bir zaman damgasına dönüşür — ve yineleme aralıkları, eninde sonunda havlu atan süslü bir cümle olmak yerine birinci sınıf bir özellik olarak çalışır. Bir görevi tamamlamak, ona bağlı kontrol listesinin tamamına da tik atar ve tamamlanan görevler kendilerine ayrılmış bir ekranda emekliliğin tadını çıkarır.

## Veya her şeyi doğrudan cihazın üzerinde çalıştırın

Bir `.gguf` dosyasını (veya içinde bu dosyanın bulunduğu bir `.zip` arşivini) içe aktarın; asistan doğrudan cihaz üzerinde çalışan llama.cpp aracılığıyla yanıt versin — hesap yok, API anahtarı yok, ağ yok ve uygulamadan çıktığınız anda model bellekten boşaltılır. Yaklaşık 1–4 GB boyutundaki Q4 modelleri telefonda ideal dengeyi yakalar; bir masaüstü ise çok daha büyük hedefleri kaldırabilir. Araçlar yerelde tercihe bağlıdır ve GPU hızlandırması açık bir uyarının ardından yapılan bir seçimdir: CPU her zaman çalışır, itiraz eden bir GPU ise zarafetle geri çekilir. Görme yeteneği de isteğe bağlıdır — bir mmproj dosyası içe aktarın, asistan bir fotoğrafı inceleyip onu hafif müneccim bir kütüphaneci edasıyla tartışsın.

## Tören gerektirmeksizin değiştirilen on dört dil

English, 简体中文， 繁體中文， Español, العربية, Português, Français, Русский, Deutsch, 日本語， Türkçe, 한국어, Italiano, Nederlands — her ekran, iletişim kutusu, tarih biçimi ve şablon. Dili değiştirin, tüm uygulama aynı karede onu takip etsin; çünkü ortada tek bir katalog vardır ve arayüzün okuduğu şey de tam olarak odur: her kelime özenden ziyade mimari gereği tek bir kez tutulur.

## Dış görünüşü: Utanıp sıkılmadan, tamamen camdan

Altından geçen her şeyi bulanıklaştıran buzlu panellerin arkasında, cihazın önceden onayladığı bir maliyetle, kendini neredeyse hiç tekrar etmeyen canlı bir gradyan süzülür. Sekiz stil ailesine yayılmış cömert bir palet yelpazesi; aralarında aheste aheste dolaşan otomatik döngülü veya rastgele bir refakatçi eşliğinde. Açık, koyu, sistem temaları ve Monet esintili temalardan oluşan bir galeri — üstelik Android 12 veya sonraki sürümlerde palet, gün boyunca duvar kâğıdınızın renklerini ödünç alır. Widget'lar aynı cam yüzeyi başlatıcınıza taşır; Windows'ta ise uygulama sistem tepsisine süzülür ve yan çubuk alt sekme çubuğunun yerini alır; ne de olsa geniş bir monitör, yana doğru gerilmiş bir telefon düzeninden çok daha iyisini hak eder.

## Şakası olmayan bir kilit ve sizinle birlikte gelen yedekleme

Uygulama kilidi, nazik bir ricadan ziyade gerçek bir kaba kuvvet (brute-force) önleme politikasıdır: kademeli olarak artan bekleme süreleri, yeniden başlatmalara direnen kalıcı bir sayaç, isteğe bağlı bir güvenlik sorusu, isteğe bağlı bir kendi kendini yok etme mekanizması ve tüm bekleme süresi boyunca kibarca izne ayrılan parmak izi (Android) veya Windows Hello (Windows). Veritabanı bekleme halindeyken, ekler ise tek tek şifrelenir; yedekler ise notları, görevleri, geçmişi, sohbetleri, ekleri ve ayarları cihazlar arasında taşıyan, parola korumalı tek bir `.lcb` dosyasıdır — tek bir öğe bile değiştirilmeden önce önizlenir ve otomatik çalışacak şekilde hazırlanır. Kimsenin okuyamadığı bir yedek, yedekten ziyade bir hatıra eşyasıdır; bu yüzden bu yedek hem çıkışta hem de girişte tekrar okunur.

## Sadece vaat edilen değil, yapısal olan gizlilik

Siz dışa aktarmadıkça veya bir modele bakması için bir neden vermedikçe cihazdan hiçbir şey çıkmaz. Paylaşım menüsü entegrasyonu varsayılan olarak kapalıdır; tanılama verileri varsayılan olarak devre dışıdır ve yerel olarak tutulur; bağladığınız asistan hizmetleri tamamen sizin seçiminizdir; bulut yansıtma ise WebDAV üzerinden bizzat yapılandırdığınız bir modüldür. Şifrelemenin bilinçli tek istisnası — Markdown, Word, PDF veya Excel'e aktarma — başka herhangi bir yerde açabileceğiniz bir dosyadır; ki asıl amaç da zaten budur.

## Rust, ama yalnızca hakkını verdiği yerlerde

İki kritik işlem yolu Rust ile yazılmıştır ve JNI aracılığıyla erişilir — yedeklemelerin ve ek şifrelemelerinin arkasındaki PBKDF2 ve AES-256-GCM yordamları ile süzülen arka planın arkasındaki matematik. Yerel kütüphane mevcut olmadığında her ikisi de birebir aynı Kotlin koduna geri döner; böylece uygulama hiçbir zaman bir derleyicinin rehinesi haline gelmez.

## Derleme (evet, bir telefondan, sırtınızda sabahlığınızla)

Android Studio yok, yerel SDK yok, komut satırı yok. GitHub'a push yapın, **Actions** sekmesini açın, istediğiniz iş akışını çalıştırın ve sonucu indirin — usulünce imzalanmış bir sürüm veya çift tıklamalı bir yükleyici. İş akışları doğrudan depoda yaşar; böylece tarif yazarın kafasında değil kutunun içindedir ve derleme süreci de en az kodun kendisi kadar denetlenebilirdir.

## Proje düzeni

```
shared/       TEK ortak kaynak ağacı — iş mantığı ve arayüzün büyük bölümü burada TEK SEFERDE yer alır
              ve hem :app hem de :desktop tarafından derlenir. Her iki platformu birlikte değiştirmek için
              buradaki bir dosyayı düzenleyin.
app/          Android modülü (:app) — Activity kabuğu, Room veritabanı katmanı, JNI köprüleri, widget'lar
              ve yalnızca sahiden Android'e bağlı olan kodlar
desktop/      Masaüstü modülü (:desktop) — Compose for Desktop kabuğu, android.* JVM uyumluluk katmanları,
              JDBC veritabanı çekirdeği, motor DLL'i için yerel CMake derlemesi
rust/         Rust hızlandırıcısı (platformlar arasında paylaşımlı)
.github/      build.yml (Android APK), build-windows.yml (Windows yükleyicisi), check.yml
```

## Sırada ne var

Lucent, alelade bir yapılacaklar listesine kıyasla epey heybetli bir uygulamadır ve bu durum için bir kez bile özür dilememiştir. Hafızası olan notlar, dişini gösteren görevler, eli iş tutan ve görgü kurallarını bilen bir asistan, sözünü tutan bir şifreleme ve üzerine düşünmeye değer bir görsel yüzey. Bir hafta kullanın ve neleri fark ettiğinizi gözlemleyin; bir ay kullanın ve artık hiçbir şeyi fark etmediğinizi fark edin.

## Üzerinde yükseldiğimiz o devlerin omuzlarına şükranla

Cam yüzeyin altında Lucent, başka insanların ortaya koyduğu pek çok mükemmel çalışmayı barındırır. Bunu açıkça dile getirmemek kabalık — ve bir iki durumda düpedüz bir lisans ihlali — olurdu. Metinlerin tamamı ve telif hakkı bildirimleri **[`THIRD-PARTY-NOTICES.md`](./docs/THIRD-PARTY-NOTICES.md)** dosyasında yer almaktadır; minnettarlığımızla birlikte özet hali ise şöyledir:

| Ödünç alınan deha | Üstlendiği görev | Lisans |
|---|---|---|
| [Kotlin](https://github.com/JetBrains/kotlin) & [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | dil ve eşzamanlılığa karşı gösterdiği engin sabır | Apache-2.0 |
| [Jetpack Compose & AndroidX](https://developer.android.com/jetpack/androidx) | arayüzün Android yarısı | Apache-2.0 |
| [Compose Multiplatform & Skiko](https://github.com/JetBrains/compose-multiplatform) | arayüzün masaüstü yarısı | Apache-2.0 |
| [Material Icons](https://github.com/google/material-design-icons) | bir anlam taşıyan küçük resimler | Apache-2.0 |
| [Haze](https://github.com/chrisbanes/haze) — © Chris Banes | o pek moda olan tüm bulanıklık | Apache-2.0 |
| [OkHttp](https://github.com/square/okhttp) — © Square, Inc. | bulutla muhabbet | Apache-2.0 |
| [Apache PDFBox](https://pdfbox.apache.org/) | masaüstünde PDF desteği | Apache-2.0 |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) — © Taro L. Saito et al. | masaüstünün SQLite'a giden yolu | Apache-2.0 |
| [Skia](https://skia.org/) | arayüzün her iki yarısının da altında yatan çizim gücü | BSD-3-Clause |
| [SQLite](https://www.sqlite.org/) | veritabanının kendisi, sessiz sedasız dünyayı döndüren güç | Public Domain |
| [SQLCipher](https://www.zetetic.net/sqlcipher/) — © Zetetic LLC | Android tarafında bu veritabanının üzerindeki kilit | BSD-style |
| [llama.cpp & GGML](https://github.com/ggml-org/llama.cpp) — © Georgi Gerganov & contributors | bizzat kendi silikonunuz üzerinde koca bir dil modeli | MIT |
| [Shizuku API](https://github.com/RikkaApps/Shizuku-API) — © Rikka and contributors | telefonun sunabilmesi durumunda ayrıcalıklı kabuk | MIT |
| [org.json](https://github.com/stleary/JSON-java) — © JSON.org | masaüstünde JSON okuma | JSON License |
| [Great Vibes](https://github.com/googlefonts/great-vibes) — © The Great Vibes Pro Project Authors | dolmakalemin çizdiği el yazısı yazı tipi | OFL-1.1 |

Model Context Protocol, Anthropic tarafından sunulan ve MIT Lisansı altında lisanslanan açık bir belirtimdir; Lucent'ın istemcisi özgün bir uygulamadır ve işaret ettiğiniz herhangi bir MCP sunucusu kendi koşullarına tabi üçüncü taraf bir yazılımdır. Word, Excel ve PowerPoint okuyucuları ve yazıcıları, platformun kendi zip ve XML kütüphaneleri üzerine inşa edilmiş özgün Office Open XML uygulamalarıdır — pakete hiçbir ofis kütüphanesi veya üçüncü taraf PDF kütüphanesi dahil edilmemiştir.

### Ve tek bir sözünüzle getirebileceğiniz araçlar

Lucent, ağır siklet araç zincirleri ya da bir ofis paketi içermez. Eklentiler sayfasından veya asistana rica ederek bir Linux kullanıcı alanını, bir belge dönüştürücüsünü ya da bir medya aracını doğrudan ana kaynağından (upstream) size ait bir depolama alanına çekebilir. Bunların hiçbiri paketin bir parçası değildir, hiçbirine doğrudan bağlanılmaz ve her biri kendi lisansı altında kalır: PRoot ve Ubuntu Base'den LibreOffice'e, belge kütüphaneleriyle Python'a, Node.js ve Playwright'a; Git, Pandoc, FFmpeg, ripgrep, 7-Zip, Tesseract, ImageMagick, qpdf, Poppler ve yt-dlp'ye kadar uzanan eksiksiz liste **[bildirimler dosyasında](./docs/THIRD-PARTY-NOTICES.md)** yer almaktadır. Lucent bu yazarların her birine şükranlarını sunar; bunlardan herhangi birini — beraberinde gelen lisansla birlikte — yükleme tercihi tamamen size aittir.

### Ve incelediğimiz not uygulamaları

Bu proje için not ve görev alanında olgunlaşmış altı uygulama incelendi, kurcalandı ve kendileriyle birer fincan çay içildi — Lucent'ın bünyesinde barındırmadığı kodları için değil, yazarlarının herkesten önce akıl ettiği şeyler için. Parmak izleri Lucent'ın ikili dosyalarında değil, yapısında ve sergilediği tavırlarda saklıdır; tasarım borcumuzu yüksek sesle teslim etmek ise yapabileceğimiz en asgari şeydir:

| Neden incelendi | Lucent neyi ödünç aldı | Kendi koşulları |
|---|---|---|
| [Quillpad](https://github.com/quillpad/quillpad) | iki seviyeli çekmece menü — elinizin uzanabileceği yerde sabitleme, bir seviye derinde yapılandırma | GPL-3.0 |
| [Omni-Notes](https://github.com/federicoiosue/Omni-Notes) | sistem dosya seçicilerinin dürüstlüğü ve kendini anlatan uzun basma eylemleri | GPL-3.0 |
| [OpenNote-Compose](https://github.com/YangDai2003/OpenNote-Compose) | dinamik renk öncelik modeli — önce duvar kâğıdı, saklanan tercihler ise asla yok edilmez | GPL-3.0 |
| [AppFlowy](https://github.com/AppFlowy-IO/AppFlowy) | ayarlar sayfasında ve her yerde ne olup bittiğini açıkça söyleme anlayışı | AGPL-3.0 |
| [Logseq](https://github.com/logseq/logseq) | bir not uygulamasının bir kontrol panelinden ziyade bir mekân gibi hissettirmesi gerektiği | AGPL-3.0 |
| [MarkLeaf](https://github.com/jeiel85/markleaf-android) | ayarlar sırasını beklerken yazı yazma alanının dingin kalması gerektiği | Apache-2.0 |

### Ve örnek aldığımız çalışma düzeneği

Lucent'ın asistanı, **DeepSeek Harness** (dsh) ile bu düzeneği root olmadan ve Termux'a ihtiyaç duymadan telefona taşıyan MIT lisanslı Android projesi **[DSHA](https://github.com/DSH-APP/DSHA)**'nın araç dağarcığını ve çalışma alışkanlıklarını takip eder. Lucent bunların hiçbirini içinde barındırmaz: araçlar, döngü, paneller ve hedefler, bu iki projenin yerleştirdiği standartlara uygun davranacak şekilde kendi Kotlin kodumuzla yazılmıştır. Çıkış kodları hata yerine sonuç olarak döner, bir dosya yazılmadan önce okunur, bir hedef kendi turlarını sayar, yardımcılara laf anlatılabilir veya durdurulabilir ve tartışmaya açık her şey önce sorulur. Yaklaşım onlara aittir ve burada hakkı teslim edilmektedir; kod ise bize aittir.

Yüklediğiniz her şey Lucent'ın kendi lisansına tabidir; yukarıdaki projeler dahil edilen eserler olarak değil, fikir ve düzenleme kaynakları olarak saygıyla anılmaktadır. Yapı bize aittir; nezaket ise onlara.

BSD tarzı lisansı — günlüğünüzün kapağını kapalı tutan şeyin ta kendisi olduğu düşünülürse gayet makul bir şekilde — telif hakkı ve bildiriminin kullanıcının gerçekten bulabileceği bir yerde yeniden üretilmesini talep eden **SQLCipher** için de özel bir parantez açmak gerek. Nitekim yukarıdaki bildirim dosyasında yer almaktadırlar; bir Lucent derlemesi dağıtırsanız, bunların bulunabilir kalmasını sağlayın. Lucent'ın bizzat bünyesinde barındırdığı tek yazı tipi, açılış ekranındaki kalemin el yazısı stili olan ve ayrılmış adı korunarak SIL Open Font License altında değiştirilmeden paketlenen Great Vibes'tır.

## Lisans ve karşılığında istediği o tek küçük şey

Lucent, **MIT Lisansı** altında yayımlanmıştır — bkz. [`LICENSE`](./LICENSE). Onunla neredeyse canınız ne istiyorsa yapın: kullanın, değiştirin, ticari bir şeye dönüştürün, üzerine daha iyi bir şey inşa edin ve bize teşekkür etmek için tek satır bile yazmayın. Tamamen makul olan tek koşul, telif hakkı bildirimimizin ve lisans metnimizin, kodun herhangi bir kopyasında veya önemli bir kısmında yolculuğa eşlik etmesidir — bu yüzden Lucent'ı yeniden kullanırsanız, `LICENSE` dosyasını (ve üzerindeki adı) dağıttığınız şeyin yanında tutun, böylece ödeşmiş oluruz. Yukarıdaki üçüncü taraf bileşenler de benzer şekilde mütevazı taleplerde bulunmaktadır; bunlara da aynı anlayışla riayet edin, dostluğumuz baki kalsın.

Gizlilik politikası: **[`privacy.html`](https://lucent-project.github.io/lucentapp/privacy.html)** — Lucent'ın ne depoladığı, cihazınızdan nelerin ayrıldığı ve elinizdeki tüm kontrol mekanizmaları.

## Katkıda Bulunma

Şayet içinizi Lucent'ı geliştirme arzusu kaplarsa, bundan sessiz bir memnuniyet duyarız. Hata raporları, derinlemesine düşünülmüş öneriler ve iyi niyetle sunulan çekme istekleri (pull requests) başımızın üstündedir. Tek ricamız, herkesin kesin bir nezaket çerçevesinde kalması, çayını sıcak tutması ve katkıda bulunan diğer dostlara saygın bir okuma salonunda beklenen zarafetle yaklaşmasıdır.
