# SGC Teknisyen — Android Client

Teknik servis personeli için mobil uygulama. Jetpack Compose, Hilt, Room, Retrofit, WorkManager, Foreground Service.

## Özellikler

- **Giriş/Çıkış**: Token tabanlı auth (EncryptedSharedPreferences)
- **İş Emirleri**: Liste, detay, ürünler, check-in/out, başlat/tamamla
- **Konum Takibi**: Foreground Service ile 60 sn heartbeat, 15 sn konum isteği polling
- **Çevrimdışı Çalışma**: Room DB kuyruğu, WorkManager ile otomatik senkron
- **Formlar**: Şantiye faaliyet formu (tarih, saat, konu, işler, malzemeler, arızalar, notlar, imza, fotoğraf)
- **Profil**: Kullanıcı bilgisi, senkron durumu, konum servisi toggle, çıkış

## Teknoloji Stack

| Kategori | Teknoloji |
|----------|-----------|
| Dil | Kotlin 2.0.21 |
| UI | Jetpack Compose (Material3) |
| DI | Hilt 2.51 |
| Veritabanı | Room 2.6 |
| Network | Retrofit 2.11 + OkHttp 4.12 |
| Arka Plan | WorkManager 2.9 + Foreground Service |
| Konum | Google Play Services Location 21.2 (FusedLocationProvider) |
| Güvenlik | EncryptedSharedPreferences (AES256) |
| Resim | Coil 2.6 |
| Serileştirme | Kotlinx Serialization 1.6 |

## Gereksinimler

- Android Studio Koala (2024.1.1) veya üzeri
- JDK 17 (Temurin önerilir)
- Android SDK 34
- Google Play Services (konum için)

## Kurulum

### 1. Depoyu Klonlayın
```bash
git clone https://github.com/mytiflis/sgc-teknisyen.git
cd sgc-teknisyen
```

### 2. Android Studio ile Açın
- File → Open → `sgc-teknisyen` klasörünü seçin
- Gradle sync bekleyin

### 3. Debug Keystore (Hazır)
`app/debug.keystore` zaten dahil:
- Store password: `android`
- Key alias: `androiddebugkey`
- Key password: `android`

### 4. Release Keystore (Opsiyonel)
Environment variables ayarlayın:
```bash
export SGC_KEYSTORE=/path/to/release.keystore
export SGC_KEYSTORE_PASSWORD=***
export SGC_KEY_ALIAS=***
export SGC_KEY_PASSWORD=***
```

### 5. Build
```bash
# Debug
./gradlew assembleDebug

# Release (imzalı)
./gradlew assembleRelease
```

APK konumu: `app/build/outputs/apk/release/app-release.apk`

## Mock Server (Geliştirme İçin)

Mock server, tüm API endpoint'lerini simüle eder.

### Kurulum
```bash
cd mock-server
npm install
```

### Çalıştırma
```bash
npm start
# veya development için:
npm run dev
```

Server `http://localhost:3000` adresinde çalışır.

### API Base URL
Android uygulaması `BuildConfig.BASE_URL` kullanır. Varsayılan:
```
https://10.0.2.2:3000/api/
```
(10.0.2.2 = emulator'dan host makinesine erişim)

Gerçek cihazda test için:
1. Bilgisayarınızın LAN IP'sini öğrenin (`ipconfig` / `ifconfig`)
2. `app/build.gradle.kts` içinde `BASE_URL` güncelleyin:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://192.168.x.x:3000/api/\"")
   ```

### Test Kullanıcıları
| Kullanıcı | Şifre | Rol |
|-----------|-------|-----|
| teknisyen1 | 123456 | Teknisyen |
| teknisyen2 | 123456 | Teknisyen |
| admin | admin123 | Admin |

### Mock Veri
- **5 İş Emri** (IE-2026-001...005) - teknisyen1'e atanmış
- **2 Form Taslağı** - IE-2026-003, IE-2026-004 için
- **Konum İsteği Testi**: `POST http://localhost:3000/api/admin/konum-istek` body: `{"teknisyen_id": 1}`

## Proje Yapısı

```
sgc-teknisyen/
├── .github/workflows/build.yml     # CI/CD
├── mock-server/                     # Node/Express mock API
│   ├── server.js                   # Express app (tüm endpoint'ler)
│   ├── package.json
│   └── data/
│       ├── users.json              # Test kullanıcıları
│       ├── jobs.json               # İş emirleri
│       └── forms.json              # Form taslakları
├── app/
│   ├── build.gradle.kts            # App dependencies
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml     # İzinler, servisler, Hilt
│       ├── java/com/sgc/teknisyen/
│       │   ├── MainApplication.kt  # @HiltAndroidApp
│       │   ├── MainActivity.kt     # Entry point + NavHost
│       │   ├── SgcApp.kt           # Compose tema
│       │   ├── data/
│       │   │   ├── local/          # Room: entities, DAOs, database
│       │   │   ├── remote/         # Retrofit: ApiService, DTOs, interceptors
│       │   │   └── repository/     # Repository impls
│       │   ├── di/                 # Hilt modules
│       │   ├── domain/             # Models, use cases
│       │   ├── presentation/
│       │   │   ├── navigation/     # Compose Navigation
│       │   │   └── screens/        # Login, Home, JobDetail, Form, Profile
│       │   ├── service/            # LocationService, HeartbeatService
│       │   └── worker/             # SyncWorker, HeartbeatWorker
│       └── res/                    # Strings, colors, themes, xml
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml       # Version catalog
├── gradle.properties
└── README.md
```

## İzinler

| İzin | Açıklama |
|------|----------|
| INTERNET | API iletişimi |
| ACCESS_FINE/COARSE_LOCATION | GPS/Ağ konumu |
| ACCESS_BACKGROUND_LOCATION | Arka plan konumu |
| FOREGROUND_SERVICE | Servis çalıştırma |
| FOREGROUND_SERVICE_LOCATION | Location foreground type |
| WAKE_LOCK | CPU uyanık tutma |
| POST_NOTIFICATIONS | Bildirim (Android 13+) |
| CAMERA | Fotoğraf çekme |
| READ_MEDIA_IMAGES | Galeri erişimi |

## CI/CD (GitHub Actions)

`.github/workflows/build.yml`:
- Her push'ta çalışır
- JDK 17 + Gradle 8.9
- İmzalı release APK üretir
- Artifact olarak yükler: `SGC-Teknisyen-Client`

## Geliştirme Notları

### Konum Servisi
`LocationService` sürekli çalışır:
- 60 sn'de bir heartbeat (pil < %15 ise 300 sn)
- 15 sn'de bir `/konum-istek/bekleyen` polling
- FusedLocationProviderClient (high accuracy, 30 sn interval)

### Çevrimdışı Senkron
- Tüm yazma işlemleri `PendingOpEntity` kuyruğuna alınır
- `SyncWorker` her 15 dk'da bir `/sync/batch` dener
- Başarılı işlemler silinir, başarısızlar retry sayacı artırılır

### Mock Server'a Yeni Endpoint Ekleme
1. `mock-server/server.js` içinde route ekleyin
2. Gerekirse `data/` altına JSON dosyası ekleyin
3. Server otomatik yeniden yükler (nodemon ile)

## Lisans

Proprietary — Synergy Global Company

## İletişim

SGC Teknoloji Ekibi — teknik@sgc.com.tr