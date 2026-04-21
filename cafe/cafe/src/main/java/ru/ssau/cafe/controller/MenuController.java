package ru.ssau.cafe.controller;

import ru.ssau.cafe.dto.MenuItemDto;
import ru.ssau.cafe.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    private final MenuService menuService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                logger.warn("Upload attempt with empty file");
                return ResponseEntity.badRequest().body("Файл пустой");
            }
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "photo";
            String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String filename = UUID.randomUUID() + "_" + safeName;

            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
            logger.info("Uploading file: {} to directory: {}", filename, uploadDir.toAbsolutePath());
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            logger.info("File uploaded successfully: {}", filename);

            return ResponseEntity.ok(filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    @DeleteMapping("/upload/{filename}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String filename) {
        try {
            Path file = Paths.get(System.getProperty("user.dir"), "uploads").resolve(filename);
            Files.deleteIfExists(file);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<List<MenuItemDto>> getAllMenuItems() {
        return ResponseEntity.ok(menuService.getAllMenuItems());
    }

    @GetMapping("/available")
    public ResponseEntity<List<MenuItemDto>> getAvailableMenuItems() {
        return ResponseEntity.ok(menuService.getAvailableMenuItems());
    }

    @GetMapping("/bot")
    public ResponseEntity<List<MenuItemDto>> getMenuForBot() {
        return ResponseEntity.ok(menuService.getMenuItemsForBot());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<MenuItemDto>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(menuService.getMenuItemsByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MenuItemDto>> search(@RequestParam String q) {
        return ResponseEntity.ok(menuService.searchMenuItems(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemDto> getMenuItem(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getMenuItemById(id));
    }

    @PostMapping
    public ResponseEntity<MenuItemDto> createMenuItem(@RequestBody MenuItemDto dto, @RequestParam Long userId) {
        MenuItemDto created = menuService.createMenuItem(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemDto> updateMenuItem(@PathVariable Long id, @RequestBody MenuItemDto dto) {
        return ResponseEntity.ok(menuService.updateMenuItem(id, dto));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<MenuItemDto> updateAvailability(@PathVariable Long id, @RequestParam Boolean available) {
        return ResponseEntity.ok(menuService.updateAvailability(id, available));
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<MenuItemDto> updateVisibility(@PathVariable Long id, @RequestParam Boolean hidden) {
        return ResponseEntity.ok(menuService.updateVisibility(id, hidden));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories/all")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(menuService.getAllCategories());
    }

    @GetMapping("/subcategories/all")
    public ResponseEntity<List<String>> getAllSubcategories() {
        return ResponseEntity.ok(menuService.getAllSubcategories());
    }

    @GetMapping("/subcategories/by-category")
    public ResponseEntity<List<String>> getSubcategoriesByCategory(@RequestParam String category) {
        return ResponseEntity.ok(menuService.getSubcategoriesByCategory(category));
    }
}