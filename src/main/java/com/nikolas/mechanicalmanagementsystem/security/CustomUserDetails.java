package com.nikolas.mechanicalmanagementsystem.security;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

//@EqualsAndHashCode
@Data
@Service
@AllArgsConstructor
@NoArgsConstructor
public class CustomUserDetails implements UserDetails {

    @NonNull
    private User user;

    public String getFirstName() {
        return user.getFirstName();
    }

    public String getLastName() {
        return user.getLastName();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public Long getId() {
        return user.getId();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getFirstName() + " " + user.getLastName();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }
    @Override
    public boolean isAccountNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabled();
    }

}
