package customDesign.E_commerce.entity;

public enum Role {
    BUYER,
    SELLER,
    MEDIATOR,
    ADMIN;

    public boolean equalsIgnoreCase(Role role) {
        return false;
    }
}