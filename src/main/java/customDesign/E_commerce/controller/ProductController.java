package customDesign.E_commerce.controller;

import customDesign.E_commerce.dto.ListingResponse;
import customDesign.E_commerce.dto.PageInfo;
import customDesign.E_commerce.dto.PagedApiResponse;
import customDesign.E_commerce.entity.Product;
import customDesign.E_commerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/listings")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public PagedApiResponse<ListingResponse> searchListings(
            @RequestParam(required=false) Long categoryId,
            @RequestParam(required=false) Long designTypeId,
            @RequestParam(required=false) String q,
            @RequestParam(required=false) java.math.BigDecimal minPrice,
            @RequestParam(required=false) java.math.BigDecimal maxPrice,
            @RequestParam(required=false) Long shopId,
            @RequestParam(required=false, defaultValue="created_desc") String sort,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        org.springframework.data.domain.Page<Product> result = productRepository.searchListings(
                categoryId, designTypeId, q, minPrice, maxPrice, pageable);
        return new PagedApiResponse<>(true, result.getContent().stream().map(this::toListing).toList(),
                new PageInfo(result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()));
    }

    @GetMapping("/{id}")
    public customDesign.E_commerce.dto.ApiResponse<ListingResponse> getListing(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new customDesign.E_commerce.Exceptions.ResourceNotFoundException("Listing not found"));
        return new customDesign.E_commerce.dto.ApiResponse<>(true, toListing(product));
    }

    private ListingResponse toListing(Product product) {
        java.util.List<String> images = product.getImageUrl() == null || product.getImageUrl().isBlank()
                ? java.util.List.of() : java.util.List.of(product.getImageUrl());
        ListingResponse.SellerSummary seller = new ListingResponse.SellerSummary(
                null, product.getSellerEmail());
        return new ListingResponse(product.getId(), product.getName(),
                product.getCategory() == null ? null : product.getCategory().getId(),
                product.getDesignTypeId(), product.getPrice(), product.getBulkPrice(),
                product.getMinBulkQty(), product.getDaysToMake(), images, seller, null,
                product.isActive() ? "ACTIVE" : "HIDDEN");
    }
}