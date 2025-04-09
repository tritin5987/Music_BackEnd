package org.example.bts_backend;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Cookie;
import io.github.bonigarcia.wdm.WebDriverManager;

public class CookieFetcher {

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            String url = "https://www.nhaccuatui.com/";
            driver.get(url);

            Thread.sleep(3000); // Đợi trang load và cookie được tạo

            System.out.println("🍪 Cookie hiện tại:");
            for (Cookie cookie : driver.manage().getCookies()) {
                System.out.println(cookie.getName() + "=" + cookie.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
