package customDesign.E_commerce.service.seller;

import customDesign.E_commerce.Exceptions.UserAlreadyFound;
import customDesign.E_commerce.Exceptions.UserNotFoundException;
import customDesign.E_commerce.dto.PortfolioRequest;
import customDesign.E_commerce.dto.ProductRequest;
import customDesign.E_commerce.dto.PortfolioResponse;
import customDesign.E_commerce.dto.seller.SellerApplyRequest;
import customDesign.E_commerce.dto.seller.SellerApplyRequest;
import customDesign.E_commerce.dto.seller.SellerProfileResponse;
import customDesign.E_commerce.dto.seller.SellerProfileUpdateRequest;
import customDesign.E_commerce.entity.InviteStatus;
import customDesign.E_commerce.entity.Portfolio;
import customDesign.E_commerce.entity.Product;
import customDesign.E_commerce.entity.ProductCategory;
import customDesign.E_commerce.entity.seller.SellerInvite;
import customDesign.E_commerce.entity.Role;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.repository.MediaRepository;
import customDesign.E_commerce.repository.SellerInviteRepository;
import customDesign.E_commerce.repository.ProductRepository;
import customDesign.E_commerce.repository.ProductCategoryRepository;
import customDesign.E_commerce.repository.PortfolioRepository;
import customDesign.E_commerce.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SellerService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final MediaRepository mediaRepository;
    private final SellerInviteRepository inviteRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public SellerService(UserRepository userRepository, PortfolioRepository portfolioRepository,
                         MediaRepository mediaRepository, SellerInviteRepository inviteRepository,
                         ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.mediaRepository = mediaRepository;
        this.inviteRepository = inviteRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public String acceptInvitation(String email, String token) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        SellerInvite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid invitation"));
        if (invite.getStatus() != InviteStatus.INVITED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation is no longer active");
        }
        if (invite.getExpiresAt().isBefore(java.time.Instant.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation has expired");
        }
        if (invite.getEmail() != null && !invite.getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This invitation belongs to another account");
        }
        user.setRole(Role.SELLER);
        user.setMediatorId(invite.getMediatorId());
        user.setStatus("PENDING");
        user.setCategoryIds(invite.getCategoryIds().isEmpty() ? null :
                invite.getCategoryIds().stream().map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(",")));
        userRepository.save(user);
        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);
        return "Seller invitation accepted; application is pending approval";
    }

    public User applyAsSeller(String email, SellerApplyRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("PENDING".equals(user.getStatus())) {
            throw new RuntimeException("Application already submitted");
        }

        user.setRole(Role.SELLER);
        user.setShopName(req.getShopName());
        user.setBio(req.getBio());
//        user.setMediatorCode(req.getMediatorCode());
        user.setAccountName(req.getAccountName());
        if (req.getMediatorCode() != null && !req.getMediatorCode().isBlank()) {
            User mediator = userRepository.findByMediatorCode(req.getMediatorCode().trim())
                    .filter(u -> u.getRole() == Role.MEDIATOR)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mediator code"));
            user.setMediatorId(mediator.getId());
        }
        user.setAccountNumber(req.getAccountNumber());
//        user.setInstagramId(req.getInstagramId());
        user.setStatus("PENDING");

        // store categoryIds as "1,3"
        if (req.getCategoryIds() != null) {
            user.setCategoryIds(req.getCategoryIds().toString()
                    .replace("[", "").replace("]", "").replace(" ", ""));
        }

        return userRepository.save(user);
    }


    public SellerProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> categoryIds = new java.util.ArrayList<>();
        if (user.getCategoryIds() != null && !user.getCategoryIds().isEmpty()) {
            for (String id : user.getCategoryIds().split(",")) {
                categoryIds.add(Long.parseLong(id.trim()));
            }
        }

        return new SellerProfileResponse(
                user.getId(),
                user.getShopName(),
                user.getBio(),
                categoryIds,
                user.getMediatorId(),
                user.getStatus(),
                null
        );
    }
    public SellerProfileResponse updateProfile(String email, SellerProfileUpdateRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.getShopName() != null) user.setShopName(req.getShopName());
        if (req.getBio() != null) user.setBio(req.getBio());
        if (req.getStory() != null) user.setStory(req.getStory());

        userRepository.save(user);

        List<Long> categoryIds = new java.util.ArrayList<>();
        if (user.getCategoryIds() != null && !user.getCategoryIds().isEmpty()) {
            for (String id : user.getCategoryIds().split(",")) {
                categoryIds.add(Long.parseLong(id.trim()));
            }
        }

        return new SellerProfileResponse(
                user.getId(),
                user.getShopName(),
                user.getBio(),
                categoryIds,
                user.getMediatorId(),
                user.getStatus(),
                null
        );
    }



    public java.util.List<Product> getProducts(String email) {
        return productRepository.findBySellerEmail(email);
    }

    public Product createProduct(String email, ProductRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (req.getPrice() == null || req.getPrice().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid product price is required");
        }
        ProductCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        Product product = new Product();
        product.setName(req.getName().trim());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setImageUrl(req.getImageUrl());
        product.setStock(req.getStock() == null ? 0 : Math.max(req.getStock(), 0));
        product.setCategory(category);
        product.setSellerEmail(email);
        product.setActive(true);
        return productRepository.save(product);
    }

    public Product updateProduct(String email, Long id, ProductRequest req) {
        Product product = productRepository.findByIdAndSellerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (req.getName() != null) product.setName(req.getName().trim());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getPrice() != null) {
            if (req.getPrice().signum() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price cannot be negative");
            product.setPrice(req.getPrice());
        }
        if (req.getImageUrl() != null) product.setImageUrl(req.getImageUrl());
        if (req.getStock() != null) product.setStock(Math.max(req.getStock(), 0));
        if (req.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    public void deleteProduct(String email, Long id) {
        Product product = productRepository.findByIdAndSellerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        productRepository.delete(product);
    }

    public java.util.List<PortfolioResponse> getPortfolio(String email) {
        return portfolioRepository.findBySellerEmail(email).stream().map(saved ->
                new PortfolioResponse(saved.getId(), saved.getTitle(),
                        saved.getImageUrls() == null || saved.getImageUrls().isBlank()
                                ? java.util.List.of()
                                : java.util.Arrays.asList(saved.getImageUrls().split("\\s*,\\s*")),
                        saved.getCategoryId())).toList();
    }

    public void deletePortfolio(String email, Long id) {
        Portfolio portfolio = portfolioRepository.findByIdAndSellerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio item not found"));
        portfolioRepository.delete(portfolio);
    }

    public PortfolioResponse addPortfolio(String email, PortfolioRequest req) {
        Portfolio portfolio = new Portfolio();
        portfolio.setSellerEmail(email);
        portfolio.setTitle(req.getTitle());
        portfolio.setCategoryId(req.getCategoryId());

        // get image URLs from media table
        List<String> urls = new java.util.ArrayList<>();
        if (req.getImageIds() != null) {
            for (Long imageId : req.getImageIds()) {
                mediaRepository.findById(imageId).ifPresent(m -> urls.add(m.getUrl()));
            }
        }
        portfolio.setImageUrls(String.join(",", urls));

        Portfolio saved = portfolioRepository.save(portfolio);

        return new PortfolioResponse(
                saved.getId(),
                saved.getTitle(),
                urls,
                saved.getCategoryId()
        );
    }

}