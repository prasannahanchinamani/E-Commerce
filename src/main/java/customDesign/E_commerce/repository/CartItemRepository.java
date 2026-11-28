package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByIdAndCartBuyerEmail(Long id, String buyerEmail);
    List<CartItem> findByCartBuyerEmail(String buyerEmail);
}
