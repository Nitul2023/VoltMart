package com.voltmart.repository;

import com.voltmart.domain.entity.FilterDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface FilterDefinitionRepository extends JpaRepository<FilterDefinition, Long> {
    List<FilterDefinition> findByCategoryIdOrderBySortOrderAsc(Long categoryId);
    List<FilterDefinition> findByCategoryIdInOrderBySortOrderAsc(Collection<Long> categoryIds);
}
