package org.example.bts_backend.Controller;

import lombok.RequiredArgsConstructor;
import org.example.bts_backend.Services.MusicCrawlerService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/songss")
@RequiredArgsConstructor
public class SongCrawlController {

    private final MusicCrawlerService musicCrawlerService;

    @GetMapping("/crawl-top10")
    public Map<String, Object> crawlTop10SongsManually() {
        List<String> added = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            System.out.println("🔍 Đang truy cập trang playlist top 100 nhạc trẻ...");
            String url = "https://www.nhaccuatui.com/playlist/top-100-nhac-tre-hay-nhat-various-artists.m3liaiy6vVsF.html";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.google.com/")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(10000)
                    .get();

            Elements songs = doc.select("div.item_content");
            System.out.println("🎧 Số bài hát tìm thấy: " + songs.size());

            int count = 0;
            for (Element song : songs) {
                Element titleEl = song.selectFirst("a.name_song");
                Element singerEl = song.selectFirst("a.name_singer");

                if (titleEl != null && singerEl != null) {
                    String title = titleEl.text().trim();
                    String link = titleEl.absUrl("href");
                    String artist = singerEl.text().trim();

                    System.out.println("🎵 Bài: " + title + " - " + artist);
                    System.out.println("🔗 Link: " + link);

                    String status = musicCrawlerService.crawlAndSaveWithFeedback(title, artist, link);
                    System.out.println("📥 Trạng thái: " + status);

                    if ("added".equals(status)) {
                        added.add(title + " - " + artist);
                    } else if ("skipped".equals(status)) {
                        skipped.add(title + " - " + artist);
                    } else {
                        errors.add(title + " - " + artist);
                    }
                }
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
                if (count >= 10) break;
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi toàn cục khi crawl: " + e.getMessage());
            errors.add("Lỗi toàn cục: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("added", added);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return result;
    }
}
