package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Product> findBySellerEmail(String sellerEmail);
    Optional<Product> findByIdAndSellerEmail(Long id, String sellerEmail);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select p from Product p
            where p.active = true
              and (:categoryId is null or p.category.id = :categoryId)
              and (:designTypeId is null or p.designTypeId = :designTypeId)
              and (:query is null or lower(p.name) like lower(concat('%', :query, '%'))
                   or lower(p.description) like lower(concat('%', :query, '%')))
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            """)
    Page<Product> searchListings(@Param("categoryId") Long categoryId,
                                 @Param("designTypeId") Long designTypeId,
                                 @Param("query") String query,
                                 @Param("minPrice") java.math.BigDecimal minPrice,
                                 @Param("maxPrice") java.math.BigDecimal maxPrice,
                                 Pageable pageable);
}