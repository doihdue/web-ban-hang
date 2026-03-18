package com.example.demo.controller;

import com.example.demo.Model.Setting;
import com.example.demo.Service.SettingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
public class AdminSettingController {

    private final SettingService service;

    public AdminSettingController(SettingService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(value = "success", required = false) String success, Model model) {
        model.addAttribute("settings", service.findAll());
        model.addAttribute("settingsMap", service.getSettingsMap());
        model.addAttribute("success", success != null);
        return "admin/settings";
    }

    @GetMapping("/form")
    public String form(Model model) {
        // Provide the same settings map and success flag
        model.addAttribute("settings", service.findAll());
        model.addAttribute("settingsMap", service.getSettingsMap());
        model.addAttribute("success", false);
        return "admin/settings-form"; // new template
    }

    @PostMapping("/save")
    public String save(@RequestParam Map<String, String> allParams) {
        String newKey = allParams.remove("newKey");
        String newValue = allParams.remove("newValue");

        allParams.forEach((k, v) -> {
            if (k == null || k.isBlank()) return;
            service.upsert(k, v);
        });

        if (newKey != null && !newKey.isBlank()) {
            service.upsert(newKey.trim(), newValue == null ? "" : newValue);
        }
        return "redirect:/admin/settings?success";
    }

    @PostMapping
    public String saveForm(@RequestParam Map<String, String> allParams) {
        // expected keys from settings-form: aboutHtml, shippingPolicyHtml, bannerHtml, contactPhone, contactEmail, mapEmbedHtml
        // Reuse the same upsert mechanism in SettingService
        allParams.forEach((k, v) -> {
            if (k == null || k.isBlank()) return;
            service.upsert(k, v == null ? "" : v);
        });
        return "redirect:/admin/settings/form?success";
    }
}
