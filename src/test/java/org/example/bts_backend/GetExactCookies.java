package org.example.bts_backend;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GetExactCookies {

    private static final List<String> TARGET_COOKIES = Arrays.asList(
            "NCT_BALLOON_INDEX", "_ga", "_gid", "cto_bundle", "NCTNPLS",
            "_ga_CNWMB8R32F", "nct_uuid", "JSESSIONID"
    );

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            driver.get("https://www.nhaccuatui.com/bai-hat/mat-ket-noi-duong-domic.uJ8qLJzC9wH5.html");

            // Đợi lâu hơn để cookie được khởi tạo đầy đủ (có thể tương tác giả nếu cần)
            Thread.sleep(10000);

            Set<Cookie> allCookies = driver.manage().getCookies();
            Map<String, String> cookieMap = new LinkedHashMap<>();

            for (Cookie cookie : allCookies) {
                if (TARGET_COOKIES.contains(cookie.getName())) {
                    cookieMap.put(cookie.getName(), cookie.getValue());
                }
            }

            // Tạo chuỗi cookie để dùng trong request
            StringBuilder cookieString = new StringBuilder();
            for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                cookieString.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }

            if (!cookieMap.isEmpty()) {
                cookieString.setLength(cookieString.length() - 2); // xoá dấu ; cuối
                System.out.println("🍪 Cookie chính xác đã lấy:");
                System.out.println(cookieString);
                try (FileWriter writer = new FileWriter("cookie.txt")) {
                    String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    writer.write("thời gian: " + currentTime + "\n");
                    writer.write("cookie=" + cookieString.toString() + "\n");
                    System.out.println("✅ Cookie đã được lưu vào file cookie.txt");
                } catch (IOException e) {
                    System.out.println("❌ Lỗi khi ghi file: " + e.getMessage());
                }
            } else {
                System.out.println("⚠️ Không lấy được cookie nào trong danh sách yêu cầu.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
