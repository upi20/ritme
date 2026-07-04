# Ritme 📶 — Network Rhythm Monitor

**Ritme** adalah aplikasi Android modern berbasis **Jetpack Compose** dan **Material Design 3** yang dirancang untuk melakukan pemantauan stabilitas jaringan secara real-time dan nonstop 24 jam. Aplikasi ini memantau kualitas latensi (ping), mencatat transisi status konektivitas, serta melakukan pengujian kecepatan (speedtest) secara berkala dan efisien.

---

## 🎨 Tampilan Utama & Desain Visual

Aplikasi ini menggunakan tema **Ultra-Dark/Amoled** (`#09080C`) dengan aksen warna kontras tinggi (Vibrant Emerald, Scarlet Red, dan Purple M3) untuk kenyamanan mata saat digunakan sebagai monitor permanen (always-on display).

### 📱 Mode Monitoring Responsif (Portrait & Landscape)
*   **Landscape Mode:** Memaksimalkan visual status utama berdampingan secara horizontal dengan indikator unduh (download) dan unggah (upload) di sisi kiri dan kanan secara simetris.
*   **Portrait Mode:** Menyusun teks status utama di atas secara elegan, diikuti oleh metrik kecepatan internet terpadu dengan pembatas visual yang bersih dan minimalis.
*   **Sparkline Dinamis:** Menampilkan 100 data ping terakhir dalam bentuk grafik batang mini yang responsif terhadap perubahan orientasi layar.

---

## 🚀 Fitur Utama

### 1. Pemantauan Latensi Real-time
*   Melacak ping ke target server secara terus-menerus dengan indikator visual instan.
*   Menyediakan grafik **Ritme Sparkline** (100 ping terakhir) untuk melihat tren latensi dengan cepat.

### 2. Speedtest Terintegrasi & Otomatis
*   Mengukur kecepatan **Download**, **Upload**, dan **Ping** secara akurat.
*   Siklus pengujian otomatis setiap **3 menit** secara background jika monitoring aktif dan tidak dijeda (non-paused).
*   Hasil pengujian ditampilkan secara real-time di dashboard utama maupun di dalam Mode Monitoring.

### 3. Log Transisi Koneksi (Local Persistence)
*   Mencatat setiap kali jaringan beralih status (misalnya dari *Sangat Baik* ke *RTO / Offline*).
*   Menggunakan **Room Database** untuk menyimpan riwayat transisi status dan hasil uji kecepatan secara lokal.

### 4. Optimalisasi Nonstop 24 Jam (Anti-Leak & Hemat Resource)
Untuk memastikan aplikasi dapat berjalan berhari-hari tanpa mengalami *memory leak* atau penurunan performa:
*   **Auto-Pruning Database:** Sistem secara otomatis membatasi jumlah data di database Room. Hanya 100 rekaman terbaru dari riwayat transisi (`transitions`) dan uji kecepatan (`speedtests`) yang disimpan. Rekaman lama otomatis dihapus untuk mencegah penumpukan memori penyimpanan.
*   **Proses & Memory Clean-up:** Penggunaan perintah ping sistem (`/system/bin/ping`) dikelola secara aman dengan penutupan pembaca aliran data (*reader*) menggunakan blok `use` serta penghancuran proses (`process?.destroy()`) di blok `finally`.
*   **Siklus Coroutine Aman:** Semua *job* asinkronus (seperti deteksi ping dan auto-speedtest) terikat pada `viewModelScope` dan dibatalkan secara eksplisit di fungsi `onCleared()` ViewModel saat siklus hidup layar berakhir.

---

## 🛠️ Arsitektur & Teknologi

*   **UI Framework:** Jetpack Compose (Material Design 3)
*   **Architecture Pattern:** MVVM (Model-View-ViewModel) dengan StateFlow untuk manajemen state yang reaktif dan searah (Unidirectional Data Flow).
*   **Lokal Database:** Room ORM (SQLite) dengan Kotlin Symbol Processing (KSP).
*   **Concurreny & Async:** Kotlin Coroutines & Flow (termasuk penanganan delay non-blocking).
*   **System Integration:** Eksekusi biner ping bawaan Android dengan *fallback* koneksi soket TCP pada port DNS 53 jika izin sistem dibatasi.

---

## 📂 Struktur Folder Penting

```text
/app/src/main/java/com/example/
│
├── data/
│   ├── database/
│   │   ├── DatabaseDaos.kt     # DAO Room dengan fungsi Prune otomatis (Limit 100)
│   │   ├── DatabaseEntities.kt # Entitas Tabel Transisi & Speedtest
│   │   └── AppDatabase.kt      # Kelas Database Room utama
│   └── repository/
│       └── NetworkRepository.kt# Logika Ping aman, Speedtest, dan operasi database
│
└── ui/
    ├── screens/
    │   └── RitmeDashboardScreen.kt # UI Dashboard, Sparkline, dan Mode Monitoring Responsif
    └── viewmodel/
        └── RitmeViewModel.kt   # Pengelola State, Loop Ping, dan Siklus Auto-Speedtest 3 Menit
```

---

## ⚙️ Persyaratan Sistem & Instalasi

*   **SDK Minimum:** Android 26 (Android 8.0 Oreo)
*   **SDK Target:** Android 34 (Android 14)
*   **Koneksi Internet:** Diperlukan untuk melakukan tes ping dan speedtest.
*   **Izin Aplikasi:** Aplikasi secara otomatis meminta izin `android.permission.INTERNET` di dalam berkas manifest.

---

## 📝 Lisensi

Proyek ini dibuat untuk kebutuhan pemantauan stabilitas jaringan mandiri secara andal, efisien, dan ramah memori perangkat. Enjoy monitoring! 🎯
