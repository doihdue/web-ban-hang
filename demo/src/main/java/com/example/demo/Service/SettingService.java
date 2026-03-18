package com.example.demo.Service;

import com.example.demo.Model.Setting;
import com.example.demo.Repository.SettingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SettingService {

    private final SettingRepository repo;

    public SettingService(SettingRepository repo) {
        this.repo = repo;
    }

    public List<Setting> findAll() {
        return repo.findAll();
    }

    public Setting upsert(String key, String value) {
        Setting s = repo.findByKeyName(key).orElse(new Setting(key, value));
        s.setValue(value);
        return repo.save(s);
    }

    public Map<String, String> getSettingsMap() {
        return repo.findAll().stream()
                .collect(Collectors.toMap(Setting::getKeyName, Setting::getValue, (a,b)->b));
    }

    public String get(String key) {
        return repo.findByKeyName(key).map(Setting::getValue).orElse(null);
    }
}
