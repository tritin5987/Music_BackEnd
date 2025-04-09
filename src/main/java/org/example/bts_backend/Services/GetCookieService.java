package org.example.bts_backend.Services;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GetCookieService {

    private static final List<String> TARGET_COOKIES = Arrays.asList(
            "NCT_BALLOON_INDEX", "_ga", "_gid", "cto_bundle", "NCTNPLS",
            "_ga_CNWMB8R32F", "nct_uuid", "JSESSIONID"
    );

    /**
     * Hàm này tương đương hàm "main" cũ của bạn:
     * Mở trang, chờ 10s, lấy cookie, lưu cookie.txt
     */
    public void fetchCookiesAndSaveToFile() {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://www.nhaccuatui.com/bai-hat/mat-ket-noi-duong-domic.uJ8qLJzC9wH5.html");
            Thread.sleep(10_000);

            Set<Cookie> allCookies = driver.manage().getCookies();
            Map<String, String> cookieMap = new LinkedHashMap<>();

            for (Cookie ck : allCookies) {
                if (TARGET_COOKIES.contains(ck.getName())) {
                    cookieMap.put(ck.getName(), ck.getValue());
                }
            }

            // Chuyển map -> chuỗi cookie
            if (!cookieMap.isEmpty()) {
                StringBuilder cookieString = new StringBuilder();
                for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                    cookieString.append(entry.getKey())
                            .append("=")
                            .append(entry.getValue())
                            .append("; ");
                }
                // Xoá dư ; ở cuối
                cookieString.setLength(cookieString.length() - 2);

                // Ghi file cookie.txt
                String currentTime = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                try (FileWriter writer = new FileWriter("cookie.txt")) {
                    writer.write("thời gian: " + currentTime + "\n");
                    writer.write("cookie=" + cookieString + "\n");
                }
                System.out.println("✅ Cookie được lưu: " + cookieString);
            } else {
                System.out.println("⚠️ Không lấy được cookie nào!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
