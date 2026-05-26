package ru.ssau.cafe.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.cafe.service.TelegramStorageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.time.Duration;

@RestController
@RequestMapping("/api/images")
public class ImageProxyController {

    private final TelegramStorageService telegramStorageService;

    public ImageProxyController(TelegramStorageService telegramStorageService) {
        this.telegramStorageService = telegramStorageService;
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileId) {
        try {
            byte[] bytes = telegramStorageService.downloadFile(fileId);

            String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            if (contentType == null) contentType = "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
