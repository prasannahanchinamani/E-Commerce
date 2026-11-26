package customDesign.E_commerce.dto;

import customDesign.E_commerce.entity.Role;

public record AuthUserResponse(Long id, String name, String email, Role role) {}
