package org.example.bts_backend;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class NctXmlMp3Extractor {

    public static void main(String[] args) {
        try {
            // 🔑 key1 từ trang HTML đã lấy được
            String key1 = "59f5f659fa360bfae4f207e77490890f";

            // 🎯 URL flash/xml
            String url = "https://www.nhaccuatui.com/flash/xml?html5=true&key1=" + key1;

            // ✅ Cookie thực tế copy từ Chrome DevTools hoặc lấy bằng Selenium
            String rawCookie = "NCT_BALLOON_INDEX=true; _ga=GA1.2.1118602385.1744195278; _gid=GA1.2.763663548.1744195278; cto_bundle=...; NCTNPLS=e66b31a9a808927a98132d2d6b53e41d; _ga_CNWMB8R32F=...; nct_uuid=8C1A508C499144EEA6917C97B2219130; JSESSIONID=61meh03d91f715ignkshm14cs";

            Map<String, String> cookies = new HashMap<>();
            for (String c : rawCookie.split(";")) {
                String[] pair = c.trim().split("=", 2);
                if (pair.length == 2) {
                    cookies.put(pair[0], pair[1]);
                }
            }

            // 🛰️ Gửi request XML
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .header("Referer", "https://www.nhaccuatui.com/bai-hat/mat-ket-noi-duong-domic.uJ8qLJzC9wH5.html")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/135.0.0.0 Safari/537.36")
                    .header("Accept-Encoding", "gzip, deflate, br, zstd")
                    .method(Connection.Method.GET)
                    .execute();

            String xml = response.body();

            // 🧠 Parse XML để lấy dữ liệu bài hát
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            org.w3c.dom.Document xmlDoc = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            xmlDoc.getDocumentElement().normalize();

            NodeList trackList = xmlDoc.getElementsByTagName("track");
            if (trackList.getLength() == 0) {
                System.out.println("❌ Không tìm thấy thẻ <track>");
                return;
            }

            Node track = trackList.item(0);
            if (track.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) track;

                String title = getElementValue(element, "title");
                String artist = getElementValue(element, "creator");
                String location = getElementValue(element, "location");
                String locationHQ = getElementValue(element, "locationHQ");

                System.out.println("🎶 Bài hát: " + title + " - " + artist);
                System.out.println("🎧 Link MP3 thường: " + location);
                System.out.println("🎧 Link MP3 HQ    : " + locationHQ);
            }

        } catch (Exception e) {
            System.out.println("❌ Lỗi: ");
            e.printStackTrace();
        }
    }

    private static String getElementValue(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) return "";
        return list.item(0).getTextContent().trim();
    }
}
