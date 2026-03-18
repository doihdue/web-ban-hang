package com.example.demo.config;

import com.example.demo.Service.SettingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SiteSettingDataLoader {

    @Bean
    public CommandLineRunner loadDefaultSettings(SettingService settingService) {
        return args -> {
            Map<String, String> defaults = new HashMap<>();
            defaults.put("aboutHtml", "<h2>CHÚNG TÔI LÀ AI?</h2><p>Giới thiệu mặc định về công ty...</p>");
            defaults.put("shippingPolicyHtml", "<h2>Chính sách giao hàng mặc định</h2><p>Nội dung chính sách mặc định...</p>");
            defaults.put("contactPhone", "0398693680");
            defaults.put("contactEmail", "dominhdue04@gmail.com");
            defaults.put("openingHours", "8:00 - 20:00 (Thứ 2 - Chủ Nhật)");
            defaults.put("mapEmbedUrl", "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d29822.109164875303!2d105.64849939299074!3d20.881574800299937!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31344ffa6d0081e1%3A0xe90a01da2102c93e!2zWMOzbSDEkOG7k25nIETDonU!5e0!3m2!1svi!2s!4v1741012853595!5m2!1svi!2s");
            defaults.put("mapLink", "https://www.google.com/maps/place/%C4%90%C3%B4ng+D%C3%A2u,+Ch%C6%B0%C6%A1ng+M%C3%BD,+H%C3%A0+N%E1%BB%96i");
            defaults.put("paymentBankName", "Vietcombank");
            defaults.put("paymentAccountNumber", "1234567890");
            defaults.put("paymentAccountName", "CONG TY TNHH DMD");
            defaults.put("paymentQrCodeUrl", "https://upload.wikimedia.org/wikipedia/commons/d/d0/QR_code_for_mobile_English_Wikipedia.svg");

            // Save defaults using existing SettingService (will not overwrite existing keys)
            Map<String, String> existing = settingService.getSettingsMap();
            for (Map.Entry<String, String> e : defaults.entrySet()) {
                if (!existing.containsKey(e.getKey())) {
                    settingService.upsert(e.getKey(), e.getValue());
                }
            }

            // If there is an existing banner with the default welcome text, clear it
            String welcomeSnippet = "Chào mừng bạn đến với DMD";
            String banner = existing.get("bannerHtml");
            if (banner != null && banner.contains(welcomeSnippet)) {
                settingService.upsert("bannerHtml", "");
            }
        };
    }
}
