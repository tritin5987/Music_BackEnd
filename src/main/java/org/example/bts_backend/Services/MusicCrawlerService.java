package org.example.bts_backend.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.Repository.SongsRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MusicCrawlerService {

    private final SongsRepository songsRepository;

    @Transactional
    public String crawlAndSaveWithFeedback(String title, String artist, String songPageUrl) {
        try {
            // Kiểm tra xem đã tồn tại trong DB chưa
            Optional<Songs> existing = songsRepository.findByTitleAndArtist(title, artist);
            if (existing.isPresent()) return "skipped";

            // Lấy mp3Url từ trang bài hát thông qua key1
            String mp3Url = getMp3UrlFromSongPage(songPageUrl);
            if (mp3Url == null || mp3Url.isBlank()) return "error";

            // Lấy ảnh đại diện
            Document doc = Jsoup.connect(songPageUrl)
                    .userAgent("Mozilla/5.0")
                    .get();
            Element imageEl = doc.selectFirst("meta[property=og:image]");
            String image = imageEl != null ? imageEl.attr("content") : "";

            // Tải file mp3 về máy
            String fileName = (title + "-" + artist).replaceAll("[^a-zA-Z0-9]", "_") + ".mp3";
            String savePath = "storage/music/" + fileName;

            File folder = new File("storage/music");
            if (!folder.exists()) folder.mkdirs();

            try (InputStream in = new URL(mp3Url).openStream();
                 FileOutputStream out = new FileOutputStream(savePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Lưu vào DB
            Songs song = new Songs();
            song.setTitle(title);
            song.setArtist(artist);
            song.setSource("/music/" + fileName);
            song.setImage(image);
            song.setLyrics(""); // có thể update sau nếu lấy được lyrics
            song.setFavorite(false);
            song.setDuration(0);
            song.setCounter(0);
            song.setReplay(0);
            song.setAlbum("Top Nhạc Trẻ");

            songsRepository.save(song);
            return "added";

        } catch (Exception e) {
            System.err.println("❌ Lỗi xử lý bài: " + title + " - " + artist);
            e.printStackTrace();
            return "error";
        }
    }

    // ✅ Hàm lấy mp3Url từ trang bài hát (dựa vào key1)
    public String getMp3UrlFromSongPage(String songPageUrl) {
        try {
            Document doc = Jsoup.connect(songPageUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.google.com/")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(10000)
                    .get();

            // Tìm <script> chứa "songencryptkey"
            Element script = doc.selectFirst("script:contains(songencryptkey)");
            if (script == null) {
                System.err.println("❌ Không tìm thấy thẻ script chứa songencryptkey");
                return null;
            }

            String scriptContent = script.html();

            // Dùng regex để lấy key1 (songencryptkey)
            Pattern pattern = Pattern.compile("songencryptkey\\s*[:=]\\s*\"([a-f0-9]{32})\"");
            Matcher matcher = pattern.matcher(scriptContent);

            if (!matcher.find()) {
                System.err.println("❌ Không tìm thấy key1 trong script");
                return null;
            }

            String key1 = matcher.group(1);
            System.out.println("🔑 key1: " + key1);

            // Gọi API JSON
            String apiUrl = "https://www.nhaccuatui.com/ajax/get-media-info?key1=" + key1;
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            if (!json.has("data")) {
                System.err.println("❌ Không có trường 'data' trong JSON");
                return null;
            }

            JSONObject data = json.getJSONObject("data");
            if (!data.has("stream_url")) {
                System.err.println("❌ Không có stream_url trong dữ liệu JSON");
                return null;
            }

            return data.getString("stream_url");

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy MP3 từ bài: " + songPageUrl);
            e.printStackTrace();
            return null;
        }
    }


}
