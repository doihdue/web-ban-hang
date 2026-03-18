package com.example.demo.Repository;

import com.example.demo.Model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByKeyName(String keyName);
}
