package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media,Long> {
    Optional<Media> findByIdAndUploadedBy(Long id, String uploadedBy);
}
