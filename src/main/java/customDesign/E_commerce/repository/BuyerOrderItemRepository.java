package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.BuyerOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BuyerOrderItemRepository extends JpaRepository<BuyerOrderItem, Long> {
    List<BuyerOrderItem> findByOrderId(Long orderId);
}
