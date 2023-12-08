package ua.com.obox.authserver.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.com.obox.authserver.token.Token;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tools.State;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String userId;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "tenant_id")
    Tenant tenant;
    private String email;
    private String password;

    @JsonProperty("language")
    @Transient
    private String language;

    @Transient
    private String name;

    private long createdAt;
    private long updatedAt;

    @JsonIgnore
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.DISABLED + "'")
    private String state;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return getState().equals(State.ENABLED);
    }
}
