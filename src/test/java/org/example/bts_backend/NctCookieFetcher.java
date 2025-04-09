package org.example.bts_backend;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NctCookieFetcher {

    private static final List<String> DEFAULT_COOKIES = List.of(
            "touchEnable", "playLoopAll", "playLoopOne", "autoPlayNext", "playShuffle"
    );

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));

        WebDriver driver = new ChromeDriver(options);

        try {
            String url = "https://www.nhaccuatui.com/bai-hat/mat-ket-noi-duong-domic.uJ8qLJzC9wH5.html";
            driver.get(url);

            // ✅ Đợi trang load hoàn tất
            Thread.sleep(10000);

            // ✅ Click vào nút play để tạo phiên session
            try {
                WebElement playBtn = driver.findElement(By.cssSelector(".icon-play, .btn-play"));
                playBtn.click();
                Thread.sleep(5000); // đợi cookie sinh ra
            } catch (Exception e) {
                System.out.println("⚠️ Không tìm thấy nút play hoặc không thể click.");
            }

            // ✅ Lấy toàn bộ cookie
            Set<Cookie> cookies = driver.manage().getCookies();
            Map<String, String> cookieMap = new LinkedHashMap<>();
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }

            // ✅ Tạo chuỗi cookie theo thứ tự
            StringBuilder cookieString = new StringBuilder();
            for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                cookieString.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            cookieString.setLength(cookieString.length() - 2); // bỏ dấu ; cuối

            // ✅ In chuỗi cookie đầy đủ
            System.out.println("🍪 COOKIE:");
            System.out.println(cookieString);

            // ✅ Ghi ra file
            try (FileWriter writer = new FileWriter("cookie.txt")) {
                writer.write("thời gian: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                writer.write("cookie=" + cookieString + "\n");
            }

            // ✅ Kiểm tra riêng cookie WEB_SESSION_ID
            if (cookieMap.containsKey("WEB_SESSION_ID")) {
                System.out.println("✅ Đã lấy được cookie WEB_SESSION_ID: " + cookieMap.get("WEB_SESSION_ID"));
            } else {
                System.out.println("❌ Cookie WEB_SESSION_ID vẫn chưa xuất hiện.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
