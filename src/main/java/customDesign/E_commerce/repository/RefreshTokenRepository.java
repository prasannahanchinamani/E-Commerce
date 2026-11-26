package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.RefreshToken;
import customDesign.E_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteByUser(User user);
}
