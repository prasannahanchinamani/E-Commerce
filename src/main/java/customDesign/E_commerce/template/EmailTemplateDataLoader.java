package customDesign.E_commerce.template;

import customDesign.E_commerce.entity.EmailTemplate;
import customDesign.E_commerce.repository.EmailTemplateRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateDataLoader implements CommandLineRunner {

    private final EmailTemplateRepository repository;

    public EmailTemplateDataLoader(
            EmailTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {

        saveResetPasswordPage();
    }

    private void saveResetPasswordPage() {

        String code = TemplateCode.PASSWORD_RESET.name();

        if (repository.existsByCode(code)) {
            return;
        }

        EmailTemplate template = new EmailTemplate(
                code,
                ResetPasswordPageTemplate.NAME,
                ResetPasswordPageTemplate.SUBJECT,
                ResetPasswordPageTemplate.HTML
        );

        repository.save(template);
    }
}