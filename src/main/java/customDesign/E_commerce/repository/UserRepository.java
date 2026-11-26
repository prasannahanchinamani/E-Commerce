package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.Role;
import customDesign.E_commerce.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :newPassword WHERE u.email = :email")
    String updatePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);

    Optional<User> findByMediatorCode(String mediatorCode);

    List<User> findByRole(Role role);

    List<User> findByMediatorIdAndRole(
            Long mediatorId,
            Role role
    );
    boolean existsByMediatorIdAndId(
            Long mediatorId,
            Long sellerId
    );
    boolean existsByNumber(Long number);   // use String if you change User.number to String

    Page<User> findByMediatorIdAndRole(Long mediatorId, Role role, Pageable pageable);

    Page<User> findByMediatorIdAndRoleAndStatus(Long mediatorId, Role role, String status, Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findByStatus(String status, Pageable pageable);
    Page<User> findByRoleAndStatus(Role role, String status, Pageable pageable);

    @Query("select u from User u where (:role is null or u.role = :role) and (:status is null or u.status = :status) and (:q is null or lower(u.name) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%')))")
    Page<User> searchAdmin(@Param("role") Role role,
                           @Param("status") String status,
                           @Param("q") String q,
                           Pageable pageable);
}
