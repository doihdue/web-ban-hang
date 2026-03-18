package com.example.demo.config;

import com.example.demo.Model.Category;
import com.example.demo.Model.Role;
import com.example.demo.Model.User;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Tạo role nếu chưa tồn tại
        Role adminRole;
        try {
            adminRole = roleRepository.findByName("admin")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("admin").build()));
        } catch (Exception ex) {
            // Có thể xảy ra nếu bảng chưa sẵn sàng; cố gắng tạo role mà không truy vấn trước
            adminRole = Role.builder().name("admin").build();
            try {
                roleRepository.save(adminRole);
            } catch (Exception e) {
                // nếu vẫn lỗi, ghi log và tiếp tục (không ném ngoại lệ để app không crash)
                System.err.println("Warning: cannot create admin role at startup: " + e.getMessage());
            }
        }

        try {
            roleRepository.findByName("user")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("user").build()));
        } catch (Exception ex) {
            try {
                roleRepository.save(Role.builder().name("user").build());
            } catch (Exception e) {
                System.err.println("Warning: cannot create user role at startup: " + e.getMessage());
            }
        }

        // 2. Tạo tài khoản admin mặc định nếu chưa tồn tại
        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@admin.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFname("Admin");
            admin.setLname("System");
            admin.setPhone("0000000000");
            admin.setRole(adminRole);
            admin.setActive(true);
            admin.setRegistrationDate(new Date());
            userRepository.save(admin);
            System.out.println("=== Tài khoản admin đã được tạo: admin@admin.com / admin123 ===");
        }

        // 3. Tạo danh mục mặc định nếu chưa có danh mục nào
        if (categoryRepository.count() == 0) {
            categoryRepository.save(Category.builder().name("Hạt Dinh Dưỡng").build());
            categoryRepository.save(Category.builder().name("Trái Cây Sấy").build());
            categoryRepository.save(Category.builder().name("Nguyên Liệu Làm Bánh").build());
            categoryRepository.save(Category.builder().name("Đồ Ăn Vặt").build());
            System.out.println("=== Đã tạo các danh mục mặc định ===");
        }
    }
}
