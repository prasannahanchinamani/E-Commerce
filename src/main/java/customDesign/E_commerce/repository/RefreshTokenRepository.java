package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.RefreshToken;
import customDesign.E_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from RefreshToken r where r.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
    Optional<RefreshToken> findBySessionId(String sessionId);
    @org.springframework.data.jpa.repository.Query("select r from RefreshToken r join fetch r.user where r.sessionId = :sessionId")
    Optional<RefreshToken> findSessionBySessionId(@Param("sessionId") String sessionId);
    void deleteByUser(User user);
}
