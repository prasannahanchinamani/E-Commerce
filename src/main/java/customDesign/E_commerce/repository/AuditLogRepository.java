package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("""
      select a from AuditLog a where
      (:actorId is null or a.actorId = :actorId) and
      (:action is null or a.action = :action) and
      (:from is null or a.at >= :from) and
      (:to is null or a.at <= :to)
    """)
    Page<AuditLog> search(@Param("actorId") Long actorId,
                          @Param("action") String action,
                          @Param("from") Instant from,
                          @Param("to") Instant to,
                          Pageable pageable);
}
