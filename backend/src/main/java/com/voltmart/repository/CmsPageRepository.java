package com.voltmart.repository;

import com.voltmart.domain.entity.CmsPage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CmsPageRepository extends JpaRepository<CmsPage, Long> {
    Optional<CmsPage> findBySlugAndPublishedTrue(String slug);
    Optional<CmsPage> findBySlug(String slug);
}
