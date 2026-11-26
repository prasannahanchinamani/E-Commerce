package customDesign.E_commerce.dto;

import java.util.List;

public record TwoFactorVerifyResponse(boolean enabled, List<String> backupCodes) {}
