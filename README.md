# Forecast App (Java)

Aplikasi Java sederhana untuk menampilkan data cuaca Jakarta dari Open-Meteo API dalam tabel.

## API
`https://api.open-meteo.com/v1/forecast?latitude=-6.2&longitude=106.8&hourly=temperature_2m`

## Data yang Ditampilkan
- `time` (waktu pengukuran)
- `temperature_2m` (suhu per jam dalam °C)

## Cara Menjalankan
1. Compile:
```bash
mvn compile
```
2. Jalankan aplikasi:
```bash
mvn exec:java
```

Jendela Swing akan tampil dengan tabel data prakiraan suhu per jam.

## Build di Linux, Run di Windows (tanpa Maven di Windows)
1. Di Linux, buat fat-jar:
```bash
mvn clean package
```
2. Copy folder project (minimal `target/` dan `run.bat`) ke Windows.
3. Di Windows, cukup jalankan:
```bat
run.bat
```

Catatan:
- Windows tetap harus punya Java (JRE/JDK).
- File yang dijalankan adalah `target\forecast-app.jar` (sudah termasuk dependency).
