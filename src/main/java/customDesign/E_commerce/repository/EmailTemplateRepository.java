package customDesign.E_commerce.repository;

import customDesign.E_commerce.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByCode(String code);

    // used by the loader to avoid inserting the same template twice
    boolean existsByCode(String code);
}
