package customDesign.E_commerce.service;

import customDesign.E_commerce.entity.EmailTemplate;
import customDesign.E_commerce.repository.EmailTemplateRepository;
import customDesign.E_commerce.template.RenderedEmail;
import customDesign.E_commerce.template.TemplateCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class EmailTemplateService {

    private final EmailTemplateRepository repository;

    public EmailTemplateService(
            EmailTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public RenderedEmail render(
            TemplateCode code,
            Map<String, String> values) {

        EmailTemplate template = getActiveTemplate(code);

        String subject = template.getSubject();
        String body = template.getBody();

        for (Map.Entry<String, String> entry : values.entrySet()) {

            String placeholder = "{{" + entry.getKey() + "}}";

            String value = entry.getValue() == null
                    ? ""
                    : entry.getValue();

            subject = subject.replace(placeholder, value);
            body = body.replace(placeholder, value);
        }

        if (subject.contains("{{") || body.contains("{{")) {
            throw new IllegalArgumentException(
                    "Unresolved email template placeholders"
            );
        }

        return new RenderedEmail(subject, body);
    }

    @Transactional(readOnly = true)
    public String getTemplateBody(TemplateCode code) {

        return getActiveTemplate(code).getBody();
    }

    private EmailTemplate getActiveTemplate(TemplateCode code) {

        EmailTemplate template = repository.findByCode(code.name())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Email template not found"
                        )
                );

        if (!template.isActive()) {
            throw new IllegalStateException(
                    "Email template is inactive"
            );
        }

        return template;
    }
}