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
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

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

    // Đọc cookie từ file
    private Map<String, String> readCookiesFromFile(String filePath) {
        Map<String, String> cookies = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("cookie=")) {
                    String rawCookie = line.substring("cookie=".length());
                    for (String c : rawCookie.split(";")) {
                        String[] pair = c.trim().split("=", 2);
                        if (pair.length == 2) cookies.put(pair[0], pair[1]);
                    }
                    break; // Chỉ lấy dòng đầu tiên chứa cookie
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Không thể đọc cookie từ file: " + e.getMessage());
        }
        return cookies;
    }


    public Map<String, Object> compareSongsFromNCT() {
        Map<String, Object> result = new LinkedHashMap<>();

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

            int counter = 0;

            for (Element item : items) {
                if (counter >= 10) break;

                String title = item.selectFirst("meta[itemprop=name]").attr("content").trim();
                String artist = item.select(".name_singer").text().trim();
                String url = item.selectFirst("meta[itemprop=url]").attr("content").trim();
                String normalizedTitle = normalizeKeyword(title);

                Map<String, String> songData = new HashMap<>();

                if (dbTitles.contains(normalizedTitle)) {
                    songData.put("status", "Skip");
                } else {
                    songData.put("status", "Add");
                    try {
                        Document songPage = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                .get();

                        String html = songPage.html();
                        String key1 = null;
                        Matcher matcher = Pattern.compile(
                                        "player\\.peConfig\\.xmlURL\\s*=\\s*['\"]https://www\\.nhaccuatui\\.com/flash/xml\\?html5=true&key1=([a-zA-Z0-9]+)['\"]")
                                .matcher(html);
                        if (matcher.find()) {
                            key1 = matcher.group(1);
                        }

                        if (key1 != null) {
                            String apiUrl = "https://www.nhaccuatui.com/flash/xml?html5=true&key1=" + key1;

                            // Đọc cookie từ file
                            Map<String, String> cookies = readCookiesFromFile("cookie.txt");

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

                songData.put("artist", artist);
                result.put(title, songData);
                counter++;
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
