package ru.ssau.cafe.service;

import ru.ssau.cafe.dto.MenuItemDto;
import ru.ssau.cafe.entity.MenuItem;
import ru.ssau.cafe.entity.User;
import ru.ssau.cafe.exception.ResourceNotFoundException;
import ru.ssau.cafe.repository.MenuItemRepository;
import ru.ssau.cafe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Autowired
    public MenuService(MenuItemRepository menuItemRepository, UserRepository userRepository) {
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    private MenuItemDto convertToDto(MenuItem item) {
        return new MenuItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCategory(),
                item.getSubcategory(),
                item.getPhotoFileId(),
                item.getIsAvailable(),
                item.getIsHidden()
        );
    }

    private List<MenuItemDto> convertToDtoList(List<MenuItem> items) {
        return items.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<MenuItemDto> getAllMenuItems() {
        return convertToDtoList(menuItemRepository.findAll());
    }

    public List<MenuItemDto> getAvailableMenuItems() {
        return convertToDtoList(menuItemRepository.findByIsAvailableTrue());
    }

    public List<MenuItemDto> getMenuItemsForBot() {
        return convertToDtoList(menuItemRepository.findAvailableForBot());
    }

    public List<MenuItemDto> getMenuItemsByCategory(String category) {
        return convertToDtoList(menuItemRepository.findByCategory(category));
    }

    public List<MenuItemDto> searchMenuItems(String query) {
        return convertToDtoList(menuItemRepository.searchByName(query));
    }

    public MenuItemDto getMenuItemById(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        return convertToDto(item);
    }

    @Transactional
    public MenuItemDto createMenuItem(MenuItemDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("Название не может быть пустым");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Цена должна быть больше 0");
        }
        if (dto.getCategory() == null || dto.getCategory().trim().isEmpty()) {
            throw new RuntimeException("Категория обязательна");
        }
        MenuItem item = new MenuItem(
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getCategory(),
                dto.getSubcategory()
        );
        item.setPhotoFileId(dto.getPhotoFileId());
        item.setIsAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true);
        item.setIsHidden(dto.getIsHidden() != null ? dto.getIsHidden() : false);
        item.setCreatedBy(user);

        MenuItem savedItem = menuItemRepository.save(item);
        return convertToDto(savedItem);
    }

    @Transactional
    public MenuItemDto updateMenuItem(Long id, MenuItemDto dto) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCategory(dto.getCategory());
        item.setSubcategory(dto.getSubcategory());
        if (dto.getPhotoFileId() != null) {
            item.setPhotoFileId(dto.getPhotoFileId());
        }
        item.setIsAvailable(dto.getIsAvailable());
        item.setIsHidden(dto.getIsHidden());

        MenuItem updatedItem = menuItemRepository.save(item);
        return convertToDto(updatedItem);
    }

    @Transactional
    public MenuItemDto updateAvailability(Long id, Boolean isAvailable) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        item.setIsAvailable(isAvailable);
        MenuItem updatedItem = menuItemRepository.save(item);
        return convertToDto(updatedItem);
    }

    @Transactional
    public MenuItemDto updateVisibility(Long id, Boolean isHidden) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        item.setIsHidden(isHidden);
        MenuItem updatedItem = menuItemRepository.save(item);
        return convertToDto(updatedItem);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        menuItemRepository.delete(item);
    }
}