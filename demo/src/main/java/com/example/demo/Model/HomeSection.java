package com.example.demo.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "home_sections")
public class HomeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // slot 1..3 to indicate column
    @Column(nullable = false, unique = true)
    private Integer slot;

    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(length = 2000)
    private String content;

    private String buttonText;

    private String buttonLink;

    public HomeSection() {}

    public HomeSection(Integer slot, String name, String imageUrl, String content, String buttonText, String buttonLink) {
        this.slot = slot;
        this.name = name;
        this.imageUrl = imageUrl;
        this.content = content;
        this.buttonText = buttonText;
        this.buttonLink = buttonLink;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getButtonLink() {
        return buttonLink;
    }

    public void setButtonLink(String buttonLink) {
        this.buttonLink = buttonLink;
    }
}
