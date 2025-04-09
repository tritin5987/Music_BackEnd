package org.example.bts_backend;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v122.network.Network;
import org.openqa.selenium.devtools.v122.network.model.Response;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Optional;

public class CdpCookieTracker {
    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
        ChromeDriver driver = new ChromeDriver(options);
        DevTools devTools = driver.getDevTools();

        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        devTools.addListener(Network.responseReceived(), response -> {
            Response res = response.getResponse();
            if (res.getHeaders().toString().contains("Set-Cookie")) {
                System.out.println("➡️ URL: " + res.getUrl());
                System.out.println("🍪 Set-Cookie: " + res.getHeaders().get("Set-Cookie"));
            }
        });

        driver.get("https://www.nhaccuatui.com/");
    }
}