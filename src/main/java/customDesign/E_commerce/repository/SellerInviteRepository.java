package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.InviteStatus;
import customDesign.E_commerce.entity.seller.SellerInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerInviteRepository extends JpaRepository<SellerInvite, Long> {
    Optional<SellerInvite> findByToken(String token);
    boolean existsByMediatorIdAndEmailAndStatus(Long mediatorId, String email, InviteStatus status);
    boolean existsByMediatorIdAndPhoneAndStatus(Long mediatorId, String phone, InviteStatus status);
    List<SellerInvite> findByMediatorIdOrderByCreatedAtDesc(Long mediatorId);
}