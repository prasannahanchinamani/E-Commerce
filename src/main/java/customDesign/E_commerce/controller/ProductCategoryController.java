package customDesign.E_commerce.controller;

import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.entity.ProductCategory;
import customDesign.E_commerce.repository.ProductCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/categories")
public class ProductCategoryController {

    private final ProductCategoryRepository categoryRepository;

    public ProductCategoryController(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductCategory>>> getAllCategories() {

        List<ProductCategory> categories = categoryRepository.findByActiveTrue();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, categories));
    }
}