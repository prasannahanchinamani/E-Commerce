package customDesign.E_commerce.controller.Buyer;

import customDesign.E_commerce.dto.AddressRequest;
import customDesign.E_commerce.dto.AddressResponse;
import customDesign.E_commerce.dto.CreateOrderRequest;
import customDesign.E_commerce.dto.OrderResponse;
import customDesign.E_commerce.dto.PagedApiResponse;
import customDesign.E_commerce.dto.PageInfo;
import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.entity.Address;
import customDesign.E_commerce.service.buyer.BuyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer")
@PreAuthorize("hasRole('BUYER')")
public class BuyerController {

    @Autowired
    private BuyerService buyerService;

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(Authentication authentication) {
        List<AddressResponse> addresses = buyerService.getAddresses(authentication.getName()).stream()
                .map(this::toAddressResponse).toList();
        return ResponseEntity.ok(new ApiResponse<>(true, addresses));
    }
    @PutMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        Address address = buyerService.updateAddress(authentication.getName(), id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, toAddressResponse(address)));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @PathVariable Long id,
            Authentication authentication) {
        buyerService.deleteAddress(authentication.getName(), id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Address deleted successfully"));
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @jakarta.validation.Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, buyerService.createOrder(authentication.getName(), request)));
    }

    @GetMapping("/orders")
    public PagedApiResponse<OrderResponse> listOrders(
            @RequestParam(required=false) String status,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            Authentication authentication) {
        org.springframework.data.domain.Page<OrderResponse> result = buyerService.listOrders(
                authentication.getName(), status,
                org.springframework.data.domain.PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        return new PagedApiResponse<>(true, result.getContent(),
                new PageInfo(result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long id, Authentication authentication) {
        return new ApiResponse<>(true, buyerService.getOrder(authentication.getName(), id));
    }

    @PutMapping("/orders/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long id,
                                                   Authentication authentication) {
        return new ApiResponse<>(true, buyerService.cancelOrder(authentication.getName(), id));
    }

    @PutMapping("/orders/{id}/confirm-delivery")
    public ApiResponse<OrderResponse> confirmDelivery(@PathVariable Long id,
                                                       Authentication authentication) {
        return new ApiResponse<>(true, buyerService.confirmDelivery(authentication.getName(), id));
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(address.getId(), address.getLine1(), address.getLine2(),
                address.getCity(), address.getState(), address.getPincode(), address.getPhone());
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(@jakarta.validation.Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        Address address = buyerService.addAddress(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, toAddressResponse(address)));
    }

}
