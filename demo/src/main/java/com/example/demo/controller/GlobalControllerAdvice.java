package com.example.demo.controller;

import com.example.demo.Model.Category;
import com.example.demo.Service.CategoryService;
import com.example.demo.Service.SettingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SettingService settingService;

    @ModelAttribute("allCategories")
    public List<Category> populateCategories() {
        return categoryService.getAllCategories();
    }

    @ModelAttribute("siteSettings")
    public Map<String,String> populateSiteSettings() {
        return settingService.getSettingsMap();
    }

    // Add currentUri and activeMenu to model for menu highlighting
    @ModelAttribute
    public void addGlobalAttributes(HttpServletRequest request, Model model) {
        String uri = request.getRequestURI();
        model.addAttribute("currentUri", uri);

        String activeMenu = "";
        if (uri == null) {
            activeMenu = "";
        } else if (uri.startsWith("/admin/dashboard")) {
            activeMenu = "dashboard";
        } else if (uri.contains("admin-product") || uri.startsWith("/admin-product") || uri.startsWith("/admin/product")) {
            activeMenu = "products";
        } else if (uri.startsWith("/admin/categories") || uri.startsWith("/categories")) {
            activeMenu = "categories";
        } else if (uri.startsWith("/admin/home-sections")) {
            activeMenu = "homeSections";
        } else if (uri.startsWith("/orders") || uri.startsWith("/order-detail")) {
            activeMenu = "orders";
        } else if (uri.startsWith("/customers") || uri.startsWith("/customer-")) {
            activeMenu = "customers";
        } else if (uri.contains("/admin/settings") || uri.startsWith("/admin/settings")) {
            activeMenu = "settings";
        }

        model.addAttribute("activeMenu", activeMenu);
    }
}
