package ru.ssau.cafe.dto;

import java.math.BigDecimal;

public class MenuItemDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String subcategory;
    private String photoFileId;
    private Boolean isAvailable;
    private Boolean isHidden;

    public MenuItemDto() {}

    public MenuItemDto(Long id, String name, String description, BigDecimal price, String category,
                       String subcategory, String photoFileId, Boolean isAvailable, Boolean isHidden) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.subcategory = subcategory;
        this.photoFileId = photoFileId;
        this.isAvailable = isAvailable;
        this.isHidden = isHidden;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public String getPhotoFileId() { return photoFileId; }
    public Boolean getIsAvailable() { return isAvailable; }
    public Boolean getIsHidden() { return isHidden; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public void setPhotoFileId(String photoFileId) { this.photoFileId = photoFileId; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }
}