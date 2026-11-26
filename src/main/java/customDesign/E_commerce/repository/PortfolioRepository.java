package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findBySellerEmail(String sellerEmail);
    Optional<Portfolio> findByIdAndSellerEmail(Long id, String sellerEmail);
}
