package customDesign.E_commerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "platform_settings")
public class PlatformSetting {
    @Id
    @Column(name = "\"KEY\"", length = 100)
    private String key;
    @Column(name = "\"VALUE\"", length = 4000)
    private String value;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
