package org.example.bts_backend;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

public class NctSongUrlExtractor {

    public static void main(String[] args) {
        try {
            String playlistUrl = "https://www.nhaccuatui.com/playlist/top-100-nhac-tre-hay-nhat-various-artists.m3liaiy6vVsF.html";

            Document doc = Jsoup.connect(playlistUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .referrer("https://www.google.com/")
                    .get();

            Elements songItems = doc.select("li[itemprop=tracks]");

            Map<String, String> songMap = new LinkedHashMap<>();

            for (Element item : songItems) {
                String songName = item.select("meta[itemprop=name]").attr("content");
                String songUrl = item.select("meta[itemprop=url]").attr("content");

                // Tùy chọn: lấy tên ca sĩ nếu cần
                Element artistEl = item.selectFirst(".name_singer");
                String artist = (artistEl != null) ? artistEl.text() : "(Không rõ)";

                if (!songName.isBlank() && !songUrl.isBlank()) {
                    songMap.put(songName + " - " + artist, songUrl);
                }
            }

            System.out.println("🎵 Danh sách bài hát:");
            songMap.forEach((name, url) -> {
                System.out.println("- " + name);
                System.out.println("  🔗 " + url);
            });

        } catch (Exception e) {
            System.out.println("❌ Lỗi khi lấy dữ liệu:");
            e.printStackTrace();
        }
    }
}
