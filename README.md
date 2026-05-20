<h3 align="center">Aplikasi LapakAi - Tampilan Antarmuka</h3>

<table align="center">
  <tr>
    <th width="25%" align="center">📱 Halaman Utama</th>
    <th width="25%" align="center">⚙️ Persiapan Generate</th>
    <th width="25%" align="center">🔧 Halaman Settings</th>
    <th width="25%" align="center">👤 Halaman Account</th>
  </tr>
  <tr valign="top">
    <!-- Kolom 1: Halaman Utama (Dua gambar disejajarkan ke bawah) -->
    <td align="center">
      <img width="720" height="1600" alt="Screenshot_20260519_101842" src="https://github.com/user-attachments/assets/fe06a9ca-aca3-4f04-95fe-1ad1ae9f3f9e" />
      <br/><br/>
     <img width="720" height="1600" alt="Screenshot_20260519_101853" src="https://github.com/user-attachments/assets/d0edf851-96ad-4cef-aea2-d028078c74c7" />
    </td>
    <!-- Kolom 2: Halaman Persiapan Generate -->
    <td align="center">
      <img width="720" height="1600" alt="Screenshot_20260519_102132" src="https://github.com/user-attachments/assets/d433184d-89dd-4266-954c-b338f1c1e546" />
    </td>
    <!-- Kolom 3: Halaman Settings (Dua gambar disejajarkan ke bawah) -->
    <td align="center">
      <img width="720" height="1600" alt="Screenshot_20260519_101926" src="https://github.com/user-attachments/assets/c751df94-9c9f-4744-a2db-8fd6c990ff87" />
      <br/><br/>
     <img width="720" height="1600" alt="Screenshot_20260519_101933" src="https://github.com/user-attachments/assets/082d0a95-e5b7-4d99-bc88-89b418b3506c" />
    </td>
    <!-- Kolom 4: Halaman Account -->
    <td align="center">
<img width="720" height="1600" alt="Screenshot_20260519_101902" src="https://github.com/user-attachments/assets/d0270709-c8cc-45b9-8473-6df09a1e7f1f" />
    </td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td width="55%" valign="top">
      <h3>🚀 Fitur Utama Lapak AI v1.0</h3>
      <ul>
        <li><b>Verifikasi Biometrik:</b> Keamanan data lokal terenkripsi dengan Sidik Jari & Face ID untuk akses terjamin.</li>
        <li><b>Analisis Gambar Cerdas:</b> OCR Google ML Kit untuk ekstraksi teks & info produk otomatis dari foto dengan akurasi tinggi.</li>
        <li><b>Database Aman:</b> Enkripsi end-to-end dengan SQLCipher (AES-256) + JNI native C++ untuk penyimpanan data ultra-aman.</li>
        <li><b>Customizable Copywriting:</b> Tone of Voice fleksibel, Firebase Remote Config untuk konfigurasi AI secara dinamis tanpa update app.</li>
        <li><b>Firebase Integration:</b> Analytics real-time, Performance monitoring, & Crashlytics untuk tracking kejadian & error handling.</li>
        <li><b>Native Security:</b> Keystore System + C++ NDK untuk menyembunyikan salt & encryption keys di level binary code.</li>
      </ul>
    </td>
    <td width="45%" align="center" valign="middle" style="padding: 16px;">
      <video width="100%" height="auto" autoplay loop muted playsinline style="border-radius: 16px; box-shadow: 0 8px 24px rgba(233,69,96,0.3); margin-bottom: 12px;">
        <source src="https://github.com/user-attachments/assets/471e86e2-006d-49e0-bf11-ba85ab2b76ae" type="video/mp4" loop muted/>
        Your browser does not support the video tag.
      </video>
      <div style="background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); border-radius: 12px; padding: 12px; border: 2px solid #0f3460;">
        <p style="color: #e94560; font-size: 11px; font-weight: bold; margin: 0;">📊 BUILD INFO</p>
        <p style="color: #fff; font-size: 12px; margin: 4px 0 0 0;"><b>v1.0.0</b> • API 24-36</p>
        <p style="color: #00d4ff; font-size: 11px; margin: 2px 0 0 0;">✅ R8 Full Mode</p>
      </div>
    </td>
  </tr>
</table>

## 🛠️ Tech Stack & Libraries

Proyek **Cap&Tag Generator** ini dibangun menggunakan standar pengembangan Android modern (*Modern Android Development*) untuk memastikan performa yang optimal, aman, dan responsif.

### 1. Core Language & UI Framework
* **Kotlin (v2.2.10)** — Bahasa pemrograman utama yang modern, aman (*null-safety*), dan dioptimasi penuh untuk pengembangan Android.
* **Jetpack Compose** — Framework UI deklaratif modern untuk membangun antarmuka aplikasi yang reaktif tanpa menggunakan XML jadul.
* **Material Design 3 (M3)** — Standar desain visual terbaru dari Google untuk komponen UI yang adaptif, dinamis, dan estetik.

### 2. Architecture & Concurrency
* **MVVM (Model-View-ViewModel)** — Pola arsitektur standar industri untuk memisahkan logika bisnis aplikasi dengan layer tampilan (*UI*).
* **Kotlin Coroutines & Flow** — Manajemen *thread* asinkron untuk menangani proses berat (seperti enkripsi database dan scan OCR) di latar belakang (*background thread*) agar UI tetap mulus tanpa *lag*.
* **Jetpack Lifecycle** — Mengelola siklus hidup komponen Android secara aman untuk mencegah kebocoran memori (*memory leaks*).

### 3. Data Storage & Advanced Security
* **Room Persistence (v2.8.4)** — Lapisan enkapsulasi di atas SQLite untuk penyimpanan data lokal yang terstruktur dan stabil.
* **SQLCipher (v4.6.1)** — Komponen enkripsi tingkat militer (**AES-256**) yang mengunci seluruh file database basis data lokal secara total.
* **C++ (Android NDK/JNI)** — Digunakan sebagai *"Native Brankas Rahasia"* untuk menyembunyikan kunci enkripsi dan *salt* database di level kode mesin binary (`.so`) agar tidak bisa di-decompile.
* **Android Keystore System** — Mengamankan dan membungkus (*wrapping*) kunci kriptografi di dalam perangkat keras perangkat (TEE/SE) agar tidak dapat diekstrak oleh malware.

### 4. Intelligence (AI & OCR)
* **Google ML Kit (Text Recognition)** — Fitur OCR cerdas untuk mengekstrak teks dari foto. Menggunakan opsi *Unbundled Api* (via Google Play Services) sehingga ukuran file APK tetap sangat kecil (~260 KB).
* **Firebase (BoM v34.13.0)**
  * `Firebase Analytics` — Memantau aktivitas dan interaksi pengguna di dalam aplikasi.
  * `Firebase Remote Config` — *(Rencana)* Untuk mengambil dan memperbarui konfigurasi atau API Key AI secara dinamis dari cloud tanpa update aplikasi.

### 5. Utilities & Networking
* **Coil (Compose Edition)** — Library pemuat gambar (*image loader*) berbasis Coroutines yang super ringan untuk memuat foto produk di Jetpack Compose.
* **Biometric Auth** — Menangani popup otentikasi sidik jari atau pemindaian wajah (*Biometric Prompt*) sebelum memberikan akses ke data sensitif.
* **ZXing Android Embedded** — Library pendukung untuk pemindaian dan pembuatan *Barcode/QR Code* jika dibutuhkan di masa mendatang.

### 6. Build Tools & Compilers
* **Gradle (Kotlin DSL)** — Manajemen dependensi dan konfigurasi sistem build proyek menggunakan bahasa Kotlin penuh.
* **KSP (Kotlin Symbol Processing)** — Pengganti KAPT generasi baru yang jauh lebih cepat untuk memproses anotasi kode pada *Room compiler*.
* **CMake** — Alat eksternal untuk mengompilasi file kode sumber C++ (`native-lib.cpp`) menjadi arsitektur file binary Android.


## 🚀 Cara Memulai (Getting Started)

Untuk menjalankan atau menguji proyek ini di komputer lokal Anda, silakan ikuti panduan langkah demi langkah di bawah ini:

### 1. Kloning Repositori (Clone Repository)
Buka terminal atau Git Bash Anda, lalu jalankan perintah berikut:
```bash
git clone https://github.com/AMillionDriver/Cap_Tag.Generator
```

### 2. Membuka Proyek di Android Studio
1. Jalankan **Android Studio** (Disarankan versi Ladybug atau yang lebih baru).
2. Pilih menu **File > Open**, lalu arahkan ke folder proyek hasil kloning tadi.
3. Tunggu hingga proses **Gradle Sync** selesai secara otomatis.

### 3. Konfigurasi C++ & NDK (Penting)
Pastikan Anda sudah menginstal **NDK** dan **CMake** melalui SDK Manager di Android Studio Anda agar proyek dapat mengompilasi library keamanan native (`native-lib.cpp`) dengan sukses tanpa kendala.

---

## 📦 Unduh Aplikasi (Download Release APK)

Jika Anda hanya ingin langsung mencoba aplikasi ini di perangkat Android Anda tanpa harus melakukan *compile* kode sumber:

1. Pergi ke tab **[Releases](https://github.com)** di sebelah kanan halaman repositori ini.
2. Cari versi rilis terbaru (misalnya `v1.0.0-alpha`).
3. Unduh file `app-release.apk`.
4. Instal file APK tersebut di HP Android Anda (Pastikan Anda telah mengizinkan instalasi dari sumber tidak dikenal di pengaturan keamanan HP).

> **Catatan Pengujian:** Aplikasi ini telah diuji dan dioptimasi penuh agar berjalan sangat lancar, responsif, dan hemat memori pada perangkat berspesifikasi menengah ke bawah seperti **Oppo A16**.

