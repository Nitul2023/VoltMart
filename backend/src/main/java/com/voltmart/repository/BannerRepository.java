package com.voltmart.repository;

import com.voltmart.domain.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByActiveTrueAndPlacementOrderBySortOrderAsc(String placement);
}
