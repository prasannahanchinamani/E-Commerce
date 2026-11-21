package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, Long> {
}