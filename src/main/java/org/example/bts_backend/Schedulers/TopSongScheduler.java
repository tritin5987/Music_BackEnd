package org.example.bts_backend.Schedulers;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopSongScheduler {

    private final MusicCrawlerService musicCrawlerService;

    // Lên lịch lúc 12h đêm hàng ngày
    @Scheduled(cron = "0 0 0 * * *")
    public void crawlTopSongsFromPlaylist() {
        System.out.println("🕛 Bắt đầu crawl top 10 playlist nhạc trẻ...");

        try {
            String url = "https://www.nhaccuatui.com/playlist/top-100-nhac-tre-hay-nhat-various-artists.m3liaiy6vVsF.html";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            Elements songs = doc.select("div.item_content");
            System.out.println("🔎 Số bài hát tìm thấy: " + songs.size());

            int count = 0;
            for (Element song : songs) {
                Element titleEl = song.selectFirst("a.name_song");
                Element singerEl = song.selectFirst("a.name_singer");

                if (titleEl != null && singerEl != null) {
                    String title = titleEl.text().trim();
                    String link = titleEl.absUrl("href");
                    String artist = singerEl.text().trim();

                    System.out.println("🎵 Xử lý: " + title + " - " + artist);
                    System.out.println("🔗 Link: " + link);

                    String status = musicCrawlerService.crawlAndSaveWithFeedback(title, artist, link);
                    System.out.println("📌 Kết quả: " + status);
                }

                count++;
                if (count >= 10) break;
            }

            System.out.println("✅ Hoàn tất crawl top 10 playlist nhạc trẻ.");

        } catch (Exception e) {
            System.err.println("❌ Lỗi crawl tự động: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
