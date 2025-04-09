package org.example.bts_backend.Services;

import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.Repository.SongsRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        String normalized = Normalizer.normalize(keyword, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        return normalized.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
    }

    // So sánh bài hát từ trang NCT với database và trả về kết quả dưới dạng JSON
    public Map<String, Object> compareSongsFromNCT() {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            String playlistUrl = "https://www.nhaccuatui.com/playlist/top-100-nhac-tre-hay-nhat-various-artists.m3liaiy6vVsF.html";
            Document doc = Jsoup.connect(playlistUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            Elements items = doc.select("li[itemprop=tracks]");  // Get song list
            Set<String> dbTitles = new HashSet<>();
            for (Songs song : songsRepository.findAll()) {
                dbTitles.add(normalizeKeyword(song.getTitle())); // Normalize title for comparison
            }

            int counter = 0;  // To keep track of the number of songs processed

            for (Element item : items) {
                // Process only the first 10 songs
                if (counter >= 10) {
                    break;
                }

                String title = item.selectFirst("meta[itemprop=name]").attr("content").trim();
                String artist = item.select(".name_singer").text().trim(); // Get artist name from the h4 tag
                String url = item.selectFirst("meta[itemprop=url]").attr("content").trim();
                String normalizedTitle = normalizeKeyword(title);

                Map<String, String> songData = new HashMap<>();

                if (dbTitles.contains(normalizedTitle)) {
                    songData.put("status", "Skip");
                } else {
                    songData.put("status", "Add");
                    // Retrieve MP3 link for the added song
                    try {
                        Document songPage = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                .get();

                        String html = songPage.html();
                        String key1 = null;
                        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                                        "player\\.peConfig\\.xmlURL\\s*=\\s*['\"]https://www\\.nhaccuatui\\.com/flash/xml\\?html5=true&key1=([a-zA-Z0-9]+)['\"]")
                                .matcher(html);
                        if (matcher.find()) {
                            key1 = matcher.group(1);
                        }

                        if (key1 != null) {
                            String apiUrl = "https://www.nhaccuatui.com/flash/xml?html5=true&key1=" + key1;

                            String rawCookie = "cookie=_ga_CNWMB8R32F=GS1.1.1744196538.1.0.1744196538.60.0.0; nct_uuid=8C1A508C499144EEA6917C97B2219130; NCTNPLS=e66b31a9a808927a98132d2d6b53e41d; _ga=GA1.2.2135771342.1744196538; JSESSIONID=14j9369paqemkojyi18u4lnyp; _gid=GA1.2.1912492853.1744196538";
                            Map<String, String> cookies = new HashMap<>();
                            for (String c : rawCookie.split(";")) {
                                String[] pair = c.trim().split("=", 2);
                                if (pair.length == 2) cookies.put(pair[0], pair[1]);
                            }

                            Connection.Response response = Jsoup.connect(apiUrl)
                                    .ignoreContentType(true)
                                    .cookies(cookies)
                                    .header("Referer", url)
                                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/135.0.0.0 Safari/537.36")
                                    .header("Accept-Encoding", "gzip, deflate, br, zstd")
                                    .method(Connection.Method.GET)
                                    .execute();

                            String xml = response.body();
                            org.w3c.dom.Document xmlDoc = DocumentBuilderFactory.newInstance()
                                    .newDocumentBuilder()
                                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
                            xmlDoc.getDocumentElement().normalize();
                            NodeList trackList = xmlDoc.getElementsByTagName("track");

                            if (trackList.getLength() > 0) {
                                Node track = trackList.item(0);
                                if (track.getNodeType() == Node.ELEMENT_NODE) {
                                    org.w3c.dom.Element trackElement = (org.w3c.dom.Element) track;
                                    String location = getElementValue(trackElement, "location");

                                    songData.put("mp3_url", location);
                                    songData.put("html_url", url);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        songData.put("status", "❌ Error getting mp3");
                        songData.put("error", ex.getMessage());
                    }
                }

                // Add artist info along with other details
                songData.put("artist", artist);  // Add the artist name

                result.put(title, songData);
                counter++;  // Increment counter after processing each song
            }
        } catch (Exception e) {
            result.put("❌ Error", e.getMessage());
        }

        return result;
    }

    private static String getElementValue(org.w3c.dom.Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) return "";
        return list.item(0).getTextContent().trim();
    }
}
