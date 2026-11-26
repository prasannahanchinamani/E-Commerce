package customDesign.E_commerce.controller;

import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.entity.Product;
import customDesign.E_commerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(
            @PathVariable Long categoryId) {

        List<Product> products =
                productRepository.findByCategoryIdAndActiveTrue(categoryId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, products));
    }
}