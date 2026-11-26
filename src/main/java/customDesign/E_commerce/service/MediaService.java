package customDesign.E_commerce.service;

import customDesign.E_commerce.Exceptions.NotAllowedFileException;
import customDesign.E_commerce.dto.MediaResponse;
import customDesign.E_commerce.entity.Media;
import customDesign.E_commerce.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    @Autowired
    private MediaRepository mediaRepository;

    private static final String UPLOAD_DIR = "uploads/";

    private static final long MAX_SIZE = 30 * 1024 * 1024; // 5MB

    /*
    who can use which purpose
    // SELLER   → LISTING_IMAGE, PORTFOLIO, DELIVERY_PROOF, DESIGN_SHOWCASE
    // BUYER    → CUSTOM_REQUEST, REVIEW_IMAGE
    // MEDIATOR → DESIGN_REQUEST, SHOP_BANNER
    // ALL      → PROFILE_PHOTO
    */
    private static final List<String> ALLOWED_PURPOSES = List.of(
            "PROFILE_PHOTO",    // all users — profile picture
            "LISTING_IMAGE",    // seller — product photo shown in storefront, needs category
            "PORTFOLIO",        // seller — past work showcase
            "DELIVERY_PROOF",   // seller — photo before delivery, buyer must confirm, needs orderId
            "DESIGN_SHOWCASE",  // seller — extra design photos
            "CUSTOM_REQUEST",   // buyer — reference image for custom design request
            "REVIEW_IMAGE",     // buyer — photo with review/complaint after delivery, needs orderId
            "DESIGN_REQUEST",   // mediator — image when requesting a design from seller
            "SHOP_BANNER"       // mediator — shop banner image
    );

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @Transactional
    public MediaResponse upload(MultipartFile file, String purpose,
                                String category, Long orderId,
                                String uploadedBy) throws IOException {
        // validate file
        if (file.isEmpty())
            throw new FileNotFoundException("File is empty");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new NotAllowedFileException(
                    "Only image files allowed: jpeg, png, webp");

        if (file.getSize() > MAX_SIZE)
            throw new NotAllowedFileException("Max file size is 10MB");

        String purposeUpper = purpose.toUpperCase();
        if (!ALLOWED_PURPOSES.contains(purposeUpper))
            throw new NotAllowedFileException(
                    "Invalid purpose. Allowed: " + ALLOWED_PURPOSES);

        // category required for listing image and design showcase
        if ((purposeUpper.equals("LISTING_IMAGE") ||
                purposeUpper.equals("DESIGN_SHOWCASE")) &&
                (category == null || category.isBlank()))
            throw new NotAllowedFileException(
                    "category is required for " + purposeUpper);

        // orderId required for delivery proof and review
        if ((purposeUpper.equals("DELIVERY_PROOF") ||
                purposeUpper.equals("REVIEW_IMAGE")) && orderId == null)
            throw new NotAllowedFileException(
                    "orderId is required for " + purposeUpper);

        // save file to disk
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path savePath = Paths.get(UPLOAD_DIR + filename);
        Files.createDirectories(savePath.getParent());
        Files.write(savePath, file.getBytes());

        // save record to DB
        Media media = new Media();
        media.setUrl("/uploads/" + filename);
        media.setContentType(contentType);
        media.setSize(file.getSize());
        media.setPurpose(purposeUpper);
        media.setCategory(category);
        media.setOrderId(orderId);
        media.setUploadedBy(uploadedBy);
        media.setUploadedAt(new Date());
        Media saved = mediaRepository.save(media);

        return toResponse(saved);
    }

    @Transactional
    public void deleteOwnedMedia(Long id, String uploadedBy) {
        Media media = mediaRepository.findByIdAndUploadedBy(id, uploadedBy)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Media not found"));
        try {
            if (media.getUrl() != null && media.getUrl().startsWith("/uploads/")) {
                java.nio.file.Path path = java.nio.file.Paths.get("uploads", media.getUrl().substring("/uploads/".length()));
                java.nio.file.Files.deleteIfExists(path);
            }
        } catch (java.io.IOException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete media file");
        }
        mediaRepository.delete(media);
    }

    private MediaResponse toResponse(Media media) {
        return new MediaResponse(
                media.getId(),
                media.getUrl(),
                media.getContentType(),
                media.getSize(),
                media.getPurpose(),
                media.getCategory(),
                media.getOrderId()
        );
    }
}