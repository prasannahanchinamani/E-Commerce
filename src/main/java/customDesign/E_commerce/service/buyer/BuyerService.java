package customDesign.E_commerce.service.buyer;

import customDesign.E_commerce.dto.AddressRequest;
import customDesign.E_commerce.entity.Address;
import customDesign.E_commerce.repository.AddressRepository;
import customDesign.E_commerce.repository.BuyerOrderRepository;
import customDesign.E_commerce.repository.ProductRepository;
import customDesign.E_commerce.repository.UserRepository;
import customDesign.E_commerce.dto.CreateOrderRequest;
import customDesign.E_commerce.dto.OrderResponse;
import customDesign.E_commerce.dto.AddressResponse;
import customDesign.E_commerce.entity.BuyerOrder;
import customDesign.E_commerce.entity.BuyerOrderItem;
import customDesign.E_commerce.entity.OrderType;
import customDesign.E_commerce.entity.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BuyerService {

    private final AddressRepository addressRepository;
    private final BuyerOrderRepository buyerOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public BuyerService(AddressRepository addressRepository,
                        BuyerOrderRepository buyerOrderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.buyerOrderRepository = buyerOrderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Address> getAddresses(String email) {
        return addressRepository.findByUserEmail(email);
    }

    @Transactional
    public Address addAddress(String email, AddressRequest req) {
        boolean duplicate = addressRepository.findByUserEmail(email).stream()
                .anyMatch(existing -> sameAddress(existing, req));
        if (duplicate) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "That address is already saved");
        }
        Address address = new Address();
        address.setUserEmail(email);
        address.setFullName(req.getFullName());
        address.setPhone(req.getPhone());
        address.setLine1(req.getLine1());
        address.setLine2(req.getLine2());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPincode(req.getPincode());
        address.setDefault(req.isDefault());
        if (req.isDefault()) {
            addressRepository.findByUserEmail(email).forEach(existing -> {
                if (existing.isDefault()) {
                    existing.setDefault(false);
                    addressRepository.save(existing);
                }
            });
        }
        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(String email, Long id, AddressRequest req) {
        Address address = addressRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Address not found"));
        boolean duplicate = addressRepository.findByUserEmail(email).stream()
                .anyMatch(existing -> !existing.getId().equals(id) && sameAddress(existing, req));
        if (duplicate) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "That address is already saved");
        }
        address.setFullName(req.getFullName());
        address.setPhone(req.getPhone());
        address.setLine1(req.getLine1());
        address.setLine2(req.getLine2());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPincode(req.getPincode());
        if (req.isDefault()) {
            addressRepository.findByUserEmail(email).forEach(existing -> {
                if (!existing.getId().equals(id) && existing.isDefault()) {
                    existing.setDefault(false);
                    addressRepository.save(existing);
                }
            });
        }
        address.setDefault(req.isDefault());
        return addressRepository.save(address);
    }

    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        customDesign.E_commerce.entity.Product listing = productRepository.findByIdForUpdate(request.getListingId())
                .filter(customDesign.E_commerce.entity.Product::isActive)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Listing not found"));
        Address address = addressRepository.findByIdAndUserEmail(request.getAddressId(), email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Address not found"));
        if (listing.getStock() != null && request.getQuantity() > listing.getStock()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Requested quantity exceeds available stock");
        }

        boolean bulk = request.getQuantity() >= 50;
        java.math.BigDecimal unitPrice = bulk && listing.getBulkPrice() != null
                ? listing.getBulkPrice() : listing.getPrice();
        if (unitPrice == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Listing has no price");
        }
        java.math.BigDecimal amount = unitPrice.multiply(java.math.BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal commissionRate = bulk
                ? new java.math.BigDecimal("0.0600")
                : new java.math.BigDecimal("0.1000");
        java.math.BigDecimal commissionAmount = amount.multiply(commissionRate)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal sellerPayout = amount.subtract(commissionAmount)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        BuyerOrder order = new BuyerOrder();
        order.setBuyerEmail(email);
        order.setSellerEmail(listing.getSellerEmail());
        order.setType(bulk ? OrderType.BULK : OrderType.STANDARD);
        order.setQuantity(request.getQuantity());
        order.setAmount(amount);
        order.setCommissionRate(commissionRate);
        order.setCommissionAmount(commissionAmount);
        order.setSellerPayout(sellerPayout);
        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        order.setAddress(address);
        order.setNote(request.getNote());
        BuyerOrderItem item = new BuyerOrderItem();
        item.setProductId(listing.getId());
        item.setProductName(listing.getName());
        item.setProductDescription(listing.getDescription());
        item.setSpecifications(listing.getSpecifications());
        item.setImageUrl(listing.getImageUrl());
        item.setCategoryId(listing.getCategory() == null ? null : listing.getCategory().getId());
        item.setCategoryName(listing.getCategory() == null ? null : listing.getCategory().getName());
        item.setSellerEmail(listing.getSellerEmail());
        item.setUnitPrice(unitPrice);
        item.setQuantity(request.getQuantity());
        item.setEstimatedProductionHours(listing.getEstimatedProductionHours());
        item.setEstimatedProductionDays(listing.getDaysToMake());
        order.addItem(item);
        if (listing.getStock() != null) listing.setStock(listing.getStock() - request.getQuantity());
        return toOrderResponse(buyerOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<OrderResponse> listOrders(String email, String status,
                                                                          org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<BuyerOrder> result;
        if (status == null || status.isBlank()) {
            result = buyerOrderRepository.findByBuyerEmail(email, pageable);
        } else {
            try {
                result = buyerOrderRepository.findByBuyerEmailAndStatus(email,
                        OrderStatus.valueOf(status.toUpperCase()), pageable);
            } catch (IllegalArgumentException ex) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid order status");
            }
        }
        return result.map(this::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String email, Long id) {
        BuyerOrder order = buyerOrderRepository.findByIdAndBuyerEmail(id, email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Order not found"));
        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String email, Long id, String reason) {
        BuyerOrder order = buyerOrderRepository.findByIdAndBuyerEmail(id, email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Order not found"));
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Order can no longer be cancelled");
        }
        order.setCancellationReason(reason.trim());
        order.setCancelledAt(java.time.Instant.now());
        order.setStatus(OrderStatus.CANCELLED);
        return toOrderResponse(buyerOrderRepository.save(order));
    }

    @Transactional
    public OrderResponse confirmDelivery(String email, Long id) {
        BuyerOrder order = buyerOrderRepository.findByIdAndBuyerEmail(id, email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Order not found"));
        if (order.getStatus() != OrderStatus.IN_PRODUCTION) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Order must be in production or shipped before delivery confirmation");
        }
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(java.time.Instant.now());
        return toOrderResponse(buyerOrderRepository.save(order));
    }

    private OrderResponse toOrderResponse(BuyerOrder order) {
        Address address = order.getAddress();
        AddressResponse addressResponse = address == null ? null : new AddressResponse(address.getId(),
                address.getLine1(), address.getLine2(), address.getCity(), address.getState(),
                address.getPincode(), address.getPhone());
        Long buyerId = userRepository.findByEmail(order.getBuyerEmail()).map(customDesign.E_commerce.entity.User::getId).orElse(null);
        Long sellerId = order.getSellerEmail() == null ? null : userRepository.findByEmail(order.getSellerEmail())
                .map(customDesign.E_commerce.entity.User::getId).orElse(null);
        java.util.List<customDesign.E_commerce.dto.OrderItemResponse> items = order.getItems().stream().map(item -> {
            customDesign.E_commerce.entity.User seller = userRepository.findByEmail(item.getSellerEmail()).orElse(null);
            return new customDesign.E_commerce.dto.OrderItemResponse(
                    item.getProductId(),
                    item.getProductName(), item.getProductDescription(), item.getSpecifications(),
                    item.getImageUrl(), item.getCategoryId(), item.getCategoryName(),
                    seller == null ? null : seller.getId(), seller == null ? null : seller.getName(),
                    item.getSellerEmail(), item.getUnitPrice(), item.getQuantity(),
                    item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())),
                    item.getEstimatedProductionDays(), item.getEstimatedProductionHours(),
                    item.getActualProductionHours(),
                    item.getProductionStartedAt(), item.getProductionCompletedAt());
        }).toList();
        return new OrderResponse(order.getId(), buyerId, sellerId, order.getType().name(),
                order.getQuantity(), order.getAmount(), order.getCommissionRate(),
                order.getCommissionAmount(), order.getSellerPayout(), order.getStatus().name(),
                addressResponse, order.getCreatedAt(), items,
                paymentStatus(order.getStatus()),
                null, order.getDeliveredAt(), order.getCancellationReason(), order.getCancelledAt());
    }

    private String paymentStatus(OrderStatus status) {
        return switch (status) {
            case AWAITING_PAYMENT, CANCELLED -> "UNPAID";
            case PAID, IN_PRODUCTION, DELIVERED, COMPLETED -> "PAID";
            case REFUND_REQUESTED -> "REFUND_PENDING";
            case REFUNDED -> "REFUNDED";
        };
    }

    @Transactional
    public void deleteAddress(String email, Long id) {
        Address address = addressRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Address not found"));
        addressRepository.delete(address);
    }

    private boolean sameAddress(Address saved, AddressRequest request) {
        return normalized(saved.getLine1()).equals(normalized(request.getLine1()))
                && normalized(saved.getLine2()).equals(normalized(request.getLine2()))
                && normalized(saved.getCity()).equals(normalized(request.getCity()))
                && normalized(saved.getState()).equals(normalized(request.getState()))
                && normalized(saved.getPincode()).equals(normalized(request.getPincode()))
                && normalized(saved.getPhone()).equals(normalized(request.getPhone()));
    }

    private String normalized(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }
}