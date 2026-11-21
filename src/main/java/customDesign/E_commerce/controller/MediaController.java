package customDesign.E_commerce.controller;

import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.dto.MediaResponse;
import customDesign.E_commerce.service.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<MediaResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("purpose") String purpose,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "orderId", required = false) Long orderId,
            Authentication authentication) throws IOException {

        MediaResponse response = mediaService.upload(
                file, purpose, category, orderId,
                authentication.getName());

        return ResponseEntity.ok(new ApiResponse<>(true, response));
    }
}