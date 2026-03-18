package com.example.demo.Service;

import com.example.demo.Model.HomeSection;
import com.example.demo.Repository.HomeSectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HomeSectionService {

    private final HomeSectionRepository repo;

    public HomeSectionService(HomeSectionRepository repo) {
        this.repo = repo;
    }

    public List<HomeSection> findAll() {
        return repo.findAll();
    }

    public Optional<HomeSection> findBySlot(Integer slot) {
        return repo.findBySlot(slot);
    }

    public HomeSection save(HomeSection s) {
        return repo.save(s);
    }

}
