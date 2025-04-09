package org.example.bts_backend.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.io.FileUtils; // Thêm dependency "commons-io" nếu chưa có
// <dependency>
//   <groupId>commons-io</groupId>
//   <artifactId>commons-io</artifactId>
//   <version>2.11.0</version> <!-- hoặc phiên bản phù hợp -->
// </dependency>

@Service
public class ScheduledCrawlService {

    @Autowired
    private SongsCrawlService songsCrawlService;  // Đã có hàm compareSongsFromNCT()

    @Autowired
    private SongsService songsService;

    @Autowired
    private GetCookieService getCookieService;// Đã có hàm addSong(...)

    /**
     * Chạy lúc 00:00 (nửa đêm) mỗi ngày
     */
    @Scheduled(cron = "0 49 22 * * ?")
    public void autoCrawlAndAddSongs() {
        try {

            getCookieService.fetchCookiesAndSaveToFile();
            // (1) Gọi hàm crawl
            Map<String, Object> crawledSongs = songsCrawlService.compareSongsFromNCT();

            // (2) Xử lý kết quả
            for (Map.Entry<String, Object> entry : crawledSongs.entrySet()) {
                String title = entry.getKey();
                Map<String, String> songData = (Map<String, String>) entry.getValue();

                // Chỉ xử lý khi status = "Add"
                if ("Add".equalsIgnoreCase(songData.get("status"))) {
                    String artist = songData.get("artist");
                    String mp3Url = songData.get("mp3_url");

                    // (2a) Tải file mp3 về server -> trả về tên file
                    // Ví dụ: MatKetNoi-DuongDomic-16783113.mp3
                    String fileName = downloadMp3File(mp3Url);

                    if (fileName != null) {
                        // (2b) Tạo URL ảo để lưu vào DB
                        //      -> http://10.0.2.2:8080/api/music/MatKetNoi-DuongDomic-16783113.mp3
                        String sourceUrl = "http://10.0.2.2:8080/api/music/" + fileName;

                        // (2c) Gọi hàm addSong của bạn để lưu DB
                        songsService.addSong(title, artist, sourceUrl);

                        System.out.println("Đã thêm bài hát: " + title + " - " + sourceUrl);
                    }
                }
            }

            System.out.println("✅ Hoàn tất crawl & lưu nhạc lúc nửa đêm!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi autoCrawlAndAddSongs: " + e.getMessage());
        }
    }

    /**
     * Tải file mp3 về thư mục: C:\Users\bdtcl\Desktop\Flutter\music
     * Giữ nguyên tên file từ cuối URL.
     * @return fileName (VD: "MatKetNoi-DuongDomic-16783113.mp3") hoặc null nếu lỗi
     */
    private String downloadMp3File(String mp3Url) {
        try {
            URL url = new URL(mp3Url);

            // Lấy phần path -> /NhacCuaTui2057/MatKetNoi-DuongDomic-16783113.mp3
            String path = url.getPath();
            // Cắt chuỗi để lấy tên file -> MatKetNoi-DuongDomic-16783113.mp3
            String fileName = path.substring(path.lastIndexOf('/') + 1);

            // Tạo đường dẫn đích
            String downloadFolder = "C:/Users/bdtcl/Desktop/Flutter/music";
            File destFile = new File(downloadFolder, fileName);

            // Tải file (dùng Apache Commons IO)
            FileUtils.copyURLToFile(url, destFile);

            System.out.println("Đã tải về: " + destFile.getAbsolutePath());
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
