package customDesign.E_commerce.service.seller;

import customDesign.E_commerce.Exceptions.UserAlreadyFound;
import customDesign.E_commerce.Exceptions.UserNotFoundException;
import customDesign.E_commerce.dto.PortfolioRequest;
import customDesign.E_commerce.dto.PortfolioResponse;
import customDesign.E_commerce.dto.seller.SellerApplyRequest;
import customDesign.E_commerce.dto.seller.SellerApplyRequest;
import customDesign.E_commerce.dto.seller.SellerProfileResponse;
import customDesign.E_commerce.dto.seller.SellerProfileUpdateRequest;
import customDesign.E_commerce.entity.Portfolio;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.repository.MediaRepository;
import customDesign.E_commerce.repository.PortfolioRepository;
import customDesign.E_commerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final MediaRepository mediaRepository;

    public SellerService(UserRepository userRepository, PortfolioRepository portfolioRepository, MediaRepository mediaRepository) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.mediaRepository = mediaRepository;
    }

    public User applyAsSeller(String email, SellerApplyRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("PENDING".equals(user.getStatus())) {
            throw new RuntimeException("Application already submitted");
        }

        user.setShopName(req.getShopName());
        user.setBio(req.getBio());
        user.setMediatorCode(req.getMediatorCode());
        user.setAccountName(req.getAccountName());
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