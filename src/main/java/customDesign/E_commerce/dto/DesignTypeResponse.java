package customDesign.E_commerce.dto;

import java.util.List;

public record DesignTypeResponse(Long id, Long categoryId, String name, List<OptionFieldResponse> optionFields, boolean active) {
    public record OptionFieldResponse(String key, String label, List<String> options) {}
}
