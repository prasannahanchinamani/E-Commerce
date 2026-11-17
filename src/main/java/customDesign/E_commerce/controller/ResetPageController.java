package customDesign.E_commerce.controller;

import customDesign.E_commerce.service.EmailTemplateService;
import customDesign.E_commerce.template.TemplateCode;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResetPageController {

    private final EmailTemplateService emailTemplateService;

    public ResetPageController(
            EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }

    @GetMapping(
            value = "/reset-password",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> resetPage() {

        String html = emailTemplateService.getTemplateBody(
                TemplateCode.PASSWORD_RESET
        );

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "no-referrer")
                .header("X-Content-Type-Options", "nosniff")
                .body(html);
    }
}