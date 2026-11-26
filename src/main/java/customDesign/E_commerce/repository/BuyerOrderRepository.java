package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.BuyerOrder;
import customDesign.E_commerce.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuyerOrderRepository extends JpaRepository<BuyerOrder, Long> {
    Page<BuyerOrder> findByBuyerEmail(String buyerEmail, Pageable pageable);
    Page<BuyerOrder> findByBuyerEmailAndStatus(String buyerEmail, OrderStatus status, Pageable pageable);
    Optional<BuyerOrder> findByIdAndBuyerEmail(Long id, String buyerEmail);
}
