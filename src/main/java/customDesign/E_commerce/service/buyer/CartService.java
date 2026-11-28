package customDesign.E_commerce.service.buyer;

import customDesign.E_commerce.dto.CartDtos.*;
import customDesign.E_commerce.entity.*;
import customDesign.E_commerce.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CartService {
    private final CartRepository carts;
    private final CartItemRepository items;
    private final ProductRepository products;
    private final AddressRepository addresses;
    private final BuyerOrderRepository orders;
    private final UserRepository users;

    public CartService(CartRepository carts, CartItemRepository items, ProductRepository products,
                       AddressRepository addresses, BuyerOrderRepository orders, UserRepository users) {
        this.carts = carts;
        this.items = items;
        this.products = products;
        this.addresses = addresses;
        this.orders = orders;
        this.users = users;
    }

    @Transactional
    public CartResponse getCart(String email) {
        return toResponse(getOrCreate(email));
    }

    @Transactional
    public CartResponse addItem(String email, AddCartItemRequest request) {
        Cart cart = getOrCreate(email);
        Product product = availableProduct(request.productId());
        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId())).findFirst().orElse(null);
        int quantity = request.quantity() + (existing == null ? 0 : existing.getQuantity());
        checkStock(product, quantity);
        if (existing == null) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        } else {
            existing.setQuantity(quantity);
        }
        return toResponse(carts.save(cart));
    }

    @Transactional
    public CartResponse updateItem(String email, Long itemId, int quantity) {
        CartItem item = items.findByIdAndCartBuyerEmail(itemId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        Product product = availableProduct(item.getProduct().getId());
        checkStock(product, quantity);
        item.setQuantity(quantity);
        return toResponse(carts.findByBuyerEmail(email).orElseThrow());
    }

    @Transactional
    public CartResponse removeItem(String email, Long itemId) {
        CartItem item = items.findByIdAndCartBuyerEmail(itemId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        return toResponse(carts.save(cart));
    }

    @Transactional
    public CartResponse clear(String email) {
        Cart cart = getOrCreate(email);
        cart.getItems().clear();
        return toResponse(carts.save(cart));
    }

    @Transactional
    public Long checkout(String email, Long addressId) {
        Cart cart = carts.findByBuyerEmailForUpdate(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }
        Address address = addresses.findByIdAndUserEmail(addressId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
        BuyerOrder order = new BuyerOrder();
        order.setBuyerEmail(email);
        order.setAddress(address);
        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        order.setType(OrderType.STANDARD);
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal commission = BigDecimal.ZERO;
        int totalQuantity = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
        String soleSeller = null;
        boolean multipleSellers = false;
        for (CartItem cartItem : cart.getItems().stream()
                .sorted(java.util.Comparator.comparing(item -> item.getProduct().getId())).toList()) {
            Product product = products.findByIdForUpdate(cartItem.getProduct().getId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product is unavailable"));
            checkStock(product, cartItem.getQuantity());
            boolean bulk = cartItem.getQuantity() >= 50;
            BigDecimal price = bulk && product.getBulkPrice() != null ? product.getBulkPrice() : product.getPrice();
            if (price == null || price.signum() < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A cart product has an invalid price");
            }
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal rate = bulk
                    ? new BigDecimal("0.0600") : new BigDecimal("0.1000");
            total = total.add(lineTotal);
            commission = commission.add(lineTotal.multiply(rate).setScale(2, RoundingMode.HALF_UP));
            if (soleSeller == null) soleSeller = product.getSellerEmail();
            else if (!soleSeller.equals(product.getSellerEmail())) multipleSellers = true;

            BuyerOrderItem orderItem = new BuyerOrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductDescription(product.getDescription());
            orderItem.setSpecifications(product.getSpecifications());
            orderItem.setImageUrl(product.getImageUrl());
            orderItem.setCategoryId(product.getCategory() == null ? null : product.getCategory().getId());
            orderItem.setCategoryName(product.getCategory() == null ? null : product.getCategory().getName());
            orderItem.setSellerEmail(product.getSellerEmail());
            orderItem.setUnitPrice(price);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setEstimatedProductionHours(product.getEstimatedProductionHours());
            orderItem.setEstimatedProductionDays(product.getDaysToMake());
            order.addItem(orderItem);
            if (product.getStock() != null) product.setStock(product.getStock() - cartItem.getQuantity());
        }
        total = total.setScale(2, RoundingMode.HALF_UP);
        commission = commission.setScale(2, RoundingMode.HALF_UP);
        order.setSellerEmail(multipleSellers ? null : soleSeller);
        order.setQuantity(totalQuantity);
        order.setAmount(total);
        order.setCommissionAmount(commission);
        order.setCommissionRate(total.signum() == 0 ? BigDecimal.ZERO : commission.divide(total, 4, RoundingMode.HALF_UP));
        order.setSellerPayout(total.subtract(commission).setScale(2, RoundingMode.HALF_UP));
        order.setType(totalQuantity >= 50 ? OrderType.BULK : OrderType.STANDARD);
        BuyerOrder saved = orders.save(order);
        cart.getItems().clear();
        carts.save(cart);
        return saved.getId();
    }

    private Cart getOrCreate(String email) {
        return carts.findByBuyerEmail(email).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setBuyerEmail(email);
            return carts.save(cart);
        });
    }

    private Product availableProduct(Long id) {
        Product product = products.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product is unavailable"));
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product has an invalid price");
        }
        return product;
    }

    private void checkStock(Product product, int quantity) {
        if (product.getStock() != null && quantity > product.getStock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requested quantity exceeds available stock");
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> lines = cart.getItems().stream().map(item -> {
            Product p = item.getProduct();
            BigDecimal unit = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
            User seller = p.getSellerEmail() == null ? null : users.findByEmail(p.getSellerEmail()).orElse(null);
            return new CartItemResponse(item.getId(), p.getId(), p.getName(), p.getDescription(),
                    p.getImageUrl(), seller == null ? null : seller.getId(),
                    seller == null ? null : seller.getName(), p.getSellerEmail(), unit, item.getQuantity(),
                    unit.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP));
        }).toList();
        BigDecimal total = lines.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Long buyerId = users.findByEmail(cart.getBuyerEmail()).map(User::getId).orElse(null);
        return new CartResponse(cart.getId(), buyerId, lines, total.setScale(2, RoundingMode.HALF_UP));
    }
}
