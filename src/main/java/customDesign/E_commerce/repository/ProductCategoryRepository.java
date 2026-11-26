package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByActiveTrue();
}