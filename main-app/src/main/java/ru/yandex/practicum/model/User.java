package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    private Integer id;
    @Column("username")
    private String username;
    @Column("password")
    private String password;
    @Column("email")
    private String email;
    @Column("enabled")
    private boolean enabled;
    @Column("roles")
    private String roles;

    public Set<String> getRoleSet() {
        if (roles == null || roles.isEmpty()) {
            return Set.of("ROLE_USER");
        }
        return Set.of(roles.split(","));
    }

} 