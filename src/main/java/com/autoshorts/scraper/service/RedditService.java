package com.autoshorts.scraper.service;

import com.autoshorts.scraper.Model.Story;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

public class RedditService {

    // Using a "User Agent" makes us look like a real browser so Reddit doesn't block us immediately
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    public Story scrapeStoryFromUrl(String url){
        try{
            Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(10000).get();

            Story story = new Story();

            //This extrats the CSS
            Element titleElement = doc.selectFirst("h1");

            return story;
        }catch (Exception e){
            System.err.println("Scrapping failed for: " + url + " | Error: " + e.getMessage());
            return null;
        }
    }
}
