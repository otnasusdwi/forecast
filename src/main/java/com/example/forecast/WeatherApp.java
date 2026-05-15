package com.example.forecast;

import com.google.gson.Gson;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Aplikasi desktop Java sederhana untuk menampilkan prakiraan suhu Jakarta per jam
 * dari API Open-Meteo dalam bentuk tabel.
 */
public class WeatherApp {

    /**
     * URL API sesuai kebutuhan soal (latitude/longitude Jakarta, data hourly temperature_2m).
     */
    private static final String API_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=-6.2&longitude=106.8&hourly=temperature_2m";

    /**
     * Titik awal program. Method ini:
     * 1) Mengambil data dari API
     * 2) Membuat UI Swing
     * 3) Menampilkan data pada tabel
     */
    public static void main(String[] args) {
        try {
            List<WeatherRow> rows = fetchWeatherRows();

            // Menjalankan pembuatan UI di Event Dispatch Thread agar aman untuk Swing.
            SwingUtilities.invokeLater(() -> buildAndShowUI(rows));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Gagal mengambil atau menampilkan data cuaca: " + e.getMessage());
        }
    }

    /**
     * Mengambil JSON dari API, mem-parse data, lalu mengubahnya menjadi list baris tabel.
     */
    private static List<WeatherRow> fetchWeatherRows() throws IOException, InterruptedException {
        // HttpClient digunakan untuk mengirim request HTTP secara modern di Java 11+.
        HttpClient client = HttpClient.newHttpClient();

        // Request GET ke API Open-Meteo.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        // Mengirim request dan menerima response sebagai String JSON.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Validasi status code agar error server/client API bisa terdeteksi jelas.
        if (response.statusCode() != 200) {
            throw new IOException("HTTP error: " + response.statusCode());
        }

        // Parse JSON ke object Java menggunakan Gson.
        ForecastResponse forecast = new Gson().fromJson(response.body(), ForecastResponse.class);

        // Konversi data array time + temperature_2m menjadi list WeatherRow untuk tabel.
        return mapToRows(forecast);
    }

    /**
     * Menggabungkan array time dan temperature_2m dari response menjadi list objek baris.
     */
    private static List<WeatherRow> mapToRows(ForecastResponse forecast) {
        if (forecast == null || forecast.hourly == null
                || forecast.hourly.time == null || forecast.hourly.temperature_2m == null) {
            throw new IllegalArgumentException("Format data API tidak sesuai atau data kosong.");
        }

        int total = Math.min(forecast.hourly.time.size(), forecast.hourly.temperature_2m.size());
        List<WeatherRow> rows = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            String rawTime = forecast.hourly.time.get(i);
            Double temp = forecast.hourly.temperature_2m.get(i);

            // Ubah format waktu ISO (contoh: 2026-05-15T10:00) agar lebih mudah dibaca manusia.
            String formattedTime = formatTime(rawTime);
            rows.add(new WeatherRow(formattedTime, temp));
        }

        return rows;
    }

    /**
     * Memformat waktu dari pola API (yyyy-MM-dd'T'HH:mm) ke format tampilan (dd-MM-yyyy HH:mm).
     */
    private static String formatTime(String rawTime) {
        LocalDateTime dt = LocalDateTime.parse(rawTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    /**
     * Membuat jendela aplikasi dan menampilkan data cuaca dalam JTable.
     */
    private static void buildAndShowUI(List<WeatherRow> rows) {
        try {
            // Mengaktifkan look and feel bawaan sistem operasi agar UI lebih natural.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Jika gagal set look and feel, aplikasi tetap bisa berjalan dengan default Swing theme.
        }

        JFrame frame = new JFrame("Prakiraan Suhu Jakarta per Jam");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Model tabel dengan dua kolom sesuai requirement soal.
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Time (Waktu)", "Temperature_2m (°C)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Tabel dibuat read-only agar user fokus membaca data.
                return false;
            }
        };

        // Mengisi tabel dari list data hasil API.
        for (WeatherRow row : rows) {
            model.addRow(new Object[]{row.time, row.temperature});
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    /**
     * Model root response JSON dari API.
     * Contoh struktur: { "hourly": { "time": [...], "temperature_2m": [...] } }
     */
    private static class ForecastResponse {
        HourlyData hourly;
    }

    /**
     * Model bagian "hourly" pada JSON API.
     */
    private static class HourlyData {
        List<String> time;
        List<Double> temperature_2m;
    }

    /**
     * Representasi 1 baris data tabel: waktu + suhu.
     */
    private static class WeatherRow {
        String time;
        Double temperature;

        WeatherRow(String time, Double temperature) {
            this.time = time;
            this.temperature = temperature;
        }
    }
}
