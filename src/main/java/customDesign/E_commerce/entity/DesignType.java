package customDesign.E_commerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "design_types")
public class DesignType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(nullable = false)
    private String name;

    @Column(length = 4000)
    private String optionFieldsJson;

    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOptionFieldsJson() { return optionFieldsJson; }
    public void setOptionFieldsJson(String optionFieldsJson) { this.optionFieldsJson = optionFieldsJson; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
