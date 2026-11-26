package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Product> findBySellerEmail(String sellerEmail);
    Optional<Product> findByIdAndSellerEmail(Long id, String sellerEmail);
}