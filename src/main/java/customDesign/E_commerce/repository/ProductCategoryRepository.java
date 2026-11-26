package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByActiveTrue();
    Page<ProductCategory> findByActiveTrue(Pageable pageable);
}