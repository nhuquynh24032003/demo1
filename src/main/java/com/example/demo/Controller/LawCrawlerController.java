package com.example.demo.Controller;

import com.example.demo.Service.LawCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LawCrawlerController {

    @Autowired
    private LawCrawlerService lawCrawlerService;

    @GetMapping("/crawl")
    public String startCrawling() {
        lawCrawlerService.crawlLawList();
        return "Quá trình crawl đã hoàn tất!";
    }
}