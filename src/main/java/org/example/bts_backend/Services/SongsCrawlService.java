package org.example.bts_backend.Services;

import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.Repository.SongsRepository;
import org.example.bts_backend.dto.SongDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.text.Normalizer;
@Service
public class SongsCrawlService {
    @Autowired
    private SongsRepository songsRepository;

    @Autowired
    private LuceneIndexer luceneIndexer;

    @Autowired
    private LuceneSearcher luceneSearcher;

    public String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return "";
        }
        // Loại bỏ dấu
        String normalized = Normalizer.normalize(keyword, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        // Loại bỏ ký tự đặc biệt và chuyển thành chữ thường
        return normalized.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
    }


    // So sánh bài hát từ trang NCT với database
    public Map<String, String> compareSongsFromNCT() {
        Map<String, String> result = new LinkedHashMap<>();

        try {
            String playlistUrl = "https://www.nhaccuatui.com/playlist/top-100-nhac-tre-hay-nhat-various-artists.m3liaiy6vVsF.html";
            Document doc = Jsoup.connect(playlistUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            Elements items = doc.select("li[itemprop=tracks]");
            Set<String> dbTitles = new HashSet<>();
            for (Songs song : songsRepository.findAll()) {
                dbTitles.add(normalizeKeyword(song.getTitle()));
            }

            for (Element item : items) {
                String title = item.selectFirst("meta[itemprop=name]").attr("content").trim();
                String url = item.selectFirst("meta[itemprop=url]").attr("content").trim();
                String normalizedTitle = normalizeKeyword(title);

                if (dbTitles.contains(normalizedTitle)) {
                    result.put(title, "✅ Skip");
                } else {
                    result.put(title, "➕ Add " + url);
                }
            }
        } catch (Exception e) {
            result.put("❌ Lỗi", e.getMessage());
        }

        return result;
    }
}
