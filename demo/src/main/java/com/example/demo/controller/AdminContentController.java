package com.example.demo.controller;

import com.example.demo.Service.SettingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/content-settings")
public class AdminContentController {

    private final SettingService settingService;

    public AdminContentController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public String editContent(@RequestParam(value = "success", required = false) String success, Model model) {
        model.addAttribute("settingsMap", settingService.getSettingsMap());
        model.addAttribute("success", success != null);
        return "admin/content-settings";
    }

    @PostMapping("/save")
    public String saveContent(@RequestParam(value = "aboutHtml", required = false) String aboutHtml,
                              @RequestParam(value = "shippingPolicyHtml", required = false) String shippingPolicyHtml) {
        if (aboutHtml != null) {
            settingService.upsert("aboutHtml", aboutHtml);
        }
        if (shippingPolicyHtml != null) {
            settingService.upsert("shippingPolicyHtml", shippingPolicyHtml);
        }
        return "redirect:/admin/content-settings?success";
    }
}
