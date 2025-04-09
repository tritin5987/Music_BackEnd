package org.example.bts_backend;

import okhttp3.*;

import java.io.IOException;

public class NctOkHttpExample {
    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String url = "https://www.nhaccuatui.com/ajax/get-media-info?key1=...";
        String cookie = "nct_uuid=...; WEB_SESSION_ID=...; NCTNPLP=...";

        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", cookie)
                .header("Referer", "https://www.nhaccuatui.com/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("X-Requested-With", "XMLHttpRequest")
                .build();

        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }
}
