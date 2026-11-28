package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByBuyerEmail(String buyerEmail);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.buyerEmail = :email")
    Optional<Cart> findByBuyerEmailForUpdate(@Param("email") String email);
}
