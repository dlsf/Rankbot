package net.seliba.rankbot.youtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class NewVideoFetcher {

  public static String getLatestVideoData() throws IOException {
    List<String> list =
        getURLSource("https://www.youtube.com/channel/UCoZtjPCrPugVD9Q7Fb3tJ6w/videos");
    return list.stream()
        .filter(line -> line.startsWith("data-context-item-id"))
        .findFirst()
        .orElseThrow(NoSuchElementException::new)
        .split("\"")[1];
  }

  private static List<String> getURLSource(String url) throws IOException {
    URL urlObject = new URL(url);
    URLConnection urlConnection = urlObject.openConnection();
    urlConnection.setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

    return toList(urlConnection.getInputStream());
  }

  private static List<String> toList(InputStream inputStream) throws IOException {
    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String inputLine;
      List<String> input = new ArrayList<>();
      while ((inputLine = bufferedReader.readLine()) != null) {
        input.add(inputLine);
      }

      return input;
    }
  }
}
