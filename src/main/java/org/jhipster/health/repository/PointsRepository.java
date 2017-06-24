package org.jhipster.health.repository;

import org.jhipster.health.domain.Points;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the Points entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PointsRepository extends JpaRepository<Points,Long> {

    @Query("select points from Points points where points.user.login = ?#{principal.username}")
    Page<Points> findByUserIsCurrentUser(Pageable pageable);

    @Query("select points from Points points order by points.timestamp desc")
    Page<Points> findAllByOrderByTimestampDesc(Pageable pageable);

    List<Points> findAllByTimestampBetween(LocalDate firstDate, LocalDate secondDate);
}
