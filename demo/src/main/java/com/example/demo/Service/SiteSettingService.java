package com.example.demo.Service;

import com.example.demo.Model.SiteSetting;
import com.example.demo.Repository.SiteSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SiteSettingService {

    private final SiteSettingRepository repository;

    public SiteSettingService(SiteSettingRepository repository) {
        this.repository = repository;
    }

    public Map<String, String> getAll() {
        List<SiteSetting> list = repository.findAll();
        Map<String, String> map = new HashMap<>();
        for (SiteSetting s : list) {
            map.put(s.getKey(), s.getValue());
        }
        return map;
    }

    public String get(String key) {
        return repository.findByKey(key).map(SiteSetting::getValue).orElse(null);
    }

    public void save(String key, String value) {
        SiteSetting setting = repository.findByKey(key).orElseGet(() -> new SiteSetting(key, value));
        setting.setValue(value);
        repository.save(setting);
    }

    public void saveAll(Map<String, String> values) {
        for (Map.Entry<String, String> e : values.entrySet()) {
            save(e.getKey(), e.getValue());
        }
    }
}
