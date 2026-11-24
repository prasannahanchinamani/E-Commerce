package customDesign.E_commerce.controller.Buyer;

import customDesign.E_commerce.dto.AddressRequest;
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
@RequestMapping("/api/buyer")
@PreAuthorize("hasRole('BUYER')")
public class BuyerController {

    @Autowired
    private BuyerService buyerService;

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<Address>>> getAddresses(Authentication authentication) {
        List<Address> addresses = buyerService.getAddresses(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, addresses));
    }
    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<Address>> addAddress(@RequestBody AddressRequest request,
            Authentication authentication) {
        Address address = buyerService.addAddress(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, address));
    }

}
