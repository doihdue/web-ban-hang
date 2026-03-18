package com.example.demo.Repository;

import com.example.demo.Model.HomeSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomeSectionRepository extends JpaRepository<HomeSection, Long> {
    Optional<HomeSection> findBySlot(Integer slot);
}
