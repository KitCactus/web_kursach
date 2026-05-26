package ru.ssau.cafe.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramStorageService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramStorageService.class);
    private static final String API_BASE = "https://api.telegram.org/bot";
    private static final String FILE_BASE = "https://api.telegram.org/file/bot";

    @Value("${telegram.storage.bot-token}")
    private String botToken;

    @Value("${telegram.storage.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();
    // Кэш: file_id → байты, чтобы не дёргать Telegram при каждом запросе
    private final ConcurrentHashMap<String, byte[]> imageCache = new ConcurrentHashMap<>();

    public String uploadFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "photo.jpg";
        byte[] bytes = file.getBytes();

        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() { return originalName; }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId);
        body.add("photo", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                API_BASE + botToken + "/sendPhoto", request, Map.class);

        if (!Boolean.TRUE.equals(response.getBody().get("ok"))) {
            throw new IOException("Telegram API вернул ошибку при загрузке файла");
        }

        Map<?, ?> result = (Map<?, ?>) response.getBody().get("result");
        // sendPhoto возвращает массив размеров фото — берём наибольший (последний)
        java.util.List<?> photos = (java.util.List<?>) result.get("photo");
        Map<?, ?> largestPhoto = (Map<?, ?>) photos.get(photos.size() - 1);
        String fileId = (String) largestPhoto.get("file_id");

        // Кэшируем сразу — файл уже в памяти
        imageCache.put(fileId, bytes);
        logger.info("Файл '{}' загружен в Telegram, file_id: {}", originalName, fileId);
        return fileId;
    }

    public byte[] downloadFile(String fileId) throws IOException {
        byte[] cached = imageCache.get(fileId);
        if (cached != null) {
            return cached;
        }

        // Шаг 1: получаем file_path
        ResponseEntity<Map> getFileResponse = restTemplate.getForEntity(
                API_BASE + botToken + "/getFile?file_id=" + fileId, Map.class);

        if (!Boolean.TRUE.equals(getFileResponse.getBody().get("ok"))) {
            throw new IOException("Telegram API: не удалось получить file_path для " + fileId);
        }

        Map<?, ?> result = (Map<?, ?>) getFileResponse.getBody().get("result");
        String filePath = (String) result.get("file_path");

        // Шаг 2: скачиваем файл
        String downloadUrl = FILE_BASE + botToken + "/" + filePath;
        byte[] bytes = restTemplate.getForObject(downloadUrl, byte[].class);
        if (bytes == null) {
            throw new IOException("Telegram вернул пустой файл для " + fileId);
        }

        imageCache.put(fileId, bytes);
        logger.debug("Файл скачан из Telegram и закэширован, file_id: {}", fileId);
        return bytes;
    }
}
