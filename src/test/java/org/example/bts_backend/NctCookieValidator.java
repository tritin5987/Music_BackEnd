package org.example.bts_backend;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NctCookieValidator {

    private static final List<String> REQUIRED_COOKIES = Arrays.asList(
            "nct_uuid", "ai_user", "nctads_ck", "_cc_id", "_ga", "touchEnable", "playLoopAll",
            "playLoopOne", "_ga_CNWMB8R32F", "autoPlayNext", "__utma", "__utmz", "playShuffle",
            "cto_bundle", "WEB_SESSION_ID", "NCTNPLP", "NCTNPLS", "_ga_S6SVT63XQS", "ai_session"
    );

    private static final Map<String, String> DEFAULT_COOKIE_VALUES = new HashMap<>() {{
        put("touchEnable", "true");
        put("playLoopAll", "true");
        put("playLoopOne", "false");
        put("autoPlayNext", "true");
        put("playShuffle", "false");
    }};

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            String url = "https://www.nhaccuatui.com/bai-hat/mat-ket-noi-duong-domic.uJ8qLJzC9wH5.html";
            driver.get(url);

            // Scroll để kích hoạt lazy load
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

            // Bấm nút play nếu có thể
            try {
                WebElement playBtn = driver.findElement(By.cssSelector(".btn-play, .icon-play"));
                playBtn.click();
                Thread.sleep(5000); // đợi cookie sinh ra
            } catch (Exception ignored) {}

            // Đợi thêm để cookie được khởi tạo đầy đủ
            Thread.sleep(7000); // tổng cộng ~12s

            Set<Cookie> cookies = driver.manage().getCookies();
            Map<String, String> cookieMap = new HashMap<>();
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }

            // Debug: in toàn bộ cookie lấy được
            System.out.println("📦 Cookie thật sự lấy được:");
            for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }

            // Gán mặc định cho các cookie tùy chọn
            List<String> autoFilled = new ArrayList<>();
            for (String required : REQUIRED_COOKIES) {
                if (!cookieMap.containsKey(required)) {
                    if (DEFAULT_COOKIE_VALUES.containsKey(required)) {
                        cookieMap.put(required, DEFAULT_COOKIE_VALUES.get(required));
                        autoFilled.add(required);
                    }
                }
            }

            // Tạo chuỗi cookie
            StringBuilder cookieString = new StringBuilder();
            for (String key : REQUIRED_COOKIES) {
                if (cookieMap.containsKey(key)) {
                    cookieString.append(key).append("=").append(cookieMap.get(key)).append("; ");
                }
            }
            if (cookieString.length() >= 2)
                cookieString.setLength(cookieString.length() - 2); // bỏ dấu "; "

            // Ghi file
            try (FileWriter writer = new FileWriter("cookie.txt")) {
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                writer.write("thời gian: " + currentTime + "\n");
                writer.write("cookie=" + cookieString.toString() + "\n");
                System.out.println("✅ Cookie đã được lưu vào file cookie.txt");
            } catch (IOException e) {
                System.out.println("❌ Lỗi khi ghi file: " + e.getMessage());
            }

            // In cookie bị thiếu (nếu có)
            List<String> missing = new ArrayList<>();
            for (String required : REQUIRED_COOKIES) {
                if (!cookieMap.containsKey(required)) {
                    missing.add(required);
                }
            }
            if (!missing.isEmpty()) {
                System.out.println("⚠️ Thiếu cookie sau dù đã tương tác:");
                missing.forEach(c -> System.out.println("- " + c));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
