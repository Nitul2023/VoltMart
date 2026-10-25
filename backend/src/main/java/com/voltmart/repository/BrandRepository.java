package com.voltmart.repository;

import com.voltmart.domain.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findBySlug(String slug);
    List<Brand> findByActiveTrueOrderByNameAsc();
}
