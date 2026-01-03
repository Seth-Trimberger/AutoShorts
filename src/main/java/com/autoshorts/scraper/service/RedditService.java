package com.autoshorts.scraper.service;

import com.autoshorts.scraper.Model.Story;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class RedditService {

    // Using a "User Agent" makes us look like a real browser so Reddit doesn't block us immediately
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    public Story scrapeStoryFromUrl(String url){
        try{
            Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(10000).get();

            //test
            Story story = new Story();

            //This extrats the CSS
            Element titleElement = doc.selectFirst("h1");
            String title = (titleElement != null) ? titleElement.text() : "No Title Found";

            Element contentElement = doc.selectFirst("div[id$='-post-rtjson-content']");
            if (contentElement == null) {
                contentElement = doc.selectFirst("div[slot='text-body']");
            }

            String content = (contentElement != null) ? contentElement.text() : "No Content Found";

            // 3. Set the data into our Story model
            story.setTitle(title);
            story.setContent(content);
            story.setRedditId(extractIdFromUrl(url));

            return story;

        }catch (Exception e){
            System.err.println("Scrapping failed for: " + url + " | Error: " + e.getMessage());
            return null;
        }
    }

    private String extractIdFromUrl(String url) {
        // Simple helper to get the ID (e.g., '1hjk56') from the URL
        try {
            String[] parts = url.split("/");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("comments") && i + 1 < parts.length) {
                    return parts[i + 1];
                }
            }
        } catch (Exception e) { return "unknown"; }
        return "unknown";
    }
}
