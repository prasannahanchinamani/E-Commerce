package customDesign.E_commerce.controller;

import customDesign.E_commerce.dto.CategoryResponse;
import customDesign.E_commerce.dto.DesignTypeResponse;
import customDesign.E_commerce.dto.PagedApiResponse;
import customDesign.E_commerce.dto.PageInfo;
import customDesign.E_commerce.entity.ProductCategory;
import customDesign.E_commerce.repository.ProductCategoryRepository;
import customDesign.E_commerce.repository.DesignTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/categories")
public class ProductCategoryController {

    private final ProductCategoryRepository categoryRepository;
    private final DesignTypeRepository designTypeRepository;

    public ProductCategoryController(ProductCategoryRepository categoryRepository,
                                     DesignTypeRepository designTypeRepository) {
        this.categoryRepository = categoryRepository;
        this.designTypeRepository = designTypeRepository;
    }

    @GetMapping
    public PagedApiResponse<CategoryResponse> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        org.springframework.data.domain.Page<ProductCategory> result =
                categoryRepository.findByActiveTrue(pageable);
        return new PagedApiResponse<>(true, result.getContent().stream()
                .map(this::toCategory).toList(),
                new PageInfo(result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()));
    }

    @GetMapping("/{id}/design-types")
    public PagedApiResponse<DesignTypeResponse> getDesignTypes(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!categoryRepository.existsById(id)) {
            throw new customDesign.E_commerce.Exceptions.ResourceNotFoundException("Category not found");
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        org.springframework.data.domain.Page<customDesign.E_commerce.entity.DesignType> result =
                designTypeRepository.findByCategoryIdAndActiveTrue(id, pageable);
        return new PagedApiResponse<>(true, result.getContent().stream().map(type ->
                new DesignTypeResponse(type.getId(), id, type.getName(), java.util.List.of(), type.isActive())
        ).toList(), new PageInfo(result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()));
    }

    private CategoryResponse toCategory(ProductCategory category) {
        return new CategoryResponse(category.getId(), category.getName(),
                category.getSlug() == null ? "" : category.getSlug(),
                category.getImageUrl(), category.isActive());
    }
}