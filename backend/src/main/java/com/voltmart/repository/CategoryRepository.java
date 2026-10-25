package com.voltmart.repository;

import com.voltmart.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentId(Long parentId);
    List<Category> findByParentIsNullAndActiveTrueOrderBySortOrderAsc();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.sortOrder")
    List<Category> findTreeRoots();
}
