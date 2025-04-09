package org.example.bts_backend;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.*;

public class NctCookieExtractor {

    private static final List<String> REQUIRED_COOKIES = Arrays.asList(
            "nct_uuid", "ai_user", "nctads_ck", "_cc_id", "_ga", "touchEnable", "playLoopAll",
            "playLoopOne", "_ga_CNWMB8R32F", "autoPlayNext", "__utma", "__utmz", "playShuffle",
            "cto_bundle", "WEB_SESSION_ID", "NCTNPLP", "NCTNPLS", "_ga_S6SVT63XQS", "ai_session"
    );

    private static final Map<String, String> DEFAULT_COOKIE_VALUES = new HashMap<>() {{
        put("touchEnable", "true");
        put("playLoopAll", "false");
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

            System.out.println("⏳ Đang chờ cookie WEB_SESSION_ID xuất hiện...");

            // Vòng lặp chờ vô hạn đến khi cookie WEB_SESSION_ID xuất hiện
            while (driver.manage().getCookieNamed("WEB_SESSION_ID") == null) {
                Thread.sleep(500);
            }

            System.out.println("✅ Đã phát hiện WEB_SESSION_ID.");

            // Lấy cookie
            Set<Cookie> cookies = driver.manage().getCookies();
            Map<String, String> cookieMap = new HashMap<>();
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }

            // Gán các cookie mặc định nếu thiếu
            for (String required : REQUIRED_COOKIES) {
                cookieMap.putIfAbsent(required, DEFAULT_COOKIE_VALUES.getOrDefault(required, "placeholder"));
            }

            // Kiểm tra còn thiếu cookie không có default không
            List<String> missing = new ArrayList<>();
            for (String required : REQUIRED_COOKIES) {
                if (!cookieMap.containsKey(required)) {
                    missing.add(required);
                }
            }

            if (!missing.isEmpty()) {
                System.out.println("❌ Thiếu cookie bắt buộc:");
                missing.forEach(c -> System.out.println("- " + c));
            } else {
                // In kết quả
                StringBuilder cookieHeader = new StringBuilder();
                for (String key : REQUIRED_COOKIES) {
                    cookieHeader.append(key).append("=").append(cookieMap.get(key)).append("; ");
                }
                String cookieResult = cookieHeader.toString().trim();
                if (cookieResult.endsWith(";")) {
                    cookieResult = cookieResult.substring(0, cookieResult.length() - 1);
                }
                System.out.println("\n🍪 Cookie header:");
                System.out.println(cookieResult);
            }

        } catch (Exception e) {
            System.out.println("❌ Lỗi khi lấy cookie: " + e.getMessage());
        }

        // 👉 KHÔNG đóng trình duyệt nếu bạn còn cần thao tác tiếp
        // driver.quit(); // bạn có thể tự tắt tay hoặc gọi sau khi xác nhận xong
    }
}
