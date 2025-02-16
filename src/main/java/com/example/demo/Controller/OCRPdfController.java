package com.example.demo.Controller;

import com.example.demo.Service.OCRPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/ocr")
public class OCRPdfController {

    @Autowired
    private OCRPdfService ocrPdfService;

    @GetMapping("/extract-from-url")
    public String extractTextFromPdfUrl(@RequestParam("url") String pdfUrl) {
        return ocrPdfService.extractTextFromPdfUrl(pdfUrl);
    }
}
