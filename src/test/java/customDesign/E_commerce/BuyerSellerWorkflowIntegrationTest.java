package customDesign.E_commerce;

import customDesign.E_commerce.entity.*;
import customDesign.E_commerce.repository.*;
import customDesign.E_commerce.service.MailService;
import customDesign.E_commerce.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"MAIL_USERNAME=test@example.com", "app.mail.from=test@example.com"})
@AutoConfigureMockMvc
class BuyerSellerWorkflowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired ProductRepository products;
    @Autowired AddressRepository addresses;
    @Autowired BuyerOrderRepository orders;
    @Autowired CartRepository carts;
    @Autowired CartItemRepository cartItems;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired PasswordResetTokenRepository resetTokens;
    @Autowired AuditLogRepository auditLogs;
    @Autowired TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean MailService mailService;

    private User buyer;
    private User seller;
    private User anotherSeller;
    private Product product;
    private String buyerToken;
    private String sellerToken;
    private String otherSellerToken;
    private final String suffix = UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void setUp() {
        buyer = createUser("buyer-" + suffix + "@example.com", Role.BUYER);
        seller = createUser("seller-" + suffix + "@example.com", Role.SELLER);
        anotherSeller = createUser("other-" + suffix + "@example.com", Role.SELLER);
        buyerToken = createSessionToken(buyer);
        sellerToken = createSessionToken(seller);
        otherSellerToken = createSessionToken(anotherSeller);
        product = new Product();
        product.setName("Test mug");
        product.setDescription("Test description");
        product.setPrice(new BigDecimal("12.50"));
        product.setStock(8);
        product.setSellerEmail(seller.getEmail());
        product.setActive(true);
        product = products.saveAndFlush(product);
    }

    @AfterEach
    void cleanUp() {
        orders.deleteAll();
        auditLogs.deleteAll();
        resetTokens.deleteAll();
        carts.deleteAll();
        products.deleteAll();
        addresses.deleteAll();
        refreshTokens.deleteAll();
        users.deleteAll();
    }

    @Test
    void sellerCanRetrieveOwnProductButCannotReadOtherOrMissingProduct() throws Exception {
        mvc.perform(get("/api/v1/seller/products/{id}", product.getId())
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test mug"));

        String otherSellerAccess = createSessionToken(anotherSeller);
        mvc.perform(get("/api/v1/seller/products/{id}", product.getId())
                        .header("Authorization", "Bearer " + otherSellerAccess))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("hide and seek")));

        String freshSellerAccess = createSessionToken(seller);
        mvc.perform(get("/api/v1/seller/products/999999")
                        .header("Authorization", "Bearer " + freshSellerAccess))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/seller/products/not-a-number")
                        .header("Authorization", "Bearer " + freshSellerAccess))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refreshRotatesTokenAndLogoutInvalidatesTheSession() throws Exception {
        User account = createUser("refresh-" + suffix + "@example.com", Role.BUYER);
        String sessionId = UUID.randomUUID().toString();
        String rawRefresh = tokenService.refreshToken(account, sessionId);
        RefreshToken session = createRefreshSession(account, sessionId, rawRefresh, Instant.now());

        String response = mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + rawRefresh + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String rotated = objectMapper.readTree(response).path("data").path("refreshToken").asText();
        org.junit.jupiter.api.Assertions.assertNotEquals(rawRefresh, rotated);
        org.junit.jupiter.api.Assertions.assertEquals(hash(rotated),
                refreshTokens.findBySessionId(sessionId).orElseThrow().getTokenHash());

        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + rawRefresh + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + rotated + "\"}"))
                .andExpect(status().isNoContent());
        org.junit.jupiter.api.Assertions.assertTrue(
                refreshTokens.findBySessionId(sessionId).orElseThrow().isRevoked());
    }

    @Test
    void forgotPasswordOnlyCreatesTokensForKnownAccountsAndReplacesPreviousToken() throws Exception {
        User account = createUser("reset-" + suffix + "@example.com", Role.BUYER);
        String request = "{\"email\":\"" + account.getEmail() + "\"}";
        mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isAccepted());
        org.junit.jupiter.api.Assertions.assertEquals(1, resetTokens.count());
        mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isAccepted());
        org.junit.jupiter.api.Assertions.assertEquals(1, resetTokens.count());
        org.mockito.Mockito.verify(mailService, org.mockito.Mockito.times(2))
                .sendEmail(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(account.getEmail()),
                        org.mockito.ArgumentMatchers.eq("Password Reset Request"),
                        org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void expiredAndInvalidPasswordResetTokensAreRejected() throws Exception {
        User account = createUser("expired-reset-" + suffix + "@example.com", Role.BUYER);
        String expiredRaw = "expired-reset-token-" + suffix;
        customDesign.E_commerce.entity.PasswordResetToken expired =
                new customDesign.E_commerce.entity.PasswordResetToken();
        expired.setUser(account);
        expired.setTokenHash(hash(expiredRaw));
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        resetTokens.saveAndFlush(expired);

        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + expiredRaw + "\",\"newPassword\":\"newpassword123\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"invalid-" + suffix + "\",\"newPassword\":\"newpassword123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mediatorInvitationCanOnboardSellerWithoutTypingMediatorCode() throws Exception {
        User mediator = createUser("mediator-" + suffix + "@example.com", Role.MEDIATOR);
        mediator.setShopName("Mediator Shop");
        mediator = users.saveAndFlush(mediator);
        String mediatorToken = createSessionToken(mediator);
        String invitedEmail = "invitee-" + suffix + "@example.com";
        mvc.perform(post("/api/v1/mediator/sellers/invite")
                        .header("Authorization", "Bearer " + mediatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + invitedEmail + "\",\"categoryIds\":[]}"))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<String> link = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(mailService).sendInvitation(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(invitedEmail),
                org.mockito.ArgumentMatchers.eq("Seller invitation"), link.capture());
        String token = java.net.URI.create(link.getValue()).getQuery().substring("token=".length());
        User invited = createUser(invitedEmail, Role.BUYER);
        String invitedBuyerToken = createSessionToken(invited);
        mvc.perform(post("/api/v1/seller/invitations/{token}/accept", token)
                        .header("Authorization", "Bearer " + invitedBuyerToken))
                .andExpect(status().isOk());

        String application = """
                {"shopName":"Invited Seller Shop","bio":"Applying after invitation",
                 "categoryIds":[],"accountName":"Invited Seller","accountNumber":"123456789"}
                """;
        mvc.perform(post("/api/v1/seller/apply")
                        .header("Authorization", "Bearer " + invitedBuyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(application))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.mediatorId").value(mediator.getId()));
    }

    @Test
    void adminCanAssignSellerToAnApprovedMediator() throws Exception {
        User admin = createUser("admin-" + suffix + "@example.com", Role.ADMIN);
        User mediator = createUser("assigned-mediator-" + suffix + "@example.com", Role.MEDIATOR);
        User unassignedSeller = createUser("unassigned-seller-" + suffix + "@example.com", Role.SELLER);
        String adminToken = createSessionToken(admin);

        mvc.perform(put("/api/v1/admin/sellers/{id}/mediator", unassignedSeller.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediatorId\":" + mediator.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(unassignedSeller.getId()));
        org.junit.jupiter.api.Assertions.assertEquals(mediator.getId(),
                users.findById(unassignedSeller.getId()).orElseThrow().getMediatorId());
    }

    @Test
    void activityEndpointRefreshesIdleTimestampForAuthenticatedSession() throws Exception {
        User account = createUser("activity-" + suffix + "@example.com", Role.BUYER);
        String sessionId = UUID.randomUUID().toString();
        RefreshToken session = createRefreshSession(account, sessionId, UUID.randomUUID().toString(),
                Instant.now().minusSeconds(60));
        mvc.perform(post("/api/v1/auth/activity")
                        .header("Authorization", "Bearer " + tokenService.generateToken(account, sessionId)))
                .andExpect(status().isNoContent());
        org.junit.jupiter.api.Assertions.assertTrue(
                refreshTokens.findBySessionId(sessionId).orElseThrow().getLastActivityAt()
                        .isAfter(session.getLastActivityAt()));
    }

    @Test
    void idleAndExpiredRefreshSessionsAreRejectedAndEmailVerificationRouteIsGone() throws Exception {
        User account = createUser("idle-" + suffix + "@example.com", Role.BUYER);
        String sessionId = UUID.randomUUID().toString();
        String rawRefresh = tokenService.refreshToken(account, sessionId);
        RefreshToken session = createRefreshSession(account, sessionId, rawRefresh,
                Instant.now().minusSeconds(11 * 60));

        mvc.perform(get("/api/v1/buyer/cart")
                        .header("Authorization", "Bearer " + tokenService.generateToken(account, sessionId)))
                .andExpect(status().isUnauthorized());
        org.junit.jupiter.api.Assertions.assertTrue(
                refreshTokens.findBySessionId(sessionId).orElseThrow().isRevoked());

        String expiredSessionId = UUID.randomUUID().toString();
        String expiredRefresh = tokenService.refreshToken(account, expiredSessionId);
        RefreshToken expired = createRefreshSession(account, expiredSessionId, expiredRefresh, Instant.now());
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        refreshTokens.saveAndFlush(expired);
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + expiredRefresh + "\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"unused\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unregistered-" + suffix + "@example.com\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void buyerAddressDuplicateIsRejectedAndCartCheckoutCreatesDetailedOrder() throws Exception {
        String addressJson = """
                {"fullName":"Buyer","phone":"1234567890","line1":"  1 Main   Street ",
                 "line2":"","city":"Chennai","state":"Tamil Nadu","pincode":"600001","default":true}
                """;
        mvc.perform(post("/api/v1/buyer/addresses").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(addressJson))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/buyer/addresses").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson.replace("1 Main   Street", "1 Main Street")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("That address is already saved"));
        mvc.perform(post("/api/v1/buyer/addresses").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson.replace("1 Main   Street", "2 Main Street")))
                .andExpect(status().isCreated());
        org.junit.jupiter.api.Assertions.assertEquals(2, addresses.findByUserEmail(buyer.getEmail()).size());

        String addItem = "{\"productId\":" + product.getId() + ",\"quantity\":2}";
        mvc.perform(post("/api/v1/buyer/cart/items").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(addItem))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items.length()").value(1));
        mvc.perform(post("/api/v1/buyer/cart/items").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(addItem))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].quantity").value(4));

        Long addressId = addresses.findByUserEmail(buyer.getEmail()).get(0).getId();
        mvc.perform(put("/api/v1/buyer/addresses/{id}", addressId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson.replace("1 Main   Street", "Updated Main Street")))
                .andExpect(status().isOk());
        String otherBuyerToken = createSessionToken(createUser("address-owner-" + suffix + "@example.com", Role.BUYER));
        mvc.perform(delete("/api/v1/buyer/addresses/{id}", addressId)
                        .header("Authorization", "Bearer " + otherBuyerToken))
                .andExpect(status().isNotFound());
        String checkout = "{\"addressId\":" + addressId + "}";
        mvc.perform(post("/api/v1/buyer/cart/checkout").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(checkout))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items[0].productName").value("Test mug"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(4))
                .andExpect(jsonPath("$.data.amount").value(50.0));

        mvc.perform(get("/api/v1/buyer/cart").header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void cartOwnershipQuantityUpdatesAndStockLimitsAreEnforced() throws Exception {
        String addItem = "{\"productId\":" + product.getId() + ",\"quantity\":2}";
        mvc.perform(post("/api/v1/buyer/cart/items").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(addItem))
                .andExpect(status().isCreated());
        Long itemId = cartItems.findByCartBuyerEmail(buyer.getEmail()).get(0).getId();

        mvc.perform(put("/api/v1/buyer/cart/items/{id}", itemId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].quantity").value(3));
        mvc.perform(put("/api/v1/buyer/cart/items/{id}", itemId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":9}"))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/v1/buyer/cart/items/{id}", itemId)
                        .header("Authorization", "Bearer " + otherSellerToken))
                .andExpect(status().isForbidden());

        String otherBuyerToken = createSessionToken(createUser("second-" + suffix + "@example.com", Role.BUYER));
        mvc.perform(delete("/api/v1/buyer/cart/items/{id}", itemId)
                        .header("Authorization", "Bearer " + otherBuyerToken))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/buyer/cart/items/{id}", itemId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
        mvc.perform(post("/api/v1/buyer/cart/items").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productId\":" + product.getId() + ",\"quantity\":1}"))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/v1/buyer/cart").header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
        mvc.perform(post("/api/v1/buyer/cart/items").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productId\":999999,\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void failedCheckoutKeepsCartUnchanged() throws Exception {
        Address address = new Address();
        address.setUserEmail(buyer.getEmail());
        address.setLine1("3 Main Road");
        address.setCity("Chennai");
        address.setState("Tamil Nadu");
        address.setPincode("600001");
        address = addresses.saveAndFlush(address);
        mvc.perform(post("/api/v1/buyer/cart/items").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":2}"))
                .andExpect(status().isCreated());
        product.setStock(1);
        products.saveAndFlush(product);

        mvc.perform(post("/api/v1/buyer/cart/checkout").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":" + address.getId() + "}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/buyer/cart").header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    void sellerCanApplyWithoutMediatorCode() throws Exception {
        String apply = """
                {"shopName":"New shop","bio":"Application","categoryIds":[],
                 "accountName":"Buyer","accountNumber":"123456789"}
                """;
        mvc.perform(post("/api/v1/seller/apply").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(apply))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.mediatorId").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void buyerOrderActionsRequireValidStateAndCancellationReason() throws Exception {
        Address address = new Address();
        address.setUserEmail(buyer.getEmail());
        address.setLine1("2 Main Road");
        address.setCity("Chennai");
        address.setState("Tamil Nadu");
        address.setPincode("600001");
        address = addresses.saveAndFlush(address);
        String createOrder = "{\"listingId\":" + product.getId()
                + ",\"quantity\":1,\"addressId\":" + address.getId() + "}";
        String orderJson = mvc.perform(post("/api/v1/buyer/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(createOrder))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Long orderId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(orderJson)
                .path("data").path("id").asLong();

        mvc.perform(put("/api/v1/buyer/orders/{id}/cancel", orderId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/buyer/orders/{id}/cancel", orderId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Changed my mind\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cancellationReason").value("Changed my mind"));

        BuyerOrder order = orders.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.IN_PRODUCTION);
        orders.saveAndFlush(order);
        mvc.perform(post("/api/v1/buyer/orders/{id}/confirm-delivery", orderId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERED"))
                .andExpect(jsonPath("$.data.deliveredAt").isNotEmpty());
        mvc.perform(post("/api/v1/buyer/orders/{id}/confirm-delivery", orderId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isConflict());
    }

    private User createUser(String email, Role role) {
        User user = new User();
        user.setName(role.name());
        user.setEmail(email);
        user.setPassword("not-used-by-token");
        user.setRole(role);
        user.setStatus("APPROVED");
        return users.saveAndFlush(user);
    }

    private String createSessionToken(User user) {
        String sessionId = UUID.randomUUID().toString();
        createRefreshSession(user, sessionId, UUID.randomUUID().toString(), Instant.now());
        return tokenService.generateToken(user, sessionId);
    }

    private RefreshToken createRefreshSession(User user, String sessionId, String rawRefresh, Instant lastActivity) {
        RefreshToken session = new RefreshToken();
        session.setUser(user);
        session.setTokenHash(hash(rawRefresh));
        session.setSessionId(sessionId);
        session.setLastActivityAt(lastActivity);
        session.setExpiresAt(Instant.now().plusSeconds(3600));
        return refreshTokens.saveAndFlush(session);
    }

    private String hash(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
