package org.example.bts_backend;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NctMp3Crawler {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Nhập URL bài hát
            System.out.print("🔗 Nhập link bài hát: ");
            String songUrl = scanner.nextLine();

            // B1: Truy cập trang bài hát
            Document doc = Jsoup.connect(songUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();
            String html = doc.html();

            // B2: Tìm key1
            Pattern pattern = Pattern.compile("player\\.peConfig\\.xmlURL\\s*=\\s*['\"]https://www\\.nhaccuatui\\.com/flash/xml\\?html5=true&key1=([a-zA-Z0-9]+)['\"]");
            Matcher matcher = pattern.matcher(html);

            if (!matcher.find()) {
                System.out.println("❌ Không tìm thấy key1.");
                return;
            }

            String key1 = matcher.group(1);
            System.out.println("🔑 key1: " + key1);

            // B3: Lấy IP
            String rawIpJson = Jsoup.connect("https://myip.nhaccuatui.com/getClientIP?jsoncallback=?")
                    .ignoreContentType(true)
                    .execute().body();
            String cleanedJson = rawIpJson.replaceAll("^.*?\\(", "").replaceAll("\\);?$", "");
            JSONObject ipObject = new JSONObject(cleanedJson);
            String ip = ipObject.getJSONObject("data").getString("ip");
            System.out.println("🌐 IP hiện tại: " + ip);

            // B4: Đọc cookie từ file
            String rawCookie = "";
            try {
                // Đọc toàn bộ nội dung (bỏ dòng đầu là thời gian nếu có)
                try (Scanner fileScanner = new Scanner(new java.io.File("cookie1.txt"))) {
                    if (fileScanner.hasNextLine()) fileScanner.nextLine(); // bỏ dòng "thời gian: ..."
                    if (fileScanner.hasNextLine()) {
                        rawCookie = fileScanner.nextLine().trim();
                    }
                }
            } catch (Exception ex) {
                System.out.println("❌ Không đọc được file cookie1.txt: " + ex.getMessage());
                return;
            }


            // B5: Chuyển cookie string -> Map
            Map<String, String> cookies = new HashMap<>();
            for (String cookie : rawCookie.split(";")) {
                String[] pair = cookie.trim().split("=", 2);
                if (pair.length == 2) {
                    cookies.put(pair[0], pair[1]);
                }
            }

            // B6: Gọi API lấy link MP3
            String apiUrl = "https://www.nhaccuatui.com/ajax/get-media-info?key1=" + key1 + "&ip=" + ip;

            Connection.Response response = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Referer", songUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .execute();

            String mediaJson = response.body();

            // DEBUG: in toàn bộ kết quả JSON
            System.out.println("📦 JSON trả về:");
            System.out.println(mediaJson);

            // Parse kết quả
            JSONObject mediaObject = new JSONObject(mediaJson);
            if (!mediaObject.has("data")) {
                System.out.println("❌ JSON không chứa trường 'data'.");
                return;
            }

            JSONObject data = mediaObject.getJSONObject("data");

            if (!data.has("location") || data.get("location").toString().isBlank()) {
                System.out.println("❌ Không tìm thấy link MP3 trong response.");
                return;
            }

            // Hiển thị dữ liệu cần thiết
            String title = data.optString("title", "(không rõ)");
            String artist = data.optString("singerTitle", "(không rõ)");
            String mp3Link = data.getString("location");

            System.out.println("🎶 Bài hát: " + title + " - " + artist);
            System.out.println("🎧 Link MP3: " + mp3Link);

        } catch (Exception e) {
            System.out.println("❌ Đã xảy ra lỗi:");
            e.printStackTrace();
        }
    }
}
